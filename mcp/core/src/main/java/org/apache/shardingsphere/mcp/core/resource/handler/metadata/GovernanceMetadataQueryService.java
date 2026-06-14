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

package org.apache.shardingsphere.mcp.core.resource.handler.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSQLUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * Query governance metadata exposed by existing DistSQL.
 */
public final class GovernanceMetadataQueryService {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private static final TypeReference<Object> JSON_VALUE_TYPE = new TypeReference<>() {
    };
    
    private static final String REDACTED_VALUE = "******";
    
    /**
     * Query storage units.
     *
     * @param queryFacade query facade
     * @param databaseName database name
     * @return storage unit rows
     */
    public List<Map<String, Object>> queryStorageUnits(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, "", String.format("SHOW STORAGE UNITS FROM %s", format(databaseName))).stream()
                .map(this::redactStorageUnitRow).toList();
    }
    
    /**
     * Query one storage unit.
     *
     * @param queryFacade query facade
     * @param databaseName database name
     * @param storageUnitName storage unit name
     * @return storage unit row
     */
    public List<Map<String, Object>> queryStorageUnit(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String storageUnitName) {
        String actualStorageUnitName = WorkflowSQLUtils.normalizeIdentifier(storageUnitName);
        return queryStorageUnits(queryFacade, databaseName).stream()
                .filter(each -> actualStorageUnitName.equals(WorkflowSQLUtils.normalizeIdentifier(getValue(each, "name")))).toList();
    }
    
    /**
     * Query rules that use one storage unit.
     *
     * @param queryFacade query facade
     * @param databaseName database name
     * @param storageUnitName storage unit name
     * @return rule usage rows
     */
    public List<Map<String, Object>> queryRulesUsedStorageUnit(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String storageUnitName) {
        return queryFacade.query(databaseName, "", String.format("SHOW RULES USED STORAGE UNIT %s FROM %s", format(storageUnitName), format(databaseName)));
    }
    
    /**
     * Query single tables.
     *
     * @param queryFacade query facade
     * @param databaseName database name
     * @return single table rows
     */
    public List<Map<String, Object>> querySingleTables(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, "", String.format("SHOW SINGLE TABLES FROM %s", format(databaseName)));
    }
    
    /**
     * Query one single table.
     *
     * @param queryFacade query facade
     * @param databaseName database name
     * @param tableName table name
     * @return single table rows
     */
    public List<Map<String, Object>> querySingleTable(final MCPFeatureQueryFacade queryFacade, final String databaseName, final String tableName) {
        String actualTableName = WorkflowSQLUtils.normalizeIdentifier(tableName);
        return queryFacade.query(databaseName, "", String.format("SHOW SINGLE TABLE %s FROM %s", format(tableName), format(databaseName))).stream()
                .filter(each -> actualTableName.equals(WorkflowSQLUtils.normalizeIdentifier(getValue(each, "table_name")))).toList();
    }
    
    /**
     * Query default single table storage unit.
     *
     * @param queryFacade query facade
     * @param databaseName database name
     * @return default single table storage unit rows
     */
    public List<Map<String, Object>> queryDefaultSingleTableStorageUnit(final MCPFeatureQueryFacade queryFacade, final String databaseName) {
        return queryFacade.query(databaseName, "", String.format("SHOW DEFAULT SINGLE TABLE STORAGE UNIT FROM %s", format(databaseName)));
    }
    
    private Map<String, Object> redactStorageUnitRow(final Map<String, Object> row) {
        Map<String, Object> result = new LinkedHashMap<>(row.size(), 1F);
        for (Entry<String, Object> entry : row.entrySet()) {
            result.put(entry.getKey(), redactValue(entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    private Object redactValue(final String key, final Object value) {
        if (isSensitiveKey(key)) {
            return REDACTED_VALUE;
        }
        if ("other_attributes".equalsIgnoreCase(key)) {
            return redactOtherAttributes(value);
        }
        return value;
    }
    
    private Object redactOtherAttributes(final Object value) {
        RedactedValue result = redactNested(value);
        if (result.redacted()) {
            return result.value();
        }
        if (!(value instanceof String)) {
            return result.value();
        }
        String text = String.valueOf(value);
        if (containsSensitiveKeyToken(text)) {
            return REDACTED_VALUE;
        }
        return value;
    }
    
    private RedactedValue redactNested(final Object value) {
        if (value instanceof Map) {
            return redactMap((Map<?, ?>) value);
        }
        if (value instanceof Collection) {
            return redactCollection((Collection<?>) value);
        }
        if (value instanceof String) {
            return redactJsonString((String) value);
        }
        return new RedactedValue(value, false);
    }
    
    private RedactedValue redactMap(final Map<?, ?> value) {
        Map<String, Object> result = new LinkedHashMap<>(value.size(), 1F);
        boolean redacted = false;
        for (Entry<?, ?> entry : value.entrySet()) {
            String key = Objects.toString(entry.getKey(), "");
            if (isSensitiveKey(key)) {
                result.put(key, REDACTED_VALUE);
                redacted = true;
                continue;
            }
            RedactedValue nested = redactNested(entry.getValue());
            result.put(key, nested.value());
            redacted = redacted || nested.redacted();
        }
        return new RedactedValue(result, redacted);
    }
    
    private RedactedValue redactCollection(final Collection<?> value) {
        List<Object> result = new LinkedList<>();
        boolean redacted = false;
        for (Object each : value) {
            RedactedValue nested = redactNested(each);
            result.add(nested.value());
            redacted = redacted || nested.redacted();
        }
        return new RedactedValue(result, redacted);
    }
    
    private RedactedValue redactJsonString(final String value) {
        String text = value.trim();
        if (!text.startsWith("{") && !text.startsWith("[")) {
            return new RedactedValue(value, false);
        }
        try {
            return redactNested(OBJECT_MAPPER.readValue(text, JSON_VALUE_TYPE));
        } catch (final JsonProcessingException ignored) {
            return new RedactedValue(value, false);
        }
    }
    
    private boolean isSensitiveKey(final String key) {
        String actualKey = key.toLowerCase(Locale.ENGLISH);
        return actualKey.contains("password") || actualKey.contains("token") || actualKey.contains("secret") || actualKey.contains("credential");
    }
    
    private boolean containsSensitiveKeyToken(final String value) {
        String actualValue = value.toLowerCase(Locale.ENGLISH);
        return actualValue.contains("password") || actualValue.contains("token") || actualValue.contains("secret") || actualValue.contains("credential");
    }
    
    private String getValue(final Map<String, Object> row, final String key) {
        return Objects.toString(row.get(key), "");
    }
    
    private String format(final String identifier) {
        return WorkflowSQLUtils.formatDistSQLIdentifier(identifier);
    }
    
    private record RedactedValue(Object value, boolean redacted) {
    }
}
