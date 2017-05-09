package client;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Meelis on 09/05/2017.
 */
public class CommandLineClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        BlockingQueue<String> arrayBlockingQueue = new ArrayBlockingQueue<>(5);
        Client client = new Client();
        ListenGPIO listenGPIO = new ListenGPIO(client, arrayBlockingQueue);
        new Thread(listenGPIO).start();
        client.createConnection();
        try (Scanner sc = new Scanner(System.in)) {
            String username = loginClient(client, sc);
            while (true) {
                System.out.println("Enter filename: ");
                String filename = sc.nextLine();
                client.sendCommand("filename");
                client.sendCommand(filename);
                System.out.println("Would you like to do (B)uffered or (R)egular recording? ");
                while (true) {
                    String response = sc.nextLine();
                    if (response.equals("B")) {
                        int minutes;
                        while (true) {
                            System.out.println("How long would you like the buffer to be? (min)");
                            try {
                                minutes = Integer.parseInt(sc.nextLine());
                                break;
                            } catch (NumberFormatException e) {
                                System.out.println("Please enter an integer");
                            }
                        }
                        while (true) {
                            System.out.println("Please press the start button to start recording.");
                            String take = arrayBlockingQueue.take();
                            if (take.equals("start"))
                                break;
                        }
                        client.startBufferedRecording(minutes);
                        System.out.println("Please press the stop button to stop recording");
                        while (true) {
                            if (arrayBlockingQueue.take().equals("stop")) {
                                client.stopRecording();
                                break;
                            }
                        }
                        break;
                    } else if (response.equals("R")) {
                        while (true) {
                            String take = arrayBlockingQueue.take();
                            if (take.equals("start"))
                                break;
                        }
                        client.startRecording();
                        while (true) {
                            System.out.println("Press the stop button to stop recording");
                            String input = arrayBlockingQueue.take();
                            if (input.equals("pause")) {
                                client.pauseRecording();
                            }
                            if (input.equals("stop")) {
                                client.stopRecording();
                                break;
                            }
                        }
                        break;
                    } else {
                        System.out.println("Wrong input.");
                    }
                }
            }
        } finally {
            client.closeConnection();
        }
    }

    private static String loginClient(Client client, Scanner sc) throws IOException {
        String username;
        while (true) {
            System.out.println("Would you like to (L)ogin or (S)ign up?");
            String response = sc.nextLine();
            if (response.equals("L") || response.equals("S")) {
                if (response.equals("L")) {
                    username = getUsername(client, sc);
                    if (username != null)
                        break;
                }
                if (response.equals("S")) {
                    username = signUpNewUser(client, sc);
                    if (username != null)
                        break;
                }
            } else {
                System.out.println("Please enter either 'S' or 'L'");
            }
        }
        return username;
    }

    private static String getUsername(Client client, Scanner sc) throws IOException {
        String username;
        String password;
        while (true) {
            System.out.println("To cancel the login, enter 'c': ");
            if (sc.nextLine().equals("c")) {
                client.sendCommand("cancel");
                return null;
            }
            client.sendCommand("login");
            System.out.println("Enter username: ");
            username = sc.nextLine();
            System.out.println("Enter password:");
            password = sc.nextLine();
            boolean loginSuccessful = client.sendUsername(username, password);
            if (loginSuccessful) {
                break;
            } else {
                System.out.println("Wrong password!");
            }
        }
        return username;
    }

    private static String signUpNewUser(Client client, Scanner sc) throws IOException {
        String username;
        String password;
        while (true) {
            System.out.println("To cancel the signup process, enter 'c'");
            if (sc.nextLine().equals("c")) {
                client.sendCommand("cancel");
                return null;
            }
            client.sendCommand("signup");
            System.out.println("Enter your desired username: ");
            username = sc.nextLine();
            System.out.println("Enter your password: ");
            password = sc.nextLine();
            System.out.println("Enter your password again: ");
            if (sc.nextLine().equals(password)) {
                boolean signupSuccessful = client.sendUsername(username, password);
                if (signupSuccessful) {
                    System.out.println("Signup successful!");
                    break;
                } else System.out.println("Username already taken!");
            } else System.out.println("Passwords did not match");
        }
        return username;
    }
}
