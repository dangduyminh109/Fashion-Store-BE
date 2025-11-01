package com.fashion_store.repository;

import com.fashion_store.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);

    Optional<Product> findBySlug(String slug);

    List<Product> findBySlugStartingWith(String slug);

    boolean existsByNameAndIdNot(String name, Long id);

    List<Product> findAllByIsDeletedFalseAndStatusTrueAndIsFeaturedTrue();

    @Query(value = """
              select * from products p
                 where p.id in (
                         select v.product_id  from variants v
                                 where v.status = true and v.is_deleted = false
                                 and v.promotion_start_time <= SYSDATE() and v.promotion_end_time > SYSDATE()
                 group by v.product_id
             )
             LIMIT :quantity
            """, nativeQuery = true)
    List<Product> getSaleProduct(@Param("quantity") int quantity);

}
