package com.musicapp.servlet;

import com.musicapp.model.User;
import com.musicapp.service.MusicService;
import com.musicapp.service.SyncService;
import com.musicapp.util.SyncManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "SyncServlet", urlPatterns = {"/sync", "/sync/*"})
public class SyncServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private SyncService syncService;
    private MusicService musicService;
    
    @Override
    public void init() throws ServletException {
        syncService = new SyncService();
        musicService = new MusicService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // Show the sync page
            request.getRequestDispatcher("/player.jsp").forward(request, response);
            return;
        }
        
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        // For demo, create a dummy user if not logged in
        if (user == null) {
            user = musicService.getDemoUser();
            session.setAttribute("user", user);
        }
        
        if (pathInfo.equals("/create")) {
            // Create a new sync session
            String sessionId = syncService.createSyncSession(user);
            int port = syncService.getSessionPort(sessionId);
            
            session.setAttribute("syncSessionId", sessionId);
            session.setAttribute("syncRole", "host");
            
            StringBuilder json = new StringBuilder();
            json.append("{\"sessionId\":\"").append(sessionId)
                .append("\",\"port\":").append(port).append("}");
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            try (PrintWriter out = response.getWriter()) {
                out.print(json.toString());
            }
        } else if (pathInfo.startsWith("/join/")) {
            // Join an existing session
            String sessionId = pathInfo.substring("/join/".length());
            boolean joined = syncService.joinSyncSession(sessionId, user);
            
            session.setAttribute("syncSessionId", sessionId);
            session.setAttribute("syncRole", "guest");
            int port = syncService.getSessionPort(sessionId);
            
            StringBuilder json = new StringBuilder();
            json.append("{\"success\":").append(joined)
                .append(",\"port\":").append(port).append("}");
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            try (PrintWriter out = response.getWriter()) {
                out.print(json.toString());
            }
        } else if (pathInfo.equals("/status")) {
            // Get sync status (for polling by clients)
            String sessionId = (String) session.getAttribute("syncSessionId");
            
            if (sessionId != null) {
                // Get current session state from SyncManager
                SyncManager.SessionState state = syncService.getSessionState(sessionId);
                
                if (state != null) {
                    StringBuilder json = new StringBuilder();
                    json.append("{\"isPlaying\":").append(state.isPlaying())
                        .append(",\"position\":").append(state.getPosition())
                        .append(",\"songId\":").append(state.getSongId())
                        .append(",\"timestamp\":").append(System.currentTimeMillis())
                        .append("}");
                    
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    
                    try (PrintWriter out = response.getWriter()) {
                        out.print(json.toString());
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Session not found");
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No active sync session");
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || !pathInfo.equals("/update")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        HttpSession session = request.getSession();
        String sessionId = (String) session.getAttribute("syncSessionId");
        String role = (String) session.getAttribute("syncRole");
        
        if (sessionId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No active sync session");
            return;
        }
        
        if (!"host".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only host can update session");
            return;
        }
        
        String isPlayingParam = request.getParameter("isPlaying");
        String positionParam = request.getParameter("position");
        String songIdParam = request.getParameter("songId");
        
        if (isPlayingParam == null || positionParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
            return;
        }
        
        boolean isPlaying = Boolean.parseBoolean(isPlayingParam);
        int position = Integer.parseInt(positionParam);
        int songId = songIdParam != null ? Integer.parseInt(songIdParam) : 0;
        
        System.out.println("Sync update received - Session: " + sessionId + 
                           ", isPlaying: " + isPlaying + 
                           ", position: " + position + 
                           (songIdParam != null ? ", songId: " + songId : ""));
        
        syncService.updatePlayback(sessionId, isPlaying, position, songId);
        
        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true}");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.print(json.toString());
        }
    }
}