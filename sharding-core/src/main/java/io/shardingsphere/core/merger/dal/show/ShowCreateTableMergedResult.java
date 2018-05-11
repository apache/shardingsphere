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
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.merger.dql.common.MemoryMergedResult;
import io.shardingsphere.core.merger.dql.common.MemoryQueryResultRow;
import io.shardingsphere.core.parsing.SQLParsingEngine;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.rule.TableRule;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Merged result for show create table.
 *
 * @author zhangliang
 */
public final class ShowCreateTableMergedResult extends MemoryMergedResult {
    
    private static final Map<String, Integer> LABEL_AND_INDEX_MAP = new HashMap<>(2, 1);
    
    private final ShardingRule shardingRule;
    
    private final Iterator<MemoryQueryResultRow> memoryResultSetRows;
    
    static {
        LABEL_AND_INDEX_MAP.put("Table", 1);
        LABEL_AND_INDEX_MAP.put("Create Table", 2);
    }
    
    public ShowCreateTableMergedResult(final ShardingRule shardingRule, final List<QueryResult> queryResults) throws SQLException {
        super(LABEL_AND_INDEX_MAP);
        this.shardingRule = shardingRule;
        memoryResultSetRows = init(queryResults);
    }
    
    private Iterator<MemoryQueryResultRow> init(final List<QueryResult> queryResults) throws SQLException {
        List<MemoryQueryResultRow> result = new LinkedList<>();
        for (QueryResult each : queryResults) {
            while (each.next()) {
                MemoryQueryResultRow memoryResultSetRow = new MemoryQueryResultRow(each);
                String tableName = memoryResultSetRow.getCell(1).toString();
                Optional<TableRule> tableRule = shardingRule.tryFindTableRuleByActualTable(tableName);
                if (tableRule.isPresent()) {
                    String logicTableName = tableRule.get().getLogicTable();
                    memoryResultSetRow.setCell(1, logicTableName);
                    String createTableDDL = memoryResultSetRow.getCell(2).toString();
                    SQLParsingEngine sqlParsingEngine = new SQLParsingEngine(DatabaseType.MySQL, createTableDDL, shardingRule, null);
                    String actualTableName = sqlParsingEngine.parse(true).getTables().getSingleTableName();
                    if (actualTableName.startsWith("`")) {
                        logicTableName = "`" + logicTableName + "`";
                    }
                    memoryResultSetRow.setCell(2, createTableDDL.replaceFirst(actualTableName, logicTableName));
                    result.add(memoryResultSetRow);
                }
            }
        }
        if (!result.isEmpty()) {
            setCurrentResultSetRow(result.get(0));
        }
        return result.iterator();
    }
    
    @Override
    public boolean next() {
        if (memoryResultSetRows.hasNext()) {
            setCurrentResultSetRow(memoryResultSetRows.next());
            return true;
        }
        return false;
    }
}
