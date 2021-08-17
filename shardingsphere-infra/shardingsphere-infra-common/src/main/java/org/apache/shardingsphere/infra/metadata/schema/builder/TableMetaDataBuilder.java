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
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.RuleBasedTableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Table meta data builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableMetaDataBuilder {
    
    static {
        ShardingSphereServiceLoader.register(RuleBasedTableMetaDataBuilder.class);
    }
    
    /**
     * Build table meta data.
     *
     * @param tableName table name
     * @param materials schema builder materials
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public static Optional<TableMetaData> build(final String tableName, final SchemaBuilderMaterials materials) throws SQLException {
        Optional<TableMetaData> tableMetaData = load(tableName, materials);
        return tableMetaData.map(optional -> decorate(tableName, optional, materials.getRules()));
    }
    
    /**
     * Load physical table metadata.
     * 
     * @param tableName table name
     * @param materials schema builder materials
     * @return table meta data
     * @throws SQLException SQL exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Optional<TableMetaData> load(final String tableName, final SchemaBuilderMaterials materials) throws SQLException {
        DataNodes dataNodes = new DataNodes(materials.getRules());
        for (Entry<ShardingSphereRule, RuleBasedTableMetaDataBuilder> entry : OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, materials.getRules()).entrySet()) {
            if (entry.getKey() instanceof TableContainedRule) {
                TableContainedRule rule = (TableContainedRule) entry.getKey();
                RuleBasedTableMetaDataBuilder loader = entry.getValue();
                Optional<TableMetaData> result = loader.load(tableName, materials.getDatabaseType(), materials.getDataSourceMap(), dataNodes, rule, materials.getProps());
                if (result.isPresent()) {
                    TableMetaData tableMetaData = new TableMetaData(tableName, result.get().getColumns().values(), result.get().getIndexes().values());
                    return Optional.of(tableMetaData);
                }
            }
        }
        return Optional.empty();
    }
    
    /**
     * Load logic table metadata.
     *
     * @param materials schema builder materials
     * @param executorService executorService
     * @return table meta data collection
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public static Collection<TableMetaData> loadLogicTables(final SchemaBuilderMaterials materials, final ExecutorService executorService) throws SQLException {
        Collection<TableMetaData> result = new LinkedList<>();
        for (Entry<ShardingSphereRule, RuleBasedTableMetaDataBuilder> entry : OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, materials.getRules()).entrySet()) {
            if (entry.getKey() instanceof TableContainedRule) {
                loadTableContainedRuleTables(materials, executorService, result, entry);
            }
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void loadTableContainedRuleTables(final SchemaBuilderMaterials materials, final ExecutorService executorService, final Collection<TableMetaData> result,
                                                     final Entry<ShardingSphereRule, RuleBasedTableMetaDataBuilder> ruleBuilderEntry) throws SQLException {
        TableContainedRule rule = (TableContainedRule) ruleBuilderEntry.getKey();
        RuleBasedTableMetaDataBuilder loader = ruleBuilderEntry.getValue();
        Collection<String> loadedTables = result.stream().map(TableMetaData::getName).collect(Collectors.toSet());
        Collection<String> needLoadTables = rule.getTables().stream().filter(each -> !loadedTables.contains(each)).collect(Collectors.toList());
        if (!needLoadTables.isEmpty()) {
            Map<String, TableMetaData> tableMetaDataMap = loader.load(needLoadTables, rule, materials, executorService);
            result.addAll(tableMetaDataMap.entrySet().stream()
                    .map(entry -> new TableMetaData(entry.getKey(), entry.getValue().getColumns().values(), entry.getValue().getIndexes().values())).collect(Collectors.toList()));
        }
    }
    
    /**
     * Load logic table metadata.
     * @param tableName table name
     * @param tableMetaData table meta data
     * @param rules shardingSphere rules
     * @return table meta data
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static TableMetaData decorate(final String tableName, final TableMetaData tableMetaData, final Collection<ShardingSphereRule> rules) {
        TableMetaData result = null;
        for (Entry<ShardingSphereRule, RuleBasedTableMetaDataBuilder> entry : OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).entrySet()) {
            if (entry.getKey() instanceof TableContainedRule) {
                result = entry.getValue().decorate(tableName, null == result ? tableMetaData : result, (TableContainedRule) entry.getKey());
            }
        }
        return Optional.ofNullable(result).orElse(tableMetaData);
    }
}
