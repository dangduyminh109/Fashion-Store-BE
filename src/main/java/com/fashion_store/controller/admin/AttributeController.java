package com.fashion_store.controller.admin;

import com.fashion_store.dto.attribute.request.AttributeRequest;
import com.fashion_store.dto.common.response.ApiResponse;
import com.fashion_store.dto.attribute.response.AttributeResponse;
import com.fashion_store.service.AttributeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/attribute")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttributeController {
    AttributeService attributeService;

    @GetMapping
    @PreAuthorize("hasAuthority('ATTRIBUTE_VIEW')")
    public ApiResponse<List<AttributeResponse>> getAll(
            @RequestParam(value = "deleted", required = false) boolean deleted
    ) {
        return ApiResponse.<List<AttributeResponse>>builder()
                .result(attributeService.getAll(deleted))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ATTRIBUTE_VIEW')")
    public ApiResponse<AttributeResponse> getInfo(@PathVariable Long id) {
        return ApiResponse.<AttributeResponse>builder()
                .result(attributeService.getInfo(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ATTRIBUTE_CREATE')")
    public ApiResponse<AttributeResponse> create(@ModelAttribute @Valid AttributeRequest request) {
        return ApiResponse.<AttributeResponse>builder()
                .message("Tạo thuộc tính thành công")
                .result(attributeService.create(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ATTRIBUTE_UPDATE')")
    public ApiResponse<AttributeResponse> update(@ModelAttribute @Valid AttributeRequest request, @PathVariable Long id) {
        return ApiResponse.<AttributeResponse>builder()
                .message("Cập nhật thuộc tính thành công")
                .result(attributeService.update(request, id))
                .build();
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('ATTRIBUTE_UPDATE')")
    public ApiResponse<Void> restore(@PathVariable Long id) {
        attributeService.restore(id);
        return ApiResponse.<Void>builder()
                .message("Khôi phục thuộc tính thành công")
                .build();
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('CATEGORY_UPDATE')")
    public ApiResponse<Void> status(@PathVariable Long id) {
        attributeService.status(id);
        return ApiResponse.<Void>builder()
                .message("Cập nhật trạng thái thành công")
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ATTRIBUTE_UPDATE')")
    public ApiResponse<AttributeResponse> delete(@PathVariable Long id) {
        attributeService.delete(id);
        return ApiResponse.<AttributeResponse>builder()
                .message("Xóa thuộc tính thành công")
                .build();
    }

    @DeleteMapping("/{id}/destroy")
    @PreAuthorize("hasAuthority('ATTRIBUTE_DELETE')")
    public ApiResponse<AttributeResponse> destroy(@PathVariable Long id) {
        attributeService.destroy(id);
        return ApiResponse.<AttributeResponse>builder()
                .message("Thuộc tính đã bị xóa vĩnh viễn")
                .build();
    }
}
