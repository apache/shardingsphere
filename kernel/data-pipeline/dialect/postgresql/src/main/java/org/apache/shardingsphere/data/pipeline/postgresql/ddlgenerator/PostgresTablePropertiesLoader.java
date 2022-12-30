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

package org.apache.shardingsphere.data.pipeline.postgresql.ddlgenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Postgres table properties loader.
 */
public final class PostgresTablePropertiesLoader extends AbstractPostgresDDLAdapter {
    
    private final String tableName;
    
    private final String schemaName;
    
    public PostgresTablePropertiesLoader(final Connection connection, final String tableName, final String schemaName, final int majorVersion, final int minorVersion) {
        super(connection, majorVersion, minorVersion);
        this.tableName = tableName;
        this.schemaName = schemaName;
    }
    
    /**
     * Load table properties.
     *
     * @return loaded table properties
     * @throws SQLException SQL exception
     */
    public Map<String, Object> load() throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        fetchDataBaseId(result);
        fetchSchemaId(result);
        fetchTableId(result);
        fetchTableProperties(result);
        return result;
    }
    
    private void fetchDataBaseId(final Map<String, Object> context) throws SQLException {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("databaseName", getConnection().getCatalog());
        appendFirstRow(executeByTemplate(params, "component/table/%s/get_database_id.ftl"), context);
    }
    
    private void fetchTableId(final Map<String, Object> context) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("schemaName", schemaName);
        params.put("tableName", tableName);
        appendFirstRow(executeByTemplate(params, "component/table/%s/get_table_id.ftl"), context);
    }
    
    private void fetchSchemaId(final Map<String, Object> context) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("schemaName", schemaName);
        appendFirstRow(executeByTemplate(params, "component/table/%s/get_schema_id.ftl"), context);
    }
    
    private void fetchTableProperties(final Map<String, Object> context) throws SQLException {
        appendFirstRow(executeByTemplate(context, "component/table/%s/properties.ftl"), context);
        updateAutovacuumProperties(context);
        checkRlspolicySupport(context);
        formatSecurityLabels(context);
    }
    
    private void updateAutovacuumProperties(final Map<String, Object> context) {
        if (null == context.get("autovacuum_enabled")) {
            context.put("autovacuum_enabled", "x");
        } else if (Boolean.TRUE.toString().equalsIgnoreCase(context.get("autovacuum_enabled").toString())) {
            context.put("autovacuum_enabled", "t");
        } else {
            context.put("autovacuum_enabled", "f");
        }
        if (null == context.get("toast_autovacuum_enabled")) {
            context.put("toast_autovacuum_enabled", "x");
        } else if (Boolean.TRUE.toString().equalsIgnoreCase(context.get("toast_autovacuum_enabled").toString())) {
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
            if (context.get("rlspolicy") instanceof String && Boolean.TRUE.toString().equals(context.get("rlspolicy"))) {
                context.put("rlspolicy", true);
            }
            if (context.get("forcerlspolicy") instanceof String && Boolean.TRUE.toString().equals(context.get("forcerlspolicy"))) {
                context.put("forcerlspolicy", true);
            }
        }
    }
    
    private boolean anyIsTrue(final Collection<Object> collection) {
        for (Object each : collection) {
            if (each instanceof Boolean && (Boolean) each) {
                return true;
            }
        }
        return false;
    }
    
    private void appendFirstRow(final Collection<Map<String, Object>> rows, final Map<String, Object> context) {
        for (Map<String, Object> each : rows) {
            context.putAll(each);
            break;
        }
    }
}
