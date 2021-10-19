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

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Merged result for show table status.
 */
public final class ShowTableStatusMergedResult extends MemoryMergedResult<ShardingRule> {
    
    public ShowTableStatusMergedResult(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext,
                                       final ShardingSphereSchema schema, final List<QueryResult> queryResults) throws SQLException {
        super(shardingRule, schema, sqlStatementContext, queryResults);
    }
    
    @Override
    protected List<MemoryQueryResultRow> init(final ShardingRule shardingRule, final ShardingSphereSchema schema,
                                              final SQLStatementContext sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        Map<String, MemoryQueryResultRow> memoryQueryResultRowMap = new LinkedHashMap<>();
        for (QueryResult each : queryResults) {
            while (each.next()) {
                MemoryQueryResultRow memoryResultSetRow = new MemoryQueryResultRow(each);
                String actualTableName = memoryResultSetRow.getCell(1).toString();
                Optional<TableRule> tableRule = shardingRule.findTableRuleByActualTable(actualTableName);
                tableRule.ifPresent(rule -> memoryResultSetRow.setCell(1, rule.getLogicTable()));
                String tableName = memoryResultSetRow.getCell(1).toString();
                if (memoryQueryResultRowMap.containsKey(tableName)) {
                    merge(memoryQueryResultRowMap.get(tableName), memoryResultSetRow);
                } else {
                    memoryQueryResultRowMap.put(tableName, memoryResultSetRow);
                }
                
            }
        }
        return new LinkedList<>(memoryQueryResultRowMap.values());
    }
    
    private void merge(final MemoryQueryResultRow row, final MemoryQueryResultRow newRow) {
        mergeRows(row, newRow);
        mergeDataLength(row, newRow);
        mergeMaxDataLength(row, newRow);
        mergeIndexLength(row, newRow);
        mergeDataFree(row, newRow);
        mergeAvgRowLength(row);
    }
    
    private void mergeAvgRowLength(final MemoryQueryResultRow row) {
        row.setCell(6, row.getCell(5).equals(BigInteger.ZERO) ? BigInteger.ZERO : ((BigInteger) row.getCell(7)).divide((BigInteger) row.getCell(5)));
    }
    
    private void mergeDataFree(final MemoryQueryResultRow row, final MemoryQueryResultRow newRow) {
        sum(row, newRow, 10);
    }
    
    private void mergeIndexLength(final MemoryQueryResultRow row, final MemoryQueryResultRow newRow) {
        sum(row, newRow, 9);
    }
    
    private void mergeMaxDataLength(final MemoryQueryResultRow row, final MemoryQueryResultRow newRow) {
        sum(row, newRow, 8);
    }
    
    private void mergeDataLength(final MemoryQueryResultRow row, final MemoryQueryResultRow newRow) {
        sum(row, newRow, 7);
    }
    
    private void mergeRows(final MemoryQueryResultRow row, final MemoryQueryResultRow newRow) {
        sum(row, newRow, 5);
    }
    
    private void sum(final MemoryQueryResultRow row, final MemoryQueryResultRow newRow, final int index) {
        row.setCell(index, ((BigInteger) row.getCell(index)).add((BigInteger) newRow.getCell(index)));
    }
}
