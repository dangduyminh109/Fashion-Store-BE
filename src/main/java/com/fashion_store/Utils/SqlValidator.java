package com.fashion_store.Utils;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Set;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SqlValidator {
    Set<String> dangerousKeywords = Set.of("DROP", "DELETE", "INSERT", "UPDATE", "ALTER", "CREATE", "--", "#", "TRUNCATE");

    public boolean isSafe(String rawSql) {
        String up = rawSql.toUpperCase();
        String sql = up.replaceAll("(?i)IS_DELETED", "");

        for (String k : dangerousKeywords) if (sql.contains(k)) return false;

        if (!sql.trim().startsWith("SELECT")) return false;

        if (!sql.contains("LIMIT")) {
            // append LIMIT 50 or reject
            return false;
        }

        // crude check: find "FROM <token>" and ensure in whitelist
        Pattern p = Pattern.compile("FROM\\s+([`\"]?)(\\w+)\\1", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sql);
        while (m.find()) {
            String t = m.group(2);
            if (!DbSchemaConfig.ALLOWED_TABLES.contains(t.toLowerCase())) return false;
        }
        return true;
    }
}
