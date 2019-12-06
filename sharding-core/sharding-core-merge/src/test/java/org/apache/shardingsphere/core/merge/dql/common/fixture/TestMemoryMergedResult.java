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

package org.apache.shardingsphere.core.merge.dql.common.fixture;

import lombok.Getter;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.dql.common.MemoryMergedResult;
import org.apache.shardingsphere.core.merge.dql.common.MemoryQueryResultRow;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;

@Getter
public final class TestMemoryMergedResult extends MemoryMergedResult {
    
    private MemoryQueryResultRow memoryQueryResultRow;
    
    public TestMemoryMergedResult() throws SQLException {
        super(null, null, null, Collections.<QueryResult>emptyList());
    }
    
    @Override
    protected List<MemoryQueryResultRow> init(final ShardingRule shardingRule, final TableMetas tableMetas, 
                                                    final SQLStatementContext sqlStatementContext, final List<QueryResult> queryResults) {
        memoryQueryResultRow = mock(MemoryQueryResultRow.class);
        return Collections.singletonList(memoryQueryResultRow);
    }
}
