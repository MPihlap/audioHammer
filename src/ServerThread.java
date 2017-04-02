import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;
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
                fileBytes = fileReadingFromClient(dataInputStream);
                fileSaving(dataInputStream, fileBytes);
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    //Reads sent file as bytearray
    private byte[] fileReadingFromClient(DataInputStream dataInputStream) throws IOException {
        int length = dataInputStream.readInt();
        byte[] fileBytes = new byte[length];
        for (int i = 0; i < length; i++) {
            fileBytes[i] = dataInputStream.readByte();
        }

        return fileBytes;

    }
    //Saves file
    private void fileSaving(DataInputStream dataInputStream, byte[] fileBytes) throws IOException {
        String filename = dataInputStream.readUTF();
        String serverFilename = "ServerFile_" + filename;

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.now();
        String directory = dateTimeFormatter.format(localDate);
        String pathString = System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator + directory + File.separator + serverFilename;
        System.out.println(pathString);
        pathString=fileCheck(pathString);
        Path path = Paths.get(pathString);

        Files.createDirectories(path.getParent());
        File newFile = new File(pathString);

        //enne oli probleem selles, et salvestati küll .wav laiendiga, kuid see ei tähenda, et ta reaalselt WAVE formaadis oleks
        //alljärgnev kood lahendab selle

            ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes);
            double[] d = new double[fileBytes.length/2];
            for (int i = 0; i < fileBytes.length/2; i++) {
                d[i] = ((short) (((fileBytes[2*i+1] & 0xFF) << 8) + (fileBytes[2*i] & 0xFF))) / ((double) Short.MAX_VALUE);
            }
            AudioInputStream ais = new AudioInputStream(bais, new AudioFormat(44100, 16, 1, true, true), d.length);
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, newFile);


            /**
            fileOutputStream.write(fileBytes);
            fileOutputStream.flush();
             **/


        System.out.println("File " + filename + " is saved as " + path.getFileName());
    }
    //Checks if file name is unique in this fodler. Adds "(Copyxx)" if needed
    private String fileCheck(String pathString) {
        String pathStringFixed=pathString;
        FilenameFilter filter= (dir, name) -> name.endsWith(".wav");
        Path path = Paths.get(pathString);
        File folder=new File(path.getParent().toString());
        System.out.println(folder);
        File[] filesInFolder=folder.listFiles(filter);
        if (filesInFolder!=null) {
            for (File file:filesInFolder) {
                if (file.getName().equals(path.getFileName().toString())){
                    if (pathString.charAt(pathString.length()-5)==(')')){
                        int number2=Character.getNumericValue(pathString.charAt(pathString.length()-6))+1;
                        int number1=Character.getNumericValue(pathString.charAt(pathString.length()-7));
                        if (number1==9&&number2==9){
                            System.out.println("Selle faili nimega copisied on juba 100 tükki.Esimese faili ümbersalvestamine.");
                            return (pathString.substring(0,(pathString.length()-8))+pathString.substring(pathString.length()-4,pathString.length()));
                        }
                        if (number2==10){
                            number1=Character.getNumericValue(pathString.charAt(pathString.length()-7))+1;
                            number2=0;
                        }
                        pathStringFixed=pathString.substring(0,(pathString.length()-8))+"("+number1+number2+")"+pathString.substring(pathString.length()-4,pathString.length());
                    }else{
                        pathStringFixed=pathString.substring(0,(pathString.length()-4))+"(01)"+pathString.substring(pathString.length()-4,pathString.length());
                    }
                    pathStringFixed=fileCheck(pathStringFixed);
                }
            }
        }

        return pathStringFixed;


    }

}
