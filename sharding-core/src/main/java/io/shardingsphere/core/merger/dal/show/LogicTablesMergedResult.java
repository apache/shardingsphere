/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.merger.dal.show;

import com.google.common.base.Optional;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.merger.dql.common.MemoryMergedResult;
import io.shardingsphere.core.merger.dql.common.MemoryQueryResultRow;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.rule.TableRule;

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
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    public LogicTablesMergedResult(final Map<String, Integer> labelAndIndexMap, 
                                   final ShardingRule shardingRule, final List<QueryResult> queryResults, final ShardingTableMetaData shardingTableMetaData) throws SQLException {
        super(labelAndIndexMap);
        this.shardingRule = shardingRule;
        this.shardingTableMetaData = shardingTableMetaData;
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
                    if (shardingRule.getTableRules().isEmpty() || shardingTableMetaData.containsTable(actualTableName) && tableNames.add(actualTableName)) {
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
