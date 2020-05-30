package com.fdu.uiautomatortest.utils;

import android.util.Log;

import java.sql.*;

public class DBUtil {
    private final static String DRIVER = "com.mysql.jdbc.Driver";
    private final static String URL = "jdbc:mysql://192.168.101.23:3306/android?useUnicode=true&characterEncoding=UTF-8&useSSL=false";
    private final static String USERNAME = "root";
    private final static String PASSWORD = "fdsefdse";

    public static Connection getConnection(){
        Connection connection = null;
        try{
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(URL,USERNAME,PASSWORD);
            //Log.i("mysql connect info","connect successfully");

        }catch(Exception e ){
            //Log.i("mysql connect info", "connect failed");
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection(Connection connection) {
        try {
            if (connection != null)
                connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection = null;
    }
    public static void closePreparedStatement(PreparedStatement preparedStatement) {
        try {
            if (preparedStatement != null)
                preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        preparedStatement = null;
    }
    public static void closeStatement(Statement statement){
        try{
            if(statement != null){
                statement.close();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        statement = null;
    }
    public static void closeResultset(ResultSet resultSet){
        try{
            if(resultSet != null)
                resultSet.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        resultSet = null;
    }
}

