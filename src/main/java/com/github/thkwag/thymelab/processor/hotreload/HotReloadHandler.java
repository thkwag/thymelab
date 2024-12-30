package com.github.thkwag.thymelab.processor.hotreload;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class HotReloadHandler {

    @Value("${watch.directory.static:}")
    private String staticDir;

    @Value("${watch.directory.templates:}")
    private String templatesDir;

    @Value("${watch.directory.thymeleaf-data:}")
    private String thymeleafDataDir;

    private final HotReloadWebSocketHandler webSocketHandler;
    private final ExecutorService watchService = Executors.newSingleThreadExecutor();
    private final Set<Path> registeredDirectories = new HashSet<>();
    private Path[] WATCH_DIRECTORIES;
    private volatile boolean isWatching = false;
    private volatile boolean isShutdown = false;

    @Autowired
    public HotReloadHandler(HotReloadWebSocketHandler webSocketHandler, ResourceLoader resourceLoader) {
        this.webSocketHandler = webSocketHandler;
    }

    @PostConstruct
    private void init() {
        Path[] resolvedPaths = new Path[3];
        resolvedPaths[0] = resolvePath(staticDir);
        resolvedPaths[1] = resolvePath(templatesDir);
        resolvedPaths[2] = resolvePath(thymeleafDataDir);

        WATCH_DIRECTORIES = resolvedPaths;
        startWatching();
    }

    private Path resolvePath(String directory) {
        if (directory == null || directory.trim().isEmpty()) {
            return null;
        }

        if (directory.startsWith("classpath:")) {
            log.debug("Hot reload is disabled for classpath resource: {}", directory);
            return null;
        }

        try {
            Path path = Paths.get(directory);
            if (Files.exists(path)) {
                return path;
            } else {
                log.warn("Directory does not exist: {}", directory);
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to resolve path: {}", directory, e);
            return null;
        }
    }

    private void startWatching() {
        if (isWatching) return;
        isWatching = true;

        watchService.submit(() -> {
            try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
                registerDirectories(watcher);
                watchForChanges(watcher);
            } catch (Exception e) {
                if (!isShutdown) {
                    log.error("Watch service error", e);
                }
            }
        });
    }

    private void registerDirectories(WatchService watcher) throws IOException {
        for (Path rootDir : WATCH_DIRECTORIES) {
            if (rootDir != null && Files.exists(rootDir)) {
                Files.walk(rootDir)
                        .filter(Files::isDirectory)
                        .forEach(dir -> registerDirectory(watcher, dir));
            }
        }
    }

    private void registerDirectory(WatchService watcher, Path dir) {
        try {
            if (!registeredDirectories.contains(dir)) {
                dir.register(watcher,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.OVERFLOW);
                registeredDirectories.add(dir);
                log.debug("Registered directory: {}", dir);
            }
        } catch (Exception e) {
            log.error("Failed to register directory: {}", dir, e);
        }
    }

    private void watchForChanges(WatchService watcher) throws InterruptedException {
        while (!isShutdown) {
            WatchKey key = watcher.take();
            Path dir = (Path) key.watchable();

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    webSocketHandler.notifyClients();
                    continue;
                }

                Path name = (Path) event.context();
                Path child = dir.resolve(name);

                log.debug("Event detected: {} - {}", kind.name(), child);

                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child)) {
                            log.debug("New directory created: {}", child);
                            Files.walk(child)
                                    .filter(Files::isDirectory)
                                    .forEach(p -> registerDirectory(watcher, p));
                        }
                    } catch (IOException e) {
                        log.error("Failed to process new directory: {}", child, e);
                    }
                }

                webSocketHandler.notifyClients();
            }

            key.reset();
        }
    }

    @PreDestroy
    public void shutdown() {
        isShutdown = true;
        watchService.shutdownNow();
    }
} 