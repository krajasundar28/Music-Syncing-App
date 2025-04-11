package com.musicapp.model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String username;
    private boolean isOnline;
    
    public User() {}
    
    public User(int id, String username) {
        this.id = id;
        this.username = username;
        this.isOnline = false;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean isOnline) { this.isOnline = isOnline; }
}