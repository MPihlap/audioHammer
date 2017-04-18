package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Meelis on 17/04/2017.
 */
public class LoginHandler {

    public static boolean isUsernameTaken(String username,Scanner sc){
        while (sc.hasNextLine()){
            if (sc.nextLine().split(":")[0].equals(username))
                return true;
        }
        return false;
    }
    public static void newUserAccount() throws IOException {

        String userpath = "main/resources/UserInfo.txt";
        File file = new File(userpath);
        try (Scanner sysScanner = new Scanner(System.in);
             Scanner fileScanner = new Scanner(file, "UTF-8");
             FileWriter fileWriter = new FileWriter(file, true)
        ) {
            String username;
            String password;
            boolean isTaken;
            while (true) {
                System.out.println("Insert desired username:");
                username = sysScanner.nextLine();
                isTaken = isUsernameTaken(username,fileScanner);
                if (isTaken) {
                    System.out.println("Username is already in use.");
                } else {
                    break;
                }
            }
            while (true){
                System.out.println("Insert password (at least 8 characters):");
                password = sysScanner.nextLine();
                if (password.length()<8){
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
            fileWriter.write(username+":"+password+"\n");
        }
    }
}
