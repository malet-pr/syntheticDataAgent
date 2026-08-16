package org.acme.syntheticdata.config;

import com.google.cloud.vertexai.VertexAI;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Value("${spring.ai.google.genai.project-id}")
    private String projectId;

    @Value("${spring.ai.google.genai.location:us-central1}")
    private String location;

    @Bean
    public VertexAI vertexAI() {
        return new VertexAI(projectId, location);
    }

    @Bean
    public ChatModel chatModel(VertexAI vertexAI) {
        VertexAiGeminiChatOptions options = VertexAiGeminiChatOptions.builder()
                .model("gemini-2.5-flash")
                .build();

        return VertexAiGeminiChatModel.builder()
                .vertexAI(vertexAI)
                .defaultOptions(options)
                .build();
    }
}