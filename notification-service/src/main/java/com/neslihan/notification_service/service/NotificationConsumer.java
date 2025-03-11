package com.neslihan.notification_service.service;

import com.neslihan.notification_service.ws.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {
    private final WebSocketHandler webSocketHandler;
    @Autowired
    public NotificationConsumer(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @KafkaListener(topics = "chat-messages", groupId = "notification-group")
    public void listen(String message) {
        webSocketHandler.sendMessageToClients(message);
    }
}
