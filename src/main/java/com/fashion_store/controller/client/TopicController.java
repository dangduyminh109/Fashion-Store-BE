package com.fashion_store.controller.client;

import com.fashion_store.dto.common.response.ApiResponse;
import com.fashion_store.dto.topic.response.TopicResponse;
import com.fashion_store.service.TopicService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/api/topic")
@RestController("ClientTopicController")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TopicController {
    TopicService topicService;

    @GetMapping
    public ApiResponse<List<TopicResponse>> getAll(
    ) {
        return ApiResponse.<List<TopicResponse>>builder()
                .result(topicService.getTopic())
                .build();
    }
}
