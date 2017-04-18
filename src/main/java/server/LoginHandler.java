package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
                String[] a = fileScanner.nextLine().split(":");
                if (a[0].equals(username) && a[1].equals(password))
                    return true;
            }
        }
        return false;
    }

    public static boolean login(Scanner sysScanner) throws IOException {
        String userpath = "src/main/resources/UserInfo.txt";
        File file = new File(userpath);
            while (true) {
                System.out.println("Username:");
                String username = sysScanner.nextLine();
                if(doesUsernameExist(username, file)) {
                    System.out.println("Password:");
                    String password = sysScanner.nextLine();
                    if (checkPassword(username, password, file)) {
                        return true;
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


    public static void newUserAccount(Scanner sysScanner) throws IOException {

        String userpath = "src/main/resources/UserInfo.txt";
        File file = new File(userpath);
        try (FileWriter fileWriter = new FileWriter(file, true)
        ) {
            String username;
            String password;
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
            fileWriter.write(username+":"+password+ '\n');
        }
    }
}
