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

package org.apache.shardingsphere.infra.metadata.database.schema.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.spi.RuleBasedSchemaMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.spi.RuleBasedSchemaMetaDataBuilderFactory;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Schema meta data loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaMetaDataLoader {
    
    /**
     * Load table meta data.
     *
     * @param tableNames table names
     * @param materials schema builder materials
     * @return table meta data map
     * @throws SQLException SQL exception
     */
    public static Map<String, SchemaMetaData> load(final Collection<String> tableNames, final GenericSchemaBuilderMaterials materials) throws SQLException {
        Map<String, SchemaMetaData> result = loadSchemasWithTableContainedRules(tableNames, materials);
        if (!materials.getProtocolType().equals(materials.getStorageType())) {
            result = translate(result, materials);
        }
        return decorate(result, materials);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, SchemaMetaData> loadSchemasWithTableContainedRules(final Collection<String> tableNames, final GenericSchemaBuilderMaterials materials) throws SQLException {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>();
        for (Entry<ShardingSphereRule, RuleBasedSchemaMetaDataBuilder> entry : RuleBasedSchemaMetaDataBuilderFactory.getInstances(materials.getRules()).entrySet()) {
            ShardingSphereRule rule = entry.getKey();
            if (rule instanceof TableContainedRule) {
                Collection<String> loadingTables = filterLoadingTables(tableNames, (TableContainedRule) rule, result.values());
                mergeSchemaMetaDataMap(result, entry.getValue().load(loadingTables, (TableContainedRule) rule, materials).values());
            }
        }
        return result;
    }
    
    private static Collection<String> filterLoadingTables(final Collection<String> tableNames, final TableContainedRule rule, final Collection<SchemaMetaData> loadedSchemaMetaDataList) {
        return tableNames.stream().filter(each -> rule.getTables().contains(each) && !containsTable(loadedSchemaMetaDataList, each)).collect(Collectors.toList());
    }
    
    private static boolean containsTable(final Collection<SchemaMetaData> schemaMetaDataList, final String tableName) {
        return schemaMetaDataList.stream().anyMatch(each -> each.getTables().containsKey(tableName));
    }
    
    private static void mergeSchemaMetaDataMap(final Map<String, SchemaMetaData> schemaMetaDataMap, final Collection<SchemaMetaData> addedSchemaMetaDataList) {
        for (SchemaMetaData each : addedSchemaMetaDataList) {
            SchemaMetaData schemaMetaData = schemaMetaDataMap.computeIfAbsent(each.getName(), key -> new SchemaMetaData(each.getName(), new LinkedHashMap<>()));
            schemaMetaData.getTables().putAll(each.getTables());
        }
    }
    
    private static Map<String, SchemaMetaData> translate(final Map<String, SchemaMetaData> schemaMetaDataMap, final GenericSchemaBuilderMaterials materials) {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>();
        Map<String, TableMetaData> tableMetaDataMap = Optional.ofNullable(schemaMetaDataMap.get(
                DatabaseTypeEngine.getDefaultSchemaName(materials.getStorageType(), materials.getDefaultSchemaName()))).map(SchemaMetaData::getTables).orElseGet(Collections::emptyMap);
        String frontendSchemaName = DatabaseTypeEngine.getDefaultSchemaName(materials.getProtocolType(), materials.getDefaultSchemaName());
        result.put(frontendSchemaName, new SchemaMetaData(frontendSchemaName, tableMetaDataMap));
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, SchemaMetaData> decorate(final Map<String, SchemaMetaData> schemaMetaDataMap, final GenericSchemaBuilderMaterials materials) throws SQLException {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>(schemaMetaDataMap);
        for (Entry<ShardingSphereRule, RuleBasedSchemaMetaDataBuilder> entry : RuleBasedSchemaMetaDataBuilderFactory.getInstances(materials.getRules()).entrySet()) {
            if (!(entry.getKey() instanceof TableContainedRule)) {
                continue;
            }
            result.putAll(entry.getValue().decorate(result, (TableContainedRule) entry.getKey(), materials));
        }
        return result;
    }
}
