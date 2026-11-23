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
import org.apache.shardingsphere.database.connector.core.exception.RuleAndStorageMetaDataMismatchedException;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.revise.SchemaTableMetaDataAggregator;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.schema.SchemaMetaDataReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SchemaMetaDataUtils;
import org.apache.shardingsphere.infra.metadata.identifier.IdentifierCasePolicyResolver;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
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
    
    /**
     * Build schemas for an explicit refresh, validating all related physical tables.
     *
     * @param protocolType database type
     * @param material generic schema builder material
     * @return generic schema map
     * @throws SQLException SQL exception
     */
    public static Map<String, ShardingSphereSchema> buildForRefresh(final DatabaseType protocolType, final GenericSchemaBuilderMaterial material) throws SQLException {
        return buildForRefresh(getAllTableNames(material.getRules()), protocolType, material);
    }
    
    /**
     * Build tables for an explicit refresh, validating all related physical tables.
     *
     * @param tableNames table names
     * @param protocolType database type
     * @param material generic schema builder material
     * @return generic schema map
     * @throws SQLException SQL exception
     */
    public static Map<String, ShardingSphereSchema> buildForRefresh(final Collection<String> tableNames, final DatabaseType protocolType,
                                                                    final GenericSchemaBuilderMaterial material) throws SQLException {
        Properties props = new Properties();
        props.putAll(material.getProps().getProps());
        props.setProperty(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED.getKey(), Boolean.TRUE.toString());
        GenericSchemaBuilderMaterial refreshMaterial = new GenericSchemaBuilderMaterial(material.getStorageUnits(), material.getRules(), new ConfigurationProperties(props),
                material.getDefaultSchemaName(), material.getIdentifierContext(), material.getRevisionCandidateSchemas());
        Collection<MetaDataLoaderMaterial> loaderMaterials = SchemaMetaDataUtils.getMetaDataLoaderMaterials(tableNames, protocolType, refreshMaterial);
        Map<String, SchemaMetaData> schemas = loadSchemas(loaderMaterials);
        checkRefreshInventory(tableNames, refreshMaterial, loaderMaterials, schemas);
        checkDistributedTablesAcrossSchemas(schemas, refreshMaterial);
        return revise(isSchemaCompatible(protocolType, refreshMaterial.getStorageUnits()) ? schemas : translate(schemas, refreshMaterial, loaderMaterials), refreshMaterial, protocolType);
    }
    
    private static void checkRefreshInventory(final Collection<String> tableNames, final GenericSchemaBuilderMaterial material,
                                              final Collection<MetaDataLoaderMaterial> loaderMaterials, final Map<String, SchemaMetaData> schemas) {
        Map<String, String> defaultSchemaNames = new LinkedHashMap<>(loaderMaterials.size(), 1F);
        loaderMaterials.forEach(each -> defaultSchemaNames.putIfAbsent(each.getStorageUnitName(), each.getDefaultSchemaName()));
        Set<DataNode> loadedTables = schemas.values().stream()
                .flatMap(each -> each.getTables().stream().map(table -> new DataNode(table.getStorageUnitName(), each.getName(), table.getName()))).collect(Collectors.toSet());
        DataNodes dataNodes = new DataNodes(material.getRules());
        Map<String, IdentifierCasePolicySet> storagePolicies = tableNames.stream().allMatch(dataNodes::isDataNodeTableNameLoadedFromStorage)
                ? Collections.emptyMap()
                : material.getStorageUnits().entrySet().stream().filter(entry -> defaultSchemaNames.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey,
                        entry -> IdentifierCasePolicyResolver.resolveStorage(entry.getValue().getStorageType(), entry.getValue().getDataSource())));
        for (String each : tableNames) {
            if (isIndependentTable(each, material, dataNodes)) {
                continue;
            }
            Map<String, IdentifierCasePolicySet> policies = dataNodes.isDataNodeTableNameLoadedFromStorage(each) ? Collections.emptyMap() : storagePolicies;
            Collection<DataNode> requiredTables = getRequiredTables(each, material, defaultSchemaNames, schemas, dataNodes, policies);
            Map<DataNode, Collection<DataNode>> matchingTables = getMatchingTables(requiredTables, loadedTables, policies);
            Map<DataNode, Long> matchCounts = matchingTables.values().stream().flatMap(Collection::stream).collect(Collectors.groupingBy(table -> table, Collectors.counting()));
            Collection<DataNode> missingTables = matchingTables.entrySet().stream()
                    .filter(entry -> 1 != entry.getValue().size() || 1L != matchCounts.get(entry.getValue().iterator().next()))
                    .map(Entry::getKey).collect(Collectors.toList());
            boolean relatedTablesLoaded = requiredTables.stream().anyMatch(loadedTables::contains) || matchingTables.values().stream().anyMatch(tables -> !tables.isEmpty());
            ShardingSpherePreconditions.checkState(missingTables.isEmpty() || !relatedTablesLoaded,
                    () -> new RuleAndStorageMetaDataMismatchedException(String.format("Missing physical table metadata for '%s': %s", each,
                            missingTables.stream().map(DataNode::format).collect(Collectors.joining(", ")))));
        }
    }
    
    private static boolean isIndependentTable(final String tableName, final GenericSchemaBuilderMaterial material, final DataNodes dataNodes) {
        return dataNodes.isDataNodeTableNameLoadedFromStorage(tableName) && material.getRules().stream()
                .noneMatch(each -> each.getAttributes().findAttribute(TableMapperRuleAttribute.class).map(optional -> optional.getDistributedTableNames().contains(tableName)).orElse(false));
    }
    
    private static Collection<DataNode> getRequiredTables(final String tableName, final GenericSchemaBuilderMaterial material, final Map<String, String> defaultSchemaNames,
                                                          final Map<String, SchemaMetaData> schemas, final DataNodes dataNodes, final Map<String, IdentifierCasePolicySet> policies) {
        Set<DataNode> result = new LinkedHashSet<>();
        Collection<DataNode> tableDataNodes = dataNodes.getDataNodes(tableName);
        for (DataNode each : tableDataNodes) {
            DatabaseType storageType = material.getStorageUnits().get(each.getDataSourceName()).getStorageType();
            Collection<String> schemaNames = getRequiredSchemaNames(tableName, each, storageType, material, defaultSchemaNames, schemas, tableDataNodes, policies);
            schemaNames.forEach(schemaName -> result.add(new DataNode(each.getDataSourceName(), schemaName, each.getTableName())));
        }
        return result;
    }
    
    private static Collection<String> getRequiredSchemaNames(final String tableName, final DataNode dataNode, final DatabaseType storageType, final GenericSchemaBuilderMaterial material,
                                                             final Map<String, String> defaultSchemaNames, final Map<String, SchemaMetaData> schemas, final Collection<DataNode> tableDataNodes,
                                                             final Map<String, IdentifierCasePolicySet> policies) {
        if (!new DatabaseTypeRegistry(storageType).getDialectDatabaseMetaData().getSchemaOption().isSchemaAvailable()) {
            return Collections.singleton(defaultSchemaNames.get(dataNode.getDataSourceName()));
        }
        if (null != dataNode.getSchemaName() && !"*".equals(dataNode.getSchemaName())) {
            return Collections.singleton(dataNode.getSchemaName());
        }
        Set<String> result = schemas.values().stream().filter(each -> containsPhysicalTable(each, tableDataNodes, policies)).map(SchemaMetaData::getName).collect(Collectors.toSet());
        material.getRevisionCandidateSchemas().stream().filter(each -> each.containsTable(tableName)).map(ShardingSphereSchema::getName).forEach(result::add);
        if (result.isEmpty()) {
            result.add(defaultSchemaNames.get(dataNode.getDataSourceName()));
        }
        return result;
    }
    
    private static boolean containsPhysicalTable(final SchemaMetaData schema, final Collection<DataNode> dataNodes, final Map<String, IdentifierCasePolicySet> policies) {
        return schema.getTables().stream().anyMatch(table -> dataNodes.stream()
                .anyMatch(dataNode -> dataNode.getDataSourceName().equals(table.getStorageUnitName())
                        && matchesIdentifier(dataNode, IdentifierScope.TABLE, dataNode.getTableName(), table.getName(), policies)));
    }
    
    private static boolean matchesIdentifier(final DataNode dataNode, final IdentifierScope scope, final String expectedName, final String actualName,
                                             final Map<String, IdentifierCasePolicySet> policies) {
        if (expectedName.equals(actualName)) {
            return true;
        }
        IdentifierCasePolicySet policySet = policies.get(dataNode.getDataSourceName());
        if (null == policySet) {
            return false;
        }
        IdentifierCasePolicy policy = policySet.getPolicy(scope);
        IdentifierValue identifier = new IdentifierValue(expectedName);
        return policy.matches(actualName, identifier.getValue(), identifier.getQuoteCharacter());
    }
    
    private static Map<DataNode, Collection<DataNode>> getMatchingTables(final Collection<DataNode> requiredTables, final Set<DataNode> loadedTables,
                                                                         final Map<String, IdentifierCasePolicySet> policies) {
        Map<DataNode, Collection<DataNode>> availableTables = loadedTables.stream().filter(each -> !requiredTables.contains(each))
                .collect(Collectors.groupingBy(each -> getTableLookupKey(each, policies), LinkedHashMap::new, Collectors.toCollection(LinkedList::new)));
        return requiredTables.stream().filter(each -> !loadedTables.contains(each)).collect(Collectors.toMap(each -> each,
                each -> availableTables.getOrDefault(getTableLookupKey(each, policies), Collections.emptyList()).stream()
                        .filter(actual -> matchesIdentifier(each, IdentifierScope.SCHEMA, each.getSchemaName(), actual.getSchemaName(), policies)
                                && matchesIdentifier(each, IdentifierScope.TABLE, each.getTableName(), actual.getTableName(), policies))
                        .collect(Collectors.toList()),
                (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private static DataNode getTableLookupKey(final DataNode dataNode, final Map<String, IdentifierCasePolicySet> policies) {
        IdentifierCasePolicySet policySet = policies.get(dataNode.getDataSourceName());
        if (null == policySet) {
            return dataNode;
        }
        String schemaName = policySet.getPolicy(IdentifierScope.SCHEMA).normalizeForLookup(dataNode.getSchemaName());
        return new DataNode(dataNode.getDataSourceName(), schemaName, policySet.getPolicy(IdentifierScope.TABLE).normalizeForLookup(dataNode.getTableName()));
    }
    
    private static void checkDistributedTablesAcrossSchemas(final Map<String, SchemaMetaData> schemas, final GenericSchemaBuilderMaterial material) {
        if (schemas.size() < 2) {
            return;
        }
        Collection<ShardingSphereRule> rules = material.getRules().stream().filter(each -> each.getAttributes().findAttribute(TableMapperRuleAttribute.class).isPresent()).collect(Collectors.toList());
        Set<String> distributedTableNames = rules.stream()
                .flatMap(each -> each.getAttributes().getAttribute(TableMapperRuleAttribute.class).getDistributedTableNames().stream()).collect(Collectors.toSet());
        if (distributedTableNames.isEmpty()) {
            return;
        }
        SchemaMetaDataReviseEngine reviseEngine = new SchemaMetaDataReviseEngine(rules, material.getProps(), material.getRevisionCandidateSchemas());
        Map<String, Collection<TableMetaData>> tables = schemas.values().stream().map(reviseEngine::revise).flatMap(each -> each.getTables().stream())
                .filter(each -> distributedTableNames.contains(each.getName()))
                .collect(Collectors.groupingBy(TableMetaData::getName, LinkedHashMap::new, Collectors.toCollection(LinkedList::new)));
        new SchemaTableMetaDataAggregator(true).aggregate(tables);
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
