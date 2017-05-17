package client;

import javax.sound.sampled.*;
import java.io.*;

/**
 * Created by Meelis on 31/03/2017.
 */

/**
 * Currently replaced with PlayExistingFile
 */
public class AudioPlaybackThread implements Runnable {
    private final AudioFormat format;
    //private final ByteArrayOutputStream captureOutputStream;
    private final DataInputStream servInputStream;

    public AudioPlaybackThread(DataInputStream servInputStream,AudioFormat audioFormat) {
        this.servInputStream = servInputStream;
        this.format = audioFormat;
    }

    /**
     * Reads audio from server and sends it to system default SourceDataLine
     * @throws IOException
     * @throws LineUnavailableException
     */
    private void streamAudioFromServer() throws IOException, LineUnavailableException {
        int filesize = servInputStream.readInt();
        int totalBytesRead = 0;
        byte[] buffer = new byte[(int) format.getSampleRate()*2];
        SourceDataLine speakerDataLine = null;
        try {
            speakerDataLine = AudioSystem.getSourceDataLine(format);
            speakerDataLine.open(format);
            speakerDataLine.start();
            while (totalBytesRead < filesize) {
                int bytesToRead = servInputStream.readInt();
                servInputStream.readFully(buffer,0,bytesToRead);
                speakerDataLine.write(buffer,0,bytesToRead);
                totalBytesRead += bytesToRead;
            }
        } finally {
            if (speakerDataLine != null) {
                speakerDataLine.stop();
                speakerDataLine.close();
            }
        }
    }

    @Override
    public void run() {
        try {
            streamAudioFromServer();
        } catch (IOException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }
}
