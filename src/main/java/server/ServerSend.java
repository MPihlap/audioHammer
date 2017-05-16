package server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Stream;

/**
 * Created by Helen on 16.05.2017.
 */
public class ServerSend implements Runnable {

    @Override
    public void run() {
        while (true) {
            try (Socket socket = new Socket("172.17.202.205", 1338);
                 DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream())) {

                Queue<Path> files = new LinkedList<>();

                String pathString = System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator + "Server";
                try (Stream<Path> paths = Files.walk(Paths.get(pathString))) {
                    paths.forEach(filePath -> {
                        if (Files.isRegularFile(filePath)) {
                            files.add(filePath);
                        }
                    });
                }
                for (Path path : files) {
                    System.out.println("Saadan selle: " +
                            "" + path);
                    dataOutputStream.writeUTF(path.getFileName().toString());
                    sendFile(path.toString(), dataOutputStream);
                    File file = new File(path.toString());
                    boolean delete=file.delete();
                    if (!delete){
                        throw new RuntimeException("ei kustutanud oioi");
                    }
                }
                break;
            } catch (IOException e) {
                System.err.println("Ootan ühendust");
            }
        }
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
}