package com.fashion_store.controller.admin;

import com.fashion_store.dto.voucher.request.VoucherRequest;
import com.fashion_store.dto.voucher.request.VoucherUpdateRequest;
import com.fashion_store.dto.common.response.ApiResponse;
import com.fashion_store.dto.voucher.response.VoucherResponse;
import com.fashion_store.service.VoucherService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/voucher")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VoucherController {
    VoucherService voucherService;

    @GetMapping
    @PreAuthorize("hasAuthority('VOUCHER_VIEW')")
    public ApiResponse<List<VoucherResponse>> getAll(
            @RequestParam(value = "deleted", required = false) boolean deleted,
            @RequestParam(value = "search", required = false) String search
    ) {
        return ApiResponse.<List<VoucherResponse>>builder()
                .result(voucherService.getAll(deleted, search))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VOUCHER_VIEW')")
    public ApiResponse<VoucherResponse> getInfo(@PathVariable Long id) {
        return ApiResponse.<VoucherResponse>builder()
                .result(voucherService.getInfo(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VOUCHER_CREATE')")
    public ApiResponse<VoucherResponse> create(@RequestBody @Valid VoucherRequest request) {
        return ApiResponse.<VoucherResponse>builder()
                .message("Tạo voucher thành công")
                .result(voucherService.create(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VOUCHER_UPDATE')")
    public ApiResponse<VoucherResponse> update(@RequestBody @Valid VoucherUpdateRequest request, @PathVariable Long id) {
        return ApiResponse.<VoucherResponse>builder()
                .message("Cập nhật voucher thành công")
                .result(voucherService.update(request, id))
                .build();
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('VOUCHER_UPDATE')")
    public ApiResponse<Void> restore(@PathVariable Long id) {
        voucherService.restore(id);
        return ApiResponse.<Void>builder()
                .message("Khôi phục voucher thành công")
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('VOUCHER_UPDATE')")
    public ApiResponse<Void> status(@PathVariable Long id) {
        voucherService.status(id);
        return ApiResponse.<Void>builder()
                .message("Cập nhật trạng thái thành công")
                .build();
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VOUCHER_DELETE')")
    public ApiResponse<VoucherResponse> delete(@PathVariable Long id) {
        voucherService.delete(id);
        return ApiResponse.<VoucherResponse>builder()
                .message("Xóa voucher thành công")
                .build();
    }

    @DeleteMapping("/{id}/destroy")
    @PreAuthorize("hasAuthority('VOUCHER_DELETE')")
    public ApiResponse<VoucherResponse> destroy(@PathVariable Long id) {
        voucherService.destroy(id);
        return ApiResponse.<VoucherResponse>builder()
                .message("Voucher đã bị xóa vĩnh viễn")
                .build();
    }
}
