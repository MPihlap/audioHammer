package client;


import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Helen on 12-Mar-17.
 */
public class Client {
    private AudioFormat audioFormat = new AudioFormat(44100,16,1,true,true);
    private String username;
    private Socket servSocket;
    private DataOutputStream servOutputStream;
    private DataInputStream servInputStream;
    private AudioCaptureThread audioCaptureThread;
    private String filename;
    private BlockingQueue<String> recordingInfo = new ArrayBlockingQueue<String>(5);
    private Thread captureThread;
    private String downloadPath = System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator + "Downloads";
    private String settingsPath;
    private String localPath;
    private boolean saveLocally;
    private boolean saveRemote;

    public void setAudioFormat(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
    }

    public void streamFileFromCloud(String filename) throws IOException {
        servOutputStream.writeUTF("Listen");
        servOutputStream.writeUTF(filename);
        AudioFormat audioFormat = FileOperations.readFormat(servInputStream);
        new Thread(new AudioPlaybackThread(servInputStream,audioFormat)).start();
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setSaveLocally(boolean saveLocally) {
        this.saveLocally = saveLocally;
    }

    public void setSaveRemote(boolean saveRemote) {
        this.saveRemote = saveRemote;
    }



    public String getUsername() {
        return username;
    }
    public boolean isSocketCreated(){
        return servSocket != null;
    }

    public List<String> getAllFilesFromCloud() throws IOException {
        List<String> allFiles = new ArrayList<>();
        int nrOfFiles = servInputStream.readInt();
        for (int i = 0; i < nrOfFiles; i++) {
            allFiles.add(servInputStream.readUTF());
        }
        return allFiles;
    }
    public void setUsername(String username) throws IOException {
        this.username = username;
        this.localPath = System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator + username;
        //fileOperations = new FileOperations(username);
        System.out.println("sain username");
    }

    public void createConnection() throws IOException {
        this.servSocket = new Socket("localhost", 1337);
        this.servOutputStream = new DataOutputStream(servSocket.getOutputStream());
        this.servInputStream = new DataInputStream(servSocket.getInputStream());

    }

    public void directoryCheck() throws IOException {
        this.settingsPath  = System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator + username + File.separator +  "settings.txt";
        if (!Files.exists(Paths.get(localPath))){
            Files.createDirectories(Paths.get(localPath));
            createSettings();
        }
        else {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(settingsPath))) {
                this.downloadPath = bufferedReader.readLine();
                this.localPath = bufferedReader.readLine();
            }
        }
    }


    public void sendCommand(String command) throws IOException {
        servOutputStream.writeUTF(command);
    }

    public void startRecording(String filename) throws IOException {
        System.out.println("Remote: "+saveRemote);
        System.out.println("Local "+saveLocally);
        if (saveRemote) {
            servOutputStream.writeUTF("filename");
            servOutputStream.writeUTF(filename);
            FileOperations.sendFormat(servOutputStream,audioFormat);
            servOutputStream.writeBoolean(false);
        }
        recordingInfo.add("start");
        audioCaptureThread = new AudioCaptureThread(new ByteArrayOutputStream(), servOutputStream, recordingInfo, false);
        audioCaptureThread.setSaveLocally(saveLocally);
        audioCaptureThread.setSaveRemote(saveRemote);
        this.captureThread = new Thread(audioCaptureThread);
        captureThread.start();
        System.out.println("hakkas lindistama");
    }
    public void startBufferedRecording(int minutes,String filename) throws IOException {
        if (saveRemote) {
            servOutputStream.writeUTF("filename");
            servOutputStream.writeUTF(filename);
            FileOperations.sendFormat(servOutputStream,audioFormat);
            servOutputStream.writeBoolean(true);
            servOutputStream.writeInt(minutes);
        }
        ArrayBlockingQueue<String> bufferedCommands = new ArrayBlockingQueue<>(5);
        recordingInfo.add("start");
        audioCaptureThread = new AudioCaptureThread(true,recordingInfo,
                ByteBuffer.allocate((int) (minutes*60*audioFormat.getSampleRate())*audioFormat.getSampleSizeInBits()/8),
                servOutputStream
        );

        audioCaptureThread.setSaveLocally(saveLocally);
        audioCaptureThread.setSaveRemote(saveRemote);
        audioCaptureThread.setCommandsToClient(bufferedCommands);
        this.captureThread = new Thread(audioCaptureThread);
        captureThread.start();
    }
    public void saveBuffer() throws IOException {
        recordingInfo.add("buffer");
        if (saveLocally) {
            FileOperations.fileSaving(filename, audioCaptureThread.getRecordedBytes(), username, audioFormat, true, getLocalPath());
        }
    }

    public void pauseRecording() {
        recordingInfo.add("pause");
        System.out.println("recording paused");
    }

    public void resumeRecording() {
        recordingInfo.add("resume");
        System.out.println("resumed");
    }

    public void stopRecording() throws IOException {
        recordingInfo.add("stop");
        System.out.println("stopped");
        try {
            captureThread.join();
            FileOperations.fileSaving(filename,audioCaptureThread.getRecordedBytes(),username,audioFormat,true, localPath);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("finished recording");
    }
    public boolean renameFile(String oldName,String newName) throws IOException {
        servOutputStream.writeUTF("Rename");
        servOutputStream.writeUTF(oldName);
        servOutputStream.writeUTF(newName);
        return servInputStream.readBoolean();
    }
    public boolean deleteFile(String filename) throws IOException {
        servOutputStream.writeUTF("Delete");
        servOutputStream.writeUTF(filename);
        return servInputStream.readBoolean();
    }

    public boolean downloadFile(String filePath, String fileName) throws IOException {
        servOutputStream.writeUTF("Download");
        servOutputStream.writeUTF(filePath);
        String clientPath = downloadPath + File.separator + fileName;
        return receiveFile(clientPath, servInputStream);
    }

    public double getFileSizes() throws IOException {
        servOutputStream.writeUTF("filesizes");
        System.out.println("kas jõuan siia");
        return servInputStream.readDouble();
    }

    public String[] getFileData(String filePath) throws IOException {
        servOutputStream.writeUTF("Data");
        servOutputStream.writeUTF(filePath);
        String[] data = new String[2];
        data[0] = servInputStream.readUTF();
        data[1] = servInputStream.readUTF();
        return data;
    }

    public boolean passwordChange(String password) throws IOException {
        servOutputStream.writeUTF("pwChange");
        servOutputStream.writeUTF(password);
        return (servInputStream.readBoolean());
    }
    public boolean sendUsername(String username, String password) throws IOException {
        servOutputStream.writeUTF("username"); // Indicate incoming user info
        servOutputStream.writeUTF(username);
        servOutputStream.writeUTF(password);
        return servInputStream.readBoolean();
    }

    private static boolean receiveFile(String fileName, DataInputStream dataInputStream) {
        try {
            long fileSize = dataInputStream.readLong();
            byte[] buffer = new byte[1024];
            long totalBytesRead = 0;
            try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
                while (totalBytesRead < fileSize) {
                    int bytesRead = dataInputStream.read(buffer, 0, buffer.length);
                    totalBytesRead += bytesRead;
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
           return false;
        }
        return true;
    }
    private void createSettings() throws IOException {

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(settingsPath))) {
            bufferedWriter.write(downloadPath);
            bufferedWriter.newLine();
            bufferedWriter.write(localPath);
        }
    }
    public void updateSettings(String localPath, String downloadPath) throws IOException {
        if (localPath.equals(this.localPath) && downloadPath.equals(this.downloadPath)) {
            return;
        }
        this.localPath = localPath;
        this.downloadPath = downloadPath;
        createSettings();
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getDownloadPath() {
        return downloadPath;
    }
}



