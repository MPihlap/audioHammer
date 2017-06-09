package client;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Meelis on 30/03/2017.
 */
public class AudioCaptureThread implements Runnable {
    private String filename;
    private String username;
    private String localPath;
    private byte[] bufferBytes;

    private boolean saveLocally;
    private boolean saveRemote;
    private final boolean bufferedMode;
    private final BlockingQueue<String> commandsFromClient;
    private DataOutputStream servStream;
    private final AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
    private ByteArrayOutputStream captureOutputStream;
    private ByteBuffer byteBuffer;
    private BlockingQueue<String> commandsToClient;
    /**
     * This Queue is used to send commands to client during buffered recording
     *
     * @param commandsToClient
     */
    public void setCommandsToClient(BlockingQueue<String> commandsToClient) {
        this.commandsToClient = commandsToClient;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getRecordedBytes() {
        if (bufferedMode) {
            return bufferBytes;
        }
        return captureOutputStream.toByteArray();
    }


    public AudioCaptureThread(ByteArrayOutputStream captureOutputStream, DataOutputStream servStream, BlockingQueue<String> commandsFromClient, boolean bufferedMode) {
        this.captureOutputStream = captureOutputStream;
        this.servStream = servStream;
        this.commandsFromClient = commandsFromClient;
        this.bufferedMode = bufferedMode;
    }

    public AudioCaptureThread(boolean bufferedMode, BlockingQueue<String> commandsFromClient, ByteBuffer byteBuffer, DataOutputStream servStream) {
        this.bufferedMode = bufferedMode;
        this.commandsFromClient = commandsFromClient;
        this.byteBuffer = byteBuffer;
        this.servStream = servStream;
    }

    public void setSaveLocally(boolean saveLocally) {
        this.saveLocally = saveLocally;
    }

    public void setSaveRemote(boolean saveRemote) {
        this.saveRemote = saveRemote;
    }

    @Override
    public void run() {

        try {
            TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
            microphone.open(format);
            System.out.println("Buffer size:" + microphone.getBufferSize());
            byte[] recordByteBuffer = new byte[microphone.getBufferSize() / 5];
            recordAudio(microphone, recordByteBuffer);

        } catch (LineUnavailableException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void recordAudio(TargetDataLine microphone, byte[] recordByteBuffer) throws InterruptedException, IOException {
        int numBytesRead;
        commandsFromClient.take(); //Waits for initial input "start"
        microphone.start();
        if (saveRemote) {
            servStream.writeInt(1);
            servStream.writeInt(microphone.getBufferSize() / 5);
        }
        while (true) {
            String command = commandsFromClient.poll();
            if (command != null) {
                if (command.equals("stop")) {
                    if (bufferedMode) {
                        commandsToClient.add("stop");
                    }
                    break;
                } else if (command.equals("pause")) {
                    if (commandsFromClient.take().equals("stop")) {
                        break;
                    } else {
                        continue; //Waits for input "resume"
                    }
                } else if (command.equals("buffer")) {
                    if (saveRemote) {
                        servStream.writeInt(2); //End of audio transmission.
                        servStream.writeBoolean(true); //Start new file
                        servStream.writeInt(1);
                        servStream.writeInt(microphone.getBufferSize() / 5);
                    }
                    if (saveLocally) {
                        bufferBytes = Arrays.copyOf(byteBuffer.array(),byteBuffer.position());
                        byteBuffer.clear();
                        FileOperations.fileSaving(filename,bufferBytes,username,format,true,localPath);
                        //commandsToClient.add("buffer");
                    }
                }
            }
            numBytesRead = microphone.read(recordByteBuffer, 0, recordByteBuffer.length);
            if (saveLocally && !bufferedMode) {
                captureOutputStream.write(recordByteBuffer, 0, numBytesRead);
            }
            if (saveRemote) {
                servStream.writeInt(0);
                servStream.writeInt(numBytesRead);
                servStream.write(recordByteBuffer, 0, numBytesRead);
            }
            if (bufferedMode && saveLocally) {
                if (byteBuffer.position() + numBytesRead > byteBuffer.capacity()) { //Check if size limit has been reached
                    byteBuffer.position(recordByteBuffer.length);               //Go to position after first buffer
                    byteBuffer.compact();                          //Remove bytes before position
                }
                byteBuffer.put(recordByteBuffer, 0, numBytesRead);
            }
        }

        if (bufferedMode) {
            if (saveRemote) {
                servStream.writeInt(2); //Tell server we are done recording
                servStream.writeBoolean(false); //Tell server we do not want more files
            }
        } else {
            if (saveRemote) {
                servStream.writeInt(2);  //Tell server we are done recording
            }
        }
        microphone.stop();
        microphone.close();
    }
}
