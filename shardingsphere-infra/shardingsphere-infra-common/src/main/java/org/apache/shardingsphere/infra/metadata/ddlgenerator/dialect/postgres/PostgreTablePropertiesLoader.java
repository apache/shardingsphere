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

package org.apache.shardingsphere.infra.metadata.ddlgenerator.dialect.postgres;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Postgre table properties loader.
 */
@RequiredArgsConstructor
public final class PostgreTablePropertiesLoader extends PostgreAbstractLoader {
    
    private final Connection connection;
    
    private final String tableName;
    
    private final String schemaName;
    
    /**
     * Load table properties.
     * 
     * @return table properties
     */
    @SneakyThrows
    public Map<String, Object> loadTableProperties() {
        Map<String, Object> result = new LinkedHashMap<>();
        getDataBaseId(result);
        getSchemaId(result);
        getTableId(result);
        fetchTableProperties(result);
        return result;
    }
    
    private void getDataBaseId(final Map<String, Object> context) throws SQLException {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format("select oid as did, datlastsysoid from pg_catalog.pg_database where datname = '%s';", connection.getCatalog()))) {
            appendToMap(resultSet, context);
        }
    }
    
    private void getTableId(final Map<String, Object> context) throws SQLException {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement
                        .executeQuery(String.format("SELECT tablename::REGCLASS::OID AS tid FROM pg_catalog.pg_tables WHERE schemaname = '%s' and tablename = '%s';", schemaName, tableName))) {
            appendToMap(resultSet, context);
        }
    }
    
    private void getSchemaId(final Map<String, Object> context) throws SQLException {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format("select oid as scid from pg_catalog.pg_namespace where nspname = '%s';", schemaName))) {
            appendToMap(resultSet, context);
        }
    }
    
    private void fetchTableProperties(final Map<String, Object> context) {
        appendFirstRow(executeByTemplate(connection, context, "table/12_plus/properties.ftl"), context);
        context.put("coll_inherits", convertPgArrayToList(context.get("coll_inherits")));
        updateAutovacuumProperties(context);
        checkRlspolicySupport(context);
        setRowsCount(context);
        fetchPrivileges(context);
    }
    
    @SneakyThrows
    private Collection<String> convertPgArrayToList(final Object array) {
        return Arrays.stream((String[]) ((Array) array).getArray()).collect(Collectors.toList());
    }
    
    private void updateAutovacuumProperties(final Map<String, Object> context) {
        if (null == context.get("autovacuum_enabled")) {
            context.put("autovacuum_enabled", "x");
        } else if ("true".equalsIgnoreCase(context.get("autovacuum_enabled").toString())) {
            context.put("autovacuum_enabled", "t");
        } else {
            context.put("autovacuum_enabled", "f");
        }
        if (null == context.get("toast_autovacuum_enabled")) {
            context.put("toast_autovacuum_enabled", "x");
        } else if ("true".equalsIgnoreCase(context.get("toast_autovacuum_enabled").toString())) {
            context.put("toast_autovacuum_enabled", "t");
        } else {
            context.put("toast_autovacuum_enabled", "f");
        }
        context.put("autovacuum_custom", anyIsTrue(Arrays.asList(
                context.get("autovacuum_vacuum_threshold"),
                context.get("autovacuum_vacuum_scale_factor"),
                context.get("autovacuum_analyze_threshold"),
                context.get("autovacuum_analyze_scale_factor"),
                context.get("autovacuum_vacuum_cost_delay"),
                context.get("autovacuum_vacuum_cost_limit"),
                context.get("autovacuum_freeze_min_age"),
                context.get("autovacuum_freeze_max_age"),
                context.get("autovacuum_freeze_table_age"))) || "t".equals(context.get("autovacuum_enabled")) || "f".equals(context.get("autovacuum_enabled")));
        context.put("toast_autovacuum", anyIsTrue(Arrays.asList(
                context.get("toast_autovacuum_vacuum_threshold"),
                context.get("toast_autovacuum_vacuum_scale_factor"),
                context.get("toast_autovacuum_analyze_threshold"),
                context.get("toast_autovacuum_analyze_scale_factor"),
                context.get("toast_autovacuum_vacuum_cost_delay"),
                context.get("toast_autovacuum_vacuum_cost_limit"),
                context.get("toast_autovacuum_freeze_min_age"),
                context.get("toast_autovacuum_freeze_max_age"),
                context.get("toast_autovacuum_freeze_table_age"))) || "t".equals(context.get("toast_autovacuum_enabled")) || "f".equals(context.get("toast_autovacuum_enabled")));
    }
    
    private void checkRlspolicySupport(final Map<String, Object> context) {
        if (context.containsKey("rlspolicy")) {
            if (context.get("rlspolicy") instanceof String && "true".equals(context.get("rlspolicy"))) {
                context.put("rlspolicy", true);
            }
            if (context.get("forcerlspolicy") instanceof String && "true".equals(context.get("forcerlspolicy"))) {
                context.put("forcerlspolicy", true);
            }
        }
    }
    
    private void setRowsCount(final Map<String, Object> context) {
        context.put("rows_cnt", "0");
    }
    
    private void fetchPrivileges(final Map<String, Object> context) {
        context.put("acl", new LinkedList<>());
    }
    
    private boolean anyIsTrue(final List<Object> collection) {
        for (Object each : collection) {
            if (each instanceof Boolean && (Boolean) each) {
                return true;
            }
        }
        return false;
    }
    
    private void appendToMap(final ResultSet resultSet, final Map<String, Object> map) throws SQLException {
        List<Map<String, Object>> rows = getRows(resultSet);
        appendFirstRow(rows, map);
    }
    
    private void appendFirstRow(final List<Map<String, Object>> rows, final Map<String, Object> context) {
        for (Map<String, Object> each : rows) {
            context.putAll(each);
            break;
        }
    }
}
