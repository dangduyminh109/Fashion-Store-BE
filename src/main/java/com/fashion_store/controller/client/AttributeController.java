package com.fashion_store.controller.client;

import com.fashion_store.dto.common.response.ApiResponse;
import com.fashion_store.dto.attribute.response.AttributeClientResponse;
import com.fashion_store.service.AttributeService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("ClientAttributeController")
@AllArgsConstructor
@RequestMapping("/api/attribute")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttributeController {
    AttributeService attributeService;

    @GetMapping
    public ApiResponse<List<AttributeClientResponse>> getAll(
    ) {
        return ApiResponse.<List<AttributeClientResponse>>builder()
                .result(attributeService.getAllAttribute())
                .build();
    }
}
