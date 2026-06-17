package com.fashion_store.service;

import com.fashion_store.dto.product.response.ProductResponse;
import com.fashion_store.exception.AppException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecommendService {
    private static final int RECOMMENDATION_LIMIT = 4;
    private static final int FALLBACK_POOL_SIZE = 20;

    ProductService productService;

    @Value("${fastapi.recommend.url}")
    @lombok.experimental.NonFinal
    String fastApiUrl;
    
    public List<ProductResponse> getRecommendations(String items) {
        RestTemplate restTemplate = createRestTemplate();
        List<ProductResponse> result = new ArrayList<>();
        List<Long> sourceIds = parseItemIds(items);
        Set<Long> excludedIds = new LinkedHashSet<>(sourceIds);
        
        if (!sourceIds.isEmpty()) {
            try {
                String url = UriComponentsBuilder.fromUriString(fastApiUrl)
                        .queryParam("items", items)
                        .queryParam("limit", RECOMMENDATION_LIMIT)
                        .toUriString();
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                
                if (response != null && response.containsKey("recommendations")) {
                    List<Integer> recommendedIds = (List<Integer>) response.get("recommendations");
                    
                    if (recommendedIds != null && !recommendedIds.isEmpty()) {
                        List<Long> idsToFetch = recommendedIds.stream()
                                .map(Integer::longValue)
                                .collect(Collectors.toList());
                        
                        for (Long id : idsToFetch) {
                            try {
                                ProductResponse prod = productService.getInfo(id);
                                if (prod != null) {
                                    result.add(prod);
                                    excludedIds.add(prod.getId());
                                }
                            } catch (AppException e) {
                                log.warn("Recommended product not found or error, id: {}, error: {}", id, e.getMessage());
                            } catch (Exception e) {
                                log.warn("Unexpected error fetching product info for id: {}", id, e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to get recommendations from FastAPI, applying fallback: {}", e.getMessage());
            }
        }

        if (result.size() < RECOMMENDATION_LIMIT) {
            result.addAll(getFallbackRecommendations(
                    sourceIds,
                    excludedIds,
                    RECOMMENDATION_LIMIT - result.size()
            ));
        }
        
        return result.stream()
                .limit(RECOMMENDATION_LIMIT)
                .collect(Collectors.toList());
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }

    private List<Long> parseItemIds(String items) {
        if (items == null || items.trim().isEmpty()) {
            return List.of();
        }

        return Arrays.stream(items.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .map(item -> {
                    try {
                        return Long.parseLong(item);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid product id in recommendation request: {}", item);
                        return null;
                    }
                })
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<ProductResponse> getFallbackRecommendations(
            List<Long> sourceIds,
            Set<Long> excludedIds,
            int limit
    ) {
        log.info("Applying fallback recommendation strategy");
        Set<Long> seenIds = new LinkedHashSet<>(excludedIds);
        List<ProductResponse> result = new ArrayList<>();

        addUniqueProducts(
                result,
                seenIds,
                productService.getRelatedFallback(sourceIds, seenIds, limit),
                limit
        );

        if (result.size() < limit) {
            addUniqueProducts(
                    result,
                    seenIds,
                    productService.getNew(FALLBACK_POOL_SIZE),
                    limit
            );
        }

        return result;
    }

    private void addUniqueProducts(
            List<ProductResponse> result,
            Set<Long> seenIds,
            List<ProductResponse> products,
            int limit
    ) {
        for (ProductResponse product : products) {
            if (result.size() >= limit) {
                return;
            }

            if (product.getId() != null && seenIds.add(product.getId())) {
                result.add(product);
            }
        }
    }
}
