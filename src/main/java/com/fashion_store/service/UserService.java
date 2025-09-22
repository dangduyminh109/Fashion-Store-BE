package com.fashion_store.service;

import com.fashion_store.Utils.SecurityUtils;
import com.fashion_store.dto.topic.request.UserCreateRequest;
import com.fashion_store.dto.topic.request.UserUpdateRequest;
import com.fashion_store.dto.user.response.UserResponse;
import com.fashion_store.entity.Role;
import com.fashion_store.entity.User;
import com.fashion_store.exception.AppException;
import com.fashion_store.exception.ErrorCode;
import com.fashion_store.mapper.UserMapper;
import com.fashion_store.repository.RoleRepository;
import com.fashion_store.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    CloudinaryService cloudinaryService;
    UserMapper userMapper;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.EXISTED);
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail()))
            throw new AppException(ErrorCode.EXISTED);
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
            user.setRole(role);
        }

        // handle image
        if (request.getAvatar() != null) {
            if (!request.getAvatar().isEmpty()) {
                try {
                    String imageUrl = cloudinaryService.uploadFile(request.getAvatar());
                    user.setAvatar(imageUrl);

                } catch (IOException e) {
                    throw new AppException(ErrorCode.FILE_SAVE_FAILED);
                }
            } else {
                user.setAvatar("");
            }
        }

        user = userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    public List<UserResponse> getAll(boolean deleted) {
        return userRepository.findAll()
                .stream()
                .filter(item -> item.getIsDeleted() == deleted)
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getInfo(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        return userMapper.toUserResponse(user);
    }

    public UserResponse getMyInfo() {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            throw new AppException(ErrorCode.UNAUTHORIZED);
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        if (user.getIsDeleted())
            throw new AppException(ErrorCode.INVALID_ACCOUNT);
        return userMapper.toUserResponse(user);
    }

    public void status(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));

        String currentUserId = SecurityUtils.getCurrentUserId();

        if (id.equals(currentUserId)) {
            throw new AppException(ErrorCode.ACTION_FORBIDDEN);
        }
        try {
            user.setStatus(user.getStatus() == null || !user.getStatus());
            userRepository.save(user);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }
    }

    public UserResponse update(UserUpdateRequest request, String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), id))
            throw new AppException(ErrorCode.EXISTED);
        if (request.getEmail() != null && userRepository.existsByEmailAndIdNot(request.getEmail(), id))
            throw new AppException(ErrorCode.EXISTED);
        if (request.getStatus() != null) {
            String currentUserId = SecurityUtils.getCurrentUserId();

            if (id.equals(currentUserId)) {
                throw new AppException(ErrorCode.ACTION_FORBIDDEN);
            }
        }

        userMapper.updateUser(user, request);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
            user.setRole(role);
        } else {
            user.setRole(null);
        }

        // handle avatar
        boolean avatarDelete = request.getAvatarDelete() != null && request.getAvatarDelete();
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            try {
                String avatarUrl = cloudinaryService.uploadFile(request.getAvatar());
                // Lưu URL vào DB
                user.setAvatar(avatarUrl);
            } catch (IOException e) {
                throw new AppException(ErrorCode.FILE_SAVE_FAILED);
            }
        } else if (avatarDelete) {
            user.setAvatar("");
        }


        user = userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    public void delete(String id) {
        User item = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (id.equals(currentUserId)) {
            throw new AppException(ErrorCode.ACTION_FORBIDDEN);
        }
        try {
            item.setIsDeleted(true);
            userRepository.save(item);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }
    }

    public void restore(String id) {
        User item = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (id.equals(currentUserId)) {
            throw new AppException(ErrorCode.ACTION_FORBIDDEN);
        }
        try {
            item.setIsDeleted(false);
            userRepository.save(item);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }
    }

    public void destroy(String id) {
        User item = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (id.equals(currentUserId)) {
            throw new AppException(ErrorCode.ACTION_FORBIDDEN);
        }
        try {
            userRepository.delete(item);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }
    }
}
