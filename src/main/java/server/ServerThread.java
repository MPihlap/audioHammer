package server;

import client.FileOperations;

import javax.sound.sampled.AudioFormat;
import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


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
    private void handleMyCloud(DataInputStream dataInputStream, DataOutputStream clientOutputStream) throws IOException {
        writeAllFilesToClient(clientOutputStream, fileOperations);
        String command;
        while (!(command = dataInputStream.readUTF()).equals("back")){ //MyCloud loop
            if (command.equals("Listen")){
                //TODO: Implement
            }
            else if (command.equals("Delete")){
                String filename = dataInputStream.readUTF();
                fileOperations.deleteFile(filename);
            }
            else if (command.equals("Download")){
                System.out.println("Läksin DL (servThread)");
                String filename = dataInputStream.readUTF(); //receives filepath
                System.out.println(FileOperations.sendFile(filename, clientOutputStream));
            }
            else if (command.equals("Rename")){
                String oldFileName = dataInputStream.readUTF();
                String newFileName = dataInputStream.readUTF();
                clientOutputStream.writeBoolean(fileOperations.renameFile(oldFileName,newFileName));
                writeAllFilesToClient(clientOutputStream, fileOperations);
            }
            else if(command.equals("Data")) {
                String filePath = dataInputStream.readUTF();
                String[] data = fileOperations.getFileData(filePath);
                clientOutputStream.writeUTF(data[0]);
                clientOutputStream.writeUTF(data[1]);
            }
        }
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
                    if(command.equals("pwChange")) {
                        String password = dataInputStream.readUTF();
                        clientOutputStream.writeBoolean(LoginHandler.changePassword(username, password));
                    }
                    if(command.equals("filesizes")) {
                        double fileSizes = fileOperations.getFileSizes();
                        clientOutputStream.writeDouble(fileSizes);
                    }
                    if (command.equals("logout")) {
                        break;
                    }
                    if (command.equals("MyCloud")){
                        handleMyCloud(dataInputStream,clientOutputStream);
                    }
                    if (command.equals("Recording")) { //if filename is entered, start recording
                        while (true) {
                            command = dataInputStream.readUTF();
                            if (command.equals("MyCloud")){
                                handleMyCloud(dataInputStream,clientOutputStream);
                            }
                            if (command.equals("back")){
                                break;
                            }
                            if (command.equals("filename")) {
                                AudioFormat audioFormat;
                                String fileName = dataInputStream.readUTF();
                                System.out.println(fileName);
                                audioFormat = readFormat(dataInputStream);
                                boolean bufferedMode = dataInputStream.readBoolean(); //Buffered recording or regular recording
                                System.out.println("Buffer: " + bufferedMode);
                                byte[] fileBytes;
                                boolean isRecording;
                                if (bufferedMode) {
                                    int minutes = dataInputStream.readInt();    //length of recorded buffer
                                    System.out.println("Minutes:" + minutes);
                                    while (true) {
                                        fileBytes = bufferAudioBytesFromClient(dataInputStream, minutes * 60 * 88200);
                                        isRecording = dataInputStream.readBoolean();
                                        if (!isRecording) {
                                            break;
                                        }
                                        fileSaving(fileName, fileBytes, username,audioFormat);
                                    }
                                } else {
                                    fileBytes = readAudioBytesFromClient(dataInputStream);
                                    fileSaving(fileName, fileBytes, username,audioFormat);
                                }

                            }

                        }
                    }
                }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private AudioFormat readFormat(DataInputStream dataInputStream) throws IOException {
        float sampleRate = dataInputStream.readFloat();
        int sampleSize = dataInputStream.readInt();
        int channels = dataInputStream.readInt();
        boolean signed = dataInputStream.readBoolean();
        boolean bigEndian = dataInputStream.readBoolean();
        return new AudioFormat(sampleRate, sampleSize, channels, signed, bigEndian);
    }
    private void writeAllFilesToClient(DataOutputStream clientOutputStream, FileOperations fileOperations) throws IOException {
        ArrayList<Path> allFiles = fileOperations.getAllFiles();
        clientOutputStream.writeInt(allFiles.size());
        for (Path path : allFiles) {
            clientOutputStream.writeUTF(path.toString());
        }
    }

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
     * Uses a ByteBuffer to store a certain number of bytes. If buffer is full, bytes from start are disgarded and the
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
        return audioByteBuffer.array();
    }

    //Saves file
    private void fileSaving(String filename, byte[] fileBytes, String username, AudioFormat audioFormat) throws IOException {
        String serverFilename = filename + ".wav";
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String directory = dateTimeFormatter.format(localDate);
        String pathString = System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator + username + File.separator + directory + File.separator + serverFilename;
        System.out.println(pathString);
        pathString = fileCheck(pathString);
        Path path = Paths.get(pathString);

        Files.createDirectories(path.getParent());
        File newFile = new File(pathString);
        FileOperations.createWAV(fileBytes, newFile, audioFormat);

        System.out.println("File " + filename + " is saved as " + path.getFileName());
    }

    //Checks if file name is unique in this folder. Adds "(Copyxx)" if needed
    private String fileCheck(String pathString) {
        String pathStringFixed = pathString;
        FilenameFilter filter = (dir, name) -> name.endsWith(".wav");
        Path path = Paths.get(pathString);
        File folder = new File(path.getParent().toString());
        System.out.println(folder);

        File[] filesInFolder = folder.listFiles(filter);
        if (filesInFolder != null) {
            for (File file : filesInFolder) {
                if (file.getName().equals(path.getFileName().toString())) {
                    if (pathString.charAt(pathString.length() - 5) == (')')) {
                        int number2 = Character.getNumericValue(pathString.charAt(pathString.length() - 6)) + 1;
                        int number1 = Character.getNumericValue(pathString.charAt(pathString.length() - 7));
                        if (number1 == 9 && number2 == 9) {
                            System.out.println("Selle faili nimega copisied on juba 100 tükki.Esimese faili ümbersalvestamine.");
                            return (pathString.substring(0, (pathString.length() - 8)) + pathString.substring(pathString.length() - 4, pathString.length()));
                        }
                        if (number2 == 10) {
                            number1 = Character.getNumericValue(pathString.charAt(pathString.length() - 7)) + 1;
                            number2 = 0;
                        }

                        pathStringFixed = pathString.substring(0, (pathString.length() - 8)) + "(" + number1 + number2 + ")" + pathString.substring(pathString.length() - 4, pathString.length());
                    } else {
                        pathStringFixed = pathString.substring(0, (pathString.length() - 4)) + "(01)" + pathString.substring(pathString.length() - 4, pathString.length());
                    }
                    pathStringFixed = fileCheck(pathStringFixed);
                }
            }
        }

        return pathStringFixed;


    }




}
