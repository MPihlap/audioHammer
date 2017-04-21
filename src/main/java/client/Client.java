package client;


import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
        recordingInfo.add("start");
        AudioCaptureThread audioCaptureThread = new AudioCaptureThread(new ByteArrayOutputStream(), servStream, recordingInfo);
        this.captureThread = new Thread(audioCaptureThread);
        captureThread.start();
        System.out.println("hakkas lindistama");
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
        //TODO: parem viis seda lahendada
        //this.servStream = new DataOutputStream(servSocket.getOutputStream());
        try {
            captureThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("finished recording");
    }





    public void main(String[] args) throws IOException {
        BlockingQueue<String> recordingInfo = new ArrayBlockingQueue<String>(5);
        System.out.println("läksin tööle");

        try (Socket servSocket = new Socket("localhost", 1337);
             DataOutputStream servStream = new DataOutputStream(servSocket.getOutputStream());
             Scanner sc = new Scanner(System.in)
        ) {

            /**
            label:

            while (true) {
                System.out.println("Would you like to use buffer mode? y/n)");
                String response = sc.nextLine();
                switch (response) {
                    case "y":
                        String minutes = getMinutes(sc);
                        servStream.writeBoolean(true);
                        servStream.writeInt(Integer.parseInt(minutes));
                        break label;
                    case "n":
                        servStream.writeBoolean(false);
                        break label;
                    default:
                        System.out.println("False input");
                }
            }

            while (true) {
                System.out.println("Write 'start' to begin capturing");
                if (sc.nextLine().equals("start")) {
                    recordingInfo.add("start");
                    break;
                }
            }
             **/
            AudioCaptureThread audioCaptureThread = new AudioCaptureThread(new ByteArrayOutputStream(), servStream, recordingInfo);
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
                if (nextLine.equals("pause")) {
                    recordingInfo.add("pause");
                    System.out.println("Recording paused");
                    System.out.println("Type 'resume' to resume or 'stop' to finish recording.");
                }
                if (nextLine.equals("resume")) {
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
            String userResponse = sc.nextLine();
            if (userResponse.equals("y")) {
                new Thread(new AudioPlaybackThread(audioCaptureThread.getCaptureOutputStream())).start();
            }

        }
    }

    private static String getMinutes(Scanner sc) {
        String response;
        System.out.println("Select buffer length (min): ");
        while (true) {
            response = sc.nextLine();
            if (isInteger(response))
                break;
            else
                System.out.println("Enter an integer");
        }
        return response;
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

    public static boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

