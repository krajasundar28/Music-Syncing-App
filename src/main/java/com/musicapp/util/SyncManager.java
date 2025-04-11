package com.musicapp.util;

import com.musicapp.model.User;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages synchronized playback sessions
 * Note: In a real app, this would use WebSockets
 * This simplified version just maintains session states
 */
public class SyncManager {
    private static SyncManager instance;
    private Map<String, SyncSession> sessions;
    
    private SyncManager() {
        sessions = new ConcurrentHashMap<>();
    }
    
    public static synchronized SyncManager getInstance() {
        if (instance == null) {
            instance = new SyncManager();
        }
        return instance;
    }
    
    public String createSession(User host) {
        String sessionId = generateSessionId();
        SyncSession session = new SyncSession(sessionId, host);
        sessions.put(sessionId, session);
        System.out.println("Created sync session: " + sessionId + " for host: " + host.getUsername());
        return sessionId;
    }
    
    public boolean joinSession(String sessionId, User guest) {
        SyncSession session = sessions.get(sessionId);
        if (session != null) {
            session.guest = guest;
            System.out.println("User " + guest.getUsername() + " joined session: " + sessionId);
            return true;
        }
        System.out.println("Failed to join session: " + sessionId + " - Session not found");
        return false;
    }
    
    public void updateSessionPlayback(String sessionId, boolean isPlaying, int position, int songId) {
        SyncSession session = sessions.get(sessionId);
        if (session != null) {
            session.isPlaying = isPlaying;
            session.position = position;
            if (songId > 0) {
                session.currentSongId = songId;
            }
            session.lastUpdateTime = System.currentTimeMillis();
            System.out.println("Updated session " + sessionId + 
                              ": isPlaying=" + isPlaying + 
                              ", position=" + position + 
                              (songId > 0 ? ", songId=" + songId : ""));
        } else {
            System.out.println("Failed to update session: " + sessionId + " - Session not found");
        }
    }
    
    public int getSessionPort(String sessionId) {
        // In a real app, this would return the WebSocket port
        // For simplicity, we return a dummy port
        return 9000;
    }
    
    public SessionState getSessionState(String sessionId) {
        SyncSession session = sessions.get(sessionId);
        if (session != null) {
            return new SessionState(
                session.isPlaying,
                session.position,
                session.currentSongId,
                session.lastUpdateTime
            );
        }
        return null;
    }
    
    private String generateSessionId() {
        return Long.toString(System.currentTimeMillis() % 100000);
    }
    
    private class SyncSession {
        String sessionId;
        User host;
        User guest;
        boolean isPlaying;
        int position;
        int currentSongId;
        long lastUpdateTime;
        
        public SyncSession(String sessionId, User host) {
            this.sessionId = sessionId;
            this.host = host;
            this.isPlaying = false;
            this.position = 0;
            this.currentSongId = 0;
            this.lastUpdateTime = System.currentTimeMillis();
        }
    }
    
    public static class SessionState {
        private boolean isPlaying;
        private int position;
        private int songId;
        private long updateTime;
        
        public SessionState(boolean isPlaying, int position, int songId, long updateTime) {
            this.isPlaying = isPlaying;
            this.position = position;
            this.songId = songId;
            this.updateTime = updateTime;
        }
        
        public boolean isPlaying() { return isPlaying; }
        public int getPosition() { return position; }
        public int getSongId() { return songId; }
        public long getUpdateTime() { return updateTime; }
    }
}