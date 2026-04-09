package com.fashion_store.service;

import com.fashion_store.dto.product.response.ProductResponse;
import com.fashion_store.exception.AppException;
import com.fashion_store.exception.ErrorCode;
import com.fashion_store.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecommendService {
    ProductRepository productRepository;
    ProductService productService;

    @Value("${fastapi.recommend.url}")
    @lombok.experimental.NonFinal
    String fastApiUrl;
    
    public List<ProductResponse> getRecommendations(String items) {
        RestTemplate restTemplate = new RestTemplate();
        List<ProductResponse> result = new ArrayList<>();
        
        try {
            String url = fastApiUrl + "?items=" + items;
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
                            }
                        } catch (AppException e) {
                            log.warn("Recommended product not found or error, id: {}, error: {}", id, e.getMessage());
                        } catch (Exception e) {
                            log.error("Unexpected error fetching product info for id: {}", id, e);
                            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
                        }
                    }
                }
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get recommendations from FastAPI: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }

        if (result.isEmpty()) {
            log.info("Applying fallback recommendation strategy");
            return productService.getNew(5);
        }
        
        return result;
    }
}
