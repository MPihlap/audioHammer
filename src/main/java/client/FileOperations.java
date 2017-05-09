package client;

import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
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
    //Reads WAV file into byteArray, coudld be used later
    private static byte[] readWAV(String filename) throws IOException {
        File wavFile = new File(filename);
        byte[] audioBytes;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             BufferedInputStream in = new BufferedInputStream(new FileInputStream(wavFile))) {
            int read;
            byte[] buff = new byte[1024];
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            audioBytes = out.toByteArray();
        }
        return audioBytes;
    }

    //Sends WAV file to server, could be used later
    private static void sendWAV(ByteArrayOutputStream byteArrayOutputStream) throws IOException {
        //byte[] audioBytes = readWAV(byteArrayOutputStream);
        byte[] audioBytes = byteArrayOutputStream.toByteArray();
        int lengthAudioBytes = audioBytes.length;
        try (Socket socket = new Socket("localhost", 1337);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            dos.writeInt(lengthAudioBytes);
            String filename = selectFilename(new Scanner(System.in));
            for (byte b : audioBytes) {
                dos.writeByte(b);
            }
            dos.writeUTF(filename);
        }
    }

    private static String selectFilename(Scanner sc) throws IOException {
        String fileName;
        System.out.println("Enter file name (without '(' or ')' )"); //saab failinime ise valida
        fileName = sc.nextLine();
        while (true) {
            if (fileName.contains("(") || fileName.contains(")")) {
                System.out.println("Invalid format! Please enter a new name:");
                fileName = sc.nextLine();
            } else {
                break;
            }
        }
        return fileName;
    }
}
