package com.musicapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database connection utility class
 * Provides methods to get and close database connections
 */
public class DBConnection {
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    
    // JDBC database URL, username, and password
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/musicapp";
    private static final String JDBC_USER = "your_username";
    private static final String JDBC_PASSWORD = "your_password";
    
    // JDBC Driver - automatically loaded by modern JDBC
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    static {
        try {
            // Register JDBC driver - optional in newer JDBC versions but included for compatibility
            Class.forName(JDBC_DRIVER);
            LOGGER.info("JDBC Driver registered successfully");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "JDBC Driver not found", e);
        }
    }
    
    /**
     * Get a database connection
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            LOGGER.info("Connecting to database...");
            return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection failed", e);
            throw e;
        }
    }
    
    /**
     * Close a database connection safely
     * @param connection The connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Database connection closed");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing database connection", e);
            }
        }
    }
    
    /**
     * Test database connection
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed", e);
            return false;
        } finally {
            closeConnection(conn);
        }
    }
}