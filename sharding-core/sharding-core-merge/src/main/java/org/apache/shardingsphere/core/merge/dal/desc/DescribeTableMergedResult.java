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

package org.apache.shardingsphere.core.merge.dal.desc;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.dql.common.MemoryMergedResult;
import org.apache.shardingsphere.core.merge.dql.common.MemoryQueryResultRow;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Merged result for desc table.
 *
 * @author liya
 */
public final class DescribeTableMergedResult extends MemoryMergedResult {
    
    private static final Map<String, Integer> LABEL_AND_INDEX_MAP = new HashMap<>(6, 1);
    
    static {
        LABEL_AND_INDEX_MAP.put("Field", 1);
        LABEL_AND_INDEX_MAP.put("Type", 2);
        LABEL_AND_INDEX_MAP.put("Null", 3);
        LABEL_AND_INDEX_MAP.put("Key", 4);
        LABEL_AND_INDEX_MAP.put("Default", 5);
        LABEL_AND_INDEX_MAP.put("Extra", 6);
    }
    
    private final ShardingRule shardingRule;
    
    private final OptimizedStatement optimizedStatement;
    
    private final Iterator<MemoryQueryResultRow> memoryResultSetRows;
    
    public DescribeTableMergedResult(final ShardingRule shardingRule, final List<QueryResult> queryResults, final OptimizedStatement optimizedStatement) throws SQLException {
        super(LABEL_AND_INDEX_MAP);
        this.shardingRule = shardingRule;
        this.optimizedStatement = optimizedStatement;
        this.memoryResultSetRows = init(queryResults);
    }
    
    private Iterator<MemoryQueryResultRow> init(final List<QueryResult> queryResults) throws SQLException {
        List<MemoryQueryResultRow> result = new LinkedList<>();
        for (QueryResult each : queryResults) {
            while (each.next()) {
                Optional<MemoryQueryResultRow> memoryQueryResultRow = optimize(each);
                if (memoryQueryResultRow.isPresent()) {
                    result.add(memoryQueryResultRow.get());
                }
            }
        }
        if (!result.isEmpty()) {
            setCurrentResultSetRow(result.get(0));
        }
        return result.iterator();
    }
    
    private Optional<MemoryQueryResultRow> optimize(final QueryResult queryResult) throws SQLException {
        MemoryQueryResultRow memoryQueryResultRow = new MemoryQueryResultRow(queryResult);
        String logicTableName = optimizedStatement.getTables().getSingleTableName();
        Optional<EncryptTable> encryptTable = shardingRule.getEncryptRule().findEncryptTable(logicTableName);
        if (encryptTable.isPresent()) {
            String columnName = memoryQueryResultRow.getCell(1).toString();
            if (encryptTable.get().getAssistedQueryColumns().contains(columnName)) {
                return Optional.absent();
            }
            if (encryptTable.get().getCipherColumns().contains(columnName)) {
                memoryQueryResultRow.setCell(1, encryptTable.get().getLogicColumn(columnName));
            }
        }
        return Optional.of(memoryQueryResultRow);
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
