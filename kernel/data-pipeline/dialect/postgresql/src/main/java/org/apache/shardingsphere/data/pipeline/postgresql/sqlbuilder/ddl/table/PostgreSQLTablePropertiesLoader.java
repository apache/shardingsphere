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

package org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.table;

import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.PostgreSQLDDLTemplateExecutor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Table properties loader for PostgreSQL.
 */
public final class PostgreSQLTablePropertiesLoader {
    
    private final String tableName;
    
    private final String schemaName;
    
    private final PostgreSQLDDLTemplateExecutor templateExecutor;
    
    public PostgreSQLTablePropertiesLoader(final Connection connection, final String tableName, final String schemaName, final int majorVersion, final int minorVersion) {
        this.tableName = tableName;
        this.schemaName = schemaName;
        templateExecutor = new PostgreSQLDDLTemplateExecutor(connection, majorVersion, minorVersion);
    }
    
    /**
     * Load table properties.
     *
     * @return loaded table properties
     * @throws SQLException SQL exception
     */
    public Map<String, Object> load() throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>(fetchDatabaseId());
        result.putAll(fetchSchemaId());
        result.putAll(fetchTableId());
        fetchTableProperties(result);
        return result;
    }
    
    private Map<String, Object> fetchDatabaseId() throws SQLException {
        Map<String, Object> params = Collections.singletonMap("databaseName", templateExecutor.getConnection().getCatalog());
        return templateExecutor.executeByTemplateForSingleRow(params, "component/table/%s/get_database_id.ftl");
    }
    
    private Map<String, Object> fetchSchemaId() {
        Map<String, Object> params = Collections.singletonMap("schemaName", schemaName);
        return templateExecutor.executeByTemplateForSingleRow(params, "component/table/%s/get_schema_id.ftl");
    }
    
    private Map<String, Object> fetchTableId() {
        Map<String, Object> params = new LinkedHashMap<>(2, 1F);
        params.put("schemaName", schemaName);
        params.put("tableName", tableName);
        return templateExecutor.executeByTemplateForSingleRow(params, "component/table/%s/get_table_id.ftl");
    }
    
    private void fetchTableProperties(final Map<String, Object> context) throws SQLException {
        context.putAll(templateExecutor.executeByTemplateForSingleRow(context, "component/table/%s/properties.ftl"));
        updateAutoVacuumProperties(context);
        updateRlspolicySupport(context);
        templateExecutor.formatSecurityLabels(context);
    }
    
    private void updateAutoVacuumProperties(final Map<String, Object> context) {
        context.put("autovacuum_enabled", getAutoVacuumEnabled(context.get("autovacuum_enabled")));
        context.put("toast_autovacuum_enabled", getAutoVacuumEnabled(context.get("toast_autovacuum_enabled")));
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
    
    private String getAutoVacuumEnabled(final Object autoVacuumEnabled) {
        if (null == autoVacuumEnabled) {
            return "x";
        }
        if (Boolean.parseBoolean(autoVacuumEnabled.toString())) {
            return "t";
        }
        return "f";
    }
    
    private boolean anyIsTrue(final Collection<Object> collection) {
        return collection.stream().anyMatch(each -> each instanceof Boolean && (Boolean) each);
    }
    
    private void updateRlspolicySupport(final Map<String, Object> context) {
        if (context.containsKey("rlspolicy")) {
            if (context.get("rlspolicy") instanceof String && Boolean.parseBoolean(context.get("rlspolicy").toString())) {
                context.put("rlspolicy", true);
            }
            if (context.get("forcerlspolicy") instanceof String && Boolean.parseBoolean(context.get("forcerlspolicy").toString())) {
                context.put("forcerlspolicy", true);
            }
        }
    }
}
