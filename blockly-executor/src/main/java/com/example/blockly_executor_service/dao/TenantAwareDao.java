package com.example.blockly_executor_service.dao;

import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.HostAccess;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class TenantAwareDao {

    private static final Pattern VALID_IDENTIFIER = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private static final Pattern VALID_COLUMN_NAME = Pattern.compile("^[a-zA-Z0-9_]+$");

    private final String tenantId;
    private final String tableName;
    private final JdbcTemplate jdbcTemplate;

    public TenantAwareDao(String tenantId, String tableName, JdbcTemplate jdbcTemplate){
        // Валидация tenantId
        if (tenantId == null || tenantId.isEmpty()) {
            throw new SecurityException("Tenant ID cannot be null or empty");
        }
        if (!VALID_IDENTIFIER.matcher(tenantId).matches()) {
            throw new SecurityException("Invalid tenant ID format: " + tenantId);
        }

        // Валидация tableName
        if (tableName == null || tableName.isEmpty()) {
            throw new SecurityException("Table name cannot be null or empty");
        }
        if (!VALID_IDENTIFIER.matcher(tableName).matches()) {
            throw new SecurityException("Invalid table name format: " + tableName);
        }

        this.tenantId = tenantId;
        //tableName со схемой конкретного тенанта
        this.tableName = "tenant_" + tenantId + "." + tableName;
        this.jdbcTemplate = jdbcTemplate;
        log.debug("Created DAO for table: {} with tenantId: {}", this.tableName, tenantId);
    }

    @HostAccess.Export
    public Object findById(Object id) {
        String sql = String.format(
                "SELECT * FROM %s WHERE id = ?",
                tableName
        );

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, id);
        return results.isEmpty() ? null : results.get(0);
    }

    @HostAccess.Export
    public List<Map<String, Object>> findAll() {
        String sql = String.format(
                "SELECT * FROM %s ORDER BY id",
                tableName
        );

        return jdbcTemplate.queryForList(sql);
    }

    @HostAccess.Export
    public List<Map<String, Object>> where(Map<String, Object> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return findAll();
        }

        StringBuilder sql = new StringBuilder(String.format(
                "SELECT * FROM %s WHERE 1=1",
                tableName
        ));

        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String columnName = entry.getKey();

            // Валидация имени колонки
            if (!VALID_COLUMN_NAME.matcher(columnName).matches()) {
                throw new SecurityException("Invalid column name: " + columnName);
            }

            sql.append(" AND ").append(columnName).append(" = ?");
            params.add(entry.getValue());
        }

        sql.append(" ORDER BY id");

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }


    @HostAccess.Export
    public Object findOne(Map<String, Object> conditions) {
        List<Map<String, Object>> results = where(conditions);
        return results.isEmpty() ? null : results.get(0);
    }

    @HostAccess.Export
    public Object create(Map<String, Object> data) {
        data.remove("id");
        data.remove("created_at");
        data.remove("updated_at");

        // Валидация имен колонок
        for (String columnName : data.keySet()) {
            if (!VALID_COLUMN_NAME.matcher(columnName).matches()) {
                throw new SecurityException("Invalid column name: " + columnName);
            }
        }

        List<String> columns = new ArrayList<>(data.keySet());
        String columnsSql = String.join(", ", columns);
        String valuesSql = columns.stream().map(c -> "?").collect(Collectors.joining(", "));

        String sql = String.format(
                "INSERT INTO %s (%s) VALUES (%s) RETURNING *",
                tableName, columnsSql, valuesSql
        );

        Object[] values = columns.stream().map(data::get).toArray();

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, values);
        return results.isEmpty() ? null : results.get(0);
    }

    @HostAccess.Export
    public Object update(Object id, Map<String, Object> data) {
        data.remove("id");
        data.remove("created_at");
        data.remove("updated_at");

        if (data.isEmpty()) {
            return findById(id);
        }

        // Валидация имен колонок
        for (String columnName : data.keySet()) {
            if (!VALID_COLUMN_NAME.matcher(columnName).matches()) {
                throw new SecurityException("Invalid column name: " + columnName);
            }
        }

        List<String> setClauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            setClauses.add(entry.getKey() + " = ?");
            params.add(entry.getValue());
        }

        setClauses.add("updated_at = CURRENT_TIMESTAMP");

        params.add(id);

        String sql = String.format(
                "UPDATE %s SET %s WHERE id = ? RETURNING *",
                tableName,
                String.join(", ", setClauses)
        );

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, params.toArray());
        return results.isEmpty() ? null : results.get(0);
    }


    @HostAccess.Export
    public boolean delete(Object id) {
        String sql = String.format(
                "DELETE FROM %s WHERE id = ?",
                tableName
        );

        int deleteCount = jdbcTemplate.update(sql, id);
        return deleteCount > 0;
    }


    @HostAccess.Export
    public Long count() {
        String sql = String.format(
                "SELECT COUNT(*) FROM %s",
                tableName
        );

        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}
