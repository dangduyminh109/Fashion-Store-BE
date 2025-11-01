package com.fashion_store.service;

import com.fashion_store.Utils.GenerateSlugUtils;
import com.fashion_store.dto.attribute.response.AttributeValueResponse;
import com.fashion_store.dto.category.response.CategoryResponse;
import com.fashion_store.dto.product.request.ProductUpdateRequest;
import com.fashion_store.dto.product.response.ProductFeaturedResponse;
import com.fashion_store.dto.product.response.ProductFromCategoryResponse;
import com.fashion_store.dto.product.response.ProductResponse;
import com.fashion_store.dto.variant.request.VariantCreateRequest;
import com.fashion_store.dto.product.request.ProductCreateRequest;
import com.fashion_store.dto.variant.request.VariantUpdateRequest;
import com.fashion_store.dto.variant.response.VariantResponse;
import com.fashion_store.entity.*;
import com.fashion_store.entity.Product;
import com.fashion_store.exception.AppException;
import com.fashion_store.exception.ErrorCode;
import com.fashion_store.mapper.CategoryMapper;
import com.fashion_store.mapper.ProductMapper;
import com.fashion_store.mapper.VariantMapper;
import com.fashion_store.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService extends GenerateService<Product, Long> {
    ProductRepository productRepository;
    BrandRepository brandRepository;
    CategoryRepository categoryRepository;
    CloudinaryService cloudinaryService;

    ProductMapper productMapper;
    VariantMapper variantMapper;
    CategoryMapper categoryMapper;
    AttributeValueRepository attributeValueRepository;
    VariantRepository variantRepository;

    @Override
    JpaRepository<Product, Long> getRepository() {
        return productRepository;
    }

    public ProductResponse create(ProductCreateRequest request) {
        Product product = productMapper.toProduct(request);
        Product finalProduct = product;
        // kiểm tra sku có bị trùng hay đã tồn tại không
        Map<String, Long> skuCountMap = request.getVariantList().stream()
                .collect(Collectors.groupingBy(VariantCreateRequest::getSku, Collectors.counting()));
        skuCountMap.forEach((sku, count) -> {
            if (count > 1 || variantRepository.existsBySku(sku)) {
                throw new AppException(ErrorCode.SKU_EXISTED);
            }
        });

        // variant
        List<Variant> listVariant = new ArrayList<>();
        request.getVariantList().forEach(variantItem -> {
            Variant variant = variantMapper.toVariant(variantItem);
            if (variantItem.getAttributeValue() != null) {
                // set attributeValue
                List<Long> ListAttributeValueId = variantItem.getAttributeValue().stream()
                        .distinct().toList();
                Set<AttributeValue> listAttributeValue = new HashSet<>();
                ListAttributeValueId.forEach(attributeValueId -> {
                    AttributeValue attributeValue = attributeValueRepository.findById(attributeValueId)
                            .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
                    listAttributeValue.add(attributeValue);
                    attributeValue.getVariants().add(variant);
                });
                variant.setAttributeValues(listAttributeValue);
            }

            variant.setProduct(finalProduct);
            listVariant.add(variant);
        });
        product.setVariants(listVariant);

        // brand
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
            product.setBrand(brand);
        }

        // category
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
            product.setCategory(category);
        }

        // slug
        String baseSlug = GenerateSlugUtils.generateSlug(product.getName());
        List<Product> existing = productRepository.findBySlugStartingWith(baseSlug);
        String finalSlug = baseSlug;
        if (!existing.isEmpty()) {
            finalSlug = baseSlug + "-" + existing.size();
        }
        product.setSlug(finalSlug);

        // handle image
        List<ProductImage> listProductImage = new ArrayList<>();
        if (request.getImages() != null) {
            request.getImages().forEach(img -> {
                if (!img.isEmpty()) {
                    try {
                        String imageUrl = cloudinaryService.uploadFile(img);
                        ProductImage productImage = ProductImage.builder()
                                .url(imageUrl)
                                .product(finalProduct)
                                .build();
                        listProductImage.add(productImage);
                    } catch (IOException e) {
                        throw new AppException(ErrorCode.FILE_SAVE_FAILED);
                    }
                }
            });
        }
        product.setProductImages(listProductImage);

        product = productRepository.save(product);
        return productMapper.toProductResponse(product);
    }

    public List<ProductResponse> getProduct() {
        return productRepository.findAll()
                .stream()
                .filter(item -> item.getIsDeleted() == false && item.getStatus() == true)
                .map(item -> {
                    // Map sang DTO bằng mapper
                    ProductResponse response = productMapper.toProductResponse(item);

                    // Nếu không có variants thì trả luôn
                    if (response == null || response.getVariants() == null || item.getVariants() == null) {
                        return response;
                    }

                    Map<Long, Variant> variantEntityById = item.getVariants().stream()
                            .filter(v -> v.getId() != null)
                            .collect(Collectors.toMap(
                                    Variant::getId,
                                    v -> v,
                                    (existing, replacement) -> existing // giữ bản đầu khi trùng id
                            ));

                    // Duyệt từng variant DTO, tìm entity tương ứng và gán attribute fields
                    response.getVariants().forEach(variantResponse -> {
                        if (variantResponse == null) return;

                        Long variantId = variantResponse.getId();
                        Variant variantEntity = variantEntityById.get(variantId);
                        if (variantEntity == null || variantEntity.getAttributeValues() == null) return;

                        // Map attributeValueId -> AttributeValue entity
                        Map<Long, AttributeValue> attrValEntityById = variantEntity.getAttributeValues().stream()
                                .filter(av -> av.getId() != null)
                                .collect(Collectors.toMap(AttributeValue::getId, av -> av));

                        if (variantResponse.getAttributeValues() == null) return;

                        variantResponse.getAttributeValues().forEach(attrValResp -> {
                            if (attrValResp == null) return;

                            Long attrValId = attrValResp.getId();
                            AttributeValue attrValEntity = attrValEntityById.get(attrValId);
                            if (attrValEntity == null) return;

                            if (attrValEntity.getAttribute() != null) {
                                attrValResp.setAttributeId(attrValEntity.getAttribute().getId());
                                attrValResp.setAttributeName(attrValEntity.getAttribute().getName());
                                attrValResp.setDisplayType(attrValEntity.getAttribute().getDisplayType());
                            }
                        });
                    });

                    return response;
                })
                .collect(Collectors.toList());
    }

    public ProductResponse getVariant(Long id) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));

        Product product = variant.getProduct();

        Product copyProduct = new Product();
        copyProduct.setId(product.getId());
        copyProduct.setName(product.getName());
        copyProduct.setCategory(product.getCategory());
        copyProduct.setDescription(product.getDescription());
        copyProduct.setProductImages(new ArrayList<>(product.getProductImages()));
        copyProduct.setSlug(product.getSlug());
        copyProduct.setBrand(product.getBrand());

        List<Variant> resultVariant = product.getVariants().stream()
                .filter(v -> Objects.equals(v.getId(), id))
                .toList();

        copyProduct.setVariants(new ArrayList<>(resultVariant));
        return productMapper.toProductResponse(copyProduct);
    }

    public List<ProductResponse> getNew(Integer quantity) {
        return productRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(item -> item.getIsDeleted() == false && item.getStatus() == true)
                .limit(quantity)
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getAll(boolean deleted, String name) {
        return productRepository.findAll()
                .stream()
                .filter(item -> {
                    if (name != null && !name.trim().isEmpty()) {
                        return item.getName().trim().toLowerCase().contains(name.toLowerCase())
                                && Boolean.TRUE.equals(item.getIsDeleted()) == deleted;
                    }
                    return Boolean.TRUE.equals(item.getIsDeleted()) == deleted;
                })
                .map(item -> {
                    // Map sang DTO bằng mapper
                    ProductResponse response = productMapper.toProductResponse(item);

                    // Nếu không có variants thì trả luôn
                    if (response == null || response.getVariants() == null || item.getVariants() == null) {
                        return response;
                    }

                    Map<Long, Variant> variantEntityById = item.getVariants().stream()
                            .filter(v -> v.getId() != null)
                            .collect(Collectors.toMap(
                                    Variant::getId,
                                    v -> v,
                                    (existing, replacement) -> existing // giữ bản đầu khi trùng id
                            ));

                    // Duyệt từng variant DTO, tìm entity tương ứng và gán attribute fields
                    response.getVariants().forEach(variantResponse -> {
                        if (variantResponse == null) return;

                        Long variantId = variantResponse.getId();
                        Variant variantEntity = variantEntityById.get(variantId);
                        if (variantEntity == null || variantEntity.getAttributeValues() == null) return;

                        // Map attributeValueId -> AttributeValue entity
                        Map<Long, AttributeValue> attrValEntityById = variantEntity.getAttributeValues().stream()
                                .filter(av -> av.getId() != null)
                                .collect(Collectors.toMap(AttributeValue::getId, av -> av));

                        if (variantResponse.getAttributeValues() == null) return;

                        variantResponse.getAttributeValues().forEach(attrValResp -> {
                            if (attrValResp == null) return;

                            Long attrValId = attrValResp.getId();
                            AttributeValue attrValEntity = attrValEntityById.get(attrValId);
                            if (attrValEntity == null) return;

                            if (attrValEntity.getAttribute() != null) {
                                attrValResp.setAttributeId(attrValEntity.getAttribute().getId());
                                attrValResp.setAttributeName(attrValEntity.getAttribute().getName());
                                attrValResp.setDisplayType(attrValEntity.getAttribute().getDisplayType());
                            }
                        });
                    });

                    return response;
                })
                .collect(Collectors.toList());
    }

    public List<ProductFeaturedResponse> getFeatured(Integer quantity) {
        return productRepository.findAllByIsDeletedFalseAndStatusTrueAndIsFeaturedTrue()
                .stream()
                .map(productMapper::toProductFeaturedResponse)
                .limit(quantity)
                .collect(Collectors.toList());
    }

    public List<ProductFeaturedResponse> suggest(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        return productRepository.findAllByIsDeletedFalseAndStatusTrueAndIsFeaturedTrue()
                .stream()
                .filter(item -> item.getCategory().getId() == product.getCategory().getId() || item.getBrand().getId() == product.getBrand().getId() && item.getId() != product.getId())
                .map(productMapper::toProductFeaturedResponse)
                .limit(quantity)
                .collect(Collectors.toList());
    }

    public List<ProductFeaturedResponse> getSale(Integer quantity) {
        return productRepository.getSaleProduct(quantity)
                .stream()
                .map(productMapper::toProductFeaturedResponse)
                .limit(quantity)
                .collect(Collectors.toList());
    }

    public List<ProductFeaturedResponse> getByCategoryFeature(Integer quantity) {
        return productRepository.findAllByIsDeletedFalseAndStatusTrueAndIsFeaturedTrue()
                .stream()
                .map(productMapper::toProductFeaturedResponse)
                .limit(quantity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductFromCategoryResponse getByCategory(String slug) {
        Category root = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));

        // thu tất cả category (gồm root + descendants)
        List<Category> all = new ArrayList<>();
        collectDescendants(root, all);

        // gom products từ mọi category
        List<ProductResponse> resultProduct = all.stream()
                .filter(c -> c.getProducts() != null)
                .flatMap(c -> c.getProducts().stream())
                .distinct()
                .map(productMapper::toProductResponse)
                .toList();

        CategoryResponse resultCategory = categoryMapper.toCategoryResponse(root);
        return ProductFromCategoryResponse.builder()
                .category(resultCategory)
                .products(resultProduct)
                .build();
    }

    private void collectDescendants(Category c, List<Category> out) {
        out.add(c);
        if (c.getChildren() == null) return;
        for (Category ch : c.getChildren()) {
            collectDescendants(ch, out);
        }
    }

    public ProductResponse getInfoBySlug(String slug) {
        Product product = productRepository.findBySlug(slug).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        if (product.getIsDeleted() == true)
            throw new AppException(ErrorCode.NOT_EXIST);
        ProductResponse response = productMapper.toProductResponse(product);
        // Fill attribute info giống như getAll
        fillAttributeInfoFromEntity(product, response);
        return response;
    }

    public ProductResponse getInfo(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));

        ProductResponse response = productMapper.toProductResponse(product);

        // Nếu không có variants thì trả luôn
        if (response == null || response.getVariants() == null || product.getVariants() == null) {
            return response;
        }

        Map<Long, Variant> variantEntityById = product.getVariants().stream()
                .filter(v -> v.getId() != null)
                .collect(Collectors.toMap(
                        Variant::getId,
                        v -> v,
                        (existing, replacement) -> existing // giữ bản đầu khi trùng id
                ));

        // Duyệt từng variant DTO, tìm entity tương ứng và gán attribute fields
        response.getVariants().forEach(variantResponse -> {
            if (variantResponse == null) return;

            Long variantId = variantResponse.getId();
            Variant variantEntity = variantEntityById.get(variantId);
            if (variantEntity == null || variantEntity.getAttributeValues() == null) return;

            // Map attributeValueId -> AttributeValue entity
            Map<Long, AttributeValue> attrValEntityById = variantEntity.getAttributeValues().stream()
                    .filter(av -> av.getId() != null)
                    .collect(Collectors.toMap(AttributeValue::getId, av -> av));

            if (variantResponse.getAttributeValues() == null) return;

            variantResponse.getAttributeValues().forEach(attrValResp -> {
                if (attrValResp == null) return;

                Long attrValId = attrValResp.getId();
                AttributeValue attrValEntity = attrValEntityById.get(attrValId);
                if (attrValEntity == null) return;

                if (attrValEntity.getAttribute() != null) {
                    attrValResp.setAttributeId(attrValEntity.getAttribute().getId());
                    attrValResp.setAttributeName(attrValEntity.getAttribute().getName());
                    attrValResp.setDisplayType(attrValEntity.getAttribute().getDisplayType());
                }
            });
        });

        return response;
    }

    public ProductResponse update(ProductUpdateRequest request, Long id) {
        Product oldProduct = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        Product finalProduct = oldProduct;

        // variant
        // lấy danh sách id variant từ request
        List<Long> listVariantIdRequest = request.getVariantList().stream()
                .map(VariantUpdateRequest::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // kiểm tra nếu có id thì id đó phải thuộc danh sách id củ mới cho cập nhật
        List<Long> oldVariantIds = oldProduct.getVariants().stream().map(Variant::getId).toList();
        listVariantIdRequest.forEach(idVariantUpdate -> {
            if (!oldVariantIds.contains(idVariantUpdate))
                throw new AppException(ErrorCode.VARIANT_ID_MISMATCH);
        });

        // kiểm tra sku có bị trùng hay đã tồn tại trong db không
        Map<String, Long> skuCountMap = request.getVariantList().stream()
                .collect(Collectors.groupingBy(VariantUpdateRequest::getSku, Collectors.counting()));

        skuCountMap.forEach((sku, count) -> {
            if (count > 1) {
                throw new AppException(ErrorCode.SKU_EXISTED);
            }
        });

        for (VariantUpdateRequest variant : request.getVariantList()) {
            if (variant.getId() != null) {
                if (variantRepository.existsBySkuAndIdNot(variant.getSku(), variant.getId())) {
                    throw new AppException(ErrorCode.SKU_EXISTED);
                }
            } else {
                if (variantRepository.existsBySku(variant.getSku())) {
                    throw new AppException(ErrorCode.SKU_EXISTED);
                }
            }
        }

        // map các thông tin cơ bản
        productMapper.updateProduct(oldProduct, request);

        List<Variant> newListVariant = new ArrayList<>();

        oldProduct.getVariants().clear();

        request.getVariantList().forEach(variantItem -> {
            if (variantItem.getId() != null) {
                Variant updateVariantItem = variantRepository.findById(variantItem.getId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
                variantMapper.updateVariant(updateVariantItem, variantItem);

                List<AttributeValue> newAttributeValueList = new ArrayList<>();
                if (variantItem.getAttributeValue() != null) {
                    // thêm attribute value có trong danh sách attribute value id request
                    variantItem.getAttributeValue().forEach(attributeValueId -> {
                        // trường hợp from gửi lên là variantList[0].AttributeValue[1] mà không có variantList[0].AttributeValue[0] thì continue
                        if (attributeValueId != null) {
                            AttributeValue newAttributeValue = attributeValueRepository.findById(attributeValueId)
                                    .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
                            newAttributeValue.getVariants().add(updateVariantItem);
                            newAttributeValueList.add(newAttributeValue);
                        }
                    });
                }
                // xóa tất cả (lúc này các id trước đó không có trong list id request sẽ bị xóa)
                updateVariantItem.getAttributeValues().clear();
                // cập nhật lại toàn bộ ListVariant;
                updateVariantItem.getAttributeValues().addAll(newAttributeValueList);
                updateVariantItem.setProduct(finalProduct);
                newListVariant.add(updateVariantItem);
            } else {
                Variant newVariant = variantMapper.toVariant(variantItem);
                if (variantItem.getAttributeValue() != null) {
                    // set attributeValue
                    List<Long> ListAttributeValueId = variantItem.getAttributeValue().stream()
                            .distinct().toList();
                    Set<AttributeValue> listAttributeValue = new HashSet<>();
                    ListAttributeValueId.forEach(attributeValueId -> {
                        AttributeValue attributeValue = attributeValueRepository.findById(attributeValueId)
                                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
                        listAttributeValue.add(attributeValue);
                        attributeValue.getVariants().add(newVariant);
                    });
                    newVariant.setAttributeValues(listAttributeValue);
                }
                newVariant.setProduct(finalProduct);
                newListVariant.add(newVariant);
            }
        });

        oldProduct.getVariants().addAll(newListVariant);

        // brand
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
            oldProduct.setBrand(brand);
        }

        // category
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
            oldProduct.setCategory(category);
        }

        // slug
        String baseSlug = GenerateSlugUtils.generateSlug(oldProduct.getName());
        List<Product> existing = productRepository.findBySlugStartingWith(baseSlug)
                .stream().filter(item -> !item.getId().equals(id)).toList();
        String finalSlug = baseSlug;
        if (!existing.isEmpty()) {
            finalSlug = baseSlug + "-" + existing.size();
        }
        oldProduct.setSlug(finalSlug);

        // handle image
        List<ProductImage> listProductImage = new ArrayList<>();
        if (oldProduct.getProductImages() != null && request.getListDeletedImage() != null) {
            oldProduct.getProductImages().forEach(img -> {
                if (!request.getListDeletedImage().contains(img.getUrl())) {
                    listProductImage.add(img);
                }
            });
            oldProduct.getProductImages().clear();
        }

        if (request.getImages() != null) {
            request.getImages().forEach(img -> {
                if (!img.isEmpty()) {
                    try {
                        String imageUrl = cloudinaryService.uploadFile(img);
                        ProductImage productImage = ProductImage.builder()
                                .url(imageUrl)
                                .product(finalProduct)
                                .build();
                        listProductImage.add(productImage);
                    } catch (IOException e) {
                        throw new AppException(ErrorCode.FILE_SAVE_FAILED);
                    }
                }
            });
        }

        if (oldProduct.getProductImages() == null) {
            oldProduct.setProductImages(listProductImage);
        } else {
            oldProduct.getProductImages().addAll(listProductImage);
        }

        oldProduct = productRepository.save(oldProduct);
        return productMapper.toProductResponse(oldProduct);
    }

    public void status(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        try {
            product.setStatus(product.getStatus() == null || !product.getStatus());
            productRepository.save(product);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }
    }

    public void statusVariant(Long id) {
        Variant variant = variantRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        try {
            variant.setStatus(variant.getStatus() == null || !variant.getStatus());
            variantRepository.save(variant);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }
    }

    public void featured(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        try {
            product.setIsFeatured(product.getStatus() == null || !product.getIsFeatured());
            productRepository.save(product);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }
    }

    private void fillAttributeInfoFromEntity(Product product, ProductResponse response) {
        if (response == null || response.getVariants() == null || product.getVariants() == null) return;

        // build map variantId -> Variant (avoid duplicate keys)
        Map<Long, Variant> variantEntityById = new HashMap<>();
        for (Variant v : product.getVariants()) {
            if (v != null && v.getId() != null) variantEntityById.putIfAbsent(v.getId(), v);
        }

        for (VariantResponse vr : response.getVariants()) {
            if (vr == null) continue;
            Variant variantEntity = variantEntityById.get(vr.getId());
            if (variantEntity == null || variantEntity.getAttributeValues() == null) continue;

            Map<Long, AttributeValue> attrValById = new HashMap<>();
            for (AttributeValue av : variantEntity.getAttributeValues()) {
                if (av != null && av.getId() != null) attrValById.putIfAbsent(av.getId(), av);
            }

            if (vr.getAttributeValues() == null) continue;
            for (AttributeValueResponse avr : vr.getAttributeValues()) {
                if (avr == null) continue;
                AttributeValue avEntity = attrValById.get(avr.getId());
                if (avEntity == null || avEntity.getAttribute() == null) continue;

                avr.setAttributeId(avEntity.getAttribute().getId());
                avr.setAttributeName(avEntity.getAttribute().getName());
                avr.setDisplayType(avEntity.getAttribute().getDisplayType());
            }
        }
    }

}