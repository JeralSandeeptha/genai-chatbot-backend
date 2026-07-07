package com.thyaga.backend.controller;

import com.thyaga.backend.dto.ConversationResponse;
import com.thyaga.backend.dto.CreateConversationRequest;
import com.thyaga.backend.dto.UpdateConversationRequest;
import com.thyaga.backend.service.ConversationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ResponseEntity<Object> createConversation(@Valid @RequestBody CreateConversationRequest request) {
        log.info("POST /api/v1/conversations - creating conversation for userId={}, title={}",
                request.userId(), request.title());
        try {
            ConversationResponse conversation = conversationService.createConversation(request);

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", conversation);
            responseBody.put("statusCode", HttpStatus.CREATED.value());
            responseBody.put("message", "Create conversation query was successful");

            log.info("Create conversation query was successful, conversationId={}", conversation.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        } catch (Exception ex) {
            log.error("Create conversation query internal server error, userId={}", request.userId(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Create conversation query internal server error",
                            "error", ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Object> getAllConversations() {
        log.info("GET /api/v1/conversations - fetching all conversations");
        try {
            List<ConversationResponse> conversationList = conversationService.getAllConversations();

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", conversationList);
            responseBody.put("statusCode", HttpStatus.OK.value());
            responseBody.put("message", "Get all conversations query was successful");

            log.info("Get all conversations query was successful, count={}", conversationList.size());
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception ex) {
            log.error("Get all conversations query internal server error", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Get all conversations query internal server error",
                            "error", ex.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Object> getConversationsByUserId(@PathVariable UUID userId) {
        log.info("GET /api/v1/conversations/user/{} - fetching conversations by user", userId);
        try {
            List<ConversationResponse> conversationList = conversationService.getConversationsByUserId(userId);

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", conversationList);
            responseBody.put("statusCode", HttpStatus.OK.value());
            responseBody.put("message", "Get conversations by user query was successful");

            log.info("Get conversations by user query was successful, userId={}, count={}",
                    userId, conversationList.size());
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception ex) {
            log.error("Get conversations by user query internal server error, userId={}", userId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Get conversations by user query internal server error",
                            "error", ex.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getConversationById(@PathVariable UUID id) {
        log.info("GET /api/v1/conversations/{} - fetching conversation", id);
        try {
            ConversationResponse conversation = conversationService.getConversationById(id);

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", conversation);
            responseBody.put("statusCode", HttpStatus.OK.value());
            responseBody.put("message", "Get conversation query was successful");

            log.info("Get conversation query was successful, conversationId={}", id);
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception ex) {
            log.error("Get conversation query internal server error, conversationId={}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Get conversation query internal server error",
                            "error", ex.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateConversation(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateConversationRequest request
    ) {
        log.info("PATCH /api/v1/conversations/{} - updating conversation", id);
        try {
            ConversationResponse conversation = conversationService.updateConversation(id, request);

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", conversation);
            responseBody.put("statusCode", HttpStatus.OK.value());
            responseBody.put("message", "Update conversation query was successful");

            log.info("Update conversation query was successful, conversationId={}", id);
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception ex) {
            log.error("Update conversation query internal server error, conversationId={}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Update conversation query internal server error",
                            "error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteConversation(@PathVariable UUID id) {
        log.info("DELETE /api/v1/conversations/{} - deleting conversation", id);
        try {
            conversationService.deleteConversation(id);

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", null);
            responseBody.put("statusCode", HttpStatus.OK.value());
            responseBody.put("message", "Delete conversation query was successful");

            log.info("Delete conversation query was successful, conversationId={}", id);
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception ex) {
            log.error("Delete conversation query internal server error, conversationId={}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Delete conversation query internal server error",
                            "error", ex.getMessage()));
        }
    }
}
