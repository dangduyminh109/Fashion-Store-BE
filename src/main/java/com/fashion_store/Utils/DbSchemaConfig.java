package com.fashion_store.Utils;

import java.util.Set;

public class DbSchemaConfig {
    public static final Set<String> ALLOWED_TABLES = Set.of(
            "products",
            "variants",
            "attributes",
            "variant_attribute_value",
            "attribute_values",
            "brands",
            "categories",
            "product_images"
    );
}
