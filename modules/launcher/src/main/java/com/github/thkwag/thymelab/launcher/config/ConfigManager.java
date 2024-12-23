package com.github.thkwag.thymelab.launcher.config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigManager {
    private final Properties props = new Properties();
    private final String path;
    private final String version = loadVersion();

    public ConfigManager(String path) {
        this.path = path;
    }

    private String loadVersion() {
        String version = loadVersionFromClasspath();
        if (!"not-found".equals(version)) {
            return version;
        }
        
        version = loadVersionFromProjectRoot();
        return version;
    }

    private String loadVersionFromClasspath() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("version.properties")) {
            if (is != null) {
                Properties versionProps = new Properties();
                versionProps.load(is);
                return versionProps.getProperty("version", "not-found").trim();
            }
        } catch (IOException e) {
            // ignore
        }
        return "not-found";
    }

    private String loadVersionFromProjectRoot() {
        try {
            File currentLocation = new File(getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            
            File projectRoot = findProjectRoot(currentLocation);
            if (projectRoot != null) {
                return loadVersionFromFile(new File(projectRoot, "version.properties"));
            }
        } catch (Exception e) {
            // ignore
        }
        return "not-found";
    }

    private File findProjectRoot(File start) {
        File current = start;
        while (current != null && !new File(current, "settings.gradle").exists()) {
            current = current.getParentFile();
        }
        return current;
    }

    private String loadVersionFromFile(File versionFile) {
        if (versionFile.exists()) {
            try (FileInputStream fis = new FileInputStream(versionFile)) {
                Properties versionProps = new Properties();
                versionProps.load(fis);
                return versionProps.getProperty("version", "not-found").trim();
            } catch (IOException e) {
                // ignore
            }
        }
        return "not-found";
    }

    public void load() {
        if (Files.exists(Paths.get(path))) {
            try (InputStream in = Files.newInputStream(Paths.get(path))) {
                props.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void save() {
        try (OutputStream out = Files.newOutputStream(Paths.get(path))) {
            props.store(out, "Application Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key, String def) {
        return props.getProperty(key, def);
    }

    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    public int getInt(String key, int def) {
        try {
            return Integer.parseInt(getProperty(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public void setInt(String key, int val) {
        setProperty(key, String.valueOf(val));
    }

    public boolean getBoolean(String key, boolean def) {
        return Boolean.parseBoolean(getProperty(key, String.valueOf(def)));
    }

    public void setBoolean(String key, boolean val) {
        setProperty(key, String.valueOf(val));
    }

    public void loadProperties() {
        load();
    }

    public String getVersion() {
        return version;
    }
} 