package com.fashion_store.controller.admin;

import com.fashion_store.dto.role.request.RoleRequest;
import com.fashion_store.dto.role.request.UpdateRolePermissionsRequest;
import com.fashion_store.dto.common.response.ApiResponse;
import com.fashion_store.dto.role.resonse.PermissionResponse;
import com.fashion_store.dto.role.resonse.RoleResponse;
import com.fashion_store.dto.supplier.response.SupplierResponse;
import com.fashion_store.service.RoleService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/role")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {
    RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ApiResponse<List<RoleResponse>> getAll(
            @RequestParam(value = "deleted", required = false) boolean deleted,
            @RequestParam(value = "status", required = false) Boolean status
    ) {
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll(deleted, status))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ApiResponse<RoleResponse> getInfo(@PathVariable Long id) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.getInfo(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ApiResponse<RoleResponse> create(@RequestBody @Valid RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .message("Tạo vai trò thành công")
                .result(roleService.create(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ApiResponse<RoleResponse> update(@RequestBody @Valid RoleRequest request, @PathVariable Long id) {
        return ApiResponse.<RoleResponse>builder()
                .message("Cập nhật vai trò thành công")
                .result(roleService.update(request, id))
                .build();
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ApiResponse<Void> restore(@PathVariable Long id) {
        roleService.restore(id);
        return ApiResponse.<Void>builder()
                .message("Khôi phục vai trò thành công")
                .build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ApiResponse<Void> status(@PathVariable Long id) {
        roleService.status(id);
        return ApiResponse.<Void>builder()
                .message("Cập nhật trạng thái thành công")
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ApiResponse<SupplierResponse> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.<SupplierResponse>builder()
                .message("Xóa vai trò thành công")
                .build();
    }

    @DeleteMapping("/{id}/destroy")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ApiResponse<RoleResponse> destroy(@PathVariable Long id) {
        roleService.destroy(id);
        return ApiResponse.<RoleResponse>builder()
                .message("Vai trò đã bị xóa vĩnh viễn")
                .build();
    }

    @GetMapping("/permission")
    @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")
    // PERMISSION_UPDATE (có quền cập nhật thì mới get ra được danh sách)
    public ApiResponse<List<PermissionResponse>> getPermission() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .result(roleService.getPermission())
                .build();
    }

    @PutMapping("/permission")
    @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")
    public ApiResponse<Void> updatePermissionsForRoles(@RequestBody @Valid UpdateRolePermissionsRequest request) {
        roleService.updatePermissionsForRoles(request);
        return ApiResponse.<Void>builder()
                .message("Cập nhật quyền cho vai trò thành công")
                .build();
    }
}
