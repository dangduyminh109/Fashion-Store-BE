package com.fashion_store.repository;

import com.fashion_store.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    boolean existsByName(String name);

    List<Topic> findBySlugStartingWith(String slug);

    boolean existsByNameAndIdNot(String name, Long id);

    List<Topic> findByIdIn(Set<Long> ids);
}
