package rk.chatApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import rk.chatApp.model.User;
import rk.chatApp.repository.MessageRepository;
import rk.chatApp.repository.UserRepository;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final Map<WebSocketSession, String> sessions = new ConcurrentHashMap<>();
    private static final Map<String, Set<WebSocketSession>> groups = new ConcurrentHashMap<>();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupService groupService;

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

        // Обработка команд
        if (payload.startsWith("/join ")) {
            String groupId = payload.substring(6).trim();
            joinGroup(session, groupId);
            return;
        }

        // Отправка сообщений в формате "groupId:message"
        String[] parts = payload.split(":", 2);
        if (parts.length == 2) {
            String groupId = parts[0];
            String messageText = parts[1];

            // Сохраняем сообщение в базе данных
            User user = userRepository.findByUsername(username);
            groupService.saveMessage(Long.parseLong(groupId), user.getId(), messageText);

            // Отправляем сообщение в группу
            sendMessageToGroup(groupId, username + ": " + messageText);
        }
    }

    private void joinGroup(WebSocketSession session, String groupId) {
        session.getAttributes().put("group", groupId);
        groups.computeIfAbsent(groupId, k -> ConcurrentHashMap.newKeySet()).add(session);
        System.out.println("Пользователь " + sessions.get(session) + " присоединился к группе ID: " + groupId);
    }

    private void sendMessageToGroup(String groupId, String message) throws IOException {
        Set<WebSocketSession> groupSessions = groups.get(groupId);
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

