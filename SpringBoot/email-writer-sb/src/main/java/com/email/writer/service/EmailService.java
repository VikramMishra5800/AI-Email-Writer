package com.email.writer.service;

import com.email.writer.model.EmailRequest;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class EmailService {

    private final Client client;

    // Spring automatically injects the 'Client' bean we created in Step 3
    public EmailService(Client client) {
        this.client = client;
    }

    public String generateEmailReply(EmailRequest emailRequest){
        //build prompt
        String prompt = buildPrompt(emailRequest);

        try {
            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.5-flash",
                    prompt,
                    null
            );

            return response.text();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }

    }

    public String buildPrompt(EmailRequest emailRequest){
        StringBuilder prompt = new StringBuilder();

        prompt.append("Please generate a professional email reply for the following email content. Please don't include the subject line. ");

        if(StringUtils.isNotBlank(emailRequest.getTone())){
            prompt.append("Use a " + emailRequest.getTone() + " tone");
        }

        prompt.append("\nOriginal Email\n").append(emailRequest.getEmailContent());

        return prompt.toString();
    }
}
