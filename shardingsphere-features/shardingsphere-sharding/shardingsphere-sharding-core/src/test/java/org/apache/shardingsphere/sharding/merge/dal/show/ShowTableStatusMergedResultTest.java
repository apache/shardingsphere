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
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShowTableStatusMergedResultTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Before
    public void setUp() {
        shardingRule = buildShardingRule();
        schema = buildSchema();
    }
    
    @Test
    public void assertNextForEmptyQueryResult() throws SQLException {
        assertFalse(new ShowTableStatusMergedResult(shardingRule, mock(SQLStatementContext.class), schema, Collections.emptyList()).next());
    }
    
    @Test
    public void assertNextForTableRuleIsPresent() throws SQLException {
        MergedResult mergedResult = new ShowTableStatusMergedResult(shardingRule, mock(SQLStatementContext.class), schema, Collections.singletonList(mockQueryResult()));
        assertTrue(mergedResult.next());
        assertFalse(mergedResult.next());
    }
    
    private QueryResult mockQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getColumnCount()).thenReturn(18);
        when(result.next()).thenReturn(true, true, false);
        when(result.getValue(1, Object.class)).thenReturn("table_0", "table_1");
        when(result.getValue(2, Object.class)).thenReturn("InnoDB");
        when(result.getValue(3, Object.class)).thenReturn(10);
        when(result.getValue(4, Object.class)).thenReturn("Dynamic");
        when(result.getValue(5, Object.class)).thenReturn(new BigInteger("1"));
        when(result.getValue(6, Object.class)).thenReturn(new BigInteger("16384"));
        when(result.getValue(7, Object.class)).thenReturn(new BigInteger("16384"));
        when(result.getValue(8, Object.class)).thenReturn(new BigInteger("0"));
        when(result.getValue(9, Object.class)).thenReturn(new BigInteger("16384"));
        when(result.getValue(10, Object.class)).thenReturn(new BigInteger("0"));
        when(result.getValue(11, Object.class)).thenReturn(null);
        when(result.getValue(12, Object.class)).thenReturn(new Timestamp(1634090446000L));
        when(result.getValue(13, Object.class)).thenReturn(null);
        when(result.getValue(14, Object.class)).thenReturn(null);
        when(result.getValue(15, Object.class)).thenReturn("utf8mb4_0900_ai_ci");
        when(result.getValue(16, Object.class)).thenReturn(null);
        when(result.getValue(17, Object.class)).thenReturn("");
        when(result.getValue(18, Object.class)).thenReturn("");
        return result;
    }
    
    private ShardingRule buildShardingRule() {
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("table", "ds.table_${0..2}");
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(tableRuleConfig);
        return new ShardingRule(shardingRuleConfig, Collections.singletonList("ds"));
    }
    
    private ShardingSphereSchema buildSchema() {
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(1, 1);
        tableMetaDataMap.put("table", new TableMetaData("table", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        return new ShardingSphereSchema(tableMetaDataMap);
    }
}
