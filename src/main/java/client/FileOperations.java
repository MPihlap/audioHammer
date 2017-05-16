package client;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import javax.xml.crypto.Data;
import javax.xml.transform.Source;
import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Created by Alo on 18-Apr-17.
 */
public class FileOperations {
    private ArrayList<Path> allFilesWithPath = new ArrayList<>();
    private final String path;
    Double fileSizes = 0.0;



    public FileOperations(String username) {
        this.path = System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator + username;
    }

    //adds user recorded files to list
    private void listFiles() throws IOException {
        allFilesWithPath = new ArrayList<>();
        SimpleFileVisitor<Path> simpleFileVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (path.toString().endsWith(".wav")) {
                    allFilesWithPath.add(path);
                }
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(Paths.get(path), simpleFileVisitor);
    }

    public double getFileSizes() throws IOException {
        fileSizes = 0.0;
        SimpleFileVisitor<Path> simpleFileVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (path.toString().endsWith(".wav")) {
                    fileSizes+=attrs.size()/(1048576.0);
                }
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(Paths.get(path), simpleFileVisitor);
        return fileSizes;
    }

    //renames file
    public boolean renameFile(String oldFilename, String newFilename) {
        for (Path file :
                allFilesWithPath) {
            if (file.getFileName().toString().equals(newFilename + ".wav")) {
                System.out.println(file.getFileName());
                return false;
            }
        }
        try {
            Files.move(new File(oldFilename).toPath(), new File(oldFilename).toPath().resolveSibling(newFilename + ".wav"));
            return true;
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
    }

    //deletes file
    public void deleteFile(String fileName) throws IOException {
        Files.delete(Paths.get(fileName));
    }

    public static boolean sendFile(String filename, DataOutputStream dataOutputStream) throws IOException {
        System.out.println("in sendFile");
        File file = new File(filename);
        long size = file.length();
        dataOutputStream.writeLong(size);
        int bytesRead;
        byte[] buffer = new byte[1024];
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            while ((bytesRead = fileInputStream.read(buffer, 0, buffer.length)) > 0) {
                dataOutputStream.write(buffer, 0, bytesRead);
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public static void createWAV(byte[] fileBytes, File file, AudioFormat audioFormat) throws IOException {
        AudioInputStream ais;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes)) {
            //AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, true);
            ais = new AudioInputStream(bais, audioFormat, fileBytes.length / audioFormat.getFrameSize());
        }

        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
    }

    //for MyCloudStage
    public ArrayList<Path> getAllFiles() throws IOException {
        listFiles();
        return allFilesWithPath;
    }

    public String[] getFileData(String filePath) throws IOException {
        String length;
        File file = new File(filePath);
        Date mod = new Date(file.lastModified());
        AudioFormat format;
        long frames;
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file)) {
            format = audioInputStream.getFormat();
            frames = audioInputStream.getFrameLength();
            length = String.valueOf((frames) / format.getFrameRate()); //gets length of file in seconds
            audioInputStream.close();
        } catch (UnsupportedAudioFileException e) {
            length = "Not available";
        }

        return new String[]{mod.toString(), length};
    }


}
