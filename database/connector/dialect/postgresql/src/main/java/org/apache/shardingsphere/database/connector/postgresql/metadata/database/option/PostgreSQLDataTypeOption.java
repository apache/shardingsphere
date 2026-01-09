/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.database.connector.postgresql.metadata.database.option;

import com.cedarsoftware.util.CaseInsensitiveMap;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DefaultDataTypeOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;

import java.sql.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data type option for PostgreSQL.
 */
public final class PostgreSQLDataTypeOption implements DialectDataTypeOption {
    
    private static final Map<String, Integer> EXTRA_DATA_TYPES;
    
    private static final Map<String, Map<String, Integer>> UDT_CACHE = new ConcurrentHashMap<>();
    
    private final DialectDataTypeOption delegate = new DefaultDataTypeOption();
    
    static {
        EXTRA_DATA_TYPES = setUpExtraDataTypes();
    }
    
    private static Map<String, Integer> setUpExtraDataTypes() {
        Map<String, Integer> result = new CaseInsensitiveMap<>();
        result.put("SMALLINT", Types.SMALLINT);
        result.put("INT", Types.INTEGER);
        result.put("INTEGER", Types.INTEGER);
        result.put("BIGINT", Types.BIGINT);
        result.put("DECIMAL", Types.DECIMAL);
        result.put("NUMERIC", Types.NUMERIC);
        result.put("REAL", Types.REAL);
        result.put("BOOL", Types.BOOLEAN);
        result.put("CHARACTER VARYING", Types.VARCHAR);
        result.put("VARBIT", Types.OTHER);
        result.put("BIT VARYING", Types.OTHER);
        return result;
    }
    
    @Override
    public Map<String, Integer> getExtraDataTypes() {
        return EXTRA_DATA_TYPES;
    }
    
    @Override
    public Optional<Class<?>> findExtraSQLTypeClass(final int dataType, final boolean unsigned) {
        if (Types.SMALLINT == dataType) {
            return Optional.of(Integer.class);
        }
        return Optional.empty();
    }
    
    @Override
    public boolean isIntegerDataType(final int sqlType) {
        return delegate.isIntegerDataType(sqlType);
    }
    
    @Override
    public boolean isStringDataType(final int sqlType) {
        return delegate.isStringDataType(sqlType);
    }
    
    @Override
    public boolean isBinaryDataType(final int sqlType) {
        return delegate.isBinaryDataType(sqlType);
    }
    
    @Override
    public Map<String, Integer> loadUDTTypes(Connection connection) throws SQLException {
        // Get schemas to search for UDTs from configuration
        String[] schemas = getUDTSchemas();
        return loadUDTTypes(connection, schemas);
    }
    
    public Map<String, Integer> loadUDTTypes(Connection connection, String... schemas) throws SQLException {
        Map<String, Integer> result = new CaseInsensitiveMap<>();
        
        // Build schema filter condition
        StringBuilder schemaCondition = new StringBuilder();
        if (schemas.length > 0) {
            schemaCondition.append("n.nspname IN (");
            for (int i = 0; i < schemas.length; i++) {
                if (i > 0) {
                    schemaCondition.append(", ");
                }
                schemaCondition.append("?");
            }
            schemaCondition.append(")");
        } else {
            // Default to public schema if no schemas specified
            schemaCondition.append("n.nspname = 'public'");
        }
        
        String sql =
                "SELECT\n" +
                        "    t.typname AS udt_name,\n" +
                        "    t.typtype AS udt_kind,\n" +
                        "    n.nspname AS schema_name\n" +
                        "FROM pg_type t\n" +
                        "         JOIN pg_namespace n ON n.oid = t.typnamespace\n" +
                        "         LEFT JOIN pg_class c ON c.oid = t.typrelid\n" +
                        "WHERE\n" +
                        "    " + schemaCondition.toString() + "\n" +
                        "  AND t.typtype IN ('c', 'e', 'd')   \n" +
                        "  AND (c.relkind IS NULL OR c.relkind = 'c')  \n" +
                        "ORDER BY udt_name;";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            // Set parameters for schema names
            if (schemas.length > 0) {
                for (int i = 0; i < schemas.length; i++) {
                    ps.setString(i + 1, schemas[i]);
                }
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("udt_name"), Types.OTHER);
                }
            }
        }
        return result;
    }
    
    private String[] getUDTSchemas() {
        String schemasProperty = System.getProperty("shardingsphere.postgresql.udt.schemas");
        if (schemasProperty != null && !schemasProperty.trim().isEmpty()) {
            return schemasProperty.split(",");
        }
        // Default to public schema for backward compatibility
        return new String[]{"public"};
    }
    
    /**
     * Load UDT types with schema filter (enhanced version with caching).
     *
     * @param connection database connection
     * @param schemaFilter optional schema name to filter by; if empty, loads from all schemas
     * @return UDT type map
     * @throws SQLException when SQL Exception occurs
     */
    public Map<String, Integer> loadUDTTypesWithSchema(Connection connection, String schemaFilter) throws SQLException {
        // Create a cache key based on connection properties and schema
        String cacheKey = generateCacheKey(connection, schemaFilter);
        
        // Check if we already have cached results
        Map<String, Integer> cachedResult = UDT_CACHE.get(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        Map<String, Integer> result = new CaseInsensitiveMap<>();
        
        String sql = buildUDTQuery(schemaFilter);
        
        try (
                PreparedStatement ps = prepareStatementWithSchema(connection, sql, schemaFilter);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("udt_name"), Types.OTHER);
            }
        }
        
        // Cache the result for future use
        UDT_CACHE.put(cacheKey, result);
        return result;
    }
    
    /**
     * Generate a unique cache key based on connection properties and schema.
     *
     * @param connection database connection
     * @param schemaFilter schema filter
     * @return cache key
     * @throws SQLException if there's an issue getting connection properties
     */
    private String generateCacheKey(Connection connection, String schemaFilter) throws SQLException {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(connection.getCatalog())
                .append("_")
                .append(connection.getSchema() != null ? connection.getSchema() : "")
                .append("_")
                .append(schemaFilter != null ? schemaFilter : "all");
        return keyBuilder.toString();
    }
    
    /**
     * Build UDT query based on schema filter.
     *
     * @param schemaFilter schema filter - if null/empty loads from all schemas, else filters by specific schema
     * @return SQL query string
     */
    private String buildUDTQuery(String schemaFilter) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT\n")
                .append("    t.typname AS udt_name,\n")
                .append("    t.typtype AS udt_kind,\n")
                .append("    n.nspname AS schema_name\n")
                .append("FROM pg_type t\n")
                .append("         JOIN pg_namespace n ON n.oid = t.typnamespace\n")
                .append("         LEFT JOIN pg_class c ON c.oid = t.typrelid\n")
                .append("WHERE\n");
        
        if (schemaFilter != null && !schemaFilter.trim().isEmpty()) {
            // Specific schema filtering - using positional parameter
            sql.append("    n.nspname = ?\n");
        } else {
            // Load from all schemas (excluding system schemas)
            sql.append("    n.nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast')\n");
        }
        
        sql.append("  AND t.typtype IN ('c', 'e', 'd')   \n")
                .append("  AND (c.relkind IS NULL OR c.relkind = 'c')  \n")
                .append("ORDER BY udt_name;");
        
        return sql.toString();
    }
    
    /**
     * Prepare statement with schema parameter if needed.
     *
     * @param connection database connection
     * @param sql SQL query
     * @param schemaFilter schema filter
     * @return PreparedStatement with parameters set
     * @throws SQLException if there's an SQL error
     */
    private PreparedStatement prepareStatementWithSchema(Connection connection, String sql, String schemaFilter) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        if (schemaFilter != null && !schemaFilter.trim().isEmpty()) {
            ps.setString(1, schemaFilter);
        }
        return ps;
    }
    
    /**
     * Clears the UDT cache. Used for testing purposes.
     */
    public static void clearCache() {
        UDT_CACHE.clear();
    }
}
