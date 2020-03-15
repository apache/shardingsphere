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

package org.apache.shardingsphere.sharding.execute.metadata.decorator;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.metadata.column.ShardingGeneratedKeyColumnMetaData;
import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.index.IndexMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.decorator.TableMetaDataDecorator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Table meta data decorator for sharding.
 */
@RequiredArgsConstructor
public final class ShardingTableMetaDataDecorator implements TableMetaDataDecorator<ShardingRule> {
    
    @Override
    public TableMetas decorate(final TableMetas tableMetas, final ShardingRule shardingRule) {
        Map<String, TableMetaData> result = new HashMap<>(tableMetas.getAllTableNames().size(), 1);
        for (String each : tableMetas.getAllTableNames()) {
            result.put(each, decorate(tableMetas.get(each), each, shardingRule));
        }
        return new TableMetas(result);
    }
    
    @Override
    public TableMetaData decorate(final TableMetaData tableMetaData, final String tableName, final ShardingRule shardingRule) {
        return new TableMetaData(getColumnMetaDataList(tableMetaData, tableName, shardingRule), getIndexMetaDataList(tableMetaData, tableName, shardingRule));
    }
    
    private Collection<ColumnMetaData> getColumnMetaDataList(final TableMetaData tableMetaData, final String tableName, final ShardingRule shardingRule) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(tableName);
        if (!generateKeyColumnName.isPresent()) {
            return tableMetaData.getColumns().values();
        }
        Collection<ColumnMetaData> result = new LinkedList<>();
        for (ColumnMetaData each : tableMetaData.getColumns().values()) {
            if (each.getName().equalsIgnoreCase(generateKeyColumnName.get())) {
                result.add(new ShardingGeneratedKeyColumnMetaData(each.getName(), each.getDataType(), each.isPrimaryKey()));
            } else {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<IndexMetaData> getIndexMetaDataList(final TableMetaData tableMetaData, final String tableName, final ShardingRule shardingRule) {
        Optional<TableRule> tableRule = shardingRule.findTableRule(tableName);
        if (!tableRule.isPresent()) {
            return tableMetaData.getIndexes().values();
        }
        Collection<IndexMetaData> result = new HashSet<>();
        for (Entry<String, IndexMetaData> entry : tableMetaData.getIndexes().entrySet()) {
            for (DataNode each : tableRule.get().getActualDataNodes()) {
                getLogicIndex(entry.getKey(), each.getTableName()).ifPresent(logicIndex -> result.add(new IndexMetaData(logicIndex)));
            }
        }
        return result;
    }
    
    private Optional<String> getLogicIndex(final String actualIndexName, final String actualTableName) {
        String indexNameSuffix = "_" + actualTableName;
        return actualIndexName.endsWith(indexNameSuffix) ? Optional.of(actualIndexName.replace(indexNameSuffix, "")) : Optional.empty();
    }
}
