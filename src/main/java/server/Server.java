package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Helen on 12-Mar-17.
 */


public class Server {

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(1337)) {
            //noinspection InfiniteLoopStatement
            System.out.println("ok");
            ExecutorService executor = null;
            try {
                executor = Executors.newFixedThreadPool(1);
                ServerSend serverSend=new ServerSend();
                serverSend.run();
                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Sees");
                    executor.execute(new Thread(new ServerThread(socket)));
                }
            } finally {
                executor.shutdown();
            }
        }
    }
}

