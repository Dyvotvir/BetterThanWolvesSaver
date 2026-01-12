package com.darion.app.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigManager {
    private Path parent;
    private Path configFile;
    private Properties appProperties = new Properties();

    public ConfigManager() throws IOException {
        createParent();
        createConfigFile();
        load();
    }

    public void save() throws IOException {
        try (OutputStream os = Files.newOutputStream(configFile)) {
            appProperties.store(os, "BTWSaver Settings");
        }
    }

    public void setProperty(String property, String value) {
        appProperties.setProperty(property, value);
    }

    public void load() throws IOException {
        try (InputStream is = Files.newInputStream(configFile)) {
            appProperties.load(is);
        }
    }

    public String getProperty(String property) {
        return appProperties.getProperty(property);
    }

    public String getProperty(String property, String defaultValue) {
        return appProperties.getProperty(property, defaultValue);
    }

    public Path getParent() {
        return parent;
    }

    private void createParent() throws IOException {
        parent = Paths.get(System.getProperty("user.home"), "Documents", "BTWSaver_Backups");

        if (!Files.exists(parent))
            Files.createDirectories(parent);
    }

    private void createConfigFile() throws IOException {
        configFile = parent.resolve("config.properties");

        if (!Files.exists(configFile))
            Files.createFile(configFile);
    }
}
