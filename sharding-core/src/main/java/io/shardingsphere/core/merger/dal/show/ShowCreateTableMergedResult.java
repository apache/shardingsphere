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

import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.merger.dql.common.MemoryQueryResultRow;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.rule.ShardingRule;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Merged result for show create table.
 *
 * @author zhangliang
 */
public final class ShowCreateTableMergedResult extends LogicTablesMergedResult {
    
    private static final Map<String, Integer> LABEL_AND_INDEX_MAP = new HashMap<>(2, 1);
    
    static {
        LABEL_AND_INDEX_MAP.put("Table", 1);
        LABEL_AND_INDEX_MAP.put("Create Table", 2);
    }
    
    public ShowCreateTableMergedResult(final ShardingRule shardingRule, final List<QueryResult> queryResults, final ShardingTableMetaData shardingTableMetaData) throws SQLException {
        super(LABEL_AND_INDEX_MAP, shardingRule, queryResults, shardingTableMetaData);
    }
    
    protected void setCellValue(final MemoryQueryResultRow memoryResultSetRow, final String logicTableName, final String actualTableName) {
        memoryResultSetRow.setCell(2, memoryResultSetRow.getCell(2).toString().replaceFirst(actualTableName, logicTableName));
    }
}
