document.addEventListener('DOMContentLoaded', function() {
    const audio = document.getElementById('audio-player');
    const playBtn = document.getElementById('play-btn');
    const pauseBtn = document.getElementById('pause-btn');
    const stopBtn = document.getElementById('stop-btn');
    const progressBar = document.getElementById('progress-bar');
    const progressContainer = document.getElementById('progress-container');
    const currentTime = document.getElementById('current-time');
    const totalTime = document.getElementById('total-time');
    const songList = document.getElementById('song-list');
    const songTitle = document.getElementById('song-title');
    const songArtist = document.getElementById('song-artist');
    
    let songs = [];
    let currentSongId = null;
    let syncSessionId = null;
    let isSyncHost = false;
    let syncClient = null;
    // We'll use this flag to prevent circular updates when sync causes an audio event
    let ignoreEvents = false;
    
    // Fetch songs from the server
    fetch('player/songs', {
        cache: 'no-cache'
    })
    .then(response => response.json())
    .then(data => {
        console.log("Songs loaded:", data);
        songs = data;
        renderSongList();
    })
    .catch(error => {
        console.error('Error loading songs:', error);
        songList.innerHTML = '<li>Error loading songs. Check console for details.</li>';
    });
    
    // Render the song list
    function renderSongList() {
        songList.innerHTML = '';
        songs.forEach(song => {
            const li = document.createElement('li');
            li.className = 'song-item';
            li.innerHTML = `
                <div class="song-info">
                    <span class="song-title">${song.title}</span>
                    <span class="song-artist">${song.artist}</span>
                </div>
            `;
            li.onclick = () => loadSong(song.id);
            songList.appendChild(li);
        });
        
        // If we have songs, load the first one
        if (songs.length > 0) {
            loadSong(songs[0].id);
        }
    }
    
    // Load a song
    function loadSong(songId) {
        currentSongId = songId;
        const song = songs.find(s => s.id === songId);
        
        if (song) {
            console.log(`Loading song: ${song.title} (ID: ${songId})`);
            // Add timestamp to prevent caching
            const timestamp = new Date().getTime();
            const audioSrc = `player/stream/${songId}?t=${timestamp}`;
            console.log(`Audio source URL: ${audioSrc}`);
            audio.src = audioSrc;
            songTitle.textContent = song.title;
            songArtist.textContent = song.artist;
            audio.load();
            
            // If in sync mode and is host, notify others of song change
            if (syncClient && isSyncHost) {
                console.log(`Sending song change to sync: ${songId}`);
                syncClient.sendStatusUpdate(false, 0, songId);
            }
        } else {
            console.error(`Song not found with ID: ${songId}`);
        }
    }
    
    // Handle audio errors
    audio.addEventListener('error', function(e) {
        let errorMessage = "Unknown error";
        if (e.target.error) {
            switch(e.target.error.code) {
                case MediaError.MEDIA_ERR_ABORTED:
                    errorMessage = "You aborted the playback";
                    break;
                case MediaError.MEDIA_ERR_NETWORK:
                    errorMessage = "Network error";
                    break;
                case MediaError.MEDIA_ERR_DECODE:
                    errorMessage = "Format error - file might be corrupted or in an unsupported format";
                    break;
                case MediaError.MEDIA_ERR_SRC_NOT_SUPPORTED:
                    errorMessage = "The audio format is not supported by your browser";
                    break;
                default:
                    errorMessage = `Error code: ${e.target.error.code}`;
            }
        }
        console.error('Audio error:', errorMessage, e);
        console.error('Audio source:', audio.src);
        alert(`Error playing audio: ${errorMessage}`);
    });
    
    // Update progress bar and send sync updates
    audio.addEventListener('timeupdate', function() {
        if (ignoreEvents) return;
        
        if (audio.duration) {
            const progress = (audio.currentTime / audio.duration) * 100;
            progressBar.style.width = progress + '%';
            
            // Update time display
            currentTime.textContent = formatTime(audio.currentTime);
            
            // Send sync update if host
            if (syncClient && isSyncHost && !audio.paused) {
                // Don't send updates too frequently - every 3 seconds is enough
                if (Math.floor(audio.currentTime) % 3 === 0) {
                    syncClient.sendStatusUpdate(true, Math.floor(audio.currentTime * 1000), currentSongId);
                }
            }
        }
    });
    
    // When song is loaded, update total time and send initial sync
    audio.addEventListener('loadedmetadata', function() {
        console.log('Audio metadata loaded:', audio.duration);
        totalTime.textContent = formatTime(audio.duration);
    });
    
    // Play event handler
    audio.addEventListener('play', function() {
        if (ignoreEvents) return;
        
        console.log('Audio play event');
        if (syncClient && isSyncHost) {
            syncClient.sendStatusUpdate(true, Math.floor(audio.currentTime * 1000), currentSongId);
        }
    });
    
    // Pause event handler
    audio.addEventListener('pause', function() {
        if (ignoreEvents) return;
        
        console.log('Audio pause event');
        if (syncClient && isSyncHost) {
            syncClient.sendStatusUpdate(false, Math.floor(audio.currentTime * 1000), currentSongId);
        }
    });
    
    // Control buttons
    playBtn.addEventListener('click', function() {
        if (audio.src) {
            console.log('Play button clicked');
            audio.play()
                .catch(error => console.error('Error playing audio:', error));
        } else {
            alert('Please select a song first');
        }
    });
    
    pauseBtn.addEventListener('click', function() {
        console.log('Pause button clicked');
        audio.pause();
    });
    
    stopBtn.addEventListener('click', function() {
        console.log('Stop button clicked');
        audio.pause();
        audio.currentTime = 0;
    });
    
    // Format time in MM:SS
    function formatTime(seconds) {
        if (!seconds || isNaN(seconds)) return "0:00";
        const mins = Math.floor(seconds / 60);
        const secs = Math.floor(seconds % 60);
        return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
    }
    
    // Progress bar click to seek
    progressContainer.addEventListener('click', function(e) {
        if (!audio.duration) return;
        
        const percent = e.offsetX / this.offsetWidth;
        audio.currentTime = percent * audio.duration;
        
        // Send sync update if host
        if (syncClient && isSyncHost) {
            syncClient.sendStatusUpdate(!audio.paused, Math.floor(audio.currentTime * 1000), currentSongId);
        }
    });
    
    // Sync functionality
    document.getElementById('create-sync-btn').addEventListener('click', function() {
        console.log('Creating sync session...');
        fetch('sync/create', {
            cache: 'no-cache'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Server error: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Sync session created:', data);
            syncSessionId = data.sessionId;
            document.getElementById('session-id').textContent = syncSessionId;
            document.getElementById('sync-info').style.display = 'block';
            
            // Set as host and initialize sync client
            isSyncHost = true;
            syncClient = new SyncClient(syncSessionId, true);
            syncClient.connect();
            
            // Add listener to handle sync events (not really used for host)
            syncClient.addListener(function(event) {
                console.log('Sync event received by host:', event);
            });
            
            console.log(`Created sync session: ${syncSessionId}`);
            
            // Initial sync with current song
            if (currentSongId) {
                syncClient.sendStatusUpdate(false, 0, currentSongId);
            }
        })
        .catch(error => {
            console.error('Error creating sync session:', error);
            alert('Failed to create sync session. See console for details.');
        });
    });
    
    document.getElementById('join-sync-btn').addEventListener('click', function() {
        const sessionId = document.getElementById('session-id-input').value;
        
        if (!sessionId) {
            alert('Please enter a session ID');
            return;
        }
        
        console.log(`Joining sync session: ${sessionId}`);
        fetch(`sync/join/${sessionId}`, {
            cache: 'no-cache'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Server error: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Join response:', data);
            if (data.success === true || data.success === "true") {
                syncSessionId = sessionId;
                document.getElementById('session-id').textContent = syncSessionId;
                document.getElementById('sync-info').style.display = 'block';
                
                // Set as guest and initialize sync client
                isSyncHost = false;
                syncClient = new SyncClient(syncSessionId, false);
                syncClient.connect();
                
                // Add listener to handle sync events
                syncClient.addListener(function(event) {
                    console.log('Sync event received by guest:', event);
                    
                    // Set flag to ignore events we trigger from sync updates
                    ignoreEvents = true;
                    
                    if (event.type === 'song' && event.songId) {
                        if (event.songId !== currentSongId) {
                            console.log(`Loading song from sync: ${event.songId}`);
                            loadSong(event.songId);
                        }
                    } else if (event.type === 'status') {
                        console.log(`Status update from sync: isPlaying=${event.isPlaying}, position=${event.position}`);
                        
                        // Update current time
                        const newPosition = event.position / 1000;
                        if (Math.abs(audio.currentTime - newPosition) > 3) {
                            audio.currentTime = newPosition;
                        }
                        
                        // Update play state
                        if (event.isPlaying && audio.paused) {
                            audio.play()
                                .catch(error => console.error('Error playing audio:', error));
                        } else if (!event.isPlaying && !audio.paused) {
                            audio.pause();
                        }
                    }
                    
                    // Clear the flag after a short delay to allow events to settle
                    setTimeout(() => {
                        ignoreEvents = false;
                    }, 100);
                });
                
                console.log(`Joined sync session: ${syncSessionId}`);
            } else {
                throw new Error('Failed to join session');
            }
        })
        .catch(error => {
            console.error('Error joining sync session:', error);
            alert('Failed to join sync session. See console for details.');
        });
    });
    
    // Exit sync session
    document.getElementById('exit-sync-btn').addEventListener('click', function() {
        if (syncClient) {
            syncClient.disconnect();
            syncClient = null;
            syncSessionId = null;
            isSyncHost = false;
            
            document.getElementById('sync-info').style.display = 'none';
            console.log('Exited sync session');
        }
    });
});