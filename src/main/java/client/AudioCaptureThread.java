package client;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Meelis on 30/03/2017.
 */
public class AudioCaptureThread implements Runnable {
    private boolean bufferedMode;
    private final BlockingQueue<String> recordingQueue;
    private final DataOutputStream servStream;
    private final AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
    private final ByteArrayOutputStream captureOutputStream;

    public AudioCaptureThread(ByteArrayOutputStream captureOutputStream, DataOutputStream servStream,BlockingQueue<String> recordingQueue) {
        this.captureOutputStream = captureOutputStream;
        this.servStream = servStream;
        this.recordingQueue = recordingQueue;
    }

    public ByteArrayOutputStream getCaptureOutputStream() {
        return captureOutputStream;
    }

    public void setBufferedMode(boolean bufferedMode) {
        this.bufferedMode = bufferedMode;
    }

    @Override
    public void run() {
        try {
            TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
            microphone.open(format);
            byte[] recordByteBuffer = new byte[microphone.getBufferSize() / 5];
            int numBytesRead;
            recordingQueue.take(); //Waits for initial input "start"
            microphone.start();
            try {
                servStream.writeInt(1);
                servStream.writeInt(microphone.getBufferSize()/5);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true){
                String command = recordingQueue.poll();
                if (command != null){
                    if (command.equals("stop")){
                        break;
                    }
                    if (command.equals("pause")){
                        if (recordingQueue.take().equals("stop")) {
                            break;
                        }
                        else {
                            continue; //Waits for input "resume"
                        }
                    }
                    if (command.equals("buffer")){
                        try {
                            servStream.writeInt(2); //End of audio transmission.
                            servStream.writeInt(1); //Start new file
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                numBytesRead = microphone.read(recordByteBuffer,0,recordByteBuffer.length);
                captureOutputStream.write(recordByteBuffer,0,numBytesRead);
                try {
                    servStream.writeInt(0);
                    servStream.writeInt(numBytesRead);
                    servStream.write(recordByteBuffer, 0, numBytesRead);
                }
                catch (IOException e){
                    throw new RuntimeException(e);
                }
            }

            if (bufferedMode){
                try {
                    servStream.writeInt(2); //Tell server we are done recording
                    servStream.writeInt(2); //Tell server we do not want more files
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                try {
                    servStream.writeInt(2);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            microphone.stop();
            System.out.println(captureOutputStream.size());
            microphone.close();

        }
        catch (LineUnavailableException | InterruptedException e){
            throw new RuntimeException(e);
        }


    }
}
