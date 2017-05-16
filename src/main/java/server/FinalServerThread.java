package server;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by Helen on 16.05.2017.
 */
public class FinalServerThread implements Runnable {
    private Socket socket;

    public FinalServerThread(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        try (InputStream inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream)) {
            String filename=dataInputStream.readUTF();
            System.out.println(filename);
            String pathString = System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator +filename;
            receiveFile(pathString,dataInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean receiveFile(String fileName, DataInputStream dataInputStream) {
        try {
            long fileSize = dataInputStream.readLong();
            byte[] buffer = new byte[1024];
            long totalBytesRead = 0;
            try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
                while (totalBytesRead < fileSize) {
                    int bytesRead = dataInputStream.read(buffer, 0, buffer.length);
                    totalBytesRead += bytesRead;
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}

