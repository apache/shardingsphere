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

package org.apache.shardingsphere.core.merge.dal.show;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.dql.common.MemoryMergedResult;
import org.apache.shardingsphere.core.merge.dql.common.MemoryQueryResultRow;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Logic tables merged result.
 *
 * @author zhangliang
 */
public abstract class LogicTablesMergedResult extends MemoryMergedResult {
    
    private final ShardingRule shardingRule;
    
    private final Iterator<MemoryQueryResultRow> memoryResultSetRows;
    
    private final Set<String> tableNames = new HashSet<>();
    
    private final TableMetas tableMetas;
    
    public LogicTablesMergedResult(final Map<String, Integer> labelAndIndexMap, 
                                   final ShardingRule shardingRule, final List<QueryResult> queryResults, final TableMetas tableMetas) throws SQLException {
        super(labelAndIndexMap);
        this.shardingRule = shardingRule;
        this.tableMetas = tableMetas;
        memoryResultSetRows = init(queryResults);
    }
    
    private Iterator<MemoryQueryResultRow> init(final List<QueryResult> queryResults) throws SQLException {
        List<MemoryQueryResultRow> result = new LinkedList<>();
        for (QueryResult each : queryResults) {
            while (each.next()) {
                MemoryQueryResultRow memoryResultSetRow = new MemoryQueryResultRow(each);
                String actualTableName = memoryResultSetRow.getCell(1).toString();
                Optional<TableRule> tableRule = shardingRule.findTableRuleByActualTable(actualTableName);
                if (!tableRule.isPresent()) {
                    if (shardingRule.getTableRules().isEmpty() || tableMetas.containsTable(actualTableName) && tableNames.add(actualTableName)) {
                        result.add(memoryResultSetRow);
                    }
                } else if (tableNames.add(tableRule.get().getLogicTable())) {
                    memoryResultSetRow.setCell(1, tableRule.get().getLogicTable());
                    setCellValue(memoryResultSetRow, tableRule.get().getLogicTable(), actualTableName);
                    result.add(memoryResultSetRow);
                }
            }
        }
        if (!result.isEmpty()) {
            setCurrentResultSetRow(result.get(0));
        }
        return result.iterator();
    }
    
    protected void setCellValue(final MemoryQueryResultRow memoryResultSetRow, final String logicTableName, final String actualTableName) {
    }
    
    @Override
    public final boolean next() {
        if (memoryResultSetRows.hasNext()) {
            setCurrentResultSetRow(memoryResultSetRows.next());
            return true;
        }
        return false;
    }
}
