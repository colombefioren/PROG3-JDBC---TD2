package com.jdbctd2.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private final String JDBC_URL = "jdbc:postgresql://localhost:5433/mini_dish_db";
    private final String USERNAME = "mini_dish_db_manager";
    private final String PASSWORD = "123456";

    public Connection getDBConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
    }
}
