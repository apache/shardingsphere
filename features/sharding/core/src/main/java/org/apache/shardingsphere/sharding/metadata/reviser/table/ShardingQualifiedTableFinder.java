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

package org.apache.shardingsphere.sharding.metadata.reviser.table;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;

import java.util.Optional;

/**
 * Sharding qualified table finder.
 */
final class ShardingQualifiedTableFinder {
    
    static Optional<ShardingTable> find(final ShardingRule rule, final String tableName, final String storageUnitName) {
        if (null == storageUnitName) {
            return rule.findShardingTableByActualTable(tableName);
        }
        Optional<ShardingTable> qualified = rule.findShardingTableByActualTable(storageUnitName, tableName);
        if (qualified.isPresent()) {
            return qualified;
        }
        Optional<ShardingTable> unqualified = rule.findShardingTableByActualTable(tableName);
        if (!unqualified.isPresent()) {
            return Optional.empty();
        }
        return unqualified.filter(each -> isActualTableOnStorageUnit(rule, each, storageUnitName, tableName));
    }
    
    private static boolean isActualTableOnStorageUnit(final ShardingRule rule, final ShardingTable shardingTable, final String storageUnitName, final String actualTableName) {
        for (DataNode dataNode : shardingTable.getActualDataNodes()) {
            if (!dataNode.getTableName().equalsIgnoreCase(actualTableName)) {
                continue;
            }
            if (dataNode.getDataSourceName().equals(storageUnitName)) {
                return true;
            }
            if (rule.isStorageUnitMappedToDataSource(storageUnitName, dataNode.getDataSourceName())) {
                return true;
            }
        }
        return false;
    }
}
