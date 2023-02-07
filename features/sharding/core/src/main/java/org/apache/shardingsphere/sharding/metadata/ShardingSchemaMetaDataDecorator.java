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
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.table.TableMetaDataReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.spi.RuleBasedSchemaMetaDataDecorator;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.exception.metadata.InconsistentShardingTableMetaDataException;
import org.apache.shardingsphere.sharding.metadata.reviser.ShardingColumnGeneratedReviser;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Schema meta data decorator for sharding.
 */
public final class ShardingSchemaMetaDataDecorator implements RuleBasedSchemaMetaDataDecorator<ShardingRule> {
    
    @Override
    public Map<String, SchemaMetaData> decorate(final Map<String, SchemaMetaData> schemaMetaDataMap, final ShardingRule rule, final GenericSchemaBuilderMaterial material) {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>(schemaMetaDataMap.size(), 1);
        boolean checkTableMetaDataEnabled = material.getProps().getValue(ConfigurationPropertyKey.CHECK_TABLE_META_DATA_ENABLED);
        for (Entry<String, SchemaMetaData> entry : schemaMetaDataMap.entrySet()) {
            Collection<TableMetaData> tables = new LinkedList<>();
            for (Entry<String, Collection<TableMetaData>> tableEntry : getLogicTableMetaDataMap(entry.getValue(), rule).entrySet()) {
                if (checkTableMetaDataEnabled) {
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
        return new TableMetaDataReviseEngine<>(rule).revise(tableMetaData, Collections.singleton(new ShardingColumnGeneratedReviser(tableRule)));
    }
    
    private Map<String, Collection<TableMetaData>> getLogicTableMetaDataMap(final SchemaMetaData schemaMetaData, final ShardingRule rule) {
        Map<String, Collection<TableMetaData>> result = new LinkedHashMap<>();
        for (TableMetaData each : schemaMetaData.getTables()) {
            String logicTableName = rule.findLogicTableByActualTable(each.getName()).orElse(each.getName());
            result.computeIfAbsent(logicTableName, key -> new LinkedList<>()).add(decorate(each, rule));
        }
        return result;
    }
    
    private void checkUniformed(final String logicTableName, final Collection<TableMetaData> tableMetaDataList) {
        TableMetaData sample = tableMetaDataList.iterator().next();
        Collection<TableMetaDataViolation> violations = tableMetaDataList.stream()
                .filter(each -> !sample.equals(each)).map(each -> new TableMetaDataViolation(each.getName(), each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(violations.isEmpty(), () -> new InconsistentShardingTableMetaDataException(logicTableName, violations));
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
