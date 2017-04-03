import javax.sound.sampled.*;
import javax.xml.crypto.Data;
import javax.xml.transform.Source;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

/**
 * Created by Alo on 26-Mar-17.
 */
public class SoundCardCaptureThread implements Runnable {

    private final AudioFormat audioFormat = new AudioFormat(8000.0f, 16, 1, true, true);
    private final ByteArrayOutputStream captureOutputStream;
    private AudioFormat[] supportedFormats;

    public SoundCardCaptureThread(ByteArrayOutputStream captureOutputStream) {
        this.captureOutputStream = captureOutputStream;
    }

    public ByteArrayOutputStream getCaptureOutputStream() {
        return captureOutputStream;
    }

    @Override
    public void run() {
        try {
            Mixer.Info[] info = AudioSystem.getMixerInfo();


            Mixer mixer = AudioSystem.getMixer(info[3]);
            System.out.println(Arrays.toString(mixer.getTargetLineInfo()));

            DataLine.Info targetDataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

            TargetDataLine targetDataLine = (TargetDataLine) mixer.getLine(targetDataLineInfo);
            targetDataLine.open();
            targetDataLine.start();

            int bufferSize = (int)audioFormat.getSampleRate() *
                    audioFormat.getFrameSize();
            byte buffer[] = new byte[bufferSize];
            while (Client.recording) {
                int count = targetDataLine.read(buffer, 0, buffer.length);
                if (count > 0) {
                    captureOutputStream.write(buffer, 0, count);
                }
            }
            targetDataLine.drain();
            targetDataLine.stop();
            captureOutputStream.close();
            targetDataLine.close();










        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }




    }






}
