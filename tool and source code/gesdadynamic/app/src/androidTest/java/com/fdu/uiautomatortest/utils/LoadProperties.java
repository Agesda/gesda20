package com.fdu.uiautomatortest.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LoadProperties {

    private static Properties properties;

    private LoadProperties() {
    }

    public static Properties getInstance() {
        if (properties != null)
            return properties;
        try {
            properties = new Properties();
            InputStream in = new FileInputStream(new File("config.properties"));
            properties.load(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static String get(String key){
        if(properties != null)
            return properties.getProperty(key);
        else
            return getInstance().getProperty(key);
    }

}

