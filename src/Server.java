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
            ExecutorService executor = null;
            try {
                executor = Executors.newFixedThreadPool(4); //Needs testing
                while (true) {
                    Socket socket = serverSocket.accept();
                    executor.execute(new Thread(new ServerThread(socket)))
                    ;
                }
            } finally {
                executor.shutdown();
            }
        }
    }
}

