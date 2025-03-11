package com.neslihan.messaging_service.service;

import com.neslihan.messaging_service.model.MessageEntity;
import com.neslihan.messaging_service.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public MessageEntity saveMessage(String text, String sender, String receiver) {
        MessageEntity message = new MessageEntity(text, LocalDateTime.now(), sender, receiver);
        return messageRepository.save(message);
    }

    public List<MessageEntity> getAllMessages() {
        return messageRepository.findAll();
    }

    public List<String> getChatPartners(String user) {
        List<String> partners = messageRepository.findDistinctChatPartners(user);
        System.out.println(partners);
        return partners;
    }

    public List<MessageEntity> getChatMessages(String user1, String user2) {
        List<MessageEntity> messages = messageRepository.findBySenderAndReceiverOrReceiverAndSenderOrderByTimestampAsc(user1, user2, user1, user2);
        return messages;
    }
}
