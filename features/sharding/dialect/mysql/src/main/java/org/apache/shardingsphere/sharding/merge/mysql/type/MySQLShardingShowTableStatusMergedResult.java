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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sharding merged result for show table status for MySQL.
 */
public final class MySQLShardingShowTableStatusMergedResult extends MemoryMergedResult<ShardingRule> {
    
    public MySQLShardingShowTableStatusMergedResult(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext,
                                                    final ShardingSphereSchema schema, final List<QueryResult> queryResults) throws SQLException {
        super(shardingRule, schema, sqlStatementContext, queryResults);
    }
    
    @Override
    protected List<MemoryQueryResultRow> init(final ShardingRule shardingRule, final ShardingSphereSchema schema,
                                              final SQLStatementContext sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        Map<String, MemoryQueryResultRow> memoryQueryResultRows = new LinkedHashMap<>();
        for (QueryResult each : queryResults) {
            while (each.next()) {
                MemoryQueryResultRow memoryResultSetRow = new MemoryQueryResultRow(each);
                String actualTableName = memoryResultSetRow.getCell(1).toString();
                Optional<ShardingTable> shardingTable = shardingRule.findShardingTableByActualTable(actualTableName);
                shardingTable.ifPresent(optional -> memoryResultSetRow.setCell(1, optional.getLogicTable()));
                String tableName = memoryResultSetRow.getCell(1).toString();
                if (memoryQueryResultRows.containsKey(tableName)) {
                    merge(memoryQueryResultRows.get(tableName), memoryResultSetRow);
                } else {
                    memoryQueryResultRows.put(tableName, memoryResultSetRow);
                }
            }
        }
        return new LinkedList<>(memoryQueryResultRows.values());
    }
    
    private void merge(final MemoryQueryResultRow row, final MemoryQueryResultRow newRow) {
        row.setCell(5, sum(row.getCell(5), newRow.getCell(5)));
        row.setCell(7, sum(row.getCell(7), newRow.getCell(7)));
        row.setCell(8, sum(row.getCell(8), newRow.getCell(8)));
        row.setCell(9, sum(row.getCell(9), newRow.getCell(9)));
        row.setCell(10, sum(row.getCell(10), newRow.getCell(10)));
        row.setCell(6, avg(row.getCell(7), row.getCell(5)));
    }
    
    private BigInteger sum(final Object num1, final Object num2) {
        return null == num1 && null == num2 ? null : getNonNullBigInteger(num1).add(getNonNullBigInteger(num2));
    }
    
    private BigInteger avg(final Object sum, final Object number) {
        if (null == sum && null == number) {
            return null;
        }
        BigInteger numberBigInteger = getNonNullBigInteger(number);
        return BigInteger.ZERO.equals(numberBigInteger) ? BigInteger.ZERO : getNonNullBigInteger(sum).divide(numberBigInteger);
    }
    
    private BigInteger getNonNullBigInteger(final Object value) {
        return Optional.ofNullable(value).filter(BigInteger.class::isInstance).map(BigInteger.class::cast).orElse(BigInteger.ZERO);
    }
}
