package com.fashion_store.repository;

import com.fashion_store.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
    boolean existsByName(String name);

    Optional<Product> findBySlug(String slug);

    List<Product> findBySlugStartingWith(String slug);

    boolean existsByNameAndIdNot(String name, Long id);

    List<Product> findAllByIsDeletedFalseAndStatusTrueAndIsFeaturedTrue();

    Page<Product> findAllByIsDeletedFalseAndStatusTrue(Pageable pageable);

    @Query(value = """
            SELECT * FROM products p
            WHERE p.id IN (
                SELECT v.product_id
                FROM variants v
                WHERE v.status = true
                  AND v.is_deleted = false
                  AND v.promotional_price IS NOT NULL
                  AND (v.promotion_start_time IS NULL OR v.promotion_start_time <= SYSDATE())
                  AND (v.promotion_end_time IS NULL OR v.promotion_end_time > SYSDATE())
                GROUP BY v.product_id
            )
            LIMIT :quantity
            """, nativeQuery = true)
    List<Product> getSaleProduct(@Param("quantity") int quantity);


    Page<Product> findByIsDeletedFalseAndStatusTrueAndCategoryIdIn(List<Long> categoryIds, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT p.* 
            FROM products p
            JOIN variants v ON v.product_id = p.id
            WHERE p.is_deleted = false
              AND p.status = true
              AND v.is_deleted = false
              AND v.status = true
              AND v.promotional_price IS NOT NULL
              AND (v.promotion_start_time IS NULL OR v.promotion_start_time <= SYSDATE())
              AND (v.promotion_end_time IS NULL OR v.promotion_end_time > SYSDATE())
              AND p.category_id IN (:categoryIds)
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT p.id)
                    FROM products p
                    JOIN variants v ON v.product_id = p.id
                    WHERE p.is_deleted = false
                      AND p.status = true
                      AND v.is_deleted = false
                      AND v.status = true
                      AND v.promotional_price IS NOT NULL
                      AND (v.promotion_start_time IS NULL OR v.promotion_start_time <= SYSDATE())
                      AND (v.promotion_end_time IS NULL OR v.promotion_end_time > SYSDATE())
                      AND p.category_id IN (:categoryIds)
                    """,
            nativeQuery = true)
    Page<Product> findSaleProductsByCategoryIds(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT p.* 
            FROM products p
            JOIN variants v ON v.product_id = p.id
            WHERE p.is_deleted = false
              AND p.status = true
              AND v.is_deleted = false
              AND v.status = true
              AND v.promotional_price IS NOT NULL
              AND (v.promotion_start_time IS NULL OR v.promotion_start_time <= SYSDATE())
              AND (v.promotion_end_time IS NULL OR v.promotion_end_time > SYSDATE())
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT p.id)
                    FROM products p
                    JOIN variants v ON v.product_id = p.id
                    WHERE p.is_deleted = false
                      AND p.status = true
                      AND v.is_deleted = false
                      AND v.status = true
                      AND v.promotional_price IS NOT NULL
                      AND (v.promotion_start_time IS NULL OR v.promotion_start_time <= SYSDATE())
                      AND (v.promotion_end_time IS NULL OR v.promotion_end_time > SYSDATE())
                    """,
            nativeQuery = true)
    Page<Product> findSaleProductsHasPromotion(Pageable pageable);

    Page<Product> findByIsDeletedFalseAndStatusTrueAndCategoryIdInAndNameContainingIgnoreCase(
            List<Long> categoryIds, String search, Pageable pageable);

    Page<Product> findByIsDeletedFalseAndStatusTrueAndNameContainingIgnoreCase(
            String search, Pageable pageable);
}