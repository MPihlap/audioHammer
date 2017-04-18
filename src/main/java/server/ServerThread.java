package main.java.server;

import com.sun.deploy.util.ArrayUtil;

import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by Helen on 30.03.2017.
 */
public class ServerThread implements Runnable {
    private Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        byte[] fileBytes;
        try (InputStream inputStream = socket.getInputStream();
             DataInputStream dataInputStream = new DataInputStream(inputStream)) {
            try {
                String fileName = dataInputStream.readUTF();
                fileBytes = readAudioBytesFromClient(dataInputStream);
                fileSaving(fileName, fileBytes);
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
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

    //Reads sent audio as bytearray
    private byte[] readAudioBytesFromClient(DataInputStream dataInputStream) throws IOException {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
        int len;
        int dataLen = 0;
        File file = new File("recorded\\test.wav");
        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(new byte[]{}), new AudioFormat(44100, 16, 1, true, true), 0);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
        ais.close();
        FileOutputStream fileOutputStream = new FileOutputStream(file, true);
        while ((len = dataInputStream.read(buffer, 0, buffer.length)) > 0) {
            byteArrayOut.write(buffer, 0, len);
            fileOutputStream.write(buffer, 0, len);
            dataLen += len;
        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        byte[] dataLenArray = ByteBuffer.allocate(4).putInt(dataLen).array();
        int fileLength = Math.toIntExact(file.length());
        byte[] fileLenArray = ByteBuffer.allocate(4).putInt(0,fileLength).array();
        System.out.print("File absolute len: ");
        System.out.println(file.length());
        System.out.print("File data length: ");
        System.out.println(dataLen);
        System.out.println("dataLenArray:");
        for (byte b : dataLenArray
                ) {
            System.out.println(b);
        }
        byte[] bytes = new byte[4];
        randomAccessFile.seek(40);
        randomAccessFile.read(bytes, 0, 4);
        System.out.println("before change");
        for (byte b : bytes
                ) {
            System.out.println(b);
        }
        randomAccessFile.seek(40);
        for (int i = 0; i < 2; i++) {
            byte temp = fileLenArray[i];
            fileLenArray[i] = fileLenArray[fileLenArray.length - i - 1];
            fileLenArray[fileLenArray.length - i - 1] = temp;
        }
        for (int i = 0; i < 2; i++) {
            byte temp = dataLenArray[i];
            dataLenArray[i] = dataLenArray[dataLenArray.length - i - 1];
            dataLenArray[dataLenArray.length - i - 1] = temp;
        }
        randomAccessFile.seek(40);
        randomAccessFile.write(dataLenArray, 0, 4);
        randomAccessFile.seek(4);
        randomAccessFile.write(fileLenArray, 0, 4);
        randomAccessFile.seek(40);
        randomAccessFile.read(bytes, 0, 4);
        randomAccessFile.seek(4);
        randomAccessFile.read(bytes, 0, 4);
        System.out.println("fileLenArray: ");
        for (byte b : fileLenArray) {
            System.out.println(b);
        }
        for (byte b : bytes
                ) {
            System.out.println(b);
        }
        randomAccessFile.close();
        fileOutputStream.close();
        System.out.println("byteArrayOut.size:"+byteArrayOut.size());
        return byteArrayOut.toByteArray();

    }

    //Saves file
    private void fileSaving(String filename, byte[] fileBytes) throws IOException {
        String serverFilename = "ServerFile_" + filename + ".wav";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.now();
        String directory = dateTimeFormatter.format(localDate);
        String pathString = System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator + directory + File.separator + serverFilename;
        System.out.println(pathString);
        pathString = fileCheck(pathString);
        Path path = Paths.get(pathString);

        Files.createDirectories(path.getParent());
        File newFile = new File(pathString);

        //enne oli probleem selles, et salvestati küll .wav laiendiga, kuid see ei tähenda, et ta reaalselt WAVE formaadis oleks
        //alljärgnev kood lahendab selle

        ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);
        double[] d = new double[fileBytes.length / 2];
        for (int i = 0; i < fileBytes.length / 2; i++) {
            d[i] = ((short) (((fileBytes[2 * i + 1] & 0xFF) << 8) + (fileBytes[2 * i] & 0xFF))) / ((double) Short.MAX_VALUE);
        }
        AudioInputStream ais = new AudioInputStream(bais, new AudioFormat(44100, 16, 1, true, true), d.length);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, newFile);


        System.out.println("File " + filename + " is saved as " + path.getFileName());
    }

    //Checks if file name is unique in this fodler. Adds "(Copyxx)" if needed
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
