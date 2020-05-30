package com.fdu.uiautomatortest.utils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {
    public static void filePrintln(String fileName, String content){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String path = Environment.getExternalStorageDirectory().getPath() + "/dfsStaticTest/" + fileName;
                    File file = new File(path);
                    if(!file.exists()){
                        file.createNewFile();
                    }
                    FileWriter writer = new FileWriter(file,true);
                    BufferedWriter out=new BufferedWriter(writer);
                    out.write(content);
                    out.newLine();
                    //out.flush();
                    out.close();
                    writer.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }
}
