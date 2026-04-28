package com.hotel.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton that centralizes the application configuration.
 * Loads database.properties and app.properties from the classpath.
 */
public class AppConfig {

    private static AppConfig instance;
    private final Properties dbProps  = new Properties();
    private final Properties appProps = new Properties();

    private AppConfig() {
        load("database.properties", dbProps);
        load("app.properties",      appProps);
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) instance = new AppConfig();
            }
        }
        return instance;
    }

    private void load(String filename, Properties target) {
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream(filename)) {
            if (is == null) throw new RuntimeException(
                    "Not found: " + filename);
            target.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Error loading " + filename, e);
        }
    }

    // ── DB getters ──
    public String getDbUrl()      { return dbProps.getProperty("db.url"); }
    public String getDbUser()     { return dbProps.getProperty("db.user"); }
    public String getDbPassword() { return dbProps.getProperty("db.password"); }
    public String getDbDriver()   { return dbProps.getProperty("db.driver"); }

    // ── App getters ──
    public String getAppName()    { return appProps.getProperty("app.name"); }
    public String getViewType()   { return appProps.getProperty("view.type", "console"); }
}
