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

package org.apache.shardingsphere.sharding.merge.mysql.type;

import com.cedarsoftware.util.CaseInsensitiveMap;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereConstraint;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sharding merged result for show create table for MySQL.
 */
public final class MySQLShardingShowCreateTableMergedResult extends MySQLShardingLogicTablesMergedResult {
    
    public MySQLShardingShowCreateTableMergedResult(final ShardingRule rule,
                                                    final SQLStatementContext sqlStatementContext, final ShardingSphereSchema schema, final List<QueryResult> queryResults) throws SQLException {
        super(rule, sqlStatementContext, schema, queryResults);
    }
    
    @Override
    protected void setCellValue(final MemoryQueryResultRow memoryResultSetRow, final String logicTableName, final String actualTableName,
                                final ShardingSphereTable table, final ShardingRule rule) {
        replaceTables(memoryResultSetRow, logicTableName, actualTableName);
        replaceBindingTables(memoryResultSetRow, logicTableName, actualTableName, rule);
        replaceIndexes(memoryResultSetRow, actualTableName, table);
        replaceConstraints(memoryResultSetRow, actualTableName, table, rule);
    }
    
    private void replaceTables(final MemoryQueryResultRow memoryResultSetRow, final String logicTableName, final String actualTableName) {
        memoryResultSetRow.setCell(2, memoryResultSetRow.getCell(2).toString().replaceFirst(actualTableName, logicTableName));
    }
    
    private void replaceBindingTables(final MemoryQueryResultRow memoryResultSetRow, final String logicTableName, final String actualTableName, final ShardingRule rule) {
        Optional<ShardingTable> shardingTable = rule.findShardingTable(logicTableName);
        Optional<BindingTableRule> bindingTableRule = rule.findBindingTableRule(logicTableName);
        if (!shardingTable.isPresent() || !bindingTableRule.isPresent()) {
            return;
        }
        Collection<DataNode> actualDataNodes = shardingTable.get().getActualDataNodes().stream().filter(each -> each.getTableName().equalsIgnoreCase(actualTableName)).collect(Collectors.toList());
        Map<String, String> logicAndActualTablesFromBindingTables = new CaseInsensitiveMap<>();
        for (DataNode each : actualDataNodes) {
            logicAndActualTablesFromBindingTables
                    .putAll(rule.getLogicAndActualTablesFromBindingTable(each.getDataSourceName(), logicTableName, actualTableName, bindingTableRule.get().getAllLogicTables()));
        }
        for (Entry<String, String> entry : logicAndActualTablesFromBindingTables.entrySet()) {
            memoryResultSetRow.setCell(2, memoryResultSetRow.getCell(2).toString().replaceFirst(entry.getValue(), entry.getKey()));
        }
    }
    
    private void replaceIndexes(final MemoryQueryResultRow memoryResultSetRow, final String actualTableName, final ShardingSphereTable table) {
        for (ShardingSphereIndex each : table.getAllIndexes()) {
            String actualIndexName = IndexMetaDataUtils.getActualIndexName(each.getName(), actualTableName);
            memoryResultSetRow.setCell(2, memoryResultSetRow.getCell(2).toString().replace(actualIndexName, each.getName()));
        }
    }
    
    private void replaceConstraints(final MemoryQueryResultRow memoryResultSetRow, final String actualTableName, final ShardingSphereTable table, final ShardingRule rule) {
        for (ShardingSphereConstraint each : table.getAllConstraints()) {
            String actualIndexName = IndexMetaDataUtils.getActualIndexName(each.getName(), actualTableName);
            memoryResultSetRow.setCell(2, memoryResultSetRow.getCell(2).toString().replace(actualIndexName, each.getName()));
            Optional<ShardingTable> shardingTable = rule.findShardingTable(each.getReferencedTableName());
            if (!shardingTable.isPresent()) {
                continue;
            }
            for (DataNode dataNode : shardingTable.get().getActualDataNodes()) {
                memoryResultSetRow.setCell(2, memoryResultSetRow.getCell(2).toString().replace(dataNode.getTableName(), each.getReferencedTableName()));
            }
        }
    }
}
