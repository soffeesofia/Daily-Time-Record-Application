package object;

import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private boolean isRegistered;
    private boolean onDuty;

    private boolean isAdmin;

    public User(String username, String password, boolean isRegistered, String firstName, String lastName) {
        this.username = username;
        this.password = password;
        this.isRegistered = isRegistered;
        this.onDuty = false;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // getters and setters
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public boolean isOnDuty() {
        return onDuty;
    }
    public void setOnDuty(boolean onDuty) {
        this.onDuty = onDuty;
    }
    public boolean getIsRegistered() {
        return isRegistered;
    }

    public void setIsRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}

