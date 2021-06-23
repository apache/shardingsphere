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
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShowIndexMergedResultTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Test
    public void assertNextForEmptyQueryResult() throws SQLException {
        assertFalse(new ShowIndexMergedResult(shardingRule, mock(SQLStatementContext.class), schema, Collections.emptyList()).next());
    }
    
    @Test
    public void assertNextForTableRuleIsPresent() throws SQLException {
        assertTrue(new ShowIndexMergedResult(shardingRule, mock(SQLStatementContext.class), schema, Collections.singletonList(mockQueryResult())).next());
    }
    
    private QueryResult mockQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(3);
        when(result.next()).thenReturn(true, false);
        when(result.getValue(1, Object.class)).thenReturn("t_order_0");
        when(result.getValue(2, Object.class)).thenReturn(1);
        when(result.getValue(3, Object.class)).thenReturn("t_order_index_t_order_0");
        return result;
    }
}
