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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowTablesMergedResultTest {
    
    private ShardingRule shardingRule;
    
    private ShardingSphereSchema schema;
    
    @Before
    public void setUp() {
        shardingRule = buildShardingRule();
        schema = buildSchema();
    }
    
    private ShardingRule buildShardingRule() {
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("table", "ds.table_${0..2}");
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(tableRuleConfig);
        return new ShardingRule(shardingRuleConfig, mock(DatabaseType.class), Collections.singletonMap("ds", mock(DataSource.class, RETURNS_DEEP_STUBS)));
    }
    
    private ShardingSphereSchema buildSchema() {
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(1, 1);
        tableMetaDataMap.put("table", new TableMetaData(Collections.emptyList(), Collections.emptyList()));
        return new ShardingSphereSchema(tableMetaDataMap);
    }
    
    private QueryResult mockQueryResult(final String value) throws SQLException {
        QueryResult result = mock(QueryResult.class, RETURNS_DEEP_STUBS);
        when(result.next()).thenReturn(true, false);
        when(result.getValue(1, Object.class)).thenReturn(value);
        when(result.getMetaData().getColumnCount()).thenReturn(1);
        return result;
    }
    
    @Test
    public void assertNextForEmptyQueryResult() throws SQLException {
        LogicTablesMergedResult actual = new LogicTablesMergedResult(shardingRule, mock(SQLStatementContext.class), schema, Collections.emptyList());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertNextForActualTableNameInTableRule() throws SQLException {
        LogicTablesMergedResult actual = new LogicTablesMergedResult(shardingRule, mock(SQLStatementContext.class), schema, Collections.singletonList(mockQueryResult("table_0")));
        assertTrue(actual.next());
    }
}
