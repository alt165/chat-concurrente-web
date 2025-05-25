package com.example.chat.controller;

import com.example.chat.core.Message;
import com.example.chat.core.MessageStorage;
import com.example.chat.core.UserSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;

import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final UserSessionManager sessionManager = UserSessionManager.getInstance();
    private final MessageStorage messageStorage = MessageStorage.getInstance();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (!sessionManager.tryAddSession(session)) {
            session.close(new CloseStatus(429, "Límite de usuarios alcanzado"));
            return;
        }

        // Enviar historial solo si existe
        for (Message msg : messageStorage.getMessages()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        }

        sessions.put(session.getId(), session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws IOException {
        Message msg = objectMapper.readValue(textMessage.getPayload(), Message.class);
        messageStorage.addMessage(msg);

        String json = objectMapper.writeValueAsString(msg);

        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(json));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        sessionManager.removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        sessions.remove(session.getId());
        sessionManager.removeSession(session);
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
