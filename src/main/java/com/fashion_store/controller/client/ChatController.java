package com.fashion_store.controller.client;

import com.fashion_store.dto.chat.request.ChatRequest;
import com.fashion_store.dto.chat.request.ConversationHistory;
import com.fashion_store.dto.chat.response.ChatResponse;
import com.fashion_store.dto.common.response.ApiResponse;
import com.fashion_store.service.ChatService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/chat")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {
    ChatService chatService;

    @PostMapping()
    public ApiResponse<ChatResponse> chat(
            @RequestBody @Valid ChatRequest chatRequest
    ) {
        return ApiResponse.<ChatResponse>builder()
                .result(chatService.handleMessage(chatRequest))
                .build();
    }

    @GetMapping("/history")
    public ApiResponse<List<ConversationHistory>> getHistory() {
        return ApiResponse.<List<ConversationHistory>>builder()
                .result(chatService.getHistory())
                .build();
    }

}
