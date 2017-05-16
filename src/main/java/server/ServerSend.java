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
    public void run() {/*
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
                for (Path path:files){
                    System.out.println("Saadan selle: " +
                            ""+path);
                    File file=path.toFile();
                    long length=file.length();
                    DataInputStream inputStream=new DataInputStream(new FileInputStream(file)
                    /*dataOutputStream

                    }

                } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }*/
}}