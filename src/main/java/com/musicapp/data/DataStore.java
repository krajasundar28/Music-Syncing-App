package com.musicapp.data;

import com.musicapp.model.Song;
import com.musicapp.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory data store to replace database
 */
public class DataStore {
    private static final Map<Integer, Song> songs = new HashMap<>();
    private static final Map<Integer, User> users = new HashMap<>();
    private static int nextSongId = 1;
    private static int nextUserId = 1;
    
    static {
        // Initialize with actual MP3 file path - make sure it matches exactly where your file is
        addSong(new Song(nextSongId++, "Song One", "Artist One", "music/song1.mp3", 180));
        
        // If you have more songs, add them here with their correct paths
        // addSong(new Song(nextSongId++, "Song Two", "Artist Two", "music/song2.mp3", 240));
        // addSong(new Song(nextSongId++, "Song Three", "Artist Three", "music/song3.mp3", 200));
        
        addUser(new User(nextUserId++, "user1"));
        addUser(new User(nextUserId++, "user2"));
    }
    
    private static void addSong(Song song) {
        songs.put(song.getId(), song);
    }
    
    private static void addUser(User user) {
        users.put(user.getId(), user);
    }
    
    public static List<Song> getAllSongs() {
        return new ArrayList<>(songs.values());
    }
    
    public static Song getSongById(int id) {
        return songs.get(id);
    }
    
    public static List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    public static User getUserById(int id) {
        return users.get(id);
    }
    
    public static User getUserByUsername(String username) {
        for (User user : users.values()) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
    
    public static List<User> getOnlineUsers() {
        List<User> onlineUsers = new ArrayList<>();
        for (User user : users.values()) {
            if (user.isOnline()) {
                onlineUsers.add(user);
            }
        }
        return onlineUsers;
    }
    
    public static boolean updateUserStatus(int userId, boolean isOnline) {
        User user = users.get(userId);
        if (user != null) {
            user.setOnline(isOnline);
            return true;
        }
        return false;
    }
    
    // Get a demo user (for testing purposes)
    public static User getDemoUser() {
        return users.get(1); // Return the first user
    }
}