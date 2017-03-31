import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Helen on 12-Mar-17.
 */
public class Client {
    public static void main(String[] args) throws IOException {
        sendWAV("LoTR2.wav");
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
    private static void sendWAV(String filename) throws IOException {
        byte[] audioBytes = readWAV(filename);
        int lengthAudioBytes = audioBytes.length;
        try (Socket socket = new Socket("localhost", 1337);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
            dos.writeInt(lengthAudioBytes);
            try {
                for (int byteCount = 0; byteCount < lengthAudioBytes; byteCount++) {
                    dos.writeByte(audioBytes[byteCount]);
                }
                dos.writeUTF(filename);
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

