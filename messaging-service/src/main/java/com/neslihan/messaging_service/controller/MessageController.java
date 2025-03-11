package com.neslihan.messaging_service.controller;

import com.neslihan.messaging_service.model.MessageEntity;
import com.neslihan.messaging_service.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ResponseEntity<List<MessageEntity>> getMessages() {
        List<MessageEntity> messages = messageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/chat-partners/{user}")
    public List<String> getChatPartners(@PathVariable String user) {
        return messageService.getChatPartners(user);
    }

    @GetMapping("/chat/{user1}/{user2}")
    public List<MessageEntity> getChatMessages(@PathVariable String user1, @PathVariable String user2) {
        return messageService.getChatMessages(user1, user2);
    }

    @GetMapping("/last-message/{username}/{partner}")
    public MessageEntity getLastMessage(@PathVariable String username, @PathVariable String partner) {
        List<MessageEntity> messages = messageService.getChatMessages(username, partner);
        return messages.get(messages.size() - 1);
    }

}
