package by.eugene.maven.config;


import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


@Slf4j
public class ConnectionManager {
    private final String url;
    private final String username;
    private final String password;

    public ConnectionManager(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            log.error("Driver PostgreSQL not found: ", e);
            throw new RuntimeException("Driver PostgreSQL not found: ", e);
        }
    }

    /**
     * @return established {@link Connection}
     * by provided in properties file {@code url}, {@code username}, {@code password}
     */
    public Connection getConnection() {
        try {
            log.debug("Connecting to the database...");
            Connection connection = DriverManager.getConnection(url, username, password);
            log.info("Database connection established");
            return connection;
        } catch (SQLException e) {
            log.error("Connection can't be established: URL={}, username={}, password={}", url, username, password);
            throw new RuntimeException(e);
        }
    }

}
