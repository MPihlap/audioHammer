package client;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Meelis on 30/03/2017.
 */
public class AudioCaptureThread implements Runnable {
    private final boolean bufferedMode;
    private final BlockingQueue<String> recordingQueue;
    private final DataOutputStream servStream;
    private final AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
    private final ByteArrayOutputStream captureOutputStream;

    public AudioCaptureThread(ByteArrayOutputStream captureOutputStream, DataOutputStream servStream, BlockingQueue<String> recordingQueue, boolean bufferedMode) {
        this.captureOutputStream = captureOutputStream;
        this.servStream = servStream;
        this.recordingQueue = recordingQueue;
        this.bufferedMode = bufferedMode;
    }

    public ByteArrayOutputStream getCaptureOutputStream() {
        return captureOutputStream;
    }

    @Override
    public void run() {
        try {
            TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
            microphone.open(format);
            System.out.println("Buffer size:" + microphone.getBufferSize());
            byte[] recordByteBuffer = new byte[microphone.getBufferSize() / 5];
            int numBytesRead;
            recordingQueue.take(); //Waits for initial input "start"
            microphone.start();
            servStream.writeInt(1);
            servStream.writeInt(microphone.getBufferSize() / 5);

            while (true) {
                String command = recordingQueue.poll();
                if (command != null) {
                    if (command.equals("stop")) {
                        break;
                    }
                    if (command.equals("pause")) {
                        if (recordingQueue.take().equals("stop")) {
                            break;
                        } else {
                            continue; //Waits for input "resume"
                        }
                    }
                    if (command.equals("buffer")) {
                        System.out.println("In buffer");
                        servStream.writeInt(2); //End of audio transmission.
                        servStream.writeBoolean(true); //Start new file
                        servStream.writeInt(1);
                        servStream.writeInt(microphone.getBufferSize() / 5);
                    }
                }
                numBytesRead = microphone.read(recordByteBuffer, 0, recordByteBuffer.length);
                captureOutputStream.write(recordByteBuffer, 0, numBytesRead);
                servStream.writeInt(0);
                servStream.writeInt(numBytesRead);
                servStream.write(recordByteBuffer, 0, numBytesRead);
            }

            if (bufferedMode) {
                servStream.writeInt(2); //Tell server we are done recording
                servStream.writeBoolean(false); //Tell server we do not want more files
            } else {
                servStream.writeInt(2);  //Tell server we are done recording
            }
            microphone.stop();
            System.out.println(captureOutputStream.size());
            microphone.close();

        } catch (LineUnavailableException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }


    }
}
