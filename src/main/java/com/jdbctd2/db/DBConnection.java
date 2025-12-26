package com.jdbctd2.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
  private final String JDBC_URL;
  private final String USERNAME;
  private final String PASSWORD;

  public DBConnection() {
    this.JDBC_URL = System.getenv("JDBC_URL");
    this.USERNAME = System.getenv("USERNAME");
    this.PASSWORD = System.getenv("PASSWORD");

    if (JDBC_URL == null || JDBC_URL.isBlank()) {
      throw new IllegalStateException("JDBC_URL is not set");
    }
    if (USERNAME == null || USERNAME.isBlank()) {
      throw new IllegalStateException("USERNAME is not set");
    }
    if (PASSWORD == null || PASSWORD.isBlank()) {
      throw new IllegalStateException("PASSWORD is not set");
    }
  }

  public Connection getDBConnection() throws SQLException {
    return DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
  }

  public void attemptCloseDBConnection(Connection con) {
    if (con != null) {
      try {
        if (!con.isClosed()) {
          con.close();
        }
      } catch (SQLException e) {
        throw new RuntimeException("Error while trying to close the db connection" + e);
      }
    }
  }

  public void attemptCloseDBConnection(AutoCloseable... resources) {
    for (AutoCloseable resource : resources) {
      if (resource != null) {
        try {
          resource.close();
        } catch (Exception e) {
          throw new RuntimeException("Error while trying to close the resource" + e);
        }
      }
    }
  }
}
