package com.musicapp.servlet;

import com.musicapp.model.Song;
import com.musicapp.service.MusicService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "MusicPlayerServlet", urlPatterns = {"/player", "/player/*"})
public class MusicPlayerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private MusicService musicService;
    
    @Override
    public void init() throws ServletException {
        musicService = new MusicService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // Show the player page
            request.getRequestDispatcher("/player.jsp").forward(request, response);
        } else if (pathInfo.startsWith("/stream/")) {
            try {
                // Stream a song
                int songId = Integer.parseInt(pathInfo.substring("/stream/".length()));
                streamSong(songId, request, response);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid song ID");
            }
        } else if (pathInfo.equals("/songs")) {
            // Return list of songs as JSON
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            try (PrintWriter out = response.getWriter()) {
                out.print(getSongsJson());
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    private void streamSong(int songId, HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        Song song = musicService.getSongById(songId);
        
        if (song == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Song not found");
            return;
        }
        
        // Check for Range header
        String rangeHeader = request.getHeader("Range");
        
        try {
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                // Handle range request
                String rangeValue = rangeHeader.substring("bytes=".length());
                String[] ranges = rangeValue.split("-");
                long rangeStart = Long.parseLong(ranges[0]);
                long rangeEnd = ranges.length > 1 && !ranges[1].isEmpty() ? 
                                Long.parseLong(ranges[1]) : -1;
                
                System.out.println("Range request: " + rangeStart + "-" + rangeEnd);
                musicService.streamAudioRange(song.getFilePath(), getServletContext(), response, rangeStart, rangeEnd);
            } else {
                // Handle normal request
                System.out.println("Normal audio stream request");
                musicService.streamAudio(song.getFilePath(), getServletContext(), response);
            }
        } catch (Exception e) {
            System.err.println("Error streaming audio: " + e.getMessage());
            e.printStackTrace();
            
            // Only send error if response hasn't been committed yet
            if (!response.isCommitted()) {
                response.reset();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error streaming audio: " + e.getMessage());
            }
        }
    }
    
    private String getSongsJson() {
        StringBuilder json = new StringBuilder();
        json.append('[');
        
        boolean first = true;
        for (Song song : musicService.getAllSongs()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            
            json.append('{');
            json.append("\"id\":").append(song.getId()).append(',');
            json.append("\"title\":\"").append(escapeJson(song.getTitle())).append("\",");
            json.append("\"artist\":\"").append(escapeJson(song.getArtist())).append("\",");
            json.append("\"duration\":").append(song.getDuration());
            json.append('}');
        }
        
        json.append(']');
        return json.toString();
    }
    
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}