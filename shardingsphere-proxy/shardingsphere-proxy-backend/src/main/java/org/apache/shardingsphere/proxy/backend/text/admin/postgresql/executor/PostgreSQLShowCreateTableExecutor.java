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

package org.apache.shardingsphere.proxy.backend.text.admin.postgresql.executor;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.proxy.backend.util.FreemarkerManager;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Show create database executor.
 */
@RequiredArgsConstructor
@Getter
public final class PostgreSQLShowCreateTableExecutor {
    
    @SneakyThrows
    public String getCreateSQL(final String tableName, final String schemaName, final Connection connection) {
        Map<String, Object> context = new HashMap<>();
        getDataBaseId(context, connection);
        getSchemaId(context, schemaName, connection);
        getTableId(context, schemaName, tableName, connection);
        fetchTableProperties(context, connection);
        return getReverseEngineeredSql(context, connection);
    }
    
    private String getReverseEngineeredSql(final Map<String, Object> context, final Connection connection) {
        return getReSqlForTable(context, connection);
    }
    
    private String getReSqlForTable(final Map<String, Object> context, final Connection connection) {
        formatter(context, connection);
        formatColumnList(context, connection);
        return getSqlFromTemplate(context, "table/12_plus/create.ftl");
    }
    
    private void formatColumnList(final Map<String, Object> context, final Connection connection) {
    }
    
    private void formatter(final Map<String, Object> context, final Connection connection) {
        if (null != context.get("seclabels")) {
        }
        getFormattedColumns(context, connection);
        addConstraintsToOutput(context, connection);
    }
    
    @SneakyThrows
    private void getFormattedColumns(final Map<String, Object> context, final Connection connection) {
        List<Map<String, Object>> allColumns = executeByTemplate(connection, context, "columns/12_plus/properties.ftl");
        Map<String, Object> editTypes = new HashMap<>();
        for (Map<String, Object> each : allColumns) {
            editTypes.put(each.get("atttypid").toString(), new LinkedList<>());
        }
        context.put("columns", allColumns);
        Map<String, Object> param = new HashMap<>();
        param.put("type_ids", String.join(",", editTypes.keySet()));
        if (!allColumns.isEmpty()) {
            for (Map<String, Object> each : executeByTemplate(connection, param, "columns/default/edit_mode_types_multi.ftl")) {
                editTypes.put(each.get("main_oid").toString(), each.get("edit_types"));
            }
            for (Map<String, Object> each : allColumns) {
                columnFormatter(each, editTypes.get(each.get("atttypid").toString()));
            }
        }
    }
    
    private void columnFormatter(final Map<String, Object> column, final Object editTypeList) throws SQLException {
        checkPrimaryColumn(column);
        fetchLengthPrecision(column);
        if (null != column.get("attoptions")) {
        }
        if (null != column.get("seclabels")) {
        }
        Set<String> editTypes = Arrays.stream((String[]) ((Array) editTypeList).getArray()).collect(Collectors.toSet());
        editTypes.add(column.get("cltype").toString());
        column.put("edit_types", editTypes);
    }
    
    private void addConstraintsToOutput(final Map<String, Object> context, final Connection connection) {
        Map<String, String> indexConstraints = new HashMap<>();
        indexConstraints.put("p", "primary_key");
        indexConstraints.put("u", "unique_constraint");
        for (Map.Entry<String, String> entry : indexConstraints.entrySet()) {
            Map<String, Object> param = new HashMap<>();
            param.put("did", context.get("did"));
            param.put("tid", context.get("tid"));
            param.put("cid", context.get("cid"));
            param.put("constraint_type", entry.getKey());
            List<Map<String, Object>> rows = executeByTemplate(connection, param, "index_constraint/11_plus/properties.ftl");
            for (Map<String, Object> each : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("cid", each.get("oid"));
                map.put("colcnt", each.get("col_count"));
                List<Map<String, Object>> list = executeByTemplate(connection, map, "index_constraint/default/get_costraint_cols.ftl");
                List<Map<String, Object>> columns = new LinkedList<>();
                for (Map<String, Object> col : list) {
                    Map<String, Object> r = new HashMap<>();
                    r.put("column", col.get("column"));
                    columns.add(r);
                }
                each.put("columns", columns);
                each.put("include", new LinkedList<>());
            }
            context.put(entry.getValue(), rows);
        }
        context.put("foreign_key", new LinkedList<>());
    }
    
    private void fetchLengthPrecision(final Map<String, Object> each) {
        each.put("attlen", null);
        each.put("attprecision", null);
    }
    
    private void checkPrimaryColumn(final Map<String, Object> column) {
        if (column.containsKey("attnum") && column.containsKey("indkey")) {
            if (Arrays.stream(column.get("indkey").toString().split(" ")).collect(Collectors.toList()).contains(column.get("attnum").toString())) {
                column.put("is_pk", true);
                column.put("is_primary_key", true);
            } else {
                column.put("is_pk", false);
                column.put("is_primary_key", false);
            }
        }
    }
    
    private void fetchTableProperties(final Map<String, Object> context, final Connection connection) throws SQLException, IOException, TemplateException {
        appendFirstRow(executeByTemplate(connection, context, "table/12_plus/properties.ftl"), context);
        updateAutovacuumProperties(context);
        checkRlspolicySupport(context);
        setRowsCount(context);
        fetchPrivileges(context);
    }
    
    private void fetchPrivileges(final Map<String, Object> context) {
        context.put("acl", new LinkedList<>());
    }
    
    private void setRowsCount(final Map<String, Object> context) {
        context.put("rows_cnt", "0");
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
    
    @SneakyThrows
    private String getSqlFromTemplate(final Map<String, Object> context, final String path) {
        Template template = FreemarkerManager.getInstance().getTemplateConfig().getTemplate(path);
        try (StringWriter result = new StringWriter()) {
            template.process(context, result);
            return result.toString();
        }
        
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
    
    private boolean anyIsTrue(final List<Object> collection) {
        for (Object each : collection) {
            if (each instanceof Boolean && (Boolean) each) {
                return true;
            }
        }
        return false;
    }
    
    private void getTableId(final Map<String, Object> context, final String schemaName, final String tableName, final Connection connection) throws SQLException {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement
                        .executeQuery(String.format("SELECT tablename::REGCLASS::OID AS tid FROM pg_catalog.pg_tables WHERE schemaname = '%s' and tablename = '%s';", schemaName, tableName))) {
            appendToMap(resultSet, context);
        }
    }
    
    private void getSchemaId(final Map<String, Object> context, final String schemaName, final Connection connection) throws SQLException {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format("select oid as scid from pg_catalog.pg_namespace where nspname = '%s';", schemaName))) {
            appendToMap(resultSet, context);
        }
    }
    
    private void getDataBaseId(final Map<String, Object> context, final Connection connection) throws SQLException {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format("select oid as did, datlastsysoid from pg_catalog.pg_database where datname = '%s';", connection.getCatalog()))) {
            appendToMap(resultSet, context);
        }
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
    
    private List<Map<String, Object>> getRows(final ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        List<Map<String, Object>> result = new LinkedList<>();
        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                row.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
            result.add(row);
        }
        return result;
    }
    
    @SneakyThrows
    private List<Map<String, Object>> executeByTemplate(final Connection connection, final Map<String, Object> param, final String path) {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(getSqlFromTemplate(param, path))) {
            return getRows(resultSet);
        }
    }
}
