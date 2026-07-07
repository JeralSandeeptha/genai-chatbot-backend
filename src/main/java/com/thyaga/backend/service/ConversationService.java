package com.thyaga.backend.service;

import com.thyaga.backend.dto.ConversationResponse;
import com.thyaga.backend.dto.CreateConversationRequest;
import com.thyaga.backend.dto.UpdateConversationRequest;
import com.thyaga.backend.entity.Conversation;
import com.thyaga.backend.entity.User;
import com.thyaga.backend.exceptions.NotFoundException;
import com.thyaga.backend.repository.ConversationRepository;
import com.thyaga.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public ConversationService(
            ConversationRepository conversationRepository,
            UserRepository userRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    public ConversationResponse createConversation(CreateConversationRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Conversation conversation = new Conversation();
        conversation.setTitle(request.title());
        conversation.setUser(user);

        return ConversationResponse.from(conversationRepository.save(conversation));
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getAllConversations() {
        return conversationRepository.findAll().stream()
                .map(ConversationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversationsByUserId(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(ConversationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversationById(UUID id) {
        return conversationRepository.findById(id)
                .map(ConversationResponse::from)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));
    }

    public ConversationResponse updateConversation(UUID id, UpdateConversationRequest request) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        conversation.setTitle(request.title());

        return ConversationResponse.from(conversationRepository.save(conversation));
    }

    public void deleteConversation(UUID id) {
        if (!conversationRepository.existsById(id)) {
            throw new NotFoundException("Conversation not found");
        }
        conversationRepository.deleteById(id);
    }
}
