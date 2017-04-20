package main.java.server;

import javax.crypto.SecretKeyFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Scanner;


/**
 * Created by Meelis on 17/04/2017.
 */
public class LoginHandler {

    public static boolean doesUsernameExist(String username,File file) throws IOException{
        try (Scanner fileScanner = new Scanner(file, "UTF-8")) {
            while (fileScanner.hasNextLine()){
                if (fileScanner.nextLine().split(":")[0].equals(username)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean checkPassword(String username, String password, File file) throws IOException {
        try (Scanner fileScanner = new Scanner(file, "UTF-8")) {
            while (fileScanner.hasNextLine()){
                String[] userData = fileScanner.nextLine().split(":");
                if (userData[0].equals(username)) {
                    String storedPassword =userData[1];
                    String salt = userData[2];
                    if (PasswordEncryption.passwordCheck(password, storedPassword, salt)) {
                        return true;
                    }
                    break;
                }
            }
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        return false;

    }

    public static String login(Scanner sysScanner) throws IOException {
        String userpath = "src/resources/UserInfo.txt"; //all usernames/passwords are stored here
        File file = new File(userpath);
            while (true) {
                System.out.println("Username:");
                String username = sysScanner.nextLine();
                if(doesUsernameExist(username, file)) {
                    System.out.println("Password:");
                    String password = sysScanner.nextLine();
                    if (checkPassword(username, password, file)) {
                        return username;
                    }
                    else {
                        System.out.println("Wrong password!");
                    }
                }
                else {
                    System.out.println("Username not found!");
                }

            }

    }


    public static String newUserAccount(Scanner sysScanner) throws IOException {

        String userpath = "src/resources/UserInfo.txt";
        File file = new File(userpath);
        String username;
        String password;
        try (FileWriter fileWriter = new FileWriter(file, true)
        ) {

            boolean isTaken;
            while (true) {
                System.out.println("Insert desired username:");
                username = sysScanner.nextLine();
                isTaken = doesUsernameExist(username,file);
                if (isTaken) {
                    System.out.println("Username is already in use.");
                } else {
                    break;
                }
            }
            while (true){
                System.out.println("Insert password (at least 3 characters):");
                password = sysScanner.nextLine();

                if (password.length()<3){
                    System.out.println("Password too short!");
                }
                else {
                    System.out.println("Retype password: ");
                    if (sysScanner.nextLine().equals(password)){
                        break;
                    }
                    else
                        System.out.println("Passwords did not match");
                }
            }
            fileWriter.write(username+":"+PasswordEncryption.passwordEncrypter(password)+ '\n');
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        return username;
    }

}
