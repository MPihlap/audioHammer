package main.java.server;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

/**
 * Created by Alo on 19-Apr-17.
 */
public class PasswordEncryption  {

    public static String passwordEncrypter(String password) throws NoSuchAlgorithmException, InvalidKeySpecException{
        char[] passwordAsChars = password.toCharArray();
        byte[] salt = saltGenerator();
        PBEKeySpec spec = new PBEKeySpec(passwordAsChars, salt, 100, 128);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] passwordAsHash = secretKeyFactory.generateSecret(spec).getEncoded();
        return String.format("%x", new BigInteger(passwordAsHash)) + ":" +  String.format("%x", new BigInteger(salt)); //siin %x tähendab, et antud argument muudetakse hex stringiks
    }



    private static byte[] saltGenerator() {
        byte[] salt = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        return salt;

    }

    public static boolean passwordCheck(String checkPassword, String storedPassword, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        char[] passwordAsChars = checkPassword.toCharArray();

        byte[] saltAsBytes = new BigInteger(salt, 16).toByteArray();

        PBEKeySpec spec = new PBEKeySpec(passwordAsChars, saltAsBytes, 100, 128);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        byte[] passwordAsHash = secretKeyFactory.generateSecret(spec).getEncoded();
        checkPassword = String.format("%x", new BigInteger(passwordAsHash));

        byte[] checkPasswordAsBytes = new BigInteger(checkPassword, 16).toByteArray();
        byte[] storedPasswordAsBytes = new BigInteger(storedPassword, 16).toByteArray();

        for (int i = 0; i < checkPasswordAsBytes.length && i< storedPasswordAsBytes.length; i++) {
            if(!(checkPasswordAsBytes[i]==storedPasswordAsBytes[i])) {
                return false;
            }

        }

        return true;
    }


}
