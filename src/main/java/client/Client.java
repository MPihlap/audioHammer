package client;


import server.ServerThread;

import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Helen on 12-Mar-17.
 */
public class Client {
    private AudioFormat audioFormat = new AudioFormat(44100,16,1,true,true);
    private String username;
    private Socket servSocket;
    private DataOutputStream servOutputStream;
    private DataInputStream servInputStream;
    private BlockingQueue<String> recordingInfo = new ArrayBlockingQueue<String>(5);
    private Thread captureThread;
    private Path downloadPath = Paths.get(System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator + "Downloads");

    public String getUsername() {
        return username;
    }
    public boolean isSocketCreated(){
        return servSocket != null;
    }
    private void sendFormat() throws IOException {
        servOutputStream.writeFloat(audioFormat.getSampleRate());
        servOutputStream.writeInt(audioFormat.getSampleSizeInBits());
        servOutputStream.writeInt(audioFormat.getChannels());
        servOutputStream.writeBoolean(true);
        servOutputStream.writeBoolean(true);
    }
    public List<String> getAllFilesFromCloud() throws IOException {
        List<String> allFiles = new ArrayList<>();
        int nrOfFiles = servInputStream.readInt();
        for (int i = 0; i < nrOfFiles; i++) {
            allFiles.add(servInputStream.readUTF());
        }
        return allFiles;
    }
    public void setUsername(String username) {
        this.username = username;
        System.out.println("sain username");
    }

    public void createConnection() throws IOException {
        this.servSocket = new Socket("localhost", 1337);
        this.servOutputStream = new DataOutputStream(servSocket.getOutputStream());
        this.servInputStream = new DataInputStream(servSocket.getInputStream());
    }

    public void closeConnection() throws IOException {
        this.servSocket.close();
    }

    public void sendCommand(String command) throws IOException {
        servOutputStream.writeUTF(command);
    }

    public void startRecording() throws IOException {
        sendFormat();
        servOutputStream.writeBoolean(false);
        recordingInfo.add("start");
        AudioCaptureThread audioCaptureThread = new AudioCaptureThread(new ByteArrayOutputStream(), servOutputStream, recordingInfo, false);
        this.captureThread = new Thread(audioCaptureThread);
        captureThread.start();
        System.out.println("hakkas lindistama");
    }
    public void startBufferedRecording(int minutes)throws IOException{
        sendFormat();
        servOutputStream.writeBoolean(true);
        servOutputStream.writeInt(minutes);
        recordingInfo.add("start");
        AudioCaptureThread audioCaptureThread = new AudioCaptureThread(new ByteArrayOutputStream(), servOutputStream, recordingInfo, true);
        this.captureThread = new Thread(audioCaptureThread);
        captureThread.start();
    }
    public void saveBuffer() throws IOException {
        recordingInfo.add("buffer");
    }

    public void pauseRecording() {
        recordingInfo.add("pause");
        System.out.println("recording paused");
    }

    public void resumeRecording() {
        recordingInfo.add("resume");
        System.out.println("resumed");
    }

    public void stopRecording() throws IOException {
        recordingInfo.add("stop");
        System.out.println("stopped");
        try {
            captureThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("finished recording");
    }
    public boolean renameFile(String oldName,String newName) throws IOException {
        servOutputStream.writeUTF("Rename");
        servOutputStream.writeUTF(oldName);
        servOutputStream.writeUTF(newName);
        return servInputStream.readBoolean();
    }
    public boolean deleteFile(String filename) throws IOException {
        servOutputStream.writeUTF("Delete");
        servOutputStream.writeUTF(filename);
        return servInputStream.readBoolean();
    }

    public boolean downloadFile(String filePath, String fileName) throws IOException {
        servOutputStream.writeUTF("Download");
        servOutputStream.writeUTF(filePath);
        String clientPath = downloadPath + File.separator + fileName;
        return receiveFile(clientPath, servInputStream);
    }


    public String[] getFileData(String filePath) throws IOException {
        servOutputStream.writeUTF("Data");
        servOutputStream.writeUTF(filePath);
        String[] data = new String[2];
        data[0] = servInputStream.readUTF();
        data[1] = servInputStream.readUTF();
        return data;
    }

    public boolean passwordChange(String password) throws IOException {
        servOutputStream.writeUTF("pwChange");
        servOutputStream.writeUTF(password);
        return (servInputStream.readBoolean());
    }
    public boolean sendUsername(String username, String password) throws IOException {
        servOutputStream.writeUTF("username"); // Indicate incoming user info
        servOutputStream.writeUTF(username);
        servOutputStream.writeUTF(password);
        return servInputStream.readBoolean();
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



