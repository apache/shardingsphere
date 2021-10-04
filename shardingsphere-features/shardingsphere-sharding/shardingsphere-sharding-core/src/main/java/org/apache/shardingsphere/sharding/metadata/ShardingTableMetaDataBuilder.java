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

package org.apache.shardingsphere.sharding.metadata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.TableMetaDataLoaderEngine;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.TableMetaDataLoaderMaterial;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.RuleBasedTableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.builder.util.TableMetaDataUtil;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Table meta data builder for sharding.
 */
public final class ShardingTableMetaDataBuilder implements RuleBasedTableMetaDataBuilder<ShardingRule> {
    
    @Override
    public Map<String, TableMetaData> load(final Collection<String> tableNames, final ShardingRule rule, final SchemaBuilderMaterials materials) throws SQLException {
        Collection<String> needLoadTables = tableNames.stream().filter(each -> rule.findTableRule(each).isPresent() || rule.isBroadcastTable(each)).collect(Collectors.toList());
        if (needLoadTables.isEmpty()) {
            return Collections.emptyMap();
        }
        boolean isCheckingMetaData = materials.getProps().getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED);
        Collection<TableMetaDataLoaderMaterial> tableMetaDataLoaderMaterials = TableMetaDataUtil.getTableMetaDataLoadMaterial(needLoadTables, materials, isCheckingMetaData);
        if (tableMetaDataLoaderMaterials.isEmpty()) {
            return Collections.emptyMap();
        }
        Collection<TableMetaData> tableMetaDataList = TableMetaDataLoaderEngine.load(tableMetaDataLoaderMaterials, materials.getDatabaseType());
        if (isCheckingMetaData) {
            checkTableMetaData(tableMetaDataList, rule);
        }
        return getTableMetaDataMap(tableMetaDataList, rule);
    }
    
    private void checkTableMetaData(final Collection<TableMetaData> tableMetaDataList, final ShardingRule rule) {
        Map<String, Collection<TableMetaData>> logicTableMetaDataMap = new LinkedHashMap<>();
        for (TableMetaData each : tableMetaDataList) {
            Optional<String> logicName = rule.findLogicTableByActualTable(each.getName());
            if (logicName.isPresent()) {
                Collection<TableMetaData> logicTableMetaDataList = logicTableMetaDataMap.getOrDefault(logicName.get(), new LinkedList<>());
                logicTableMetaDataList.add(each);
                logicTableMetaDataMap.putIfAbsent(logicName.get(), logicTableMetaDataList);
            }
        }
        for (Entry<String, Collection<TableMetaData>> entry : logicTableMetaDataMap.entrySet()) {
            checkUniformed(entry.getKey(), entry.getValue(), rule);
        }
    }
    
    private Map<String, TableMetaData> getTableMetaDataMap(final Collection<TableMetaData> tableMetaDataList, final ShardingRule rule) {
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        for (TableMetaData each : tableMetaDataList) {
            result.putIfAbsent(rule.findLogicTableByActualTable(each.getName()).orElse(each.getName()), each);
        }
        return result;
    }
    
    private void checkUniformed(final String logicTableName, final Collection<TableMetaData> tableMetaDataList, final ShardingRule shardingRule) {
        TableMetaData sample = decorate(logicTableName, tableMetaDataList.iterator().next(), shardingRule);
        Collection<TableMetaDataViolation> violations = tableMetaDataList.stream()
                .filter(each -> !sample.equals(decorate(logicTableName, each, shardingRule)))
                .map(each -> new TableMetaDataViolation(each.getName(), each)).collect(Collectors.toList());
        throwExceptionIfNecessary(violations, logicTableName);
    }
    
    private void throwExceptionIfNecessary(final Collection<TableMetaDataViolation> violations, final String logicTableName) {
        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder(
                    "Cannot get uniformed table structure for logic table `%s`, it has different meta data of actual tables are as follows:").append(System.lineSeparator());
            for (TableMetaDataViolation each : violations) {
                errorMessage.append("actual table: ").append(each.getActualTableName()).append(", meta data: ").append(each.getTableMetaData()).append(System.lineSeparator());
            }
            throw new ShardingSphereException(errorMessage.toString(), logicTableName);
        }
    }

    @Override
    public TableMetaData decorate(final String tableName, final TableMetaData tableMetaData, final ShardingRule shardingRule) {
        return shardingRule.findTableRule(tableName).map(
            tableRule -> new TableMetaData(tableName, getColumnMetaDataList(tableMetaData, tableRule), getIndexMetaDataList(tableMetaData, tableRule))).orElse(tableMetaData);
    }
    
    private Collection<ColumnMetaData> getColumnMetaDataList(final TableMetaData tableMetaData, final TableRule tableRule) {
        Collection<ColumnMetaData> result = new LinkedList<>();
        for (Entry<String, ColumnMetaData> entry : tableMetaData.getColumns().entrySet()) {
            boolean generated = entry.getKey().equalsIgnoreCase(tableRule.getGenerateKeyColumn().orElse(null));
            ColumnMetaData columnMetaData = entry.getValue();
            result.add(new ColumnMetaData(columnMetaData.getName(), columnMetaData.getDataType(), columnMetaData.isPrimaryKey(), generated, columnMetaData.isCaseSensitive()));
        }
        return result;
    }
    
    private Collection<IndexMetaData> getIndexMetaDataList(final TableMetaData tableMetaData, final TableRule tableRule) {
        Collection<IndexMetaData> result = new HashSet<>();
        for (Entry<String, IndexMetaData> entry : tableMetaData.getIndexes().entrySet()) {
            for (DataNode each : tableRule.getActualDataNodes()) {
                getLogicIndex(entry.getKey(), each.getTableName()).ifPresent(logicIndex -> result.add(new IndexMetaData(logicIndex)));
            }
        }
        return result;
    }
    
    private Optional<String> getLogicIndex(final String actualIndexName, final String actualTableName) {
        String indexNameSuffix = "_" + actualTableName;
        return actualIndexName.endsWith(indexNameSuffix) ? Optional.of(actualIndexName.replace(indexNameSuffix, "")) : Optional.empty();
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<ShardingRule> getTypeClass() {
        return ShardingRule.class;
    }
    
    @RequiredArgsConstructor
    @Getter
    private static final class TableMetaDataViolation {
        
        private final String actualTableName;
        
        private final TableMetaData tableMetaData;
    }
}
