package com.jts.hms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseService {
    private static Connection conn;

    private static Connection createConn() throws ClassNotFoundException, SQLException {
        // Load Oracle JDBC driver
        Class.forName("oracle.jdbc.driver.OracleDriver");

        // Service name style (better for XE 21c+)
        String url = "jdbc:oracle:thin:@//localhost:1521/XE";
        String user = "system";       // or the schema you created
        String password = "123456789";

        conn = DriverManager.getConnection(url, user, password);

        System.out.println("Database connection created successfully.");

        return conn;
    }

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        if (conn == null || conn.isClosed()) {
            return createConn();
        }
        return conn;
    }
}
