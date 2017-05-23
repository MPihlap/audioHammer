package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Helen on 12-Mar-17.
 */


public class Server {
    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(1337);
             Scanner scanner = new Scanner(System.in)) {
            String ip;
            boolean single;
            while (true) {
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
            //noinspection InfiniteLoopStatement
            System.out.println("Server is up");
            ExecutorService executor = null;
            try {
                executor = Executors.newFixedThreadPool(2);
                while (true) {
                    executor.execute(new Thread(new ServerSend(ip,single,scanner)));
                    Socket socket = serverSocket.accept();
                    executor.execute(new Thread(new ServerThread(socket)));
                }
            } finally {
                executor.shutdown();
            }
        }
    }
}

