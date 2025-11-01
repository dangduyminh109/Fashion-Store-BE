package com.fashion_store.repository;

import com.fashion_store.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    boolean existsByTitle(String title);

    List<Post> findBySlugStartingWith(String slug);

    boolean existsByTitleAndIdNot(String title, Long id);

    @Query("SELECT COUNT(p) FROM Post p " +
            "WHERE YEAR(p.createdAt) = :year " +
            "AND MONTH(p.createdAt) = :month")
    Integer countPostsByYearAndMonth(@Param("year") int year, @Param("month") int month);

    Page<Post> findAllByIsDeletedFalseAndStatusTrue(Pageable pageable);
}
