<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Music Player</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="header">
        <h1>Music Player</h1>
    </div>
    
    <div class="container">
        <div class="music-player">
            <div id="now-playing">
                <h2>Now Playing</h2>
                <h3 id="song-title">Select a song</h3>
                <p id="song-artist"></p>
            </div>
            
            <div class="progress-container" id="progress-container">
                <div class="progress-bar" id="progress-bar"></div>
            </div>
            
            <div class="time-display">
                <span id="current-time">0:00</span>
                <span id="total-time">0:00</span>
            </div>
            
            <div class="player-controls">
                <button class="btn" id="play-btn">Play</button>
                <button class="btn" id="pause-btn">Pause</button>
                <button class="btn" id="stop-btn">Stop</button>
            </div>
            
            <audio id="audio-player"></audio>
        </div>
        
        <div class="sync-container">
            <h2>Synchronized Streaming</h2>
            <p>Create a session to sync with a friend or join an existing session.</p>
            
            <div style="margin: 20px 0;">
                <button class="btn" id="create-sync-btn">Create Session</button>
                
                <div class="join-container">
                    <input type="text" id="session-id-input" placeholder="Enter Session ID">
                    <button class="btn" id="join-sync-btn">Join Session</button>
                </div>
            </div>
            
            <div id="sync-info" style="display: none;">
                <p>Active Session ID: <span id="session-id"></span></p>
                <p>Share this ID with a friend to sync your music!</p>
                <button class="btn" id="exit-sync-btn">Exit Session</button>
            </div>
        </div>
        
        <div class="music-player">
            <h2>Song Library</h2>
            <ul id="song-list" class="song-list">
                <li>Loading songs...</li>
            </ul>
        </div>
    </div>
    
    <script src="js/sync.js"></script>
    <script src="js/player.js"></script>
</body>
</html>