import java.io.ByteArrayOutputStream;

/**
 * Created by Meelis on 23/03/2017.
 */

public class Main {
    public static boolean recording;

    public static void main(String[] args) {
        AudioCaptureThread audioCaptureThread = new AudioCaptureThread(new ByteArrayOutputStream());
        SoundCardCaptureThread soundCardCaptureThread = new SoundCardCaptureThread(new ByteArrayOutputStream());
        new Thread(soundCardCaptureThread).start();
        System.out.println("Started recording");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            recording = false;
            System.out.println("Finished recording");
            ByteArrayOutputStream soundAsBytes = soundCardCaptureThread.getCaptureOutputStream();
        }
        new Thread(new AudioPlaybackThread(soundCardCaptureThread.getCaptureOutputStream())).start();
    }

}
