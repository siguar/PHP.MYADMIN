package com.company;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLConnection {
    protected Connection conn;
    /*
    Wzór:

    public static String url = "jdbc:mysql://mysql.titanaxe.com/srv71122?useSSL=false";
    public static String username = "srv71122";
    public static String password = "nBqNEwqC";

     */
    public SQLConnection(String url, String username, String password) throws SQLException {
        //String url = "jdbc:mysql://127.0.0.1:3306/SkyBlock?useSSL=false";
        //String username = "root";
        //String password = "CYyK5Y8TfbKpkhYRj9ryqdA3";
        conn = DriverManager.getConnection(url, username, password);
    }


    public Connection getConnection() {
        return conn;
    }



    public ResultSet getResults(String query) throws SQLException {
        ResultSet results = conn.prepareStatement(query).executeQuery();
        return results;
    }



    public boolean execute(String query) throws SQLException {
        boolean results = conn.prepareStatement(query).execute();
        return results;
    }

    public void close() throws SQLException {
        conn.close();
    }


}