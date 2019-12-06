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

import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.dql.common.MemoryQueryResultRow;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;

import java.sql.SQLException;
import java.util.List;

/**
 * Merged result for show create table.
 *
 * @author zhangliang
 */
public final class ShowCreateTableMergedResult extends LogicTablesMergedResult {
    
    public ShowCreateTableMergedResult(final ShardingRule shardingRule, 
                                       final SQLStatementContext sqlStatementContext, final TableMetas tableMetas, final List<QueryResult> queryResults) throws SQLException {
        super(shardingRule, sqlStatementContext, tableMetas, queryResults);
    }
    
    @Override
    protected void setCellValue(final MemoryQueryResultRow memoryResultSetRow, final String logicTableName, final String actualTableName) {
        memoryResultSetRow.setCell(2, memoryResultSetRow.getCell(2).toString().replaceFirst(actualTableName, logicTableName));
    }
}
