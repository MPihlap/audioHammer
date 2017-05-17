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
    private final AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
    private final ByteArrayOutputStream captureOutputStream;
    private final DataInputStream servInputStream;

    public AudioPlaybackThread(ByteArrayOutputStream captureOutputStream, DataInputStream servInputStream) {
        this.captureOutputStream = captureOutputStream;
        this.servInputStream = servInputStream;
    }

    private void streamAudioFromServer() throws IOException, LineUnavailableException {
        int filesize = servInputStream.readInt();
        int totalBytesRead = 0;
        byte[] buffer = new byte[(int) format.getSampleRate()];
        SourceDataLine speakerDataLine = null;
        try {
            speakerDataLine = AudioSystem.getSourceDataLine(format);
            speakerDataLine.open(format);
            speakerDataLine.start();
            while (totalBytesRead < filesize) {
                int bytesToRead = servInputStream.readInt();
                servInputStream.readFully(buffer, 0, bytesToRead);
                speakerDataLine.write(buffer, 0, bytesToRead);
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
        /*
        try {
            System.out.println(captureOutputStream.size());
            SourceDataLine speakerDataLine = AudioSystem.getSourceDataLine(format);
            int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
            byte[] recordedAudioBytes = captureOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(recordedAudioBytes);
            AudioInputStream playbackStream = new AudioInputStream(byteArrayInputStream, format, captureOutputStream.size());
            byte[] playbackBuffer = new byte[bufferSize];
            speakerDataLine.open(format);
            speakerDataLine.start();
            int len;
            while ((len = playbackStream.read(playbackBuffer, 0, playbackBuffer.length)) != -1) {
                if (len > 0) {
                    speakerDataLine.write(playbackBuffer, 0, len);
                }
            }
            speakerDataLine.stop();
            speakerDataLine.close();
        }
        catch (IOException | LineUnavailableException e){
            throw new RuntimeException(e);
        }
        */
    }
}
