package com.fashion_store.mapper;

import com.fashion_store.dto.product.request.ProductCreateRequest;
import com.fashion_store.dto.product.request.ProductUpdateRequest;
import com.fashion_store.dto.product.response.ProductFeaturedResponse;
import com.fashion_store.dto.product.response.ProductResponse;
import com.fashion_store.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.transaction.annotation.Transactional;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {VariantMapper.class})
public interface ProductMapper {
    Product toProduct(ProductCreateRequest productRequest);

    @Mapping(target = "brandName", expression = "java(product.getBrand() != null ? product.getBrand().getName() : null)")
    @Mapping(target = "brandId", expression = "java(product.getBrand() != null ? product.getBrand().getId() : null)")
    @Mapping(target = "categoryName", expression = "java(product.getCategory() != null ? product.getCategory().getName() : null)")
    @Mapping(target = "categoryId", expression = "java(product.getCategory() != null ? product.getCategory().getId() : null)")
    @Mapping(target = "productImages", expression = "java(product.getProductImages() != null ? product.getProductImages().stream().map(img->img.getUrl()).toList() : null)")
    @Mapping(target = "variants.attributeValues", ignore = true)
    @Transactional
    ProductResponse toProductResponse(Product product);

    @Mapping(target = "brandName", expression = "java(product.getBrand() != null ? product.getBrand().getName() : null)")
    @Mapping(target = "categoryName", expression = "java(product.getCategory() != null ? product.getCategory().getName() : null)")
    @Mapping(target = "productImages", expression = "java(product.getProductImages() != null ? product.getProductImages().stream().map(img->img.getUrl()).toList() : null)")
    @Mapping(target = "variants.attributeValues", ignore = true)
    @Transactional
    ProductFeaturedResponse toProductFeaturedResponse(Product product);

    void updateProduct(@MappingTarget Product product, ProductUpdateRequest productUpdateRequest);
}
