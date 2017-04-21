package client;

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
public class DisplayFiles {
    private ArrayList<String> allFiles = new ArrayList<>();
    private File file;
    private String path;


    public DisplayFiles(String username) {
        this.path = System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator + username;
        this.file = new File(path);
    }


;


    private void fileMethod() { //adds user recorded files to list
        SimpleFileVisitor<Path> simpleFileVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (path.toString().endsWith(".wav")) {
                    allFiles.add(path.getFileName().toString());
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

    public boolean renameFile(String oldFilename, String newFilename) {
        for (String file :
                allFiles) {
            if (file.equals(newFilename + ".wav")) {
                return false;
            }
            }
        SimpleFileVisitor<Path> simpleFileVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (path.getFileName().toString().equals(oldFilename)) {
                    System.out.println((new File(path.toString())).renameTo(new File(path.getParent() + File.separator + newFilename  +  ".wav")));
                    return FileVisitResult.TERMINATE;
                }
                return FileVisitResult.CONTINUE;
            }
        };
        try {
            Files.walkFileTree(Paths.get(path), simpleFileVisitor);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return true;
    }


    public ArrayList<String> getAllFiles() throws IOException {
        fileMethod();
        return allFiles;
    }
}
