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

import com.google.common.collect.Lists;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetaData;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.executor.QueryResult;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowTablesMergedResultTest {
    
    private ShardingRule shardingRule;
    
    private RelationMetas relationMetas;
    
    @Before
    public void setUp() {
        shardingRule = createShardingRule();
        relationMetas = createRelationMetas();
    }
    
    private ShardingRule createShardingRule() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration("table", "ds.table_${0..2}");
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        return new ShardingRule(shardingRuleConfig, Lists.newArrayList("ds"));
    }
    
    private RelationMetas createRelationMetas() {
        Map<String, RelationMetaData> relationMetaDataMap = new HashMap<>(1, 1);
        relationMetaDataMap.put("table", new RelationMetaData(Collections.<String>emptyList()));
        return new RelationMetas(relationMetaDataMap);
    }
    
    private QueryResult createQueryResult(final String value) throws SQLException {
        QueryResult result = mock(QueryResult.class);
        when(result.next()).thenReturn(true, false);
        when(result.getValue(1, Object.class)).thenReturn(value);
        when(result.getColumnCount()).thenReturn(1);
        return result;
    }
    
    @Test
    public void assertNextForEmptyQueryResult() throws SQLException {
        LogicTablesMergedResult actual = new LogicTablesMergedResult(shardingRule, mock(SQLStatementContext.class), relationMetas, Collections.<QueryResult>emptyList());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForActualTableNameInTableRule() throws SQLException {
        LogicTablesMergedResult actual = new LogicTablesMergedResult(shardingRule, mock(SQLStatementContext.class), relationMetas, Collections.singletonList(createQueryResult("table_0")));
        assertTrue(actual.next());
    }
    
    @Test
    public void assertNextForActualTableNameNotInTableRuleWithDefaultDataSource() throws SQLException {
        LogicTablesMergedResult actual = new LogicTablesMergedResult(shardingRule, mock(SQLStatementContext.class), relationMetas, Collections.singletonList(createQueryResult("table")));
        assertTrue(actual.next());
    }
    
    @Test
    public void assertNextForActualTableNameNotInTableRuleWithoutDefaultDataSource() throws SQLException {
        LogicTablesMergedResult actual = new LogicTablesMergedResult(shardingRule, mock(SQLStatementContext.class), relationMetas, Collections.singletonList(createQueryResult("table_3")));
        assertFalse(actual.next());
    }
}
