package com.musicapp.data;

import com.musicapp.model.Song;
import com.musicapp.model.User;
import java.util.List;

/**
 * Repository layer to access the data store
 */
public class MusicRepository {
    
    public List<Song> getAllSongs() {
        return DataStore.getAllSongs();
    }
    
    public Song getSongById(int id) {
        return DataStore.getSongById(id);
    }
    
    public List<User> getAllUsers() {
        return DataStore.getAllUsers();
    }
    
    public User getUserById(int id) {
        return DataStore.getUserById(id);
    }
    
    public User getUserByUsername(String username) {
        return DataStore.getUserByUsername(username);
    }
    
    public List<User> getOnlineUsers() {
        return DataStore.getOnlineUsers();
    }
    
    public boolean updateUserStatus(int userId, boolean isOnline) {
        return DataStore.updateUserStatus(userId, isOnline);
    }
    
    public User getDemoUser() {
        return DataStore.getDemoUser();
    }
}