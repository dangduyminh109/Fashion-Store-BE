package com.fashion_store.controller.client;

import com.fashion_store.dto.category.response.CategoryFeaturedResponse;
import com.fashion_store.dto.category.response.CategoryTreeResponse;
import com.fashion_store.dto.common.response.ApiResponse;
import com.fashion_store.service.CategoryService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController("ClientCategoryController")
@AllArgsConstructor
@RequestMapping("/api/category")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {
    CategoryService categoryService;

    @GetMapping("/getTree")
    public ApiResponse<List<CategoryTreeResponse>> getTree(
            @RequestParam(value = "id", required = false) Long id
    ) {
        return ApiResponse.<List<CategoryTreeResponse>>builder()
                .result(categoryService.getTree(id))
                .build();
    }

    @GetMapping("/featured")
    public ApiResponse<List<CategoryFeaturedResponse>> getFeatured(
            @RequestParam(value = "quantity", defaultValue = "6") Integer quantity
    ) {
        return ApiResponse.<List<CategoryFeaturedResponse>>builder()
                .result(categoryService.getFeatured(quantity))
                .build();
    }
}
