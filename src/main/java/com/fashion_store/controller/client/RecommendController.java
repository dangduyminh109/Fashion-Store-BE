package com.fashion_store.controller.client;

import com.fashion_store.dto.common.response.ApiResponse;
import com.fashion_store.dto.product.response.ProductResponse;
import com.fashion_store.service.RecommendService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/recommend")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecommendController {
    
    RecommendService recommendService;

    @GetMapping
    public ApiResponse<List<ProductResponse>> getRecommendations(@RequestParam("items") String items) {
        return ApiResponse.<List<ProductResponse>>builder()
                .message("Lấy danh sách gợi ý thành công")
                .result(recommendService.getRecommendations(items))
                .build();
    }
}
