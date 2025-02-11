package rk.chatApp.service;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final Map<WebSocketSession, String> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Получаем имя пользователя из атрибутов сессии
        String username = (String) session.getAttributes().get("username");
        if (username == null) {
            // Если имя пользователя не передано, закрываем соединение
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
        String formattedMessage = username + ": " + payload;

        System.out.println("Сообщение от " + username + ": " + payload);

        // массовая рассылка всем подключенышам
        for (WebSocketSession webSocketSession : sessions.keySet()) {
            if (webSocketSession.isOpen()) {
                webSocketSession.sendMessage(new TextMessage(formattedMessage));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String username = sessions.remove(session);
        System.out.println("Клиент отключился: " + session.getId() + " (пользователь " + username + ")");
    }
}