package client;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by Alo on 22-Apr-17.
 */


//Clip requires separate thread to be run on, which is why it's not in FileOperations

//TODO: maybe add custom media player in stage 3?
public class PlayExistingFile implements Runnable, LineListener {

    private String fileName;

    public PlayExistingFile(String fileName) {
        this.fileName = fileName;
    }

    private void runFile() {
        File audioFile = new File(fileName);
        try(AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile)) {
            Clip audioClip = AudioSystem.getClip();
            audioClip.addLineListener(this);
            audioClip.open(audioInputStream);
            audioClip.start();
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void update(LineEvent event) {
        LineEvent.Type type = event.getType();

        if(type==LineEvent.Type.START) {
            System.out.println("Started playback");
        }
        if(type==LineEvent.Type.STOP) {
            System.out.println("Stopped playback");
        }

    }

    @Override
    public void run() {
        runFile();
    }
}
