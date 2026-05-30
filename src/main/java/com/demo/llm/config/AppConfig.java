package com.demo.llm.config;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cross-cutting: Security + Spring AI wiring.
 *
 * Security: Permits the demo endpoints and H2 console; in production add
 *   JWT / OAuth2 resource server configuration here.
 *
 * Spring AI: Wires the ChatClient bean.
 *   To switch to Azure OpenAI, swap OpenAiChatClient → AzureOpenAiChatClient
 *   and update application.properties — zero code changes elsewhere.
 */
@Configuration
@EnableWebSecurity
public class AppConfig {

    // ── Security ──────────────────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Monitoring & H2 console open for demo purposes
                .requestMatchers("/actuator/**", "/h2-console/**").permitAll()
                // All API endpoints open for demo; add .authenticated() in prod
                .requestMatchers("/api/**").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions().sameOrigin()); // for H2 console
        return http.build();
    }

    // ── Spring AI ChatClient ──────────────────────────────────────────────────

    /**
     * Provides the ChatClient bean.
     * Spring AI's auto-configuration also creates this if the starter is on
     * the classpath — keeping it explicit here for clarity and testability.
     */
    @Bean
    public ChatClient chatClient(@Value("${spring.ai.openai.api-key}") String apiKey) {
        OpenAiApi openAiApi = new OpenAiApi(apiKey);
        return new OpenAiChatClient(openAiApi);
    }
}
