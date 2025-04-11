package com.musicapp.service;

import com.musicapp.data.MusicRepository;
import com.musicapp.model.Song;
import com.musicapp.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

public class MusicService {
    private static final int BUFFER_SIZE = 8192;
    private MusicRepository musicRepository;
    
    public MusicService() {
        musicRepository = new MusicRepository();
    }
    
    public List<Song> getAllSongs() {
        return musicRepository.getAllSongs();
    }
    
    public Song getSongById(int id) {
        return musicRepository.getSongById(id);
    }
    
    public User getDemoUser() {
        return musicRepository.getDemoUser();
    }
    
    public void streamAudio(String relativeFilePath, ServletContext context, HttpServletResponse response) throws IOException {
        // Try different paths to find the file
        File audioFile = null;
        String realPath = null;
        
        // First try: context relative path
        realPath = context.getRealPath(relativeFilePath);
        if (realPath != null) {
            audioFile = new File(realPath);
            System.out.println("Trying path 1: " + realPath + " - Exists: " + audioFile.exists());
        }
        
        // Second try: if path starts with /, remove it
        if ((audioFile == null || !audioFile.exists()) && relativeFilePath.startsWith("/")) {
            realPath = context.getRealPath(relativeFilePath.substring(1));
            if (realPath != null) {
                audioFile = new File(realPath);
                System.out.println("Trying path 2: " + realPath + " - Exists: " + audioFile.exists());
            }
        }
        
        // Third try: direct webapp path
        if (audioFile == null || !audioFile.exists()) {
            realPath = context.getRealPath("/") + relativeFilePath;
            audioFile = new File(realPath);
            System.out.println("Trying path 3: " + realPath + " - Exists: " + audioFile.exists());
        }
        
        // Fourth try: absolute path outside webapp
        if (audioFile == null || !audioFile.exists()) {
            // This is a fallback for testing - replace with your actual path
            // For example: C:/music/song1.mp3
            realPath = "C:/music/song1.mp3"; // Change this to your actual path outside the webapp
            audioFile = new File(realPath);
            System.out.println("Trying path 4: " + realPath + " - Exists: " + audioFile.exists());
        }
        
        // If file is still not found after all attempts
        if (audioFile == null || !audioFile.exists()) {
            throw new IOException("Audio file not found after multiple attempts. Path tried: " + realPath);
        }
        
        System.out.println("Streaming file: " + realPath + " - Size: " + audioFile.length() + " bytes");
        
        String mimeType = getMimeType(realPath);
        long length = audioFile.length();
        
        // Set additional headers to help with streaming
        response.setContentType(mimeType);
        response.setContentLength((int) length);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Cache-Control", "no-cache");
        
        try (InputStream in = new FileInputStream(audioFile);
             OutputStream out = response.getOutputStream()) {
            
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }
    
    // Rest of the code remains the same...
    
    public void streamAudioRange(String relativeFilePath, ServletContext context, HttpServletResponse response, 
                                long rangeStart, long rangeEnd) throws IOException {
        // Use the same file finding logic as streamAudio method
        File audioFile = null;
        String realPath = null;
        
        // First try: context relative path
        realPath = context.getRealPath(relativeFilePath);
        if (realPath != null) {
            audioFile = new File(realPath);
            System.out.println("Range - Trying path 1: " + realPath + " - Exists: " + audioFile.exists());
        }
        
        // Second try: if path starts with /, remove it
        if ((audioFile == null || !audioFile.exists()) && relativeFilePath.startsWith("/")) {
            realPath = context.getRealPath(relativeFilePath.substring(1));
            if (realPath != null) {
                audioFile = new File(realPath);
                System.out.println("Range - Trying path 2: " + realPath + " - Exists: " + audioFile.exists());
            }
        }
        
        // Third try: direct webapp path
        if (audioFile == null || !audioFile.exists()) {
            realPath = context.getRealPath("/") + relativeFilePath;
            audioFile = new File(realPath);
            System.out.println("Range - Trying path 3: " + realPath + " - Exists: " + audioFile.exists());
        }
        
        // Fourth try: absolute path outside webapp
        if (audioFile == null || !audioFile.exists()) {
            // This is a fallback for testing - replace with your actual path
            realPath = "C:/music/song1.mp3"; // Change this to your actual path outside the webapp
            audioFile = new File(realPath);
            System.out.println("Range - Trying path 4: " + realPath + " - Exists: " + audioFile.exists());
        }
        
        // If file is still not found after all attempts
        if (audioFile == null || !audioFile.exists()) {
            throw new IOException("Audio file not found for range request after multiple attempts. Path tried: " + realPath);
        }
        
        System.out.println("Streaming file range: " + realPath);
        
        long fileLength = audioFile.length();
        
        if (rangeEnd == -1) {
            rangeEnd = fileLength - 1;
        }
        
        long contentLength = rangeEnd - rangeStart + 1;
        
        String mimeType = getMimeType(realPath);
        response.setContentType(mimeType);
        response.setContentLength((int) contentLength);
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setHeader("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
        
        try (InputStream in = new FileInputStream(audioFile)) {
            in.skip(rangeStart);
            
            OutputStream out = response.getOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            long remaining = contentLength;
            int bytesRead;
            
            while (remaining > 0 && (bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                out.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        }
    }
    
    private String getMimeType(String filePath) {
        if (filePath.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (filePath.endsWith(".wav")) {
            return "audio/wav";
        } else if (filePath.endsWith(".ogg")) {
            return "audio/ogg";
        } else {
            return "application/octet-stream";
        }
    }
}