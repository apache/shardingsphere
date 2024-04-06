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

package org.apache.shardingsphere.infra.metadata.database.schema.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.metadata.data.loader.MetaDataLoader;
import org.apache.shardingsphere.infra.database.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.ConstraintMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereConstraint;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SchemaMetaDataUtils;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Generic schema builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GenericSchemaBuilder {
    
    /**
     * Build generic schema.
     *
     * @param material generic schema builder material
     * @return generic schema map
     * @throws SQLException SQL exception
     */
    public static Map<String, ShardingSphereSchema> build(final GenericSchemaBuilderMaterial material) throws SQLException {
        return build(getAllTableNames(material.getRules()), material);
    }
    
    /**
     * Build generic schema.
     *
     * @param tableNames table names
     * @param material generic schema builder material
     * @return generic schema map
     * @throws SQLException SQL exception
     */
    public static Map<String, ShardingSphereSchema> build(final Collection<String> tableNames, final GenericSchemaBuilderMaterial material) throws SQLException {
        Map<String, SchemaMetaData> result = loadSchemas(tableNames, material);
        if (!material.isSameProtocolAndStorageTypes()) {
            result = translate(result, material);
        }
        return revise(result, material);
    }
    
    private static Collection<String> getAllTableNames(final Collection<ShardingSphereRule> rules) {
        Collection<String> result = new HashSet<>();
        for (ShardingSphereRule each : rules) {
            each.getAttributes().findAttribute(TableMapperRuleAttribute.class).ifPresent(mapperRule -> result.addAll(mapperRule.getLogicTableNames()));
        }
        return result;
    }
    
    private static Map<String, SchemaMetaData> loadSchemas(final Collection<String> tableNames, final GenericSchemaBuilderMaterial material) throws SQLException {
        boolean checkMetaDataEnable = material.getProps().getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED);
        Collection<MetaDataLoaderMaterial> materials = SchemaMetaDataUtils.getMetaDataLoaderMaterials(tableNames, material, checkMetaDataEnable);
        return materials.isEmpty() ? Collections.emptyMap() : MetaDataLoader.load(materials);
    }
    
    private static Map<String, SchemaMetaData> translate(final Map<String, SchemaMetaData> schemaMetaDataMap, final GenericSchemaBuilderMaterial material) {
        Collection<TableMetaData> tableMetaDataList = new LinkedList<>();
        for (DatabaseType each : material.getStorageTypes().values()) {
            String defaultSchemaName = new DatabaseTypeRegistry(each).getDefaultSchemaName(material.getDefaultSchemaName());
            tableMetaDataList.addAll(Optional.ofNullable(schemaMetaDataMap.get(defaultSchemaName)).map(SchemaMetaData::getTables).orElseGet(Collections::emptyList));
        }
        String frontendSchemaName = new DatabaseTypeRegistry(material.getProtocolType()).getDefaultSchemaName(material.getDefaultSchemaName());
        Map<String, SchemaMetaData> result = new LinkedHashMap<>();
        result.put(frontendSchemaName, new SchemaMetaData(frontendSchemaName, tableMetaDataList));
        return result;
    }
    
    private static Map<String, ShardingSphereSchema> revise(final Map<String, SchemaMetaData> schemaMetaDataMap, final GenericSchemaBuilderMaterial material) {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>(schemaMetaDataMap);
        result.putAll(new MetaDataReviseEngine(material.getRules().stream()
                .filter(each -> each.getAttributes().findAttribute(TableMapperRuleAttribute.class).isPresent()).collect(Collectors.toList())).revise(result, material));
        return convertToSchemaMap(result, material);
    }
    
    private static Map<String, ShardingSphereSchema> convertToSchemaMap(final Map<String, SchemaMetaData> schemaMetaDataMap, final GenericSchemaBuilderMaterial material) {
        if (schemaMetaDataMap.isEmpty()) {
            return Collections.singletonMap(material.getDefaultSchemaName(), new ShardingSphereSchema());
        }
        Map<String, ShardingSphereSchema> result = new ConcurrentHashMap<>(schemaMetaDataMap.size(), 1F);
        for (Entry<String, SchemaMetaData> entry : schemaMetaDataMap.entrySet()) {
            Map<String, ShardingSphereTable> tables = convertToTableMap(entry.getValue().getTables());
            result.put(entry.getKey().toLowerCase(), new ShardingSphereSchema(tables, new LinkedHashMap<>()));
        }
        return result;
    }
    
    private static Map<String, ShardingSphereTable> convertToTableMap(final Collection<TableMetaData> tableMetaDataList) {
        Map<String, ShardingSphereTable> result = new LinkedHashMap<>(tableMetaDataList.size(), 1F);
        for (TableMetaData each : tableMetaDataList) {
            Collection<ShardingSphereColumn> columns = convertToColumns(each.getColumns());
            Collection<ShardingSphereIndex> indexes = convertToIndexes(each.getIndexes());
            Collection<ShardingSphereConstraint> constraints = convertToConstraints(each.getConstraints());
            result.put(each.getName(), new ShardingSphereTable(each.getName(), columns, indexes, constraints, each.getType()));
        }
        return result;
    }
    
    private static Collection<ShardingSphereColumn> convertToColumns(final Collection<ColumnMetaData> columnMetaDataList) {
        Collection<ShardingSphereColumn> result = new LinkedList<>();
        for (ColumnMetaData each : columnMetaDataList) {
            result.add(new ShardingSphereColumn(each.getName(), each.getDataType(), each.isPrimaryKey(), each.isGenerated(), each.isCaseSensitive(), each.isVisible(), each.isUnsigned(),
                    each.isNullable()));
        }
        return result;
    }
    
    private static Collection<ShardingSphereIndex> convertToIndexes(final Collection<IndexMetaData> indexMetaDataList) {
        Collection<ShardingSphereIndex> result = new LinkedList<>();
        for (IndexMetaData each : indexMetaDataList) {
            ShardingSphereIndex index = new ShardingSphereIndex(each.getName());
            index.getColumns().addAll(each.getColumns());
            index.setUnique(each.isUnique());
            result.add(index);
        }
        return result;
    }
    
    private static Collection<ShardingSphereConstraint> convertToConstraints(final Collection<ConstraintMetaData> constraintMetaDataList) {
        Collection<ShardingSphereConstraint> result = new LinkedList<>();
        for (ConstraintMetaData each : constraintMetaDataList) {
            result.add(new ShardingSphereConstraint(each.getName(), each.getReferencedTableName()));
        }
        return result;
    }
}
