import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;

/**
 * Created by Meelis on 30/03/2017.
 */
public class AudioCaptureThread implements Runnable {
    private final AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
    private final ByteArrayOutputStream captureOutputStream;

    public AudioCaptureThread(ByteArrayOutputStream captureOutputStream) {
        this.captureOutputStream = captureOutputStream;
    }

    public ByteArrayOutputStream getCaptureOutputStream() {
        return captureOutputStream;
    }


    @Override
    public void run() {
        try {
            TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
            microphone.open(format);
            byte[] recordByteBuffer = new byte[microphone.getBufferSize() / 5];
            int numBytesRead;
            microphone.start();

            while (Main.recording){
                numBytesRead = microphone.read(recordByteBuffer,0,recordByteBuffer.length);
                captureOutputStream.write(recordByteBuffer,0,numBytesRead);
            }

            microphone.drain();
            microphone.stop();
            System.out.println(captureOutputStream.size());
            microphone.close();
        }
        catch (LineUnavailableException e){
            throw new RuntimeException(e);
        }



    }
}
