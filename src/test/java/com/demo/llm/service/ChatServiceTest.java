package com.demo.llm.service;

import com.demo.llm.ai.AiOrchestrationService;
import com.demo.llm.rag.RagService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock AiOrchestrationService aiOrchestrationService;
    @Mock RagService ragService;

    @InjectMocks ChatService chatService;

    @Test
    void askQuestion_returnsAnswerMap() {
        when(aiOrchestrationService.ask("What is Spring AI?"))
                .thenReturn("Spring AI is an abstraction layer for LLMs.");

        Map<String, String> result = chatService.askQuestion("What is Spring AI?");

        assertThat(result).containsEntry("answer", "Spring AI is an abstraction layer for LLMs.");
        assertThat(result).containsEntry("question", "What is Spring AI?");
    }

    @Test
    void askQuestion_blankInput_throwsException() {
        assertThatThrownBy(() -> chatService.askQuestion("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void askQuestion_tooLong_throwsException() {
        String longInput = "x".repeat(4001);
        assertThatThrownBy(() -> chatService.askQuestion(longInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("4000");
    }

    @Test
    void sendMessage_blankSessionId_throwsException() {
        assertThatThrownBy(() -> chatService.sendMessage("", "Hello"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sessionId");
    }

    @Test
    void sendMessage_returnsReply() {
        when(aiOrchestrationService.chat("sess-1", "Hello"))
                .thenReturn("Hi there!");

        Map<String, String> result = chatService.sendMessage("sess-1", "Hello");

        assertThat(result).containsEntry("reply", "Hi there!");
        assertThat(result).containsEntry("sessionId", "sess-1");
    }
}
