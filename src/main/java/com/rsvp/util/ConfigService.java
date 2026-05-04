package com.rsvp.util;

import java.io.*;
import java.util.Properties;

public class ConfigService {
    private final File configFile;

    public ConfigService(String path) {
        this.configFile = new File(path);
    }

    public Properties load() {
        Properties props = new Properties();
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("Error loading config: " + e.getMessage());
            }
        }
        return props;
    }

    public void save(Properties props) {
        if (!configFile.getParentFile().exists()) configFile.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            props.store(fos, "RSVP Config");
        } catch (IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
        }
    }
}
