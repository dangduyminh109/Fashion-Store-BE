package com.fashion_store.controller.admin;

import com.fashion_store.dto.topic.request.UserCreateRequest;
import com.fashion_store.dto.topic.request.UserUpdateRequest;
import com.fashion_store.dto.common.response.ApiResponse;
import com.fashion_store.dto.user.response.UserResponse;
import com.fashion_store.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/user")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ApiResponse<List<UserResponse>> getAll(
            @RequestParam(value = "deleted", required = false) boolean deleted
    ) {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAll(deleted))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ApiResponse<UserResponse> getInfo(@PathVariable String id) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getInfo(id))
                .build();
    }

    @GetMapping("/myInfo")
    public ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ApiResponse<UserResponse> create(@ModelAttribute @Valid UserCreateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .message("Tạo tài khoản thành công")
                .result(userService.create(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ApiResponse<UserResponse> update(@ModelAttribute @Valid UserUpdateRequest request, @PathVariable String id) {
        return ApiResponse.<UserResponse>builder()
                .message("Cập nhật tài khoản thành công")
                .result(userService.update(request, id))
                .build();
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ApiResponse<Void> restore(@PathVariable String id) {
        userService.restore(id);
        return ApiResponse.<Void>builder()
                .message("Khôi phục tài khoản thành công")
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ApiResponse<Void> status(@PathVariable String id) {
        userService.status(id);
        return ApiResponse.<Void>builder()
                .message("Cập nhật trạng thái thành công")
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ApiResponse<UserResponse> delete(@PathVariable String id) {
        userService.delete(id);
        return ApiResponse.<UserResponse>builder()
                .message("Xóa tài khoản thành công")
                .build();
    }

    @DeleteMapping("/{id}/destroy")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ApiResponse<UserResponse> destroy(@PathVariable String id) {
        userService.destroy(id);
        return ApiResponse.<UserResponse>builder()
                .message("Tài khoản đã bị xóa vĩnh viễn")
                .build();
    }
}
