package com.example.chat.core;

import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UserSessionManager {

    private static final int MAX_USERS = 100;
    private final Set<String> activeSessions = Collections.synchronizedSet(new HashSet<>());

    private static final UserSessionManager INSTANCE = new UserSessionManager();

    private UserSessionManager() {}

    public static UserSessionManager getInstance() {
        return INSTANCE;
    }

    public boolean tryAddSession(WebSocketSession session) {
        synchronized (activeSessions) {
            if (activeSessions.size() >= MAX_USERS) {
                return false;
            }
            return activeSessions.add(session.getId());
        }
    }

    public void removeSession(WebSocketSession session) {
        activeSessions.remove(session.getId());
    }

    public int getActiveUserCount() {
        return activeSessions.size();
    }
}
