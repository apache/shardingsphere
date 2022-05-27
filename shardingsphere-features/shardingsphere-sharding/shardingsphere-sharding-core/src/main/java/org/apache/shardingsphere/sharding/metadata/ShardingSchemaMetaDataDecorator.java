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
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.spi.RuleBasedSchemaMetaDataDecorator;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ConstraintMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Schema meta data decorator for sharding.
 */
public final class ShardingSchemaMetaDataDecorator implements RuleBasedSchemaMetaDataDecorator<ShardingRule> {
    
    @Override
    public Map<String, SchemaMetaData> decorate(final Map<String, SchemaMetaData> schemaMetaDataMap, final ShardingRule rule, final GenericSchemaBuilderMaterials materials) {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>(schemaMetaDataMap.size(), 1);
        boolean isCheckingMetaData = materials.getProps().getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED);
        for (Entry<String, SchemaMetaData> entry : schemaMetaDataMap.entrySet()) {
            Map<String, TableMetaData> tables = new LinkedHashMap<>(entry.getValue().getTables().size(), 1);
            for (Entry<String, Collection<TableMetaData>> tableEntry : getLogicTableMetaDataMap(entry.getValue(), rule).entrySet()) {
                if (isCheckingMetaData) {
                    checkUniformed(tableEntry.getKey(), tableEntry.getValue());
                }
                tables.put(tableEntry.getKey(), tableEntry.getValue().iterator().next());
            }
            result.put(entry.getKey(), new SchemaMetaData(entry.getKey(), tables));
        }
        return result;
    }
    
    private TableMetaData decorate(final TableMetaData tableMetaData, final ShardingRule rule) {
        return rule.findTableRuleByActualTable(tableMetaData.getName()).map(optional -> createTableMetaData(rule, optional, tableMetaData)).orElse(tableMetaData);
    }
    
    private TableMetaData createTableMetaData(final ShardingRule rule, final TableRule tableRule, final TableMetaData tableMetaData) {
        Collection<ColumnMetaData> columnMetaDataList = getColumnMetaDataList(tableMetaData, tableRule);
        Collection<IndexMetaData> indexMetaDataList = getIndexMetaDataList(tableMetaData, tableRule);
        Collection<ConstraintMetaData> constraintMetaDataList = getConstraintMetaDataList(tableMetaData, rule, tableRule);
        return new TableMetaData(tableRule.getLogicTable(), columnMetaDataList, indexMetaDataList, constraintMetaDataList);
    }
    
    private Map<String, Collection<TableMetaData>> getLogicTableMetaDataMap(final SchemaMetaData schemaMetaData, final ShardingRule rule) {
        Map<String, Collection<TableMetaData>> result = new LinkedHashMap<>();
        for (Entry<String, TableMetaData> entry : schemaMetaData.getTables().entrySet()) {
            String logicTableName = rule.findLogicTableByActualTable(entry.getKey()).orElse(entry.getKey());
            Collection<TableMetaData> tableMetaDataList = result.computeIfAbsent(logicTableName, key -> new LinkedList<>());
            tableMetaDataList.add(decorate(entry.getValue(), rule));
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
                getLogicIndex(entry.getValue().getName(), each.getTableName()).ifPresent(optional -> result.add(new IndexMetaData(optional)));
            }
        }
        return result;
    }
    
    private Collection<ConstraintMetaData> getConstraintMetaDataList(final TableMetaData tableMetaData, final ShardingRule shardingRule, final TableRule tableRule) {
        Collection<ConstraintMetaData> result = new HashSet<>();
        for (Entry<String, ConstraintMetaData> entry : tableMetaData.getConstrains().entrySet()) {
            for (DataNode each : tableRule.getActualDataNodes()) {
                String referencedTableName = entry.getValue().getReferencedTableName();
                getLogicIndex(entry.getKey(), each.getTableName()).ifPresent(optional -> result.add(
                        new ConstraintMetaData(optional, shardingRule.findLogicTableByActualTable(referencedTableName).orElse(referencedTableName))));
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
