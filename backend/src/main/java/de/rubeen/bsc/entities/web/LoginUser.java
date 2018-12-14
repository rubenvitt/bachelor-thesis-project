package de.rubeen.bsc.entities.web;

import org.springframework.data.annotation.Id;

public class LoginUser {
    private String email, password;

    LoginUser(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
