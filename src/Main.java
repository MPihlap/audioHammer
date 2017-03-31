import java.io.ByteArrayOutputStream;

/**
 * Created by Meelis on 23/03/2017.
 */

public class Main {
    public static boolean recording;

    public static void main(String[] args) {
        recording = true;
        AudioCaptureThread audioCaptureThread = new AudioCaptureThread(new ByteArrayOutputStream());
        new Thread(audioCaptureThread).start();
        System.out.println("Started recording");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            recording = false;
            System.out.println("Finished recording");
        }
        new Thread(new AudioPlaybackThread(audioCaptureThread.getCaptureOutputStream())).start();
    }

}
