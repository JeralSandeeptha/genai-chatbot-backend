package com.thyaga.backend.controller;

import com.thyaga.backend.dto.ExtractTextResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    @PostMapping(value = "/extract-text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> extractText(@RequestPart("file") @NotNull MultipartFile file) {
        log.info("POST /api/v1/documents/extract-text - name={}, size={}", file.getOriginalFilename(), file.getSize());

        try {
            String filename = file.getOriginalFilename() == null ? "document" : file.getOriginalFilename();
            String contentType = file.getContentType() == null ? "" : file.getContentType();
            String lower = filename.toLowerCase();

            String text;
            if (contentType.contains("pdf") || lower.endsWith(".pdf")) {
                try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    text = stripper.getText(doc);
                }
            } else {
                // best-effort plain text (txt/md/json/csv/etc)
                text = new String(file.getBytes(), StandardCharsets.UTF_8);
            }

            return ResponseEntity.ok(Map.of(
                    "data", new ExtractTextResponse(filename, text == null ? "" : text),
                    "statusCode", HttpStatus.OK.value(),
                    "message", "Extract text query was successful"
            ));
        } catch (Exception ex) {
            log.error("Extract text failed", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "statusCode", HttpStatus.BAD_REQUEST.value(),
                    "message", "Extract text query failed",
                    "error", ex.getMessage()
            ));
        }
    }
}

