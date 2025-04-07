package rk.chatApp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import rk.chatApp.model.Message;
import rk.chatApp.model.User;
import rk.chatApp.repository.MessageRepository;
import rk.chatApp.repository.UserRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final Map<WebSocketSession, String> sessions = new ConcurrentHashMap<>();
    private static final Map<String, Set<WebSocketSession>> groups = new ConcurrentHashMap<>();
    private static final Map<String, String> lastUsernames = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();


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

        if (payload.startsWith("/join ")) {
            String groupId = payload.substring(6).trim();
            joinGroup(session, groupId);
            return;
        }

        try {
            // Пытаемся парсить как JSON (для команд типа delete)
            JsonNode jsonNode = objectMapper.readTree(payload);
            if (jsonNode.has("type")) {
                handleJsonCommand(session, jsonNode);
                return;
            }
        } catch (IOException e) {
            // Не JSON, обрабатываем как обычное сообщение
        }

        // Обработка обычных сообщений в формате groupId:message
        String[] parts = payload.split(":", 2);
        if (parts.length == 2) {
            String groupId = parts[0];
            String messageText = parts[1];
            handleChatMessage(username, groupId, messageText);
        }
    }
    private void handleJsonCommand(WebSocketSession session, JsonNode jsonNode) throws IOException {
        String type = jsonNode.get("type").asText();
        switch (type) {
            case "delete":
                String groupId = jsonNode.get("groupId").asText();
                String messageId = jsonNode.get("messageId").asText();
                broadcastDeleteMessage(groupId, messageId);
                break;
            // Можно добавить обработку других команд
        }
    }

    private void handleChatMessage(String username, String groupId, String messageText) throws IOException {
        User user = userRepository.findByUsername(username);
        Message savedMessage = null;
        if (user != null) {
            savedMessage = groupService.saveMessage(Long.parseLong(groupId), user.getId(), messageText);
        }

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("groupId", groupId);
        messageData.put("username", username);
        messageData.put("content", messageText);
        messageData.put("timestamp", LocalDateTime.now().toString());
        if (savedMessage != null) {
            messageData.put("id", savedMessage.getId());
        }

        String lastUser = lastUsernames.get(groupId);
        messageData.put("showUsername", !username.equals(lastUser));
        lastUsernames.put(groupId, username);

        String jsonMessage = objectMapper.writeValueAsString(messageData);
        sendMessageToGroup(groupId, jsonMessage);
    }

    private void broadcastDeleteMessage(String groupId, String messageId) throws IOException {
        Map<String, Object> deleteMessage = new HashMap<>();
        deleteMessage.put("type", "delete");
        deleteMessage.put("messageId", messageId);
        deleteMessage.put("groupId", groupId);

        String jsonMessage = objectMapper.writeValueAsString(deleteMessage);
        sendMessageToGroup(groupId, jsonMessage);
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

