package by.eugene.maven.config;


import lombok.extern.slf4j.Slf4j;
import org.postgresql.Driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


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

    public static void init(String url, String username, String password) {
        instance = new ConnectionManager(url, username, password);
        log.info("ConnectionManager initialized successfully");
    }

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
     * @return established {@link Connection}
     * by provided in properties file {@code url}, {@code username}, {@code password}
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
