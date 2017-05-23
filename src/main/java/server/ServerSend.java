package server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * Created by Helen on 16.05.2017.
 */
public class ServerSend implements Runnable {
    String ip = "local_host";
    boolean single = true;
    Scanner scanner;


    public ServerSend(String ip, boolean single, Scanner scanner) {
        this.ip = ip;
        this.single = single;
        this.scanner = scanner;
    }

    @Override
    public void run() {
        int counter=0;
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println(e);
            }
            Queue<Path> files = new LinkedList<>();

            String pathString = System.getProperty("user.home") + File.separator + "AudioHammer" + File.separator + "Server";
            try (Stream<Path> paths = Files.walk(Paths.get(pathString))) {
                paths.forEach(filePath -> {
                    if (Files.isRegularFile(filePath)) {
                        files.add(filePath);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!files.isEmpty()) {
                if (single && counter!=1) {
                    counter=0;
                    while (true) {
                        counter+=1;
                        System.out.println("Finalserver ip:");
                        ip = scanner.next();
                        System.out.println("Single (1) use or Multiple use (2)?");
                        int answer = 0;
                        try {
                            answer = scanner.nextInt();
                        } catch (InputMismatchException e) {
                        }
                        if (answer == 1) {
                            single = true;
                            break;
                        } else if (answer == 2) {
                            single = false;
                            break;
                        } else {
                            System.out.println("Try again!");
                        }
                    }
                }

                try (Socket socket = new Socket(ip, 1338);
                     DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream())) {
                    counter+=1;
                    for (Path path : files) {
                        dataOutputStream.writeBoolean(true);
                        System.out.println("Sending file: " +
                                "" + path+" to final server.");
                        dataOutputStream.writeUTF(path.getFileName().toString());
                        sendFile(path.toString(), dataOutputStream);
                        File file = new File(path.toString());
                        boolean delete = file.delete();
                        if (!delete) {
                            throw new RuntimeException("Could not delete file from server");
                        }
                    }
                    dataOutputStream.writeBoolean(false);

                } catch (IOException e) {
                    System.err.println("Waiting for connection");
                }

            }
        }
    }

    public static boolean sendFile(String filename, DataOutputStream dataOutputStream) throws IOException {
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