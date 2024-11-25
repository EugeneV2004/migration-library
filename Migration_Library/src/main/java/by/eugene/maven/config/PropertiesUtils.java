package by.eugene.maven.config;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for loading and accessing properties from a properties file.
 * <p>
 * This class provides functionality to load a properties file from the classpath and retrieve property values
 * using their keys. It automatically logs the status of loading the properties file and throws runtime exceptions
 * in case of failure.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * PropertiesUtils propertiesUtils = new PropertiesUtils("config.properties");
 * String dbUrl = propertiesUtils.getProperty("db.url");
 * </pre>
 * <p><b>Note:</b> Ensure the properties file is available in the classpath for proper initialization.</p>
 */
@Slf4j
public class PropertiesUtils {
    private final Properties properties = new Properties();

    /**
     * Constructor to initialize the PropertiesUtils with the specified properties file.
     * <p>
     * The properties file is loaded from the classpath using the provided file path.
     * </p>
     *
     * @param propertiesFilePath the path to the properties file inside the classpath (e.g., "config.properties")
     */
    public PropertiesUtils(String propertiesFilePath) {
        log.info("Initializing PropertiesUtils with file: {}", propertiesFilePath);
        this.init(propertiesFilePath);
    }

    /**
     * Retrieves the value of the specified property key.
     * <p>
     * If the property is found, its value is returned; otherwise, {@code null} is returned.
     * </p>
     *
     * @param key the property key
     * @return the property value, or {@code null} if the key is not found
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
