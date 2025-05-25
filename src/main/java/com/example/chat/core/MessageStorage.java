package com.example.chat.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageStorage {

    private static final int MAX_HISTORY = 100;
    private final List<Message> messageHistory = Collections.synchronizedList(new ArrayList<>());

    private static final MessageStorage INSTANCE = new MessageStorage();

    private MessageStorage() {}

    public static MessageStorage getInstance() {
        return INSTANCE;
    }

    public void addMessage(Message message) {
        synchronized (messageHistory) {
            if (messageHistory.size() >= MAX_HISTORY) {
                messageHistory.remove(0); // elimina el más antiguo
            }
            messageHistory.add(message);
        }
    }

    public List<Message> getMessages() {
        synchronized (messageHistory) {
            return new ArrayList<>(messageHistory); // devuelve una copia segura
        }
    }
}
