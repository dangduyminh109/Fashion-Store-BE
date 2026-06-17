package com.fashion_store.repository;

import com.fashion_store.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query("""
            SELECT cartItem
            FROM CartItem cartItem
            WHERE cartItem.cart.id = :cartId
              AND cartItem.variant.id = :variantId
            """)
    Optional<CartItem> findByCartIdAndVariantId(
            @Param("cartId") Long cartId,
            @Param("variantId") Long variantId
    );
}
