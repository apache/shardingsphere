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

package org.apache.shardingsphere.sharding.merge.dal.show;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Logic tables merged result.
 */
public class LogicTablesMergedResult extends MemoryMergedResult<ShardingRule> {
    
    public LogicTablesMergedResult(final ShardingRule shardingRule,
                                   final SQLStatementContext sqlStatementContext, final ShardingSphereSchema schema, final List<QueryResult> queryResults) throws SQLException {
        super(shardingRule, schema, sqlStatementContext, queryResults);
    }
    
    @Override
    protected final List<MemoryQueryResultRow> init(final ShardingRule shardingRule, final ShardingSphereSchema schema,
                                                    final SQLStatementContext sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        List<MemoryQueryResultRow> result = new LinkedList<>();
        Set<String> tableNames = new HashSet<>();
        for (QueryResult each : queryResults) {
            while (each.next()) {
                createMemoryQueryResultRow(shardingRule, schema, each, tableNames).ifPresent(result::add);
            }
        }
        return result;
    }
    
    private Optional<MemoryQueryResultRow> createMemoryQueryResultRow(final ShardingRule shardingRule,
                                                                      final ShardingSphereSchema schema, final QueryResult queryResult, final Set<String> tableNames) throws SQLException {
        MemoryQueryResultRow memoryResultSetRow = new MemoryQueryResultRow(queryResult);
        String actualTableName = memoryResultSetRow.getCell(1).toString();
        Optional<TableRule> tableRule = shardingRule.findTableRuleByActualTable(actualTableName);
        if (tableRule.isPresent() && tableNames.add(tableRule.get().getLogicTable())) {
            String logicTableName = tableRule.get().getLogicTable();
            memoryResultSetRow.setCell(1, logicTableName);
            setCellValue(memoryResultSetRow, logicTableName, actualTableName, schema.getTable(logicTableName), shardingRule);
            return Optional.of(memoryResultSetRow);
        }
        if (shardingRule.getTableRules().isEmpty() || tableNames.add(actualTableName)) {
            setCellValue(memoryResultSetRow, actualTableName, actualTableName, schema.getTable(actualTableName), shardingRule);
            return Optional.of(memoryResultSetRow);
        }
        return Optional.empty();
    }
    
    protected void setCellValue(final MemoryQueryResultRow memoryResultSetRow,
                                final String logicTableName, final String actualTableName, final ShardingSphereTable table, final ShardingRule shardingRule) {
    }
}
