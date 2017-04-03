import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Meelis on 30/03/2017.
 */
public class AudioCaptureThread implements Runnable {
    private final DataOutputStream servStream;
    private final AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
    private final ByteArrayOutputStream captureOutputStream;

    public AudioCaptureThread(ByteArrayOutputStream captureOutputStream, DataOutputStream servStream) {
        this.captureOutputStream = captureOutputStream;
        this.servStream = servStream;
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

            while (Client.recording){
                numBytesRead = microphone.read(recordByteBuffer,0,recordByteBuffer.length);
                captureOutputStream.write(recordByteBuffer,0,numBytesRead);
                try {
                    servStream.write(recordByteBuffer, 0, numBytesRead);
                }
                catch (IOException e){
                    throw new RuntimeException(e);
                }
            }

            microphone.stop();
            System.out.println(captureOutputStream.size());
            microphone.close();

        }
        catch (LineUnavailableException e){
            throw new RuntimeException(e);
        }



    }
}
