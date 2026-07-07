package com.thyaga.backend.controller;

import com.thyaga.backend.dto.CreateMessageRequest;
import com.thyaga.backend.dto.MessageResponse;
import com.thyaga.backend.dto.UpdateMessageRequest;
import com.thyaga.backend.service.MessageService;
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
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<Object> createMessage(@Valid @RequestBody CreateMessageRequest request) {
        log.info("POST /api/v1/messages - creating message for conversationId={}, role={}",
                request.conversationId(), request.role());
        try {
            MessageResponse message = messageService.createMessage(request);

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", message);
            responseBody.put("statusCode", HttpStatus.CREATED.value());
            responseBody.put("message", "Create message query was successful");

            log.info("Create message query was successful, messageId={}", message.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        } catch (Exception ex) {
            log.error("Create message query internal server error, conversationId={}",
                    request.conversationId(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Create message query internal server error",
                            "error", ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Object> getAllMessages() {
        log.info("GET /api/v1/messages - fetching all messages");
        try {
            List<MessageResponse> messageList = messageService.getAllMessages();

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", messageList);
            responseBody.put("statusCode", HttpStatus.OK.value());
            responseBody.put("message", "Get all messages query was successful");

            log.info("Get all messages query was successful, count={}", messageList.size());
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception ex) {
            log.error("Get all messages query internal server error", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Get all messages query internal server error",
                            "error", ex.getMessage()));
        }
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<Object> getMessagesByConversationId(@PathVariable UUID conversationId) {
        log.info("GET /api/v1/messages/conversation/{} - fetching messages by conversation", conversationId);
        try {
            List<MessageResponse> messageList = messageService.getMessagesByConversationId(conversationId);

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", messageList);
            responseBody.put("statusCode", HttpStatus.OK.value());
            responseBody.put("message", "Get messages by conversation query was successful");

            log.info("Get messages by conversation query was successful, conversationId={}, count={}",
                    conversationId, messageList.size());
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception ex) {
            log.error("Get messages by conversation query internal server error, conversationId={}",
                    conversationId, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Get messages by conversation query internal server error",
                            "error", ex.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getMessageById(@PathVariable UUID id) {
        log.info("GET /api/v1/messages/{} - fetching message", id);
        try {
            MessageResponse message = messageService.getMessageById(id);

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", message);
            responseBody.put("statusCode", HttpStatus.OK.value());
            responseBody.put("message", "Get message query was successful");

            log.info("Get message query was successful, messageId={}", id);
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception ex) {
            log.error("Get message query internal server error, messageId={}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Get message query internal server error",
                            "error", ex.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateMessage(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMessageRequest request
    ) {
        log.info("PATCH /api/v1/messages/{} - updating message", id);
        try {
            MessageResponse message = messageService.updateMessage(id, request);

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", message);
            responseBody.put("statusCode", HttpStatus.OK.value());
            responseBody.put("message", "Update message query was successful");

            log.info("Update message query was successful, messageId={}", id);
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception ex) {
            log.error("Update message query internal server error, messageId={}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Update message query internal server error",
                            "error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteMessage(@PathVariable UUID id) {
        log.info("DELETE /api/v1/messages/{} - deleting message", id);
        try {
            messageService.deleteMessage(id);

            HashMap<String, Object> responseBody = new HashMap<>();
            responseBody.put("data", null);
            responseBody.put("statusCode", HttpStatus.OK.value());
            responseBody.put("message", "Delete message query was successful");

            log.info("Delete message query was successful, messageId={}", id);
            return ResponseEntity.ok().body(responseBody);
        } catch (Exception ex) {
            log.error("Delete message query internal server error, messageId={}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "message", "Delete message query internal server error",
                            "error", ex.getMessage()));
        }
    }
}
