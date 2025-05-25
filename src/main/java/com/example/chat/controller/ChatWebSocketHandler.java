package com.example.chat.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    // Mapa sesión -> nombre usuario
    private final Map<WebSocketSession, String> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // No agregamos usuario todavía hasta que envíe nombre en el primer mensaje
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String username = userSessions.remove(session);
        if (username != null) {
            broadcastSystemMessage("El usuario '" + username + "' se ha desconectado");
            broadcastUserList();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        JsonNode node = mapper.readTree(payload);

        // Si no tenemos usuario asociado a esta sesión, esperamos que este mensaje contenga el nombre
        if (!userSessions.containsKey(session)) {
            // Esperamos JSON con campo "user"
            if (node.has("user")) {
                String username = node.get("user").asText();
                userSessions.put(session, username);

                // Enviar notificación a todos
                broadcastSystemMessage("El usuario '" + username + "' se ha conectado");
                broadcastUserList();

                // Opcional: No reenviamos este primer mensaje a todos, solo guardamos nombre
            }
            return;
        }

        // Ya tenemos usuario, reenviar el mensaje normal
        for (WebSocketSession s : userSessions.keySet()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(payload));
            }
        }
    }

    private void broadcastSystemMessage(String systemMessage) throws IOException {
        SystemMessage msg = new SystemMessage("system", systemMessage, userSessions.size(), getUserList());
        String json = mapper.writeValueAsString(msg);
        sendToAll(json);
    }

    private void broadcastUserList() throws IOException {
        // Enviar solo la lista actualizada a todos (tipo system con mensaje vacío)
        SystemMessage msg = new SystemMessage("system", "", userSessions.size(), getUserList());
        String json = mapper.writeValueAsString(msg);
        sendToAll(json);
    }

    private void sendToAll(String json) throws IOException {
        for (WebSocketSession s : userSessions.keySet()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(json));
            }
        }
    }

    private List<String> getUserList() {
        return new ArrayList<>(userSessions.values());
    }

    // Clase interna para mensajes de sistema
    static class SystemMessage {
        public final String type;
        public final String message;
        public final int userCount;
        public final List<String> users;

        public SystemMessage(String type, String message, int userCount, List<String> users) {
            this.type = type;
            this.message = message;
            this.userCount = userCount;
            this.users = users;
        }
    }
}
