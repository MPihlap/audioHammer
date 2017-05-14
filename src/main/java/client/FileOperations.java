package client;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import javax.xml.crypto.Data;
import javax.xml.transform.Source;
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
public class FileOperations {
    private ArrayList<Path> allFilesWithPath = new ArrayList<>();
    private final String path;


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
    public void deleteFile(String fileName) throws IOException {
        Files.delete(Paths.get(fileName));
    }

    /**
     * public byte[] readWAV(String filePath) throws IOException {
     * byte[] audioBytes;
     * File file = new File(filePath);
     * try (ByteArrayOutputStream out = new ByteArrayOutputStream();
     * BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
     * int read;
     * byte[] buff = new byte[1024];
     * while ((read = in.read(buff)) > 0) {
     * out.write(buff, 0, read);
     * }
     * audioBytes = out.toByteArray();
     * }
     * return audioBytes;
     * }
     * <p>
     * public void sendWAV(byte[] fileBytes, DataOutputStream dataOutputStream) throws IOException {
     * long lengthAudioBytes = fileBytes.length;
     * dataOutputStream.writeLong(lengthAudioBytes);
     * dataOutputStream.write(fileBytes);
     * <p>
     * <p>
     * }
     **/
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
        }
        return true;
    }

    public static boolean receiveFile(String filename, DataInputStream dataInputStream) {
        System.out.println("In rcv file");
        try {
            long fileSize = dataInputStream.readLong();
            byte[] buffer = new byte[1024];
            long totalBytesRead = 0;
            try (FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\Meelis\\AudioHammer\\Downloads\\test.wav")) {
                while (totalBytesRead < fileSize) {
                    int bytesRead = dataInputStream.read(buffer, 0, buffer.length);
                    totalBytesRead += bytesRead;
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean downloadFile(String filename, DataOutputStream dataOutputStream) throws IOException, UnsupportedAudioFileException {
        long totalFramesRead = 0;
        int framesRead;
        int bytesRead;
        long fileSize = new File(filename).length();
        long bytesSent = 0;
        System.out.println(fileSize);
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, true);


        try (AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(filename))) {
            int bytesPerFrame = inputStream.getFormat().getFrameSize();
            byte[] buffer = new byte[8192 * bytesPerFrame];
            dataOutputStream.writeLong(fileSize);
            dataOutputStream.writeInt(bytesPerFrame);
            dataOutputStream.writeLong(inputStream.getFrameLength());
            System.out.println(inputStream.getFrameLength());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (totalFramesRead != inputStream.getFrameLength()) {
                bytesRead = inputStream.read(buffer);
                dataOutputStream.write(buffer, 0, bytesRead);
                byteArrayOutputStream.write(buffer);
                framesRead = bytesRead / bytesPerFrame;
                totalFramesRead += framesRead;
                System.out.println(totalFramesRead);
            }
            byteArrayOutputStream.flush();
            byte[] a = byteArrayOutputStream.toByteArray();
            createWAV(a, new File("C:\\Users\\Meelis\\AudioHammer\\Downloads\\test.wav"));
            byteArrayOutputStream.close();
            System.out.println("läbi");
            return true;
        }
    }

    public static void createWAV(byte[] fileBytes, File file) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, true);
        AudioInputStream ais = new AudioInputStream(bais, audioFormat, fileBytes.length / audioFormat.getFrameSize());

        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
    }

    //for MyCloudStage
    public ArrayList<Path> getAllFiles() throws IOException {
        listFiles();
        return allFilesWithPath;
    }


}
