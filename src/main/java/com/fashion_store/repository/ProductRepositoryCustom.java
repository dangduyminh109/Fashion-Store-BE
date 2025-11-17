package com.fashion_store.repository;

import java.util.List;
import java.util.Map;

public interface ProductRepositoryCustom {
    List<Map<String, Object>> querySql(String sql);
}