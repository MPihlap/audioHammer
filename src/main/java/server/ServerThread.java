package server;

import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;


/**
 * Created by Helen on 30.03.2017.
 */
public class ServerThread implements Runnable {
    private Socket socket;
    private String username;
    private String fileName;


    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (InputStream inputStream = socket.getInputStream();
             DataInputStream dataInputStream = new DataInputStream(inputStream)) {
            while (true) {
                String command = dataInputStream.readUTF();
                if (command.equals("username")) {
                    username = dataInputStream.readUTF();
                }
                if (command.equals("filename")) {
                    fileName = dataInputStream.readUTF();
                    System.out.println(fileName);
                    boolean bufferedMode = dataInputStream.readBoolean();
                    System.out.println("Buffer: " + bufferedMode);
                    byte[] fileBytes;
                    if (bufferedMode) {
                        int minutes = dataInputStream.readInt();
                        System.out.println("Minutes:" + minutes);
                        while (true) {
                            int type = dataInputStream.readInt();
                            if (type == 2) {
                                break;
                            }
                            if (type == 0){
                                throw new RuntimeException("Socket transmission type error, got: "+type+", expected: 2 or 1");
                            }
                            fileBytes = bufferAudioBytesFromClient(dataInputStream, minutes * 60 * 88200);
                            fileSaving(fileName, fileBytes, username);
                        }
                    } else {
                        fileBytes = readAudioBytesFromClient(dataInputStream);
                        fileSaving(fileName, fileBytes, username);
                    }

                }

                if (false) {
                    socket.close();
                    break;
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

    private boolean isFinished(ByteBuffer byteBuffer, int nBytes) {
        byte[] endBytes = new byte[nBytes];
        int startPosition = byteBuffer.position();
        System.out.println(byteBuffer.position() - nBytes - 1);
        byteBuffer.position(startPosition - nBytes);
        byteBuffer.get(endBytes, 0, nBytes);
        System.out.print("Endbytes:");
        for (int i = 0; i < nBytes; i++) {
            System.out.print(endBytes[i]);
            if (endBytes[i] != 1)
                return false;
        }
        byteBuffer.position(startPosition);
        return true;
    }

    private boolean isFinished(byte[] bytes, int nBytes) {
        if (bytes.length < 8)
            return false;
        byte[] endBytes = new byte[nBytes];
        for (int i = 0; i < 8; i++) {
            System.out.println(bytes.length);
            System.out.println(bytes.length - 1 - i);
            endBytes[i] = bytes[bytes.length - 1 - i];
        }
        System.out.print("Endbytes: ");
        for (int i = 0; i < nBytes; i++) {
            System.out.print(endBytes[i]);
            if (endBytes[i] != 1)
                return false;
        }
        return true;
    }

    //Reads sent audio as bytearray
    private byte[] readAudioBytesFromClient(DataInputStream dataInputStream) throws IOException {
        int type = dataInputStream.readInt();
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

        byte[] audioBytes = byteArrayOut.toByteArray();
        return byteArrayOut.toByteArray();
    }

    private byte[] bufferAudioBytesFromClient(DataInputStream clientInputStream, int byteNumber) throws IOException {
        int type = clientInputStream.readInt();
        byte[] buffer;
        int bufferSize;
        if (type == 1) {
            bufferSize = clientInputStream.readInt();
            buffer = new byte[bufferSize];
        } else {
            throw new RuntimeException("Socket Transmission type error, got: " + type+", expected: 1");
        }
        ByteBuffer audioByteBuffer = ByteBuffer.allocate(byteNumber);
        int len;
        while (true) {
            type = clientInputStream.readInt();
            if (type == 2) {
                break;
            }
            len = clientInputStream.readInt();
            clientInputStream.readFully(buffer,0,len);
            if (audioByteBuffer.position() + len > byteNumber) { //Check if size limit has been reached
                audioByteBuffer.position(bufferSize);               //Go to position after first buffer
                audioByteBuffer.compact();                          //Remove bytes before position
            }
            audioByteBuffer.put(buffer, 0, len);
        }
        return audioByteBuffer.array();
    }

    //Saves file
    private void fileSaving(String filename, byte[] fileBytes, String username) throws IOException {
        String serverFilename = "ServerFile_" + filename + ".wav";
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String directory = dateTimeFormatter.format(localDate);
        String pathString = System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator + username + File.separator + directory + File.separator + serverFilename;
        System.out.println(pathString);
        pathString = fileCheck(pathString);
        Path path = Paths.get(pathString);

        Files.createDirectories(path.getParent());
        File newFile = new File(pathString);


        ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, true);
        AudioInputStream ais = new AudioInputStream(bais, audioFormat, fileBytes.length / audioFormat.getFrameSize());

        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, newFile);


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
