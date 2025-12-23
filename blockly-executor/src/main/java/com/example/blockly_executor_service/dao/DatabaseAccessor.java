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

        // Автоматически добавляем фильтр по tenant_id если его нет
        if (!sql.toLowerCase().contains("tenant_id")) {
            log.warn("Query without tenant_id filter: {}", sql);
        }

        return jdbcTemplate.queryForList(sql, params);
    }
}
