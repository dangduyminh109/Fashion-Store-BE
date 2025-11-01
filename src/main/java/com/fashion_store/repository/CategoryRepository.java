package com.fashion_store.repository;

import com.fashion_store.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    List<Category> findBySlugStartingWith(String slug);

    Optional<Category> findBySlug(String slug);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query(value = """
               (
                   SELECT c.id, c.slug, c.name, c.image, COUNT(p.id) AS productCount
                   FROM categories c
                   LEFT JOIN products p ON p.category_id = c.id
                   WHERE c.id IN (
                       SELECT v.product_id
                       FROM order_items oi
                       LEFT JOIN variants v ON v.id = oi.variant_id
                       GROUP BY v.product_id
                       ORDER BY SUM(oi.quantity * oi.price) DESC
                   )
                   AND c.is_deleted = false
                   AND c.status = true
                   GROUP BY c.id, c.slug, c.name, c.image
                   HAVING COUNT(p.id) > 0
               )
               UNION
               (
                   SELECT c.id, c.slug, c.name, c.image, COUNT(p.id) AS productCount
                   FROM categories c
                   LEFT JOIN products p ON p.category_id = c.id
                   WHERE c.is_deleted = false
                     AND c.status = true
                   GROUP BY c.id, c.slug, c.name, c.image
                   HAVING COUNT(p.id) > 0
               )
               LIMIT :quantity
            """, nativeQuery = true)
    List<Object[]> getFeatured(@Param("quantity") int quantity);

}
