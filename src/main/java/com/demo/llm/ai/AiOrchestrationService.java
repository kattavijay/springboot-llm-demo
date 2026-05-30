package com.demo.llm.ai;

import com.demo.llm.model.ChatMessage;
import com.demo.llm.rag.RagService;
import com.demo.llm.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI Orchestration Layer – the brain of the application.
 *
 * Implements the full flow from the architecture diagram:
 *   ① User request arrives
 *   ② Retrieve context from Vector DB (RAG)
 *   ③ Build prompt + context, send to LLM Provider
 *   ④ Receive LLM response
 *   ⑤ Persist exchange, return AI response
 *
 * Spring AI's ChatClient abstracts OpenAI / Azure OpenAI so you
 * can swap providers by changing application.properties alone.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiOrchestrationService {

    private final ChatClient chatClient;
    private final RagService ragService;
    private final PromptManager promptManager;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * Handles a single-turn question without session memory.
     */
    public String ask(String question) {
        String context = ragService.retrieveContext(question);
        String userContent = context.isBlank()
                ? question
                : promptManager.buildRagPrompt(context, question);

        log.debug("Sending prompt to LLM (RAG context: {})", context.isBlank() ? "none" : "yes");

        Prompt prompt = new Prompt(List.of(
                new SystemMessage(PromptManager.SYSTEM_PROMPT),
                new UserMessage(userContent)
        ));

        ChatResponse response = chatClient.call(prompt);
        return response.getResult().getOutput().getContent();
    }

    /**
     * Handles a multi-turn conversation with persistent memory.
     * Memory = full chat history loaded from DB for the session (step ② Memory in diagram).
     */
    public String chat(String sessionId, String userText) {
        // ── 1. RAG: retrieve context ──────────────────────────────────────────
        String context = ragService.retrieveContext(userText);
        String userContent = context.isBlank()
                ? userText
                : promptManager.buildRagPrompt(context, userText);

        // ── 2. Memory: load conversation history ──────────────────────────────
        List<ChatMessage> history = chatMessageRepository
                .findBySessionIdOrderByCreatedAtAsc(sessionId);

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(PromptManager.SYSTEM_PROMPT));

        for (ChatMessage msg : history) {
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }
        messages.add(new UserMessage(userContent));

        // ── 3. Call LLM (steps ③ + ④) ────────────────────────────────────────
        log.debug("chat[{}] → LLM | history={} msgs | RAG={}",
                sessionId, history.size(), context.isBlank() ? "no" : "yes");

        ChatResponse response = chatClient.call(new Prompt(messages));
        String assistantReply = response.getResult().getOutput().getContent();

        // ── 4. Persist exchange ───────────────────────────────────────────────
        chatMessageRepository.save(ChatMessage.builder()
                .sessionId(sessionId).role("user").content(userText).build());
        chatMessageRepository.save(ChatMessage.builder()
                .sessionId(sessionId).role("assistant").content(assistantReply).build());

        return assistantReply;
    }

    /** Clears the memory for a given session. */
    public void clearSession(String sessionId) {
        chatMessageRepository.deleteBySessionId(sessionId);
        log.info("Session '{}' cleared", sessionId);
    }
}
