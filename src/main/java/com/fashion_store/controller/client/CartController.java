package com.fashion_store.controller.client;

import com.fashion_store.dto.cart.request.CartItemRequest;
import com.fashion_store.dto.common.response.ApiResponse;
import com.fashion_store.dto.cart.response.CartItemResponse;
import com.fashion_store.service.CartService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/cart")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {
    CartService cartService;

    @GetMapping
    public ApiResponse<List<CartItemResponse>> getCart() {
        return ApiResponse.<List<CartItemResponse>>builder()
                .result(cartService.getCart())
                .build();
    }

    @PostMapping
    public ApiResponse<CartItemResponse> add(@RequestBody CartItemRequest cartItemRequest) {
        return ApiResponse.<CartItemResponse>builder()
                .result(cartService.add(cartItemRequest))
                .build();
    }

    @PutMapping
    public ApiResponse<CartItemResponse> update(@RequestBody CartItemRequest cartItemRequest) {
        return ApiResponse.<CartItemResponse>builder()
                .result(cartService.update(cartItemRequest))
                .build();
    }

    @DeleteMapping("/{variantId}")
    public ApiResponse<Void> delete(@PathVariable Long variantId) {
        cartService.delete(variantId);
        return ApiResponse.<Void>builder()
                .message("Xóa sản phẩm khỏi giỏ hàng thành công")
                .build();
    }
}
