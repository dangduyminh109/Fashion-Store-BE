package com.fashion_store.controller.client;

import com.fashion_store.dto.common.response.ApiResponse;
import com.fashion_store.dto.product.response.ProductClientResponse;
import com.fashion_store.dto.product.response.ProductFeaturedResponse;
import com.fashion_store.dto.product.response.ProductFromCategoryResponse;
import com.fashion_store.dto.product.response.ProductResponse;
import com.fashion_store.service.ProductService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/api/product")
@RestController("ClientProductController")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductService productService;

    @GetMapping
    public ApiResponse<ProductClientResponse> getAll(
            @RequestParam(required = false) String categoryIds,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean promotion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ApiResponse.<ProductClientResponse>builder()
                .result(productService.getProduct(page, size, categoryIds, promotion, search))
                .build();
    }

    @GetMapping("/featured")
    public ApiResponse<List<ProductFeaturedResponse>> getFeatured(
            @RequestParam(value = "quantity", required = false, defaultValue = "4") Integer quantity
    ) {
        return ApiResponse.<List<ProductFeaturedResponse>>builder()
                .result(productService.getFeatured(quantity))
                .build();
    }

    @GetMapping("/suggest/{id}")
    public ApiResponse<List<ProductFeaturedResponse>> suggest(
            @PathVariable Long id,
            @RequestParam(value = "quantity", required = false, defaultValue = "5") Integer quantity
    ) {
        return ApiResponse.<List<ProductFeaturedResponse>>builder()
                .result(productService.suggest(id, quantity))
                .build();
    }

    @GetMapping("/category/{slug}")
    public ApiResponse<ProductFromCategoryResponse> getByCategory(@PathVariable String slug
    ) {
        return ApiResponse.<ProductFromCategoryResponse>builder()
                .result(productService.getByCategory(slug))
                .build();
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<ProductResponse> getInfoBySlug(@PathVariable String slug) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getInfoBySlug(slug))
                .build();
    }

    @GetMapping("/variant/{id}")
    public ApiResponse<ProductResponse> getVariant(@PathVariable Long id) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getVariant(id))
                .build();
    }

    @GetMapping("/new")
    public ApiResponse<List<ProductResponse>> getNew(
            @RequestParam(value = "quantity", required = false, defaultValue = "4") Integer quantity
    ) {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getNew(quantity))
                .build();
    }

    @GetMapping("/sale")
    public ApiResponse<List<ProductFeaturedResponse>> getSale(
            @RequestParam(value = "quantity", required = false, defaultValue = "4") Integer quantity
    ) {
        return ApiResponse.<List<ProductFeaturedResponse>>builder()
                .result(productService.getSale(quantity))
                .build();
    }
}
