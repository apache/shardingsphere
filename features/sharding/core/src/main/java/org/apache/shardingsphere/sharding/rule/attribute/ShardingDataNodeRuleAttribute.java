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

package org.apache.shardingsphere.sharding.rule.attribute;

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.sharding.rule.ShardingTable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sharding data node rule attribute.
 */
public final class ShardingDataNodeRuleAttribute implements DataNodeRuleAttribute {
    
    private final Map<String, ShardingTable> shardingTables;
    
    private final Map<String, Collection<DataNode>> tableDataNodes;
    
    public ShardingDataNodeRuleAttribute(final Map<String, ShardingTable> shardingTables) {
        this.shardingTables = shardingTables;
        tableDataNodes = createShardingTableDataNodes(shardingTables);
    }
    
    private Map<String, Collection<DataNode>> createShardingTableDataNodes(final Map<String, ShardingTable> shardingTables) {
        Map<String, Collection<DataNode>> result = new CaseInsensitiveMap<>(shardingTables.size(), 1F);
        for (ShardingTable each : shardingTables.values()) {
            result.put(each.getLogicTable(), each.getActualDataNodes());
        }
        return result;
    }
    
    @Override
    public Map<String, Collection<DataNode>> getAllDataNodes() {
        return tableDataNodes;
    }
    
    @Override
    public Collection<DataNode> getDataNodesByTableName(final String tableName) {
        return tableDataNodes.getOrDefault(tableName, Collections.emptyList());
    }
    
    @Override
    public Optional<String> findFirstActualTable(final String logicTable) {
        return findShardingTable(logicTable).map(optional -> optional.getActualDataNodes().get(0).getTableName());
    }
    
    private Optional<ShardingTable> findShardingTable(final String logicTableName) {
        return Strings.isNullOrEmpty(logicTableName) || !shardingTables.containsKey(logicTableName) ? Optional.empty() : Optional.of(shardingTables.get(logicTableName));
    }
    
    @Override
    public boolean isNeedAccumulate(final Collection<String> tables) {
        return containsShardingTable(tables);
    }
    
    private boolean containsShardingTable(final Collection<String> logicTableNames) {
        for (String each : logicTableNames) {
            if (isShardingTable(each)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isShardingTable(final String logicTableName) {
        return shardingTables.containsKey(logicTableName);
    }
    
    @Override
    public Optional<String> findLogicTableByActualTable(final String actualTable) {
        return findShardingTableByActualTable(actualTable).map(ShardingTable::getLogicTable);
    }
    
    private Optional<ShardingTable> findShardingTableByActualTable(final String actualTableName) {
        for (ShardingTable each : shardingTables.values()) {
            if (each.isExisted(actualTableName)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<String> findActualTableByCatalog(final String catalog, final String logicTable) {
        return findShardingTable(logicTable).flatMap(optional -> findActualTableFromActualDataNode(catalog, optional.getActualDataNodes()));
    }
    
    private Optional<String> findActualTableFromActualDataNode(final String catalog, final List<DataNode> actualDataNodes) {
        return actualDataNodes.stream().filter(each -> each.getDataSourceName().equalsIgnoreCase(catalog)).findFirst().map(DataNode::getTableName);
    }
    
    @Override
    public boolean isReplicaBasedDistribution() {
        return false;
    }
}
