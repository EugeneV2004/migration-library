package by.eugene.maven.config;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class PropertiesUtils {
    private final Properties properties = new Properties();

    /**
     * @param propertyPath Path to properties file inside classpath
     */
    public PropertiesUtils(String propertyPath) {
        this.init(propertyPath);
    }

    /**
     * @param key Property key
     * @return property value, if not found returns null
     */
    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    private void init(String propertyPath) {
        try (InputStream inputStream = PropertiesUtils.class.getClassLoader().getResourceAsStream(propertyPath)) {
            if (inputStream == null) {
                throw new IOException("File not found: " + propertyPath);
            }
            properties.load(inputStream);
        } catch (IOException e) {
            log.error("Error while loading property file");
            throw new RuntimeException("File not found: " + propertyPath, e);
        }
    }

    public static void main(String[] args) {
        PropertiesUtils propertiesUtils = new PropertiesUtils("application.properties");

        System.out.println(propertiesUtils.getProperty("db.username"));
    }
}
