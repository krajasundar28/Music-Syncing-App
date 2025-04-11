package com.musicapp.model;

import java.io.Serializable;

public class Song implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String title;
    private String artist;
    private String filePath;
    private int duration; // in seconds
    
    public Song() {}
    
    public Song(int id, String title, String artist, String filePath, int duration) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.filePath = filePath;
        this.duration = duration;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
}