package client;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Alo on 18-Apr-17.
 */
public class FileOperations{
    private ArrayList<Path> allFilesWithPath = new ArrayList<>();
    private final String path;


    public FileOperations(String username) {
        this.path = System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator + username;
    }

    //adds user recorded files to list
    private void listFiles() {
        SimpleFileVisitor<Path> simpleFileVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (path.toString().endsWith(".wav")) {
                    allFilesWithPath.add(path);
                }
                return FileVisitResult.CONTINUE;
            }
        };
        try {
            Files.walkFileTree(Paths.get(path), simpleFileVisitor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //renames file
    public boolean renameFile(String oldFilename, String newFilename) {
        for (Path file :
                allFilesWithPath) {
            if (file.getFileName().toString().equals(newFilename + ".wav")) {
                return false;
            }
        }
        return (new File(oldFilename).renameTo(new File(Paths.get(oldFilename).getParent() + File.separator + newFilename + ".wav")));

    }

    //deletes file
    public void deleteFile(String fileName) {
        try {
            Files.delete(Paths.get(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    //for MyCloudStage
    public ArrayList<Path> getAllFiles() throws IOException {
        listFiles();
        return allFilesWithPath;
    }



}
