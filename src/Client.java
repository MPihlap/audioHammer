import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import java.util.Scanner;

/**
 * Created by Helen on 12-Mar-17.
 */
public class Client {
    public static boolean recording;

    public static void main(String[] args) throws IOException {
        recording = true;
        AudioCaptureThread audioCaptureThread = new AudioCaptureThread(new ByteArrayOutputStream());
        new Thread(audioCaptureThread).start();
        System.out.println("Started recording");
        try (Scanner sc = new Scanner(System.in)) { //lindistab kuni kirjutatakse stop
            System.out.println(audioCaptureThread.getCaptureOutputStream().size());
            if (Objects.equals(sc.nextLine(), "stop")) {
                recording=false;
                new Thread(new AudioPlaybackThread(audioCaptureThread.getCaptureOutputStream())).start();
                System.out.println("Finished recording");
                sendWAV(audioCaptureThread.getCaptureOutputStream());
            }


        }

    }
    //Reads WAV file into byteArray
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

            out.flush();
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
            try(Scanner sc = new Scanner(System.in)) {
                System.out.println("Enter file name (without '(' or ')' )"); //saab failinime ise valida

                for (byte audioByte : audioBytes) {
                    dos.writeByte(audioByte);
                }
                String fileName = sc.nextLine();

                while (true) {
                    if (fileName.contains("(") || fileName.contains(")")) {
                        System.out.println("Invalid format! Please enter a new name:");
                        fileName = sc.nextLine();
                    }
                    else {
                        break;
                    }
                }
                dos.writeUTF(fileName + ".wav");

            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

