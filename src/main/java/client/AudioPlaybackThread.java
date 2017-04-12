package main.java.client;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Meelis on 31/03/2017.
 */
public class AudioPlaybackThread implements Runnable {
    private final AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
    private final ByteArrayOutputStream captureOutputStream;

    public AudioPlaybackThread(ByteArrayOutputStream captureOutputStream) {
        this.captureOutputStream = captureOutputStream;
    }

    @Override
    public void run() {
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
    }
}
