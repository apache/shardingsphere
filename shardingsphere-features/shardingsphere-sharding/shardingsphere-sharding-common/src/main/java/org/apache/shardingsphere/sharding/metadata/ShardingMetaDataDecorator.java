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

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.schema.loader.spi.ShardingSphereMetaDataDecorator;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalIndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalTableMetaData;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Table meta data decorator for sharding.
 */
public final class ShardingMetaDataDecorator implements ShardingSphereMetaDataDecorator<ShardingRule> {
    
    @Override
    public PhysicalTableMetaData decorate(final String tableName, final PhysicalTableMetaData tableMetaData, final ShardingRule shardingRule) {
        return shardingRule.findTableRule(tableName).map(
            tableRule -> new PhysicalTableMetaData(getColumnMetaDataList(tableMetaData, tableRule), getIndexMetaDataList(tableMetaData, tableRule))).orElse(tableMetaData);
    }
    
    private Collection<PhysicalColumnMetaData> getColumnMetaDataList(final PhysicalTableMetaData tableMetaData, final TableRule tableRule) {
        Optional<String> generateKeyColumn = tableRule.getGenerateKeyColumn();
        if (!generateKeyColumn.isPresent()) {
            return tableMetaData.getColumns().values();
        }
        Collection<PhysicalColumnMetaData> result = new LinkedList<>();
        for (Entry<String, PhysicalColumnMetaData> entry : tableMetaData.getColumns().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(generateKeyColumn.get())) {
                result.add(new PhysicalColumnMetaData(
                    entry.getValue().getName(), entry.getValue().getDataType(), entry.getValue().getDataTypeName(), entry.getValue().isPrimaryKey(), true, entry.getValue().isCaseSensitive()));
            } else {
                result.add(entry.getValue());
            }
        }
        return result;
    }
    
    private Collection<PhysicalIndexMetaData> getIndexMetaDataList(final PhysicalTableMetaData tableMetaData, final TableRule tableRule) {
        Collection<PhysicalIndexMetaData> result = new HashSet<>();
        for (Entry<String, PhysicalIndexMetaData> entry : tableMetaData.getIndexes().entrySet()) {
            for (DataNode each : tableRule.getActualDataNodes()) {
                getLogicIndex(entry.getKey(), each.getTableName()).ifPresent(logicIndex -> result.add(new PhysicalIndexMetaData(logicIndex)));
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
