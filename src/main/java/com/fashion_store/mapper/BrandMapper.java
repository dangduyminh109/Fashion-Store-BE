package com.fashion_store.mapper;

import com.fashion_store.dto.brand.request.BrandRequest;
import com.fashion_store.dto.brand.response.BrandClientResponse;
import com.fashion_store.dto.brand.response.BrandResponse;
import com.fashion_store.entity.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BrandMapper {
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "image", ignore = true)
    Brand toBrand(BrandRequest brandRequest);

    BrandResponse toBrandResponse(Brand brand);

    BrandClientResponse toBrandClientResponse(Brand brand);

    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "slug", ignore = true)
    void updateBrand(@MappingTarget Brand brand, BrandRequest brandRequest);
}
