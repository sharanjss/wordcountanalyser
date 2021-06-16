package com.code.challenge.wcanlayser.utils;

import java.io.File;

public class FileUtils {

    //Current directory location
    public static String CURRENT_DIR = System.getProperty("user.dir") + File.separator;

    /**
     * create directory if not exist
     * @param folderLocation
     */
    public static void createDirectory(String folderLocation) {
        File dir = new File(folderLocation);
        if(!dir.exists()) {
            dir.mkdirs();
            dir.setWritable(true,false);
        }
    }
}
