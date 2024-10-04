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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecuteEngine;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.ShowStatusFromReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowStatusFromReadwriteSplittingRulesExecutorTest {
    
    @Test
    void assertExecuteQueryWithoutRuleName() throws SQLException {
        ContextManager contextManager = mockContextManager(mockRule());
        DistSQLQueryExecuteEngine queryExecuteEngine = new DistSQLQueryExecuteEngine(
                new ShowStatusFromReadwriteSplittingRulesStatement(null, null), "foo_db", contextManager, mock(DistSQLConnectionContext.class));
        queryExecuteEngine.executeQuery();
        List<LocalDataQueryResultRow> rows = new ArrayList<>(queryExecuteEngine.getRows());
        assertThat(rows.size(), is(2));
        assertThat(rows.get(0).getCell(1), is("read_ds_0"));
        assertThat(rows.get(0).getCell(2), is("ENABLED"));
        assertThat(rows.get(1).getCell(1), is("read_ds_1"));
        assertThat(rows.get(1).getCell(2), is("DISABLED"));
    }
    
    @Test
    void assertExecuteQueryWithRuleName() throws SQLException {
        ContextManager contextManager = mockContextManager(mockRule());
        DistSQLQueryExecuteEngine queryExecuteEngine = new DistSQLQueryExecuteEngine(
                new ShowStatusFromReadwriteSplittingRulesStatement(null, "bar_rule"), "foo_db", contextManager, mock(DistSQLConnectionContext.class));
        queryExecuteEngine.executeQuery();
        assertTrue(queryExecuteEngine.getRows().isEmpty());
    }
    
    private ReadwriteSplittingRule mockRule() {
        ReadwriteSplittingRule result = mock(ReadwriteSplittingRule.class);
        Map<String, ReadwriteSplittingDataSourceGroupRule> dataSourceGroupRules = Collections.singletonMap("group_0", mockDataSourceGroupRule());
        when(result.getDataSourceRuleGroups()).thenReturn(dataSourceGroupRules);
        return result;
    }
    
    private ReadwriteSplittingDataSourceGroupRule mockDataSourceGroupRule() {
        ReadwriteSplittingDataSourceGroupRule result = mock(ReadwriteSplittingDataSourceGroupRule.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_rule");
        when(result.getReadwriteSplittingGroup().getReadDataSources()).thenReturn(Arrays.asList("read_ds_0", "read_ds_1"));
        when(result.getDisabledDataSourceNames()).thenReturn(Collections.singleton("read_ds_1"));
        return result;
    }
    
    private ContextManager mockContextManager(final ReadwriteSplittingRule rule) {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db")).thenReturn(database);
        return result;
    }
}
