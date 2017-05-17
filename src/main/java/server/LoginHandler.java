package server;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;


/**
 * Created by Meelis on 17/04/2017.
 */
public class LoginHandler {
    public static String signUp(DataInputStream clientInputStream, DataOutputStream clientOutputStream) throws IOException {
        while (true) {
            String type = clientInputStream.readUTF();
            if (type.equals("username")) {
                String username = clientInputStream.readUTF();
                String password = clientInputStream.readUTF();
                boolean accountCreated = newUserAccount(username, password);
                clientOutputStream.writeBoolean(accountCreated);
                if (accountCreated) {
                    return username;
                }
            }
            else {
                return null;
            }
        }
    }

    public static String getLoginUsername(DataInputStream clientInputStream, DataOutputStream clientOutputStream) throws IOException {
        while (true) {
            String type = clientInputStream.readUTF();
            if (type.equals("username")) {
                String username = clientInputStream.readUTF();
                String password = clientInputStream.readUTF();
                if (login(username, password)) {
                    clientOutputStream.writeBoolean(true);
                    return username;
                } else {
                    clientOutputStream.writeBoolean(false);
                }
            }
            else {
                return null;
            }
        }
    }

    /**
     *
     * @param username Username of client
     * @param file File where user data is stored
     * @return True, if username is found in file; false otherwise
     * @throws IOException
     */
    private static boolean doesUsernameExist(String username, File file) throws IOException {
        try (Scanner fileScanner = new Scanner(file, "UTF-8")) {
            while (fileScanner.hasNextLine()) {
                if (fileScanner.nextLine().split(":")[0].equals(username)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     *
     * @param username Username of client
     * @param password password of client (not encrypted)
     * @param file File where user data is stored
     * @return true, if inserted password matches encrypted password in user data file; false otherwise
     * @throws IOException
     */
    private static boolean checkPassword(String username, String password, File file) throws IOException {
        try (Scanner fileScanner = new Scanner(file, "UTF-8")) {
            while (fileScanner.hasNextLine()) {
                String[] userData = fileScanner.nextLine().split(":");
                if (userData[0].equals(username)) {
                    String storedPassword = userData[1];
                    String salt = userData[2];
                    if (PasswordHashing.passwordCheck(password, storedPassword, salt)) {
                        return true;
                    }
                    break;
                }
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
    public static boolean changePassword(String username, String password) throws IOException {
        String tempFile = "src/resources/UserInfo.temp";
        String userpath = "src/resources/UserInfo.txt";

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(userpath));
             BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile))) {
                String userLine;
                while ((userLine = bufferedReader.readLine()) != null) {
                    String[] userData = userLine.split(":");
                    if (!userData[0].equals(username)) {
                        bufferedWriter.write(userLine);
                        bufferedWriter.newLine();
                    }
                }
                bufferedWriter.write(username + ":" +  PasswordHashing.passwordHasher(password));
                bufferedWriter.newLine();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return false;
        }
        File oldFile = new File(userpath);
        oldFile.delete();
        new File(tempFile).renameTo(oldFile);
        return true;

    }
    /**
     *
     * @param username Username of client
     * @param password password of client (not encrypted)
     * @return True, if username and password matches data in user data file; false otherwise
     * @throws IOException
     */
    public static boolean login(String username, String password) throws IOException {
        String userpath = "src/resources/UserInfo.txt"; //all usernames/passwords are stored here
        File file = new File(userpath);
        if (doesUsernameExist(username, file)) {
            if (checkPassword(username, password, file)) {
                return true;
            } else {
                System.out.println("Wrong password!");
                return false;
            }
        } else {
            System.out.println("Username not found!");
            return false;
        }


    }


    /**
     *
     * @param username Username of client
     * @param password password of Client(not encrypted)
     * @return True, if new account is created; false, if username already exists
     * @throws IOException
     */
    public static boolean newUserAccount(String username, String password) throws IOException {
        boolean isTaken;
        String userpath = "src/resources/UserInfo.txt";
        File file = new File(userpath);

        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file, true))
        ) {
            isTaken = doesUsernameExist(username, file);
            if (!isTaken) {

                fileWriter.write(username + ":" + PasswordHashing.passwordHasher(password));
                fileWriter.newLine();
            }
            else {
                return false;
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

}
