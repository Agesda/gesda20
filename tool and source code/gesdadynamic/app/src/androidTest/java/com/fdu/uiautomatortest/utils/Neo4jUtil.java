package com.fdu.uiautomatortest.utils;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

public class Neo4jUtil {
    public static Driver connect(){
//        String uri = LoadProperties.get("URI");
//        String username = LoadProperties.get("USERNAME");
//        String password = LoadProperties.get("PASSWORD");
        String uri = "bolt://192.168.101.23:7687";
        String username = "neo4j";
        String password = "gwn";
        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(username,password));
        return driver;
    }

    public static void main(String[] args) {
        Neo4jUtil.connect();
    }

    public static void closeDriver(Driver driver){
        if(driver != null){
            driver.close();
        }
    }

//    public static void closeSession(Session session){
//        if(session != null){
//            session.close();
//        }
//    }
}
