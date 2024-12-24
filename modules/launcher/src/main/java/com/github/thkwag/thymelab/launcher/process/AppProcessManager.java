package com.github.thkwag.thymelab.launcher.process;

import com.github.thkwag.thymelab.launcher.config.ConfigManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AppProcessManager {
    private final ConfigManager config;
    private Process process;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private final LogConsumer logConsumer;
    private final Runnable onProcessExit;

    public AppProcessManager(LogConsumer logConsumer, Runnable onProcessExit, ConfigManager config) {
        this.logConsumer = logConsumer;
        this.onProcessExit = onProcessExit;
        this.config = config;
        
        // Register shutdown hook for process cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            forceStopProcess();
            shutdownExecutor();
        }));
    }

    public boolean isRunning() {
        return process != null && process.isAlive();
    }

    public void startProcess() {
        if (isRunning()) return;

        List<String> command = new ArrayList<>();
        
        // Find embedded JRE path
        String javaPath;
        Path runtimePath = Paths.get("runtime", "bin", "java");
        if (new File(runtimePath.toString() + ".exe").exists()) {
            // Windows
            javaPath = runtimePath.toString() + ".exe";
        } else if (new File(runtimePath.toString()).exists()) {
            // Linux/Mac
            javaPath = runtimePath.toString();
        } else {
            // Use system Java if embedded JRE not found
            javaPath = "java";
        }

        command.add(javaPath);
        
        // Set process name for Windows
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            command.add("-XX:NativeMemoryTracking=summary");
            command.add("-Dproc_name=ThymeleafProcessor");
        } else {
            // Set process name for Unix-like systems
            command.add("-Dproc_name=ThymeleafProcessor");
        }
        
        command.add("-jar");

        try {
            String version = config.getVersion().trim();
            
            // Processor jar filename (remove whitespace)
            String jarFileName = String.format("thymelab-processor-%s.jar", version.replaceAll("\\s+", ""));
            StringBuilder logMessage = new StringBuilder("Looking for jar file: " + jarFileName + "\n");

            // Find current jar or class file location
            File currentLocation = new File(AppProcessManager.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());

            // Add debug log
            logMessage = new StringBuilder("Current location: " + currentLocation.getPath() + "\n");

            // Search paths in order
            List<File> searchPaths = new ArrayList<>();
            
            // 1. IDE or JAR execution paths
            File projectRoot = currentLocation;
            while (projectRoot != null && !new File(projectRoot, "settings.gradle").exists()) {
                projectRoot = projectRoot.getParentFile();
            }
            
            if (projectRoot != null) {
                // Gradle build directory
                searchPaths.add(new File(projectRoot, "modules/processor/build/libs"));
                searchPaths.add(new File(projectRoot, "build/modules/processor/libs"));
            }
            
            // 2. Installer paths
            File appDir = currentLocation;
            for (int i = 0; i < 5 && appDir != null; i++) {  // Search up to 5 levels of parent directories
                // macOS
                searchPaths.add(new File(appDir, "lib"));
                searchPaths.add(new File(appDir, "Contents/lib"));
                searchPaths.add(new File(appDir, "Contents/app/lib"));
                
                // Windows/Linux
                searchPaths.add(new File(appDir, "app/lib"));
                searchPaths.add(new File(appDir.getParentFile(), "lib"));
                
                appDir = appDir.getParentFile();
            }
            
            // 3. Relative paths from current directory
            searchPaths.add(new File("lib"));
            searchPaths.add(new File("../lib"));
            
            // Add debug log
            logMessage.append("Searching for processor jar in following locations:\n");
            for (File path : searchPaths) {
                logMessage.append("- ").append(path.getAbsolutePath()).append("\n");
            }
            
            // Find JAR file in each path
            File jarFile = null;
            for (File path : searchPaths) {
                File candidate = new File(path, jarFileName);
                if (candidate.exists()) {
                    jarFile = candidate;
                    break;
                }
            }

            if (jarFile == null) {
                logMessage.append("Error: Cannot find processor jar in any of the search paths\n");
                logConsumer.accept(logMessage.toString());
                return;
            }

            command.add(jarFile.getAbsolutePath());
        } catch (URISyntaxException e) {
            logConsumer.accept("Error getting jar path: " + e.getMessage() + "\n");
            return;
        }

        // Add log level setting
        String logLevel = config.getProperty("log.level", "INFO");
        command.add("--logging.level.com.github.thkwag.thymelab=" + logLevel);

        // Add port setting
        int port = config.getInt("server.port", 8080);
        command.add("--server.port=" + port);

        // Add Thymeleaf directory settings
        String staticPath = config.getProperty("static.folder.path", "");
        String templatesPath = config.getProperty("templates.folder.path", "");
        String dataPath = config.getProperty("data.folder.path", "");

        if (staticPath != null && !staticPath.isEmpty()) {
            command.add("--watch.directory.static=" + staticPath);
        }
        if (templatesPath != null && !templatesPath.isEmpty()) {
            command.add("--watch.directory.templates=" + templatesPath);
        }
        if (dataPath != null && !dataPath.isEmpty()) {
            command.add("--watch.directory.thymeleaf-data=" + dataPath);
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        
        // Set process environment variables
        Map<String, String> env = pb.environment();
        env.put("PROCESS_NAME", "ThymeleafProcessor");

        try {
            process = pb.start();
            executor.submit(() -> {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        logConsumer.accept(line + "\n");
                    }
                } catch (IOException e) {
                    logConsumer.accept("Error reading process output: " + e.getMessage() + "\n");
                }
            });

            executor.submit(() -> {
                try {
                    int exitCode = process.waitFor();
                    logConsumer.accept("Process exited with code: " + exitCode + "\n");
                    onProcessExit.run();
                } catch (InterruptedException ignored) {}
            });
        } catch (IOException e) {
            logConsumer.accept("Failed to start process: " + e.getMessage() + "\n");
        }
    }

    public void stopProcess() {
        if (!isRunning()) return;
        process.destroy();
        try {
            // Force kill process if not terminated within 5 seconds
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        } catch (InterruptedException ignored) {
            process.destroyForcibly();
        }
    }

    private void forceStopProcess() {
        if (process != null) {
            process.destroyForcibly();
            try {
                process.waitFor(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {}
        }
    }

    private void shutdownExecutor() {
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                logConsumer.accept("Failed to terminate executor threads\n");
            }
        } catch (InterruptedException ignored) {}
    }
} 