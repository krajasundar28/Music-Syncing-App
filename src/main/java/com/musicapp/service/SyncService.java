package com.musicapp.service;

import com.musicapp.model.User;
import com.musicapp.util.SyncManager;
import com.musicapp.util.SyncManager.SessionState;

public class SyncService {
    private SyncManager syncManager;
    
    public SyncService() {
        syncManager = SyncManager.getInstance();
    }
    
    public String createSyncSession(User host) {
        return syncManager.createSession(host);
    }
    
    public boolean joinSyncSession(String sessionId, User guest) {
        return syncManager.joinSession(sessionId, guest);
    }
    
    public void updatePlayback(String sessionId, boolean isPlaying, int position) {
        syncManager.updateSessionPlayback(sessionId, isPlaying, position, 0);
    }
    
    public void updatePlayback(String sessionId, boolean isPlaying, int position, int songId) {
        syncManager.updateSessionPlayback(sessionId, isPlaying, position, songId);
    }
    
    public int getSessionPort(String sessionId) {
        return syncManager.getSessionPort(sessionId);
    }
    
    public SessionState getSessionState(String sessionId) {
        return syncManager.getSessionState(sessionId);
    }
}