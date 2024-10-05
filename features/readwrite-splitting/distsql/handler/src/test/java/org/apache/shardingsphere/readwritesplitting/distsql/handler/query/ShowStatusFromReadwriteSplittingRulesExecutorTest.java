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
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.ShowStatusFromReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.test.it.distsql.handler.engine.query.DistSQLDatabaseRuleQueryExecutorTest;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowStatusFromReadwriteSplittingRulesExecutorTest extends DistSQLDatabaseRuleQueryExecutorTest {
    
    ShowStatusFromReadwriteSplittingRulesExecutorTest() {
        super(mockRule());
    }
    
    private static ReadwriteSplittingRule mockRule() {
        ReadwriteSplittingRule result = mock(ReadwriteSplittingRule.class);
        Map<String, ReadwriteSplittingDataSourceGroupRule> dataSourceGroupRules = Collections.singletonMap("group_0", mockDataSourceGroupRule());
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
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertExecuteQuery(final String name, final DatabaseRuleConfiguration ruleConfig, final DistSQLStatement sqlStatement,
                            final Collection<LocalDataQueryResultRow> expected) throws SQLException {
        assertQueryResultRows(ruleConfig, sqlStatement, expected);
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(Arguments.arguments("withoutRuleName", mock(ReadwriteSplittingRuleConfiguration.class), new ShowStatusFromReadwriteSplittingRulesStatement(null, null),
                    Arrays.asList(new LocalDataQueryResultRow("read_ds_0", "ENABLED"), new LocalDataQueryResultRow("read_ds_1", "DISABLED"))),
                    Arguments.arguments("withRuleName", mock(ReadwriteSplittingRuleConfiguration.class), new ShowStatusFromReadwriteSplittingRulesStatement(null, "bar_rule"),
                            Collections.emptyList()));
        }
    }
}
