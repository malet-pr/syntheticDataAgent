package org.acme.syntheticdata.config;

import com.google.genai.Client;
import com.google.genai.types.AutomaticFunctionCallingConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenAiClientConfig {

    @Bean
    public Client googleGenAiClient() {
        return Client.builder()
                .project("fiery-glass-428422-j6")
                .location("global")
                .vertexAI(true)
                .build();
    }


}
