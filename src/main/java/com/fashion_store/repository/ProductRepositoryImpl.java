package com.fashion_store.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> querySql(String sql) {
        return jdbcTemplate.queryForList(sql);
    }
}
