package main.java.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Alo on 18-Apr-17.
 */
public class DisplayFiles implements Runnable {
    private String username;
    private String path;

    public DisplayFiles(String username) {
        this.username = username;
        this.path = System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator + username;

    }

    private void fileMethod(File file) { //lists user recorded files
        File[] files = file.listFiles();
        if (files.length!=0) {
            for (File singleFile:
                    files) {
                if(singleFile.isDirectory()) {
                    System.out.println("Directory: " + singleFile.getName());
                    fileMethod(singleFile); //checks all directories of said user
                }
                else {
                    System.out.println("File: " + singleFile);
                }
            }
        }
    }


    @Override
    public void run(){
            File file =new File(path);
            fileMethod(file);
    }

}
