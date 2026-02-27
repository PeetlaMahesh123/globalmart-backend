package com.kodnest.learn.dto;


public class LoginRequest {

    private String username;
    private String password;

    // Default Constructor
    public LoginRequest() {
        // TODO Auto-generated constructor stub
    }

    // Parameterized Constructor
    public LoginRequest(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    // Getter for Username
    public String getUsername() {
        return username;
    }

    // Setter for Username
    public void setUsername(String username) {
        this.username = username;
    }

    // Getter for Password
    public String getPassword() {
        return password;
    }

    // Setter for Password
    public void setPassword(String password) {
        this.password = password;
    }
}