package com.fashion_store.service;

import com.fashion_store.Utils.GenerateSlugUtils;
import com.fashion_store.dto.brand.request.BrandRequest;
import com.fashion_store.dto.brand.response.BrandClientResponse;
import com.fashion_store.dto.brand.response.BrandResponse;
import com.fashion_store.entity.Brand;
import com.fashion_store.exception.AppException;
import com.fashion_store.exception.ErrorCode;
import com.fashion_store.mapper.BrandMapper;
import com.fashion_store.repository.BrandRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BrandService extends GenerateService<Brand, Long> {
    BrandRepository brandRepository;
    CloudinaryService cloudinaryService;
    BrandMapper brandMapper;

    @Override
    JpaRepository<Brand, Long> getRepository() {
        return brandRepository;
    }

    public BrandResponse create(BrandRequest request) {
        if (brandRepository.existsByName(request.getName()))
            throw new AppException(ErrorCode.EXISTED);
        Brand brand = brandMapper.toBrand(request);

        // handle image
        if (request.getImage() != null) {
            if (!request.getImage().isEmpty()) {
                try {
                    String imageUrl = cloudinaryService.uploadFile(request.getImage());
                    brand.setImage(imageUrl);

                } catch (IOException e) {
                    throw new AppException(ErrorCode.FILE_SAVE_FAILED);
                }
            } else {
                brand.setImage("");
            }
        }

        // slug
        String baseSlug = GenerateSlugUtils.generateSlug(brand.getName());
        List<Brand> existing = brandRepository.findBySlugStartingWith(baseSlug);
        String finalSlug = baseSlug;
        if (!existing.isEmpty()) {
            finalSlug = baseSlug + "-" + existing.size();
        }
        brand.setSlug(finalSlug);

        brand = brandRepository.save(brand);
        return brandMapper.toBrandResponse(brand);
    }

    public List<BrandResponse> getAll(boolean deleted) {
        return brandRepository.findAll()
                .stream()
                .filter(item -> item.getIsDeleted() == deleted)
                .map(brandMapper::toBrandResponse)
                .collect(Collectors.toList());
    }

    public List<BrandClientResponse> getAllBrand() {
        return brandRepository.findAll()
                .stream()
                .filter(item -> item.getIsDeleted() == false && item.getStatus())
                .map(brandMapper::toBrandClientResponse)
                .collect(Collectors.toList());
    }

    public BrandResponse getInfo(Long id) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        return brandMapper.toBrandResponse(brand);
    }

    public void status(Long id) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        try {
            brand.setStatus(brand.getStatus() == null || !brand.getStatus());
            brandRepository.save(brand);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }
    }

    public BrandResponse update(BrandRequest request, Long id) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        if (brandRepository.existsByNameAndIdNot(request.getName(), id))
            throw new AppException(ErrorCode.EXISTED);

        brandMapper.updateBrand(brand, request);

        // handle image
        boolean imageDelete = request.getImageDelete() != null && request.getImageDelete();
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadFile(request.getImage());
                // Lưu URL vào DB
                brand.setImage(imageUrl);
            } catch (IOException e) {
                throw new AppException(ErrorCode.FILE_SAVE_FAILED);
            }
        } else if (imageDelete) {
            brand.setImage("");
        }

        // slug
        String baseSlug = GenerateSlugUtils.generateSlug(brand.getName());
        List<Brand> existing = brandRepository.findBySlugStartingWith(baseSlug)
                .stream().filter(item -> !item.getId().equals(id)).toList();
        String finalSlug = baseSlug;
        if (!existing.isEmpty()) {
            finalSlug = baseSlug + "-" + existing.size();
        }
        brand.setSlug(finalSlug);

        brand = brandRepository.save(brand);
        return brandMapper.toBrandResponse(brand);
    }
}
