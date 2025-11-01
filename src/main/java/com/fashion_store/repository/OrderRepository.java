package com.fashion_store.repository;

import com.fashion_store.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByTransactionRef(String ref);

    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE YEAR(o.createdAt) = :year " +
            "AND MONTH(o.createdAt) = :month")
    Integer countOrdersByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT SUM(o.totalAmount) FROM Order o " +
            "WHERE YEAR(o.createdAt) = :year " +
            "AND MONTH(o.createdAt) = :month")
    BigDecimal getTotalRevenue(@Param("year") int year, @Param("month") int month);

    @Query("SELECT MONTH(o.createdAt), SUM(o.totalAmount) " +
            "FROM Order o " +
            "WHERE YEAR(o.createdAt) = :year " +
            "GROUP BY MONTH(o.createdAt)")
    List<Object[]> getMonthlyRevenue(@Param("year") int year);

    List<Order> findTop10ByOrderByCreatedAtDesc();

    @Query("""
                SELECT c.name AS categoryName, COUNT(DISTINCT o.id) AS orderCount
                FROM Order o
                JOIN o.orderItems oi
                JOIN oi.variant v
                JOIN v.product p
                JOIN p.category c
                GROUP BY c.name
                ORDER BY COUNT(DISTINCT o.id) DESC
            """)
    List<Object[]> findTopCategoriesWithOrderCount();
}
