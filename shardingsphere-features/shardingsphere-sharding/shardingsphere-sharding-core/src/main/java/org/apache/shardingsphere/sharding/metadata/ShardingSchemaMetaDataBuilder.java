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
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.RuleBasedSchemaMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.loader.SchemaMetaDataLoaderEngine;
import org.apache.shardingsphere.infra.metadata.schema.loader.TableMetaDataLoaderMaterial;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.ConstraintMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.schema.util.TableMetaDataUtil;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Schema meta data builder for sharding.
 */
public final class ShardingSchemaMetaDataBuilder implements RuleBasedSchemaMetaDataBuilder<ShardingRule> {
    
    @Override
    public Collection<SchemaMetaData> build(final Collection<String> tableNames, final ShardingRule rule, final SchemaBuilderMaterials materials) throws SQLException {
        Collection<String> needLoadTables = tableNames.stream().filter(each -> rule.findTableRule(each).isPresent() || rule.isBroadcastTable(each)).collect(Collectors.toList());
        if (needLoadTables.isEmpty()) {
            return Collections.emptyList();
        }
        boolean isCheckingMetaData = materials.getProps().getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED);
        Collection<TableMetaDataLoaderMaterial> tableMetaDataLoaderMaterials = TableMetaDataUtil.getTableMetaDataLoadMaterial(needLoadTables, materials, isCheckingMetaData);
        if (tableMetaDataLoaderMaterials.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<SchemaMetaData> schemaMetaData = SchemaMetaDataLoaderEngine.load(tableMetaDataLoaderMaterials, materials.getDatabaseType());
        Collection<SchemaMetaData> result = decorate(schemaMetaData, rule);
        if (isCheckingMetaData) {
            checkTableMetaData(result);
        }
        return result;
    }
    
    private Collection<SchemaMetaData> decorate(final Collection<SchemaMetaData> schemaMetaData, final ShardingRule rule) {
        Collection<SchemaMetaData> result = new LinkedList<>();
        for (SchemaMetaData each : schemaMetaData) {
            Map<String, TableMetaData> tables = new LinkedHashMap<>(each.getTables().size(), 1);
            for (Entry<String, TableMetaData> entry : each.getTables().entrySet()) {
                Optional<TableRule> tableRule = rule.findTableRuleByActualTable(entry.getKey());
                TableMetaData tableMetaData = entry.getValue();
                if (tableRule.isPresent()) {
                    tables.put(tableRule.get().getLogicTable(), createTableMetaData(rule, tableRule.get(), tableMetaData));
                } else {
                    tables.put(entry.getKey(), tableMetaData);
                }
            }
            result.add(new SchemaMetaData(each.getName(), tables));
        }
        return result;
    }
    
    private TableMetaData createTableMetaData(final ShardingRule rule, final TableRule tableRule, final TableMetaData tableMetaData) {
        Collection<ColumnMetaData> columnMetaDataList = getColumnMetaDataList(tableMetaData, tableRule);
        Collection<IndexMetaData> indexMetaDataList = getIndexMetaDataList(tableMetaData, tableRule);
        Collection<ConstraintMetaData> constraintMetaDataList = getConstraintMetaDataList(tableMetaData, rule, tableRule);
        return new TableMetaData(tableRule.getLogicTable(), columnMetaDataList, indexMetaDataList, constraintMetaDataList);
    }
    
    private void checkTableMetaData(final Collection<SchemaMetaData> schemaMetaData) {
        Map<String, List<SchemaMetaData>> schemaMetaDataGroup = schemaMetaData.stream().collect(Collectors.groupingBy(SchemaMetaData::getName));
        for (Entry<String, List<SchemaMetaData>> entry : schemaMetaDataGroup.entrySet()) {
            Map<String, Collection<TableMetaData>> logicTableMetaDataMap = getLogicTableMetaDataMap(entry.getValue());
            for (Entry<String, Collection<TableMetaData>> tableEntry : logicTableMetaDataMap.entrySet()) {
                checkUniformed(tableEntry.getKey(), tableEntry.getValue());
            }
        }
    }
    
    private Map<String, Collection<TableMetaData>> getLogicTableMetaDataMap(final Collection<SchemaMetaData> schemaMetaDataList) {
        Map<String, Collection<TableMetaData>> result = new LinkedHashMap<>();
        for (SchemaMetaData each : schemaMetaDataList) {
            for (Entry<String, TableMetaData> entry : each.getTables().entrySet()) {
                Collection<TableMetaData> tableMetaDataList = result.computeIfAbsent(entry.getKey(), key -> new LinkedList<>());
                tableMetaDataList.add(entry.getValue());
            }
        }
        return result;
    }
    
    private void checkUniformed(final String logicTableName, final Collection<TableMetaData> tableMetaDataList) {
        TableMetaData sample = tableMetaDataList.iterator().next();
        Collection<TableMetaDataViolation> violations = tableMetaDataList.stream()
                .filter(each -> !sample.equals(each)).map(each -> new TableMetaDataViolation(each.getName(), each)).collect(Collectors.toList());
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
                getLogicIndex(entry.getValue().getName(), each.getTableName()).ifPresent(logicIndex -> result.add(new IndexMetaData(logicIndex)));
            }
        }
        return result;
    }
    
    private Collection<ConstraintMetaData> getConstraintMetaDataList(final TableMetaData tableMetaData, final ShardingRule shardingRule, final TableRule tableRule) {
        Collection<ConstraintMetaData> result = new HashSet<>();
        for (Entry<String, ConstraintMetaData> entry : tableMetaData.getConstrains().entrySet()) {
            for (DataNode each : tableRule.getActualDataNodes()) {
                String referencedTableName = entry.getValue().getReferencedTableName();
                getLogicIndex(entry.getKey(), each.getTableName()).ifPresent(logicConstraint -> result.add(
                        new ConstraintMetaData(logicConstraint, shardingRule.findLogicTableByActualTable(referencedTableName).orElse(referencedTableName))));
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
