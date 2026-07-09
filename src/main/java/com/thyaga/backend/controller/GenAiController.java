package com.thyaga.backend.controller;

import com.thyaga.backend.dto.GenAiChatRequest;
import com.thyaga.backend.dto.CreateMessageRequest;
import com.thyaga.backend.entity.MessageRole;
import com.thyaga.backend.service.MessageService;
import com.thyaga.backend.service.OpenAiService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/v1/genai")
public class GenAiController {

    private final OpenAiService openAiService;
    private final MessageService messageService;

    public GenAiController(OpenAiService openAiService, MessageService messageService) {
        this.openAiService = openAiService;
        this.messageService = messageService;
    }

    /**
     * Streams plain text chunks (not SSE) so the frontend can append progressively.
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_PLAIN_VALUE)
    public void chat(@Valid @RequestBody GenAiChatRequest request, HttpServletResponse response) throws IOException {
        log.info("POST /api/v1/genai/chat - messages={}", request.messages().size());

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);

        PrintWriter writer = response.getWriter();
        try {
            persistLatestUserMessage(request);
            String assistantContent = openAiServiceStream(request, writer);
            persistAssistantMessage(request, assistantContent);
        } catch (Exception ex) {
            log.error("GenAI chat failed", ex);

            if (response.isCommitted()) {
                writer.write("\n\n[GenAI chat failed]");
                writer.flush();
                return;
            }

            response.resetBuffer();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            writer.write("{\"statusCode\":500,\"message\":\"GenAI chat failed\"}");
            writer.flush();
        }
    }

    private String openAiServiceStream(GenAiChatRequest request, PrintWriter writer) throws IOException, InterruptedException {
        StringBuilder assistantContent = new StringBuilder();
        openAiService.streamChat(request, delta -> {
            assistantContent.append(delta);
            writer.write(delta);
            writer.flush();
        });
        return assistantContent.toString();
    }

    private void persistLatestUserMessage(GenAiChatRequest request) {
        if (request.conversationId() == null || request.messages().isEmpty()) {
            return;
        }

        GenAiChatRequest.GenAiMessage latestMessage = request.messages().get(request.messages().size() - 1);
        if (!"user".equalsIgnoreCase(latestMessage.role())) {
            return;
        }

        messageService.createMessage(new CreateMessageRequest(
                request.conversationId(),
                MessageRole.USER,
                latestMessage.content(),
                latestMessage.imageData(),
                latestMessage.documentName(),
                latestMessage.documentText()
        ));
    }

    private void persistAssistantMessage(GenAiChatRequest request, String assistantContent) {
        if (request.conversationId() == null || assistantContent == null || assistantContent.isBlank()) {
            return;
        }

        try {
            messageService.createMessage(new CreateMessageRequest(
                    request.conversationId(),
                    MessageRole.ASSISTANT,
                    assistantContent.trim(),
                    null,
                    null,
                    null
            ));
        } catch (Exception ex) {
            log.error("Failed to persist assistant message, conversationId={}", request.conversationId(), ex);
        }
    }
}
