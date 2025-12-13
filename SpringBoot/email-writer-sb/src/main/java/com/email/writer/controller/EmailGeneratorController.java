package com.email.writer.controller;


import com.email.writer.model.EmailRequest;
import com.email.writer.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
@CrossOrigin(origins = "*")
public class EmailGeneratorController {
    private final EmailService emailService;

    public EmailGeneratorController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateResponse(@RequestBody EmailRequest requestEmail){
        String response = emailService.generateEmailReply(requestEmail);
        return ResponseEntity.ok(response);
    }
}
