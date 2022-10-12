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

import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereConstraint;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.spi.RuleBasedSchemaMetaDataDecorator;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.spi.RuleBasedSchemaMetaDataDecoratorFactory;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.SchemaMetaDataLoaderEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.SchemaMetaDataLoaderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ConstraintMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ViewMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SchemaMetaDataUtil;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Generic schema builder.
 */
public final class GenericSchemaBuilder {
    
    /**
     * Build generic schema.
     *
     * @param materials generic schema builder materials
     * @return generic schema map
     * @throws SQLException SQL exception
     */
    public static Map<String, ShardingSphereSchema> build(final GenericSchemaBuilderMaterials materials) throws SQLException {
        return build(getAllTableNames(materials.getRules()), materials);
    }
    
    /**
     * Build generic schema.
     *
     * @param tableNames table names
     * @param materials generic schema builder materials
     * @return generic schema map
     * @throws SQLException SQL exception
     */
    public static Map<String, ShardingSphereSchema> build(final Collection<String> tableNames, final GenericSchemaBuilderMaterials materials) throws SQLException {
        Map<String, SchemaMetaData> result = loadSchemas(tableNames, materials);
        if (!materials.getProtocolType().equals(materials.getStorageType())) {
            result = translate(result, materials);
        }
        return decorate(result, materials);
    }
    
    private static Collection<String> getAllTableNames(final Collection<ShardingSphereRule> rules) {
        return rules.stream().filter(each -> each instanceof TableContainedRule).flatMap(each -> ((TableContainedRule) each).getTables().stream()).collect(Collectors.toSet());
    }
    
    private static Map<String, SchemaMetaData> loadSchemas(final Collection<String> tableNames, final GenericSchemaBuilderMaterials materials) throws SQLException {
        boolean isCheckingMetaData = materials.getProps().getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED);
        Collection<SchemaMetaDataLoaderMaterials> schemaMetaDataLoaderMaterials = SchemaMetaDataUtil.getSchemaMetaDataLoaderMaterials(tableNames, materials, isCheckingMetaData);
        if (schemaMetaDataLoaderMaterials.isEmpty()) {
            return Collections.emptyMap();
        }
        return SchemaMetaDataLoaderEngine.load(schemaMetaDataLoaderMaterials, materials.getStorageType());
    }
    
    private static Map<String, SchemaMetaData> translate(final Map<String, SchemaMetaData> schemaMetaDataMap, final GenericSchemaBuilderMaterials materials) {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>();
        Collection<TableMetaData> tableMetaDataList = Optional.ofNullable(schemaMetaDataMap.get(
                DatabaseTypeEngine.getDefaultSchemaName(materials.getStorageType(), materials.getDefaultSchemaName()))).map(SchemaMetaData::getTables).orElseGet(Collections::emptyList);
        String frontendSchemaName = DatabaseTypeEngine.getDefaultSchemaName(materials.getProtocolType(), materials.getDefaultSchemaName());
        result.put(frontendSchemaName, new SchemaMetaData(frontendSchemaName, tableMetaDataList));
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, ShardingSphereSchema> decorate(final Map<String, SchemaMetaData> schemaMetaDataMap, final GenericSchemaBuilderMaterials materials) {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>(schemaMetaDataMap);
        for (Entry<ShardingSphereRule, RuleBasedSchemaMetaDataDecorator> entry : RuleBasedSchemaMetaDataDecoratorFactory.getInstances(materials.getRules()).entrySet()) {
            if (!(entry.getKey() instanceof TableContainedRule)) {
                continue;
            }
            result.putAll(entry.getValue().decorate(result, (TableContainedRule) entry.getKey(), materials));
        }
        return convertToSchemaMap(result, materials);
    }
    
    private static Map<String, ShardingSphereSchema> convertToSchemaMap(final Map<String, SchemaMetaData> schemaMetaDataMap, final GenericSchemaBuilderMaterials materials) {
        if (schemaMetaDataMap.isEmpty()) {
            return Collections.singletonMap(materials.getDefaultSchemaName(), new ShardingSphereSchema());
        }
        Map<String, ShardingSphereSchema> result = new ConcurrentHashMap<>(schemaMetaDataMap.size(), 1);
        for (Entry<String, SchemaMetaData> entry : schemaMetaDataMap.entrySet()) {
            Map<String, ShardingSphereTable> tables = convertToTableMap(entry.getValue().getTables());
            result.put(entry.getKey().toLowerCase(), new ShardingSphereSchema(tables, new LinkedHashMap<>()));
        }
        return result;
    }
    
    private static Map<String, ShardingSphereTable> convertToTableMap(final Collection<TableMetaData> tableMetaDataList) {
        Map<String, ShardingSphereTable> result = new LinkedHashMap<>(tableMetaDataList.size(), 1);
        for (TableMetaData each : tableMetaDataList) {
            Collection<ShardingSphereColumn> columns = convertToColumns(each.getColumns());
            Collection<ShardingSphereIndex> indexes = convertToIndexes(each.getIndexes());
            Collection<ShardingSphereConstraint> constraints = convertToConstraints(each.getConstrains());
            result.put(each.getName(), new ShardingSphereTable(each.getName(), columns, indexes, constraints));
        }
        return result;
    }
    
    private static Collection<ShardingSphereColumn> convertToColumns(final Collection<ColumnMetaData> columnMetaDataList) {
        Collection<ShardingSphereColumn> result = new LinkedList<>();
        for (ColumnMetaData each : columnMetaDataList) {
            result.add(new ShardingSphereColumn(each.getName(), each.getDataType(), each.isPrimaryKey(), each.isGenerated(), each.isCaseSensitive(), each.isVisible()));
        }
        return result;
    }
    
    private static Collection<ShardingSphereIndex> convertToIndexes(final Collection<IndexMetaData> indexMetaDataList) {
        Collection<ShardingSphereIndex> result = new LinkedList<>();
        for (IndexMetaData each : indexMetaDataList) {
            result.add(new ShardingSphereIndex(each.getName()));
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
    
    private static Map<String, ShardingSphereView> convertToViewMap(final Collection<ViewMetaData> viewMetaDataList) {
        Map<String, ShardingSphereView> result = new LinkedHashMap<>(viewMetaDataList.size(), 1);
        for (ViewMetaData each : viewMetaDataList) {
            result.put(each.getName(), new ShardingSphereView(each.getName(), each.getViewDefinition()));
        }
        return result;
    }
}
