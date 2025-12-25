package com.example.blockly_executor_service.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.HostAccess;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class DatabaseAccessor {

    private final String tenantId;
    private final JdbcTemplate jdbcTemplate;
    private final ConcurrentHashMap<String, TenantAwareDao> daoCache = new ConcurrentHashMap<>();

    @HostAccess.Export
    public TenantAwareDao table(String tableName) {
        // Создаем DAO только при первом обращении к таблице
        return daoCache.computeIfAbsent(tableName, name -> {
            log.debug("Creating DAO for table: {} (tenant: {})", name, tenantId);
            return new TenantAwareDao(tenantId, name, jdbcTemplate);
        });
    }

    @HostAccess.Export
    public Object query(String sql, Object... params) {
        log.debug("Executing custom query for tenant {}: {}", tenantId, sql);

        // Проверка безопасности - только SELECT
        if (!sql.trim().toUpperCase().startsWith("SELECT")) {
            throw new SecurityException("Only SELECT queries are allowed");
        }

        String allowedSchema = "tenant_" + tenantId;
        String sqlLower = sql.toLowerCase();
        
        // Проверяем что нет обращения к системным схемам
        if (sqlLower.contains("pg_") || sqlLower.contains("information_schema")) {
            throw new SecurityException("Access denied: cannot query system tables");
        }
        
        // Находим все упоминания tenant_ и проверяем что все они относятся к текущему tenant
        int index = 0;
        while ((index = sqlLower.indexOf("tenant_", index)) != -1) {
            if (!sqlLower.startsWith(allowedSchema.toLowerCase(), index)) {
                throw new SecurityException("Access denied: cannot query other tenant's data");
            }
            index += allowedSchema.length();
        }

        return jdbcTemplate.queryForList(sql, params);
    }
}
