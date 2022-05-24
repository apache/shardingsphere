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

package org.apache.shardingsphere.infra.metadata.schema.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.RuleBasedSchemaMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Table meta data builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableMetaDataBuilder {
    
    /**
     * Load table metadata.
     *
     * @param tableNames table name collection
     * @param materials schema builder materials
     * @return table meta data map
     * @throws SQLException SQL exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map<String, SchemaMetaData> load(final Collection<String> tableNames, final SchemaBuilderMaterials materials) throws SQLException {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>();
        for (Entry<ShardingSphereRule, RuleBasedSchemaMetaDataBuilder> entry : RuleBasedSchemaMetaDataBuilderFactory.getInstances(materials.getRules()).entrySet()) {
            if (!(entry.getKey() instanceof TableContainedRule)) {
                continue;
            }
            Collection<String> needLoadTables = getNeedLoadTables(tableNames, result.values(), (TableContainedRule) entry.getKey());
            Map<String, SchemaMetaData> schemaMetaDataMap = entry.getValue().load(needLoadTables, (TableContainedRule) entry.getKey(), materials);
            mergeSchemaMetaDataMap(result, schemaMetaDataMap.values());
        }
        return decorate(result, materials);
    }
    
    private static Collection<String> getNeedLoadTables(final Collection<String> tableNames, final Collection<SchemaMetaData> schemaMetaDataList, final TableContainedRule rule) {
        Collection<String> result = new LinkedList<>();
        for (String each : tableNames) {
            if (rule.getTables().contains(each) && !isSchemaContainsTable(schemaMetaDataList, each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private static boolean isSchemaContainsTable(final Collection<SchemaMetaData> schemaMetaDataList, final String tableName) {
        for (SchemaMetaData each : schemaMetaDataList) {
            if (each.getTables().containsKey(tableName)) {
                return true;
            }
        }
        return false;
    }
    
    private static void mergeSchemaMetaDataMap(final Map<String, SchemaMetaData> schemaMetaDataMap, final Collection<SchemaMetaData> addedSchemaMetaDataList) {
        for (SchemaMetaData each : addedSchemaMetaDataList) {
            SchemaMetaData schemaMetaData = schemaMetaDataMap.computeIfAbsent(each.getName(), key -> new SchemaMetaData(each.getName(), new LinkedHashMap<>()));
            schemaMetaData.getTables().putAll(each.getTables());
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, SchemaMetaData> decorate(final Map<String, SchemaMetaData> schemaMetaDataMap, final SchemaBuilderMaterials materials) throws SQLException {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>(convertToFrontendSchemaMetaDataMap(schemaMetaDataMap, materials));
        for (Entry<ShardingSphereRule, RuleBasedSchemaMetaDataBuilder> entry : RuleBasedSchemaMetaDataBuilderFactory.getInstances(materials.getRules()).entrySet()) {
            if (!(entry.getKey() instanceof TableContainedRule)) {
                continue;
            }
            result.putAll(entry.getValue().decorate(result, (TableContainedRule) entry.getKey(), materials));
        }
        return result;
    }
    
    private static Map<String, SchemaMetaData> convertToFrontendSchemaMetaDataMap(final Map<String, SchemaMetaData> schemaMetaDataMap, final SchemaBuilderMaterials materials) {
        DatabaseType protocolType = materials.getProtocolType();
        DatabaseType storageType = materials.getStorageType();
        if (protocolType.equals(storageType)) {
            return schemaMetaDataMap;
        }
        Map<String, SchemaMetaData> result = new LinkedHashMap<>();
        Map<String, TableMetaData> tableMetaDataMap = Optional.ofNullable(schemaMetaDataMap.get(
                DatabaseTypeEngine.getDefaultSchemaName(storageType, materials.getDefaultSchemaName()))).map(SchemaMetaData::getTables).orElseGet(Collections::emptyMap);
        String frontendSchemaName = DatabaseTypeEngine.getDefaultSchemaName(protocolType, materials.getDefaultSchemaName());
        result.put(frontendSchemaName, new SchemaMetaData(frontendSchemaName, tableMetaDataMap));
        return result;
    }
}
