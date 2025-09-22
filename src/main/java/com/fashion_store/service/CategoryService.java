package com.fashion_store.service;

import com.fashion_store.Utils.GenerateSlugUtils;
import com.fashion_store.dto.category.request.CategoryRequest;
import com.fashion_store.dto.category.response.CategoryResponse;
import com.fashion_store.dto.category.response.CategoryTreeResponse;
import com.fashion_store.entity.Category;
import com.fashion_store.exception.AppException;
import com.fashion_store.exception.ErrorCode;
import com.fashion_store.mapper.CategoryMapper;
import com.fashion_store.repository.CategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService extends GenerateService<Category, Long> {
    CategoryRepository categoryRepository;
    CloudinaryService cloudinaryService;
    CategoryMapper categoryMapper;

    @Override
    JpaRepository<Category, Long> getRepository() {
        return categoryRepository;
    }

    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName()))
            throw new AppException(ErrorCode.EXISTED);
        Category category = categoryMapper.toCategory(request);
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElse(null);
            category.setParent(parent);
        }

        // handle image
        if (request.getImage() != null) {
            if (!request.getImage().isEmpty()) {
                try {
                    String imageUrl = cloudinaryService.uploadFile(request.getImage());
                    category.setImage(imageUrl);

                } catch (IOException e) {
                    throw new AppException(ErrorCode.FILE_SAVE_FAILED);
                }
            } else {
                category.setImage("");
            }
        }

        // slug
        String baseSlug = GenerateSlugUtils.generateSlug(category.getName());
        List<Category> existing = categoryRepository.findBySlugStartingWith(baseSlug);
        String finalSlug = baseSlug;
        if (!existing.isEmpty()) {
            finalSlug = baseSlug + "-" + existing.size();
        }
        category.setSlug(finalSlug);

        category = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(category);
    }

    public List<CategoryTreeResponse> getTree(Long excludeId) {
        List<Category> all = categoryRepository.findAll();
        Map<Long, CategoryTreeResponse> map = new HashMap<>();
        for (Category c : all) {
            map.put(c.getId(),
                    CategoryTreeResponse.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .slug(c.getSlug())
                            .children(new ArrayList<>())
                            .build()
            );
        }

        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (Category c : all) {
            Long pid = c.getParent() == null ? null : c.getParent().getId();
            childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(c.getId());
        }

        Set<Long> excluded = new HashSet<>();
        if (excludeId != null) {
            Deque<Long> stack = new ArrayDeque<>();
            stack.push(excludeId);
            while (!stack.isEmpty()) {
                Long cur = stack.pop();
                if (!excluded.add(cur)) continue;
                List<Long> childs = childrenMap.get(cur);
                if (childs != null) {
                    for (Long cid : childs) {
                        if (!excluded.contains(cid)) {
                            stack.push(cid);
                        }
                    }
                }
            }
        }

        List<CategoryTreeResponse> roots = new ArrayList<>();
        for (Category c : all) {
            Long cid = c.getId();
            if (excluded.contains(cid)) continue;

            Long pid = c.getParent() == null ? null : c.getParent().getId();
            if (pid != null && !excluded.contains(pid)) {
                CategoryTreeResponse parent = map.get(pid);
                if (parent != null) parent.getChildren().add(map.get(cid));
                else roots.add(map.get(cid));
            } else {
                if (pid == null) {
                    roots.add(map.get(cid));
                } else {
                    roots.add(map.get(cid));
                }
            }
        }

        return roots;
    }

    public List<CategoryResponse> getAll(boolean deleted) {
        return categoryRepository.findAll()
                .stream()
                .filter(item -> item.getIsDeleted() == deleted)
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getInfo(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        return categoryMapper.toCategoryResponse(category);
    }

    public CategoryResponse update(CategoryRequest request, Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        if (categoryRepository.existsByNameAndIdNot(request.getName(), id))
            throw new AppException(ErrorCode.EXISTED);
        if (request.getParentId() != null && request.getParentId().equals(id))
            throw new AppException(ErrorCode.EXISTED);

        categoryMapper.updateCategory(category, request);

        if (request.getParentId() != null) {
            // chỉ được cập nhật nếu parentId không nằm trong danh sách các Id con
            Set<Long> listChildId = new HashSet<>();
            getAllChildrenId(category, listChildId);
            if (listChildId.contains(request.getParentId()))
                throw new AppException(ErrorCode.NOT_EXIST);

            Category parent = categoryRepository.findById(request.getParentId())
                    .orElse(null);

            category.setParent(parent);
        }
        // handle image
        boolean imageDelete = request.getImageDelete() != null && request.getImageDelete();
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadFile(request.getImage());
                // Lưu URL vào DB
                category.setImage(imageUrl);
            } catch (IOException e) {
                throw new AppException(ErrorCode.FILE_SAVE_FAILED);
            }
        } else if (imageDelete) {
            category.setImage("");
        }

        // slug
        String baseSlug = GenerateSlugUtils.generateSlug(category.getName());
        List<Category> existing = categoryRepository.findBySlugStartingWith(baseSlug)
                .stream().filter(item -> !item.getId().equals(id)).toList();
        String finalSlug = baseSlug;
        if (!existing.isEmpty()) {
            finalSlug = baseSlug + "-" + existing.size();
        }
        category.setSlug(finalSlug);

        category = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(category);
    }

    public void status(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        try {
            category.setStatus(category.getStatus() == null || !category.getStatus());
            categoryRepository.save(category);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }
    }

    private void getAllChildrenId(Category parent, Set<Long> listChildId) {
        if (parent.getChildren() != null && !parent.getChildren().isEmpty()) {
            parent.getChildren().forEach(child -> {
                listChildId.add(child.getId());
                getAllChildrenId(child, listChildId);
            });
        }
    }
}
