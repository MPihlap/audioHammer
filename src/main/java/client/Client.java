package client;


import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import server.LoginHandler;

/**
 * Created by Helen on 12-Mar-17.
 */
public class Client {
    private String username;
    private Socket servSocket;
    private DataOutputStream servStream;
    private BlockingQueue<String> recordingInfo = new ArrayBlockingQueue<String>(5);
    Thread captureThread;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        System.out.println("sain username");
    }

    public void createConnection() throws IOException {
        this.servSocket = new Socket("localhost", 1337);
        this.servStream = new DataOutputStream(servSocket.getOutputStream());
    }

    public void closeConnection() throws IOException {
        this.servSocket.close();
    }

    public void sendCommand(String command) throws IOException {
        servStream.writeUTF(command);
    }

    public void startRecording() throws IOException {
        servStream.writeBoolean(false);
        recordingInfo.add("start");
        AudioCaptureThread audioCaptureThread = new AudioCaptureThread(new ByteArrayOutputStream(), servStream, recordingInfo);
        audioCaptureThread.setBufferedMode(false);
        this.captureThread = new Thread(audioCaptureThread);
        captureThread.start();
        System.out.println("hakkas lindistama");
    }
    public void startBufferedRecording(int minutes)throws IOException{
        servStream.writeBoolean(true);
        servStream.writeInt(minutes);
        recordingInfo.add("start");
        AudioCaptureThread audioCaptureThread = new AudioCaptureThread(new ByteArrayOutputStream(), servStream, recordingInfo);
        audioCaptureThread.setBufferedMode(true);
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
}

