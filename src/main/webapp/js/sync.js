/**
 * Sync.js - Handles synchronization between two clients
 * This is a simplified simulation of what would normally be implemented with WebSockets
 */
class SyncClient {
    constructor(sessionId, isHost) {
        this.sessionId = sessionId;
        this.isHost = isHost;
        this.listeners = [];
        this.connected = false;
        this.pollInterval = null;
        this.pollDelay = 1000; // Poll every 1 second
        this.lastUpdateTime = 0;
        
        console.log(`SyncClient initialized: sessionId=${sessionId}, isHost=${isHost}`);
    }
    
    // Connect to sync server (simulated)
    connect() {
        this.connected = true;
        console.log('Sync connection established');
        
        // If guest, start polling for updates
        if (!this.isHost) {
            this.startPolling();
        }
        
        return true;
    }
    
    // Disconnect from sync server
    disconnect() {
        this.connected = false;
        
        if (this.pollInterval) {
            clearInterval(this.pollInterval);
            this.pollInterval = null;
        }
        
        console.log('Sync connection closed');
    }
    
    // Add a listener for sync events
    addListener(callback) {
        this.listeners.push(callback);
    }
    
    // Send status update (only host can do this)
    sendStatusUpdate(isPlaying, position, songId) {
        if (!this.connected || !this.isHost) return false;
        
        console.log(`Sending status update: isPlaying=${isPlaying}, position=${position}${songId ? ', songId=' + songId : ''}`);
        
        // Build the request params
        let params = `isPlaying=${isPlaying}&position=${position}`;
        if (songId) {
            params += `&songId=${songId}`;
        }
        
        // Send to server via HTTP POST with fetch
        fetch('sync/update', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: params,
            cache: 'no-cache'
        })
        .then(response => {
            if (!response.ok) {
                console.error(`Server error: ${response.status}`);
                return;
            }
            console.log('Status update sent successfully');
        })
        .catch(error => {
            console.error('Error sending status update:', error);
        });
        
        return true;
    }
    
    // Start polling for updates (for guests)
    startPolling() {
        if (this.isHost) return;
        
        console.log('Started polling for sync updates every ' + this.pollDelay + 'ms');
        
        // Do an immediate poll to get initial state
        this.pollForUpdates();
        
        this.pollInterval = setInterval(() => {
            this.pollForUpdates();
        }, this.pollDelay);
    }
    
    // Poll for updates from server
    pollForUpdates() {
        if (!this.connected) return;
        
        console.log('Polling for sync updates...');
        
        fetch('sync/status', {
            cache: 'no-cache' // Important: prevent caching
        })
        .then(response => {
            if (!response.ok) {
                console.error(`Server error during poll: ${response.status}`);
                return null;
            }
            return response.json();
        })
        .then(data => {
            if (!data) return;
            
            console.log('Received sync update:', data);
            
            // Only process if this is newer than our last update
            if (data.timestamp > this.lastUpdateTime) {
                this.lastUpdateTime = data.timestamp;
                
                // Create and notify song event if songId is present and not 0
                if (data.songId && data.songId > 0) {
                    console.log(`Notifying song change: ${data.songId}`);
                    this.notifyListeners({
                        type: 'song',
                        songId: data.songId
                    });
                }
                
                // Create and notify status event
                console.log(`Notifying status change: isPlaying=${data.isPlaying}, position=${data.position}`);
                this.notifyListeners({
                    type: 'status',
                    isPlaying: data.isPlaying,
                    position: data.position
                });
            }
        })
        .catch(error => {
            console.error('Error polling for updates:', error);
        });
    }
    
    // Notify all listeners of an event
    notifyListeners(event) {
        console.log('Notifying listeners of event:', event);
        for (const listener of this.listeners) {
            try {
                listener(event);
            } catch (e) {
                console.error('Error in sync listener:', e);
            }
        }
    }
}

// Export for use in player.js
window.SyncClient = SyncClient;