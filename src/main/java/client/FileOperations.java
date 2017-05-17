package client;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Alo on 18-Apr-17.
 */
public class FileOperations {
    private ArrayList<Path> allFilesWithPath = new ArrayList<>();
    private final String path;
    private Double fileSizes = 0.0;


    /**
     *
     * @param username name of client
     * @throws IOException when an error occurs when trying to locate path
     */
    public FileOperations(String username) throws IOException {
        this.path = System.getProperty("user.home") + File.separator + "AudioHammerServer" + File.separator + username;
        if (!Files.exists(Paths.get(path))){
            Files.createDirectories(Paths.get(path));
            System.out.println("Tried to make directories");
        }
    }

    //adds user recorded files to list
    private void listFiles() throws IOException {
        allFilesWithPath = new ArrayList<>();
        SimpleFileVisitor<Path> simpleFileVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (path.toString().endsWith(".wav")) {
                    allFilesWithPath.add(path);
                }
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(Paths.get(path), simpleFileVisitor);
    }



    //Saves file
    public static void fileSaving(String filename, byte[] fileBytes, String username, AudioFormat audioFormat,boolean saveLocal, String localPath) throws IOException {
        String serverFilename = filename + ".wav";
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String directory = dateTimeFormatter.format(localDate);
        String pathString;
        if (saveLocal) {
            pathString = localPath.toString() + File.separator + serverFilename;
        }
        else {
            pathString = System.getProperty("user.home") + File.separator + "AudioHammerServer" + File.separator + username + File.separator + directory + File.separator + serverFilename;
            pathString = fileCheck(pathString);
            Path path = Paths.get(pathString);
            Files.createDirectories(path.getParent());
        }
        File newFile = new File(pathString);
        FileOperations.createWAV(fileBytes, newFile, audioFormat);
        System.out.println("File " + filename + " is saved as " +newFile.getName());


    }

    /**
     *
     * @return total sum of recorded files in myCloud
     * @throws IOException
     */
    public double getFileSizes() throws IOException {
        fileSizes = 0.0;
        SimpleFileVisitor<Path> simpleFileVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (path.toString().endsWith(".wav")) {
                    fileSizes+=attrs.size()/(1048576.0); //bytes to megabytes
                }
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(Paths.get(path), simpleFileVisitor);
        return fileSizes;
    }

    //renames file
    public boolean renameFile(String oldFilename, String newFilename) {
        for (Path file :
                allFilesWithPath) {
            if (file.getFileName().toString().equals(newFilename + ".wav")) {
                System.out.println(file.getFileName());
                return false;
            }
        }
        try {
            Files.move(new File(oldFilename).toPath(), new File(oldFilename).toPath().resolveSibling(newFilename + ".wav"));
            return true;
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
    }

    //deletes file
    public void deleteFile(String fileName) throws IOException {
        Files.delete(Paths.get(fileName));
    }

    public static boolean sendFile(String filename, DataOutputStream dataOutputStream) throws IOException {
        System.out.println("in sendFile");
        File file = new File(filename);
        long size = file.length();
        dataOutputStream.writeLong(size);
        int bytesRead;
        byte[] buffer = new byte[1024];
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            while ((bytesRead = fileInputStream.read(buffer, 0, buffer.length)) > 0) {
                dataOutputStream.write(buffer, 0, bytesRead);
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    /**
     *
     * @param fileBytes Audio data
     * @param file file where audio is saved
     * @param audioFormat predetermined audio format for WAV files
     * @throws IOException
     */
    public static void createWAV(byte[] fileBytes, File file, AudioFormat audioFormat) throws IOException {
        AudioInputStream ais;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes)) {
            ais = new AudioInputStream(bais, audioFormat, fileBytes.length / audioFormat.getFrameSize());
        }

        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
    }

    //for MyCloudStage
    public ArrayList<Path> getAllFiles() throws IOException {
        listFiles();
        return allFilesWithPath;
    }

    /**
     *
     * @param filePath path of desired file
     * @return Array of two strings, first being the date when the file was last modified; second string is the size of the file
     * @throws IOException
     */
    public String[] getFileData(String filePath) throws IOException {
        File file = new File(filePath);
        Date mod = new Date(file.lastModified());
        String sizeString = String.valueOf((file.length()/1048576.0)).substring(0, 5);

        return new String[]{mod.toString(), sizeString};
    }
    //Checks if file name is unique in this folder. Adds "(Copyxx)" if needed
    private static String fileCheck(String pathString) {
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
