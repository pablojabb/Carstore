package com.raven.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dbase {

    private static dbase instance = null;
 

    private dbase() {
    }

    public static dbase getInstance() {
        if (instance == null) {
            instance = new dbase();
        }
        return instance;
    }

    public static Connection urcon() {

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
//            Class.forName("org.sqlite.JDBC");
     
            return DriverManager.getConnection("jdbc:mysql://localhost/cars","root", "");

        } catch (ClassNotFoundException | SQLException e) {

            System.out.println(e);
            return null;
        }

    }

    public static void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
