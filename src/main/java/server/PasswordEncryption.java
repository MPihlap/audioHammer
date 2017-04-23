package server;

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


    /**
     * Generates random salt for password and encrypts it with PBKDF2WithHmacSHA1 encryption
     * @param password Password to be encrypted
     * @return Encrypted password + salt
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    static String passwordEncrypter(String password) throws NoSuchAlgorithmException, InvalidKeySpecException{
        char[] passwordAsChars = password.toCharArray();
        byte[] salt = saltGenerator();
        PBEKeySpec spec = new PBEKeySpec(passwordAsChars, salt, 100, 128);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] passwordAsHash = secretKeyFactory.generateSecret(spec).getEncoded();
        return String.format("%x", new BigInteger(passwordAsHash)) + ":" +  String.format("%x", new BigInteger(salt)); //siin %x tähendab, et antud argument muudetakse hex stringiks
    }


    /**
     * Generates custom salt for password to be encrypted with; salt is random data that is used as an additional input to a one-way function that "hashes" a password;
     * @return Randomly generated salt
     */
    private static byte[] saltGenerator() {
        byte[] salt = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        return salt;

    }

    /**
     *
     * @param checkPassword Password that is inserted upon client login to audioHammer
     * @param storedPassword Encrypted password that is already stored in user data file;
     * @param salt random data that storedPassword was encrypted with
     * @return True, if both checkPassword and storedPassword are equal;
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    static boolean passwordCheck(String checkPassword, String storedPassword, String salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
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
