package client;

import server.LoginHandler;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Helen on 12-Mar-17.
 */
public class Client {

    public static void main(String[] args) throws IOException {
        BlockingQueue<String> recordingInfo = new ArrayBlockingQueue<String>(5);
        String username;
        try (Socket servSocket = new Socket("localhost", 1337);
             DataOutputStream servStream = new DataOutputStream(servSocket.getOutputStream());
             Scanner sc = new Scanner(System.in)
        ) {
            System.out.println("Welcome!");
            while (true) {
                System.out.println("Log in or create new account? (l/n)");
                String nextline = sc.nextLine();
                if(nextline.equals("n")) {
                    username = LoginHandler.newUserAccount(sc);
                    break;
                }
                else if(nextline.equals("l")) {
                    username = LoginHandler.login(sc);
                    if (!(username ==null)) {
                        System.out.println("Success!");
                        break;
                    }
                }
                System.out.println("Bad input! Try again.");

            }
            System.out.println("Would you like to see your files? (y/n)");
            String userResponse = sc.nextLine();
            if (userResponse.equals("y")) {
                new Thread(new DisplayFiles(username)).start();
            }

            String fileName = selectFilename(sc);
            servStream.writeUTF(username);
            servStream.writeUTF(fileName);
            while (true){
                System.out.println("Write 'start' to begin capturing");
                if (sc.nextLine().equals("start")){
                    recordingInfo.add("start");
                    break;
                }
            }
            AudioCaptureThread audioCaptureThread = new AudioCaptureThread(new ByteArrayOutputStream(), servStream,recordingInfo);
            Thread captureThread = new Thread(audioCaptureThread);
            captureThread.start();
            System.out.println("Started recording");    //lindistab kuni kirjutatakse stop;
            System.out.println("Type 'stop' to stop recording");
            System.out.println("Type 'pause' to pause");
            while (true) {
                String nextLine = sc.nextLine();
                if (nextLine.equals("stop")) {
                    recordingInfo.add("stop");
                    System.out.println("Recording stopped");
                    break;
                }
                if (nextLine.equals("pause")){
                    recordingInfo.add("pause");
                    System.out.println("Recording paused");
                    System.out.println("Type 'resume' to resume or 'stop' to finish recording.");
                }
                if (nextLine.equals("resume")){
                    recordingInfo.add("resume");
                    System.out.println("Recording resumed");
                }
            }
            try {
                captureThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(audioCaptureThread.getCaptureOutputStream().size());
            System.out.println("Finished recording");
            System.out.println("Would you like to listen to your recording? (y/n)");
            userResponse = sc.nextLine();
            if (userResponse.equals("y")) {
                new Thread(new AudioPlaybackThread(audioCaptureThread.getCaptureOutputStream())).start();
            }

        }
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

    //Sends WAV file to server
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

