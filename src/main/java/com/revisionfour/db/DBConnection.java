package com.revisionfour.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private String JDBC_URL;
    private String USERNAME;
    private String PASSWORD;

    public DBConnection(){
     this.JDBC_URL = System.getenv("JDBC_URL");
     this.USERNAME = System.getenv("USERNAME");
     this.PASSWORD = System.getenv("PASSWORD");

     if(this.JDBC_URL == null || this.JDBC_URL.isBlank()){
         throw new IllegalStateException("JDBC_URL is not set");
     }
     if(this.USERNAME == null || this.USERNAME.isBlank()){
         throw new IllegalStateException("USERNAME is not set");
     }
     if(this.PASSWORD == null || this.PASSWORD.isBlank()){
         throw new IllegalStateException("PASSWORD is not set");
     }
    }

    public Connection getDBConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL,USERNAME,PASSWORD);
    }

    public void attemptCloseDBConnection(AutoCloseable... ressources){
        for(AutoCloseable resource : ressources){
            if(resource != null){
                try{
                    resource.close();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to close ressource ",e);
                }
            }
        }
    }
}
