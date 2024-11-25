package by.eugene.maven.config;


import lombok.extern.slf4j.Slf4j;
import org.postgresql.Driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class responsible for managing database connections.
 * <p>
 * This class handles the initialization and management of a PostgreSQL database connection using the provided
 * configuration (URL, username, and password). It ensures a single connection instance is reused throughout the application.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * ConnectionManager.init("jdbc:postgresql://localhost:5432/mydb", "user", "password");
 * ConnectionManager connectionManager = ConnectionManager.getInstance();
 * Connection connection = connectionManager.getConnection();
 * </pre>
 * <p><b>Note:</b> Ensure {@code init()} is called before invoking {@code getInstance()} to properly configure the connection manager.</p>
 */
@Slf4j
public class ConnectionManager {
    private static ConnectionManager instance;

    private final String url;
    private final String username;
    private final String password;

    private Connection connection;

    private ConnectionManager(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    static {
        try {
            Class.forName("org.postgresql.Driver");
            log.info("PostgreSQL driver loaded successfully.");
            if (!Driver.isRegistered()) {
                DriverManager.registerDriver(new Driver());
                log.info("Driver class registered");
            }
        } catch (ClassNotFoundException e) {
            log.error("PostgreSQL driver class not found", e);
            throw new RuntimeException("Postgresql driver class not found", e);
        } catch (SQLException e) {
            log.error("Unable to register driver class", e);
            throw new RuntimeException("Unable to register driver class", e);
        }
    }

    /**
     * Initializes the {@link ConnectionManager} singleton instance with the given database configuration.
     *
     * @param url      the database URL
     * @param username the database username
     * @param password the database password
     */
    public static void init(String url, String username, String password) {
        instance = new ConnectionManager(url, username, password);
        log.info("ConnectionManager initialized successfully");
    }

    /**
     * Returns the singleton instance of {@link ConnectionManager}.
     *
     * @return the singleton instance
     * @throws RuntimeException if the instance is null (i.e., {@code init()} was not called before usage)
     */
    public static ConnectionManager getInstance() {
        if (instance != null) {
            log.debug("Returning existing instance of ConnectionManager.");
            return instance;
        } else {
            log.error("ConnectionManager instance is null. Please call init() method before using getInstance().");
            throw new RuntimeException("ConnectionManager is not instanced. Please use init() method first to instantiate ConnectionManager");
        }
    }

    /**
     * Retrieves a database connection. If a connection already exists and is open, it is reused. Otherwise, a new
     * connection is established using the configured URL, username, and password.
     *
     * @return an active {@link Connection} to the database
     * @throws RuntimeException if the connection cannot be established
     */
    public Connection getConnection() {
        try {
            log.debug("Checking existing database connection...");
            if (connection != null && !connection.isClosed()) {
                log.debug("Reusing existing database connection.");
                return connection;
            }

            log.info("Establishing new database connection: URL={}, Username={}", url, username);
            connection = DriverManager.getConnection(url, username, password);
            log.info("Database connection established");

            return connection;
        } catch (SQLException e) {
            log.error("Connection can't be established: URL={}, username={}, password={}", url, username, password);
            throw new RuntimeException("Database connection failed.", e);
        }
    }

}
