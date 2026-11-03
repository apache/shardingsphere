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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SchemaMetaDataUtils;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generic schema builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class GenericSchemaBuilder {
    
    /**
     * Build generic schema.
     *
     * @param protocolType database type
     * @param material generic schema builder material
     * @return generic schema map
     * @throws SQLException SQL exception
     */
    public static Map<String, ShardingSphereSchema> build(final DatabaseType protocolType, final GenericSchemaBuilderMaterial material) throws SQLException {
        return build(getAllTableNames(material.getRules()), protocolType, material);
    }
    
    /**
     * Build generic schema.
     *
     * @param tableNames table names
     * @param protocolType database type
     * @param material generic schema builder material
     * @return generic schema map
     * @throws SQLException SQL exception
     */
    public static Map<String, ShardingSphereSchema> build(final Collection<String> tableNames, final DatabaseType protocolType, final GenericSchemaBuilderMaterial material) throws SQLException {
        return build(tableNames, protocolType, material, false);
    }
    
    private static Map<String, ShardingSphereSchema> build(final Collection<String> tableNames, final DatabaseType protocolType, final GenericSchemaBuilderMaterial material,
                                                           final boolean checkInventory) throws SQLException {
        Collection<MetaDataLoaderMaterial> loaderMaterials = SchemaMetaDataUtils.getMetaDataLoaderMaterials(tableNames, protocolType, material);
        Map<String, SchemaMetaData> result = loadSchemas(loaderMaterials);
        if (checkInventory) {
            checkMissingTables(tableNames, loaderMaterials, result, material.getRules());
        }
        if (!isSchemaCompatible(protocolType, material.getStorageUnits())) {
            result = translate(result, material, loaderMaterials);
        }
        return revise(result, material, protocolType);
    }
    
    private static Collection<String> getAllTableNames(final Collection<ShardingSphereRule> rules) {
        Collection<String> result = new HashSet<>();
        for (ShardingSphereRule each : rules) {
            each.getAttributes().findAttribute(TableMapperRuleAttribute.class).ifPresent(optional -> result.addAll(optional.getLogicTableNames()));
        }
        return result;
    }
    
    private static Map<String, SchemaMetaData> loadSchemas(final Collection<MetaDataLoaderMaterial> materials) throws SQLException {
        return materials.isEmpty() ? Collections.emptyMap() : MetaDataLoader.load(materials);
    }
    
    private static void checkMissingTables(final Collection<String> tableNames, final Collection<MetaDataLoaderMaterial> materials, final Map<String, SchemaMetaData> schemaMetaDataMap,
                                           final Collection<ShardingSphereRule> rules) {
        Map<String, Set<String>> requestedTables = getRequestedTables(materials);
        Map<String, Optional<String>> storageSchemaNames = getStorageSchemaNames(materials);
        Set<DataNode> loadedTables = schemaMetaDataMap.values().stream()
                .flatMap(each -> each.getTables().stream().map(table -> new DataNode(table.getStorageUnitName(), each.getName(), table.getName()))).collect(Collectors.toSet());
        DataNodes dataNodes = new DataNodes(rules);
        Set<String> missingTables = new LinkedHashSet<>();
        for (String each : tableNames) {
            if (dataNodes.isDataNodeTableNameLoadedFromStorage(each)) {
                addMissingTables(dataNodes.getDataNodes(each), requestedTables, storageSchemaNames, loadedTables, missingTables);
            }
        }
        if (!missingTables.isEmpty()) {
            log.warn("The following tables are missing from loaded metadata: {}", missingTables);
        }
    }
    
    private static Map<String, Set<String>> getRequestedTables(final Collection<MetaDataLoaderMaterial> materials) {
        Map<String, Set<String>> result = new LinkedHashMap<>();
        for (MetaDataLoaderMaterial each : materials) {
            result.computeIfAbsent(each.getStorageUnitName(), key -> new HashSet<>(each.getActualTableNames().size(), 1F)).addAll(each.getActualTableNames());
        }
        return result;
    }
    
    private static Map<String, Optional<String>> getStorageSchemaNames(final Collection<MetaDataLoaderMaterial> materials) {
        Map<String, Optional<String>> result = new LinkedHashMap<>();
        Set<String> storageUnitNames = new HashSet<>();
        for (MetaDataLoaderMaterial each : materials) {
            if (storageUnitNames.add(each.getStorageUnitName())) {
                DialectSchemaOption schemaOption = new DatabaseTypeRegistry(each.getStorageType()).getDialectDatabaseMetaData().getSchemaOption();
                if (schemaOption.isSchemaAvailable()) {
                    result.put(each.getStorageUnitName(), Optional.empty());
                } else if (!schemaOption.getDefaultSchema().isPresent()) {
                    result.put(each.getStorageUnitName(), Optional.of(each.getDefaultSchemaName()));
                }
            }
        }
        return result;
    }
    
    private static void addMissingTables(final Collection<DataNode> inventoriedDataNodes, final Map<String, Set<String>> requestedTables, final Map<String, Optional<String>> storageSchemaNames,
                                         final Set<DataNode> loadedTables, final Set<String> missingTables) {
        for (DataNode each : inventoriedDataNodes) {
            Set<String> requestedTableNames = requestedTables.get(each.getDataSourceName());
            if (null != requestedTableNames && requestedTableNames.contains(each.getTableName()) && storageSchemaNames.containsKey(each.getDataSourceName())) {
                String schemaName = storageSchemaNames.get(each.getDataSourceName()).orElse(each.getSchemaName());
                if (!loadedTables.contains(new DataNode(each.getDataSourceName(), schemaName, each.getTableName()))) {
                    missingTables.add(String.join(".", each.getDataSourceName(), each.getSchemaName(), each.getTableName()));
                }
            }
        }
    }
    
    private static boolean isSchemaCompatible(final DatabaseType protocolType, final Map<String, StorageUnit> storageUnits) {
        return isSameProtocolAndStorageTypes(protocolType, storageUnits) || isSchemaAvailable(protocolType)
                && storageUnits.values().stream().map(StorageUnit::getStorageType).allMatch(GenericSchemaBuilder::isSchemaAvailable);
    }
    
    private static boolean isSameProtocolAndStorageTypes(final DatabaseType protocolType, final Map<String, StorageUnit> storageUnits) {
        return storageUnits.values().stream().map(StorageUnit::getStorageType).allMatch(protocolType::equals);
    }
    
    private static boolean isSchemaAvailable(final DatabaseType databaseType) {
        return DatabaseTypedSPILoader.findService(DialectDatabaseMetaData.class, databaseType)
                .map(each -> each.getSchemaOption().isSchemaAvailable()).orElse(false);
    }
    
    private static Map<String, SchemaMetaData> translate(final Map<String, SchemaMetaData> schemaMetaDataMap, final GenericSchemaBuilderMaterial material,
                                                         final Collection<MetaDataLoaderMaterial> loaderMaterials) {
        Collection<TableMetaData> tableMetaDataList = new LinkedList<>();
        for (String each : loaderMaterials.stream().map(MetaDataLoaderMaterial::getDefaultSchemaName).distinct().collect(Collectors.toList())) {
            tableMetaDataList.addAll(Optional.ofNullable(schemaMetaDataMap.get(each)).map(SchemaMetaData::getTables).orElseGet(Collections::emptyList));
        }
        String frontendSchemaName = material.getDefaultSchemaName();
        return Collections.singletonMap(frontendSchemaName, new SchemaMetaData(frontendSchemaName, tableMetaDataList));
    }
    
    private static Map<String, ShardingSphereSchema> revise(final Map<String, SchemaMetaData> schemaMetaDataMap, final GenericSchemaBuilderMaterial material, final DatabaseType protocolType) {
        Collection<ShardingSphereRule> rules = material.getRules().stream()
                .filter(each -> each.getAttributes().findAttribute(TableMapperRuleAttribute.class).isPresent()).collect(Collectors.toList());
        return new MetaDataReviseEngine(rules, protocolType).revise(schemaMetaDataMap, material);
    }
    
    /**
     * Build generic schemas and warn about missing inventoried single tables.
     * Call this after constructing fresh database rules, before mutable data nodes contain SQL refresh candidates.
     *
     * @param protocolType database type
     * @param material generic schema builder material with freshly inventoried data nodes
     * @return generic schema map
     * @throws SQLException SQL exception
     */
    public static Map<String, ShardingSphereSchema> buildWithInventoryCheck(final DatabaseType protocolType, final GenericSchemaBuilderMaterial material) throws SQLException {
        return build(getAllTableNames(material.getRules()), protocolType, material, true);
    }
}
