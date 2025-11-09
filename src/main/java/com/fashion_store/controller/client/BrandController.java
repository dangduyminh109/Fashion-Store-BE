package com.fashion_store.controller.client;

import com.fashion_store.dto.common.response.ApiResponse;
import com.fashion_store.dto.brand.response.BrandClientResponse;
import com.fashion_store.service.BrandService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("ClientBrandController")
@AllArgsConstructor
@RequestMapping("/api/brand")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BrandController {
    BrandService brandService;

    @GetMapping
    public ApiResponse<List<BrandClientResponse>> getAll(
    ) {
        return ApiResponse.<List<BrandClientResponse>>builder()
                .result(brandService.getAllBrand())
                .build();
    }
}
