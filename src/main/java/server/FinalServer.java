package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Helen on 16.05.2017.
 */
public class FinalServer {
    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(1338)) {
            System.out.println("Server is up");
            ExecutorService executor = null;
            try {
                executor = Executors.newFixedThreadPool(4);
                while (true) {
                    Socket socket = serverSocket.accept();
                    executor.execute(new Thread(new FinalServerThread(socket)));
                }
            } finally {
                if (executor != null) {
                    executor.shutdown();
                }
            }

        }
    }
}
