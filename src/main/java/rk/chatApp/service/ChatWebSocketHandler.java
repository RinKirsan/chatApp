package rk.chatApp.service;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final Map<WebSocketSession, String> sessions = new ConcurrentHashMap<>();
    private static final Map<String, Set<WebSocketSession>> groups = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String username = (String) session.getAttributes().get("username");
        if (username == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Username not provided"));
            return;
        }

        sessions.put(session, username);
        System.out.println("Новое подключение: " + session.getId() + " от пользователя " + username);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String username = sessions.get(session);
        String payload = message.getPayload();

        // Обработка команд (например, присоединение к группе)
        if (payload.startsWith("/join ")) {
            String groupName = payload.substring(6).trim();
            joinGroup(session, groupName);
            return;
        }

        // Отправка сообщения в группу
        String groupName = (String) session.getAttributes().get("group");
        if (groupName != null) {
            sendMessageToGroup(groupName, username + ": " + payload);
        } else {
            // Если пользователь не в группе, отправляем сообщение всем
            for (WebSocketSession webSocketSession : sessions.keySet()) {
                if (webSocketSession.isOpen()) {
                    webSocketSession.sendMessage(new TextMessage(username + ": " + payload));
                }
            }
        }
    }

    private void joinGroup(WebSocketSession session, String groupName) {
        session.getAttributes().put("group", groupName);
        groups.computeIfAbsent(groupName, k -> ConcurrentHashMap.newKeySet()).add(session);
        System.out.println("Пользователь " + sessions.get(session) + " присоединился к группе " + groupName);
    }

    private void sendMessageToGroup(String groupName, String message) throws IOException {
        Set<WebSocketSession> groupSessions = groups.get(groupName);
        if (groupSessions != null) {
            for (WebSocketSession webSocketSession : groupSessions) {
                if (webSocketSession.isOpen()) {
                    webSocketSession.sendMessage(new TextMessage(message));
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String username = sessions.remove(session);
        String groupName = (String) session.getAttributes().get("group");
        if (groupName != null) {
            groups.getOrDefault(groupName, ConcurrentHashMap.newKeySet()).remove(session);
        }
        System.out.println("Клиент отключился: " + session.getId() + " (пользователь " + username + ")");
    }
}