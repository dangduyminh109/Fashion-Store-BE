package com.fashion_store.service;

import com.fashion_store.Utils.SecurityUtils;
import com.fashion_store.dto.role.request.RoleRequest;
import com.fashion_store.dto.role.request.UpdateRolePermissionsRequest;
import com.fashion_store.dto.role.resonse.PermissionResponse;
import com.fashion_store.dto.role.resonse.RoleResponse;
import com.fashion_store.entity.Permission;
import com.fashion_store.entity.Role;
import com.fashion_store.entity.User;
import com.fashion_store.exception.AppException;
import com.fashion_store.exception.ErrorCode;
import com.fashion_store.mapper.PermissionMapper;
import com.fashion_store.mapper.RoleMapper;
import com.fashion_store.repository.PermissionRepository;
import com.fashion_store.repository.RoleRepository;
import com.fashion_store.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    RoleMapper roleMapper;
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;
    UserRepository userRepository;

    public RoleResponse create(RoleRequest request) {
        if (roleRepository.existsByName(request.getName()))
            throw new AppException(ErrorCode.EXISTED);
        Role role = roleMapper.toRole(request);

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public List<RoleResponse> getAll(boolean deleted, Boolean status) {
        return roleRepository.findAll()
                .stream()
                .filter(item -> item.getIsDeleted() == deleted)
                .filter(item -> status == null || item.getStatus().equals(status))
                .map(roleMapper::toRoleResponse)
                .collect(Collectors.toList());
    }

    public List<PermissionResponse> getPermission() {
        return permissionRepository.findAll()
                .stream()
                .map(permissionMapper::toPermissionResponse)
                .collect(Collectors.toList());
    }

    public RoleResponse getInfo(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        return roleMapper.toRoleResponse(role);
    }

    public RoleResponse update(RoleRequest request, Long id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        if (role.getName().equals("ADMIN"))
            throw new AppException(ErrorCode.CANNOT_BE_UPDATE);
        if (roleRepository.existsByNameAndIdNot(request.getName(), id))
            throw new AppException(ErrorCode.EXISTED);
        roleMapper.updateRole(role, request);
        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public void updatePermissionsForRoles(UpdateRolePermissionsRequest updates) {
        if (!updates.getUpdates().isEmpty()) {
            updates.getUpdates().forEach(item -> {
                Role role = roleRepository.findById(item.getId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
                if (!role.getName().equals("ADMIN") && role.getStatus() && !role.getIsDeleted()) {
                    role.getPermissions().clear();
                    Set<Permission> newPermission = new HashSet<>(permissionRepository.findAllById(item.getPermissionIds()));
                    role.getPermissions().addAll(newPermission);
                    roleRepository.save(role);
                }
            });
        }
    }

    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        if (role.getName().equals("ADMIN"))
            throw new AppException(ErrorCode.CANNOT_BE_UPDATE);
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            throw new AppException(ErrorCode.UNAUTHORIZED);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        if (!user.getRole().getName().equals("ADMIN"))
            throw new AppException(ErrorCode.ROLE_DELETE_NOT_ALLOWED);
        
        try {
            role.setIsDeleted(true);
            roleRepository.save(role);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }
    }

    public void restore(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        if (role.getName().equals("ADMIN"))
            throw new AppException(ErrorCode.CANNOT_BE_UPDATE);

        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            throw new AppException(ErrorCode.UNAUTHORIZED);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        if (!user.getRole().getName().equals("ADMIN"))
            throw new AppException(ErrorCode.ROLE_DELETE_NOT_ALLOWED);

        try {
            role.setIsDeleted(false);
            roleRepository.save(role);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }
    }

    public void destroy(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        if (role.getName().equals("ADMIN"))
            throw new AppException(ErrorCode.CANNOT_BE_DELETE);

        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            throw new AppException(ErrorCode.UNAUTHORIZED);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        if (!user.getRole().getName().equals("ADMIN"))
            throw new AppException(ErrorCode.ROLE_DELETE_NOT_ALLOWED);

        try {
            roleRepository.deleteById(id);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }
    }

    public void status(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        if (role.getName().equals("ADMIN"))
            throw new AppException(ErrorCode.CANNOT_BE_UPDATE);

        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            throw new AppException(ErrorCode.UNAUTHORIZED);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        if (!user.getRole().getName().equals("ADMIN"))
            throw new AppException(ErrorCode.ROLE_UPDATE_NOT_ALLOWED);

        try {
            role.setStatus(role.getStatus() == null || !role.getStatus());
            roleRepository.save(role);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }
    }
}
