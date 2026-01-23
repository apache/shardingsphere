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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLShardingLogicTablesMergedResultTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private ShardingRule rule;
    
    private ShardingSphereSchema schema;
    
    @BeforeEach
    void setUp() {
        rule = createShardingRule();
        schema = new ShardingSphereSchema("foo_db",
                Collections.singleton(new ShardingSphereTable("table", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())), Collections.emptyList(),
                databaseType);
    }
    
    private ShardingRule createShardingRule() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(new ShardingTableRuleConfiguration("table", "ds.table_${0..2}"));
        return new ShardingRule(shardingRuleConfig, Maps.of("ds", new MockedDataSource()), mock(ComputeNodeInstanceContext.class), Collections.emptyList());
    }
    
    @Test
    void assertNextForEmptyQueryResult() throws SQLException {
        assertFalse(new MySQLShardingLogicTablesMergedResult(rule, mock(SQLStatementContext.class), schema, Collections.emptyList()).next());
    }
    
    @Test
    void assertNextForActualTableNameInTableRule() throws SQLException {
        assertTrue(new MySQLShardingLogicTablesMergedResult(rule, mock(SQLStatementContext.class), schema, Collections.singletonList(mockQueryResult("table_0"))).next());
    }
    
    private QueryResult mockQueryResult(final String value) throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.next()).thenReturn(true, false);
        when(result.getValue(1, Object.class)).thenReturn(value);
        when(result.getMetaData().getColumnCount()).thenReturn(1);
        return result;
    }
}
