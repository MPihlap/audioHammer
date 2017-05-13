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
    public byte[] readWAV(String filePath) throws IOException {
        byte[] audioBytes;
        File file = new File(filePath);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
            int read;
            byte[] buff = new byte[1024];
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            audioBytes = out.toByteArray();
        }
        return audioBytes;
    }

    public void sendWAV(byte[] fileBytes, DataOutputStream dataOutputStream) throws IOException {
        long lengthAudioBytes = fileBytes.length;
            dataOutputStream.writeLong(lengthAudioBytes);
            dataOutputStream.write(fileBytes);


    }

     **/
    public boolean downloadFile(String filename, DataOutputStream dataOutputStream) throws IOException, UnsupportedAudioFileException {
        long totalFramesRead = 0;
        int framesRead;
        int bytesRead;
        long fileSize = new File(filename).length();
        long bytesSent=0;
        System.out.println(fileSize);
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, true);




        try (AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(filename))) {
            int bytesPerFrame = inputStream.getFormat().getFrameSize();
            byte[] buffer = new byte[8192*bytesPerFrame];
            dataOutputStream.writeLong(fileSize);
            dataOutputStream.writeInt(bytesPerFrame);
            dataOutputStream.writeLong(inputStream.getFrameLength());
            System.out.println(inputStream.getFrameLength());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (totalFramesRead!=inputStream.getFrameLength()) {
                bytesRead = inputStream.read(buffer);
                dataOutputStream.write(buffer, 0, bytesRead);
                byteArrayOutputStream.write(buffer);
                framesRead = bytesRead/bytesPerFrame;
                totalFramesRead +=framesRead;
                System.out.println(totalFramesRead);
            }
            byteArrayOutputStream.flush();
            byte[] a = byteArrayOutputStream.toByteArray();
            createWAV(a, new File("C:\\Users\\Alo\\AudioHammer\\Downloads\\test.wav"));
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
