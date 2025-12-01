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

import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.test.it.distsql.handler.engine.query.DistSQLDatabaseRuleQueryExecutorAssert;
import org.apache.shardingsphere.test.it.distsql.handler.engine.query.DistSQLRuleQueryExecutorSettings;
import org.apache.shardingsphere.test.it.distsql.handler.engine.query.DistSQLRuleQueryExecutorTestCaseArgumentsProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DistSQLRuleQueryExecutorSettings("cases/show-status-from-readwrite-splitting-rules.xml")
class ShowStatusFromReadwriteSplittingRulesExecutorTest {
    
    @ParameterizedTest(name = "DistSQL -> {0}")
    @ArgumentsSource(DistSQLRuleQueryExecutorTestCaseArgumentsProvider.class)
    void assertExecuteQuery(@SuppressWarnings("unused") final String distSQL, final DistSQLStatement sqlStatement,
                            final DatabaseRuleConfiguration currentRuleConfig, final Collection<LocalDataQueryResultRow> expected) throws SQLException {
        new DistSQLDatabaseRuleQueryExecutorAssert(mockRule()).assertQueryResultRows(currentRuleConfig, sqlStatement, expected);
    }
    
    private static ReadwriteSplittingRule mockRule() {
        ReadwriteSplittingRule result = mock(ReadwriteSplittingRule.class);
        Map<String, ReadwriteSplittingDataSourceGroupRule> dataSourceGroupRules = Collections.singletonMap("readwrite_ds", mockDataSourceGroupRule());
        when(result.getDataSourceRuleGroups()).thenReturn(dataSourceGroupRules);
        return result;
    }
    
    private static ReadwriteSplittingDataSourceGroupRule mockDataSourceGroupRule() {
        ReadwriteSplittingDataSourceGroupRule result = mock(ReadwriteSplittingDataSourceGroupRule.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_rule");
        when(result.getReadwriteSplittingGroup().getReadDataSources()).thenReturn(Arrays.asList("read_ds_0", "read_ds_1"));
        when(result.getDisabledDataSourceNames()).thenReturn(Collections.singleton("read_ds_1"));
        return result;
    }
}
