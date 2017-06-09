package server;

import client.FileOperations;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by Helen on 30.03.2017.
 */
public class ServerThread implements Runnable {
    private Socket socket;
    private String username;
    private FileOperations fileOperations;


    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (InputStream inputStream = socket.getInputStream();
             DataInputStream dataInputStream = new DataInputStream(inputStream);
             DataOutputStream clientOutputStream = new DataOutputStream(socket.getOutputStream())) {
            //Login screen loop
            this.username = setUsername(dataInputStream, clientOutputStream);
            fileOperations = new FileOperations(this.username);
            while (true) { // MainStage loop
                String command = dataInputStream.readUTF();
                if (command.equals("pwChange")) {
                    String password = dataInputStream.readUTF();
                    clientOutputStream.writeBoolean(LoginHandler.changePassword(username, password));
                }
                if (command.equals("filesizes")) {
                    double fileSizes = fileOperations.getFileSizes();
                    clientOutputStream.writeDouble(fileSizes);
                }
                if (command.equals("logout")) {
                    break;
                }
                if (command.equals("MyCloud")) {
                    handleMyCloud(dataInputStream, clientOutputStream);
                }
                if (command.equals("Recording")) { //if filename is entered, start recording
                    while (true) {
                        command = dataInputStream.readUTF();
                        if (command.equals("MyCloud")) {
                            handleMyCloud(dataInputStream, clientOutputStream);
                            break;
                        }
                        if (command.equals("back")) {
                            break;
                        }
                        if (command.equals("filename")) {
                            AudioFormat audioFormat;
                            String fileName = dataInputStream.readUTF();
                            System.out.println(fileName);
                            audioFormat = FileOperations.readFormat(dataInputStream);
                            boolean bufferedMode = dataInputStream.readBoolean(); //Buffered recording or regular recording
                            System.out.println("Buffer: " + bufferedMode);
                            byte[] fileBytes;
                            boolean isRecording;
                            if (bufferedMode) {
                                int minutes = dataInputStream.readInt();    //length of recorded buffer
                                System.out.println("Minutes:" + minutes);
                                while (true) {                                                      // minutes * 60 * sample rate * samplesize/8 = How many bytes to store
                                    fileBytes = bufferAudioBytesFromClient(dataInputStream, (int) (minutes * 60 * audioFormat.getSampleRate() * audioFormat.getSampleSizeInBits() / 8));
                                    isRecording = dataInputStream.readBoolean();
                                    if (!isRecording) {
                                        break;
                                    }
                                    FileOperations.fileSaving(fileName, fileBytes, username, audioFormat, false, null);
                                }
                            } else {
                                fileBytes = readAudioBytesFromClient(dataInputStream);
                                FileOperations.fileSaving(fileName, fileBytes, username, audioFormat, false, null);
                            }

                        }

                    }
                }
            }

        } catch (IOException | UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleMyCloud(DataInputStream dataInputStream, DataOutputStream clientOutputStream) throws IOException, UnsupportedAudioFileException {
        writeAllFilesToClient(clientOutputStream, fileOperations);
        String command;
        while (!(command = dataInputStream.readUTF()).equals("back")) { //MyCloud loop
            if (command.equals("Listen")) {
                streamAudioToClient(dataInputStream, clientOutputStream);
            } else if (command.equals("Delete")) {
                String filename = dataInputStream.readUTF();
                fileOperations.deleteFile(filename);
            } else if (command.equals("Download")) {
                System.out.println("Läksin DL (servThread)");
                String filename = dataInputStream.readUTF(); //receives filepath
                System.out.println(FileOperations.sendFile(filename, clientOutputStream));
            } else if (command.equals("Rename")) {
                String oldFileName = dataInputStream.readUTF();
                String newFileName = dataInputStream.readUTF();
                clientOutputStream.writeBoolean(fileOperations.renameFile(oldFileName, newFileName));
                writeAllFilesToClient(clientOutputStream, fileOperations);
            } else if (command.equals("Data")) {
                String filePath = dataInputStream.readUTF();
                String[] data = fileOperations.getFileData(filePath);
                clientOutputStream.writeUTF(data[0]);
                clientOutputStream.writeUTF(data[1]);
            }
        }
    }

    /**
     *  Gets filename from user and sends bytes using a buffer 2x the size of the sample rate
     * @param dataInputStream
     * @param clientOutputStream
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    private void streamAudioToClient(DataInputStream dataInputStream, DataOutputStream clientOutputStream) throws IOException, UnsupportedAudioFileException {
        String filename = dataInputStream.readUTF();
        String filepath = fileOperations.getFilePath(filename).toString();
        System.out.println(filepath);
        RandomAccessFile randomAccessFile = new RandomAccessFile(filepath, "r");
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filepath));
        AudioFormat format = audioInputStream.getFormat();
        FileOperations.sendFormat(clientOutputStream,format);
        int sampleRate = (int) format.getSampleRate();
        audioInputStream.close();
        int bytesRead;
        randomAccessFile.seek(45);
        System.out.println("Sample rate: " + sampleRate);
        byte[] buffer = new byte[sampleRate * 2];
        clientOutputStream.writeInt(Math.toIntExact(randomAccessFile.length() - 44));
        System.out.println("File length: "+Math.toIntExact(randomAccessFile.length() - 44));
        while ((bytesRead = randomAccessFile.read(buffer, 0, buffer.length)) != -1) {
            clientOutputStream.writeInt(bytesRead);
            clientOutputStream.write(buffer, 0, bytesRead);
        }
    }


    /**
     * Writes all files to client to display on MyCloud
     * @param clientOutputStream
     * @param fileOperations
     * @throws IOException
     */
    private void writeAllFilesToClient(DataOutputStream clientOutputStream, FileOperations fileOperations) throws IOException {
        ArrayList<Path> allFiles = fileOperations.getAllFiles();
        clientOutputStream.writeInt(allFiles.size());
        for (Path path : allFiles) {
            clientOutputStream.writeUTF(path.toString());
        }
    }

    /**
     * Reads username and password from client, checks if they match, returns username
     * @param dataInputStream
     * @param clientOutputStream
     * @return Username
     * @throws IOException
     */
    private String setUsername(DataInputStream dataInputStream, DataOutputStream clientOutputStream) throws IOException {
        String username;
        while (true) {
            String loginOrSignUp = dataInputStream.readUTF();
            if (loginOrSignUp.equals("login")) {
                username = LoginHandler.getLoginUsername(dataInputStream, clientOutputStream);
                if (username != null)
                    break;
            } else {
                username = LoginHandler.signUp(dataInputStream, clientOutputStream);
                if (username != null) {
                    break;
                }
            }
        }
        return username;
    }


    //Reads sent audio as bytearray

    private byte[] readAudioBytesFromClient(DataInputStream dataInputStream) throws IOException {
        int type = dataInputStream.readInt(); // type of data to follow
        byte[] buffer;
        if (type == 1) {
            buffer = new byte[dataInputStream.readInt()];
        } else {
            throw new RuntimeException("Socket Transmission type error: " + type);
        }
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        int len;
        while (true) {
            type = dataInputStream.readInt();
            if (type == 2) {
                break;
            }
            len = dataInputStream.readInt();
            System.out.println(len);
            dataInputStream.readFully(buffer, 0, len);
            byteArrayOut.write(buffer, 0, len);
        }

        return byteArrayOut.toByteArray();
    }

    /**
     * Uses a ByteBuffer to store a certain number of bytes. If buffer is full, bytes from start are discarded and the
     * buffer is shifted.
     *
     * @param clientInputStream inputStream to receive bytes
     * @param byteNumber        size of internal ByteBuffer in bytes
     * @return ByteBuffer as array
     */
    private byte[] bufferAudioBytesFromClient(DataInputStream clientInputStream, int byteNumber) throws IOException {
        int type = clientInputStream.readInt(); //type of data to follow
        byte[] buffer;
        int bufferSize;
        if (type == 1) {
            bufferSize = clientInputStream.readInt();
            buffer = new byte[bufferSize];
        } else {
            throw new RuntimeException("Socket Transmission type error: " + type);
        }
        ByteBuffer audioByteBuffer = ByteBuffer.allocate(byteNumber);
        int len;
        while (true) {
            type = clientInputStream.readInt();
            System.out.println("type: " + type);
            if (type == 2) {
                break;
            }
            if (type != 0) {
                throw new RuntimeException("Socket transmission type error. Expected: 0, got: " + type);
            }
            len = clientInputStream.readInt();
            clientInputStream.readFully(buffer, 0, len);
            if (audioByteBuffer.position() + len > byteNumber) { //Check if size limit has been reached
                audioByteBuffer.position(bufferSize);               //Go to position after first buffer
                audioByteBuffer.compact();                          //Remove bytes before position
            }
            audioByteBuffer.put(buffer, 0, len);
        }
        return Arrays.copyOf(audioByteBuffer.array(),audioByteBuffer.position());
    }


}
