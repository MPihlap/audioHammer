import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;

/**
 * Created by Helen on 12-Mar-17.
 */
public class Client {
    public static boolean recording;

    public static void main(String[] args) throws IOException {
        recording = true;
        try (Socket servSocket = new Socket("localhost", 1337);
             DataOutputStream servStream = new DataOutputStream(servSocket.getOutputStream());
             Scanner sc = new Scanner(System.in)
        ) {
            String fileName = selectFilename(sc);
            servStream.writeUTF(fileName);
            System.out.println("Write 'start' to begin capturing");
            while (true){
                if (sc.nextLine().equals("start")){
                    break;
                }
            }
            AudioCaptureThread audioCaptureThread = new AudioCaptureThread(new ByteArrayOutputStream(), servStream);
            Thread captureThread = new Thread(audioCaptureThread);
            captureThread.start();
            System.out.println("Started recording");    //lindistab kuni kirjutatakse stop
            while (true) {
                String nextLine = sc.nextLine();
                if (nextLine.equals("stop")) {
                    recording = false;
                    break;
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

