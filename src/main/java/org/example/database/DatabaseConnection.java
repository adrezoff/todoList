package org.example.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static final String JDBC_URL = System.getenv("DB_URL");
    private static final String USERNAME = System.getenv("DB_USERNAME");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load PostgreSQL JDBC driver", e);
        }
    }

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", USERNAME);
        props.setProperty("password", PASSWORD);

        props.setProperty("ssl", "false");
        props.setProperty("sslmode", "disable");
        props.setProperty("tcpKeepAlive", "true");
        props.setProperty("socketTimeout", "30");
        props.setProperty("connectTimeout", "5");

        return DriverManager.getConnection(JDBC_URL, props);
    }
}