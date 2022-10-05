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

import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.spi.RuleBasedSchemaMetaDataDecorator;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ConstraintMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.exception.metadata.InconsistentShardingTableMetaDataException;
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
            Collection<TableMetaData> tables = new LinkedList<>();
            for (Entry<String, Collection<TableMetaData>> tableEntry : getLogicTableMetaDataMap(entry.getValue(), rule).entrySet()) {
                if (isCheckingMetaData) {
                    checkUniformed(tableEntry.getKey(), tableEntry.getValue());
                }
                tables.add(tableEntry.getValue().iterator().next());
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
        for (TableMetaData each : schemaMetaData.getTables()) {
            String logicTableName = rule.findLogicTableByActualTable(each.getName()).orElse(each.getName());
            Collection<TableMetaData> tableMetaDataList = result.computeIfAbsent(logicTableName, key -> new LinkedList<>());
            tableMetaDataList.add(decorate(each, rule));
        }
        return result;
    }
    
    private void checkUniformed(final String logicTableName, final Collection<TableMetaData> tableMetaDataList) {
        TableMetaData sample = tableMetaDataList.iterator().next();
        Collection<TableMetaDataViolation> violations = tableMetaDataList.stream()
                .filter(each -> !sample.equals(each)).map(each -> new TableMetaDataViolation(each.getName(), each)).collect(Collectors.toList());
        if (!violations.isEmpty()) {
            throw new InconsistentShardingTableMetaDataException(logicTableName, violations);
        }
    }
    
    private Collection<ColumnMetaData> getColumnMetaDataList(final TableMetaData tableMetaData, final TableRule tableRule) {
        Collection<ColumnMetaData> result = new LinkedList<>();
        for (ColumnMetaData each : tableMetaData.getColumns()) {
            boolean generated = each.getName().equalsIgnoreCase(tableRule.getGenerateKeyColumn().orElse(null));
            result.add(new ColumnMetaData(each.getName(), each.getDataType(), each.isPrimaryKey(), generated, each.isCaseSensitive(), each.isVisible()));
        }
        return result;
    }
    
    private Collection<IndexMetaData> getIndexMetaDataList(final TableMetaData tableMetaData, final TableRule tableRule) {
        Collection<IndexMetaData> result = new HashSet<>();
        for (IndexMetaData each : tableMetaData.getIndexes()) {
            for (DataNode dataNode : tableRule.getActualDataNodes()) {
                getLogicIndex(each.getName(), dataNode.getTableName()).ifPresent(optional -> result.add(new IndexMetaData(optional)));
            }
        }
        return result;
    }
    
    private Collection<ConstraintMetaData> getConstraintMetaDataList(final TableMetaData tableMetaData, final ShardingRule shardingRule, final TableRule tableRule) {
        Collection<ConstraintMetaData> result = new HashSet<>();
        for (ConstraintMetaData each : tableMetaData.getConstrains()) {
            for (DataNode dataNode : tableRule.getActualDataNodes()) {
                String referencedTableName = each.getReferencedTableName();
                getLogicIndex(each.getName(), dataNode.getTableName()).ifPresent(optional -> result.add(
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
}
