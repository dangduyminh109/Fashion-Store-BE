package com.fashion_store.controller.client;

import com.fashion_store.dto.common.response.ApiResponse;
import com.fashion_store.dto.post.response.PostClientResponse;
import com.fashion_store.dto.post.response.PostResponse;
import com.fashion_store.service.PostService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("ClientPostController")
@AllArgsConstructor
@RequestMapping("/api/post")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {
    PostService postService;

    @GetMapping("/featured")
    public ApiResponse<List<PostClientResponse>> getFeatured(
            @RequestParam(value = "quantity", required = false, defaultValue = "4") Integer quantity
    ) {
        return ApiResponse.<List<PostClientResponse>>builder()
                .result(postService.getFeatured(quantity))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PostResponse> getInfo(@PathVariable Long id) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.getInfo(id))
                .build();
    }
}
