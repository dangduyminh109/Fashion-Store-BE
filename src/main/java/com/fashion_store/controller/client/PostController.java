package com.fashion_store.controller.client;

import com.fashion_store.dto.common.response.ApiResponse;
import com.fashion_store.dto.post.response.PostClientResponse;
import com.fashion_store.dto.post.response.PostFeaturedResponse;
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
    public ApiResponse<List<PostFeaturedResponse>> getFeatured(
            @RequestParam(value = "quantity", required = false, defaultValue = "4") Integer quantity
    ) {
        return ApiResponse.<List<PostFeaturedResponse>>builder()
                .result(postService.getFeatured(quantity))
                .build();
    }

    @GetMapping
    public ApiResponse<PostClientResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String topicIds
    ) {
        return ApiResponse.<PostClientResponse>builder()
                .result(postService.getPost(page, size, topicIds))
                .build();
    }

    @GetMapping("/{slug}")
    public ApiResponse<PostResponse> getInfo(@PathVariable String slug) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.getInfoBySlug(slug))
                .build();
    }
}
