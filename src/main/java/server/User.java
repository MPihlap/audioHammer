package main.java.server;

import java.util.List;

/**
 * Created by Meelis on 17/04/2017.
 */
public class User {
    private final String username;
    private final String password;
    private List<String> filenames;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getFilenames() {
        return filenames;
    }
}
