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

package org.apache.shardingsphere.sharding.merge.mysql.type;

import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLShardingShowTableStatusMergedResultTest {
    
    @Mock
    private ShardingRule rule;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @BeforeEach
    void setUp() {
        rule = buildShardingRule();
        schema = new ShardingSphereSchema("foo_db",
                Collections.singleton(new ShardingSphereTable("table", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())), Collections.emptyList());
    }
    
    private ShardingRule buildShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(new ShardingTableRuleConfiguration("table", "ds.table_${0..2}"));
        return new ShardingRule(shardingRuleConfig, Maps.of("ds", new MockedDataSource()), mock(ComputeNodeInstanceContext.class), Collections.emptyList());
    }
    
    @Test
    void assertNextForEmptyQueryResult() throws SQLException {
        assertFalse(new MySQLShardingShowTableStatusMergedResult(rule, mock(SQLStatementContext.class), schema, Collections.emptyList()).next());
    }
    
    @Test
    void assertNextForTableRuleIsPresent() throws SQLException {
        MergedResult mergedResult = new MySQLShardingShowTableStatusMergedResult(rule, mock(SQLStatementContext.class), schema, Collections.singletonList(mockQueryResult()));
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
}
