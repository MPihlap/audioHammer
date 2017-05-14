package client;


import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.net.Socket;
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

    public String getUsername() {
        return username;
    }

    public void setAudioFormat(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
    }

    private void sendFormat() throws IOException {
        servOutputStream.writeFloat(audioFormat.getSampleRate());
        servOutputStream.writeInt(audioFormat.getSampleSizeInBits());
        servOutputStream.writeInt(audioFormat.getChannels());
        servOutputStream.writeBoolean(true);
        servOutputStream.writeBoolean(true);
    }
    public boolean isSocketCreated(){
        return servSocket != null;
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
        AudioCaptureThread audioCaptureThread = new AudioCaptureThread(new ByteArrayOutputStream(),
                servOutputStream, recordingInfo, false, audioFormat);
        this.captureThread = new Thread(audioCaptureThread);
        captureThread.start();
        System.out.println("hakkas lindistama");
    }
    public void startBufferedRecording(int minutes)throws IOException{
        sendFormat();
        servOutputStream.writeBoolean(true);
        servOutputStream.writeInt(minutes);
        recordingInfo.add("start");
        AudioCaptureThread audioCaptureThread = new AudioCaptureThread(new ByteArrayOutputStream(), servOutputStream,
                recordingInfo, true, audioFormat);
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
        servOutputStream.writeUTF(filename);
        return servInputStream.readBoolean();
    }
    public boolean sendUsername(String username, String password) throws IOException {
        servOutputStream.writeUTF("username"); // Indicate incoming user info
        servOutputStream.writeUTF(username);
        servOutputStream.writeUTF(password);
        return servInputStream.readBoolean();
    }
}

