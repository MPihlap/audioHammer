package server;

import javax.crypto.SecretKeyFactory;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Scanner;


/**
 * Created by Meelis on 17/04/2017.
 */
public class LoginHandler {
    public static String signUp(DataInputStream clientInputStream, DataOutputStream clientOutputStream) throws IOException {
        while (true) {
            String username = clientInputStream.readUTF();
            String password = clientInputStream.readUTF();
            boolean accountCreated = newUserAccount(username, password);
            clientOutputStream.writeBoolean(accountCreated);
            if (accountCreated){
                return username;
            }
        }
    }

    public static String getLoginUsername(DataInputStream clientInputStream, DataOutputStream clientOutputStream) throws IOException {
        while (true) {
            String username = clientInputStream.readUTF();
            String password = clientInputStream.readUTF();
            if (login(username, password)) {
                clientOutputStream.writeBoolean(true);
                return username;
            }
            else {
                clientOutputStream.writeBoolean(false);
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
                    if (PasswordEncryption.passwordCheck(password, storedPassword, salt)) {
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

        try (FileWriter fileWriter = new FileWriter(file, true)
        ) {
            isTaken = doesUsernameExist(username, file);
            if (!isTaken) {
                fileWriter.write(username + ":" + PasswordEncryption.passwordEncrypter(password) + '\n');
                System.out.println("kirjutasin?");
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
