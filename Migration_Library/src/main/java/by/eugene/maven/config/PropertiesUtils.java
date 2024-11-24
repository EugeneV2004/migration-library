package by.eugene.maven.config;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class PropertiesUtils {
    private final Properties properties = new Properties();

    /**
     * @param propertiesFilePath Path to properties file inside classpath
     */
    public PropertiesUtils(String propertiesFilePath) {
        log.info("Initializing PropertiesUtils with file: {}", propertiesFilePath);
        this.init(propertiesFilePath);
    }

    /**
     * @param key Property key
     * @return property value, if not found returns null
     */
    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    private void init(String propertiesFilePath) {
        try (InputStream propertiesStream = PropertiesUtils.class.getClassLoader().getResourceAsStream(propertiesFilePath)) {
            if (propertiesStream == null) {
                log.error("Properties file '{}' not found in classpath", propertiesFilePath);
                throw new IOException("Unable to process properties file");
            }
            properties.load(propertiesStream);
            log.info("Properties file '{}' loaded successfully", propertiesFilePath);
        } catch (IOException e) {
            log.error("Error loading properties file '{}'", propertiesFilePath, e);
            throw new RuntimeException("Error loading properties file", e);
        }
    }
}
