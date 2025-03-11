package com.neslihan.messaging_service.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class KafkaMessageConsumer {

    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    public KafkaMessageConsumer(MessageService messageService, ObjectMapper objectMapper) {
        this.messageService = messageService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "chat-messages", groupId = "messaging-group")
    public void consumeMessage(String message) {
       // messages :  {"sender":"bob","receiver":"alice","text":"Hi."}
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String sender = jsonNode.get("sender").asText();
            String receiver = jsonNode.get("receiver").asText();
            String text = jsonNode.get("text").asText();

            messageService.saveMessage(text, sender, receiver);

        } catch (Exception e) {
            System.err.println("An error occurred while processing the message: " + e.getMessage());
        }
    }
}
