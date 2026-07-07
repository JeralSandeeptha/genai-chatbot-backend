package com.thyaga.backend.service;

import com.thyaga.backend.dto.CreateMessageRequest;
import com.thyaga.backend.dto.MessageResponse;
import com.thyaga.backend.dto.UpdateMessageRequest;
import com.thyaga.backend.entity.Conversation;
import com.thyaga.backend.entity.Message;
import com.thyaga.backend.exceptions.NotFoundException;
import com.thyaga.backend.repository.ConversationRepository;
import com.thyaga.backend.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    public MessageService(
            MessageRepository messageRepository,
            ConversationRepository conversationRepository
    ) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
    }

    public MessageResponse createMessage(CreateMessageRequest request) {
        Conversation conversation = conversationRepository.findById(request.conversationId())
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        Message message = new Message();
        message.setRole(request.role());
        message.setContent(request.content());
        message.setImageData(request.imageData());
        message.setConversation(conversation);

        return MessageResponse.from(messageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getAllMessages() {
        return messageRepository.findAll().stream()
                .map(MessageResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesByConversationId(UUID conversationId) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new NotFoundException("Conversation not found");
        }

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(MessageResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public MessageResponse getMessageById(UUID id) {
        return messageRepository.findById(id)
                .map(MessageResponse::from)
                .orElseThrow(() -> new NotFoundException("Message not found"));
    }

    public MessageResponse updateMessage(UUID id, UpdateMessageRequest request) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        if (request.content() != null) {
            message.setContent(request.content());
        }

        if (request.imageData() != null) {
            message.setImageData(request.imageData());
        }

        return MessageResponse.from(messageRepository.save(message));
    }

    public void deleteMessage(UUID id) {
        if (!messageRepository.existsById(id)) {
            throw new NotFoundException("Message not found");
        }
        messageRepository.deleteById(id);
    }
}
