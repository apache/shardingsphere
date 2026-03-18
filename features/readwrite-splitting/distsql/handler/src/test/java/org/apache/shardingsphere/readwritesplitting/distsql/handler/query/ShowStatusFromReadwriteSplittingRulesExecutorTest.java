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

import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.ShowStatusFromReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowStatusFromReadwriteSplittingRulesExecutorTest {
    
    private final ShowStatusFromReadwriteSplittingRulesExecutor executor =
            (ShowStatusFromReadwriteSplittingRulesExecutor) TypedSPILoader.getService(DistSQLQueryExecutor.class, ShowStatusFromReadwriteSplittingRulesStatement.class);
    
    @Test
    void assertGetColumnNames() {
        assertThat(executor.getColumnNames(new ShowStatusFromReadwriteSplittingRulesStatement(null, null)), is(Arrays.asList("name", "storage_unit", "status")));
    }
    
    @Test
    void assertGetRowsWithoutRuleName() {
        executor.setRule(mockRule(createDataSourceGroupRules()));
        LinkedList<LocalDataQueryResultRow> actual = new LinkedList<>(executor.getRows(new ShowStatusFromReadwriteSplittingRulesStatement(null, null), mock(ContextManager.class)));
        assertThat(actual.size(), is(3));
        assertRow(actual.get(0), "foo_rule", "read_ds_0", DataSourceState.ENABLED.name());
        assertRow(actual.get(1), "foo_rule", "read_ds_1", DataSourceState.DISABLED.name());
        assertRow(actual.get(2), "bar_rule", "read_ds_2", DataSourceState.ENABLED.name());
    }
    
    @Test
    void assertGetRowsWithRuleName() {
        executor.setRule(mockRule(createDataSourceGroupRules()));
        List<LocalDataQueryResultRow> actual = new ArrayList<>(executor.getRows(new ShowStatusFromReadwriteSplittingRulesStatement(null, "FOO_RULE"), mock(ContextManager.class)));
        assertThat(actual.size(), is(2));
        assertRow(actual.get(0), "foo_rule", "read_ds_0", DataSourceState.ENABLED.name());
        assertRow(actual.get(1), "foo_rule", "read_ds_1", DataSourceState.DISABLED.name());
    }
    
    @Test
    void assertGetRuleClass() {
        assertThat(executor.getRuleClass(), is(ReadwriteSplittingRule.class));
    }
    
    private ReadwriteSplittingRule mockRule(final Map<String, ReadwriteSplittingDataSourceGroupRule> dataSourceGroupRules) {
        ReadwriteSplittingRule result = mock(ReadwriteSplittingRule.class);
        when(result.getDataSourceRuleGroups()).thenReturn(dataSourceGroupRules);
        return result;
    }
    
    private Map<String, ReadwriteSplittingDataSourceGroupRule> createDataSourceGroupRules() {
        Map<String, ReadwriteSplittingDataSourceGroupRule> result = new LinkedHashMap<>(2, 1F);
        result.put("foo_rule", mockDataSourceGroupRule("foo_rule", Arrays.asList("read_ds_0", "read_ds_1"), Collections.singleton("read_ds_1")));
        result.put("bar_rule", mockDataSourceGroupRule("bar_rule", Collections.singletonList("read_ds_2"), Collections.emptySet()));
        return result;
    }
    
    private ReadwriteSplittingDataSourceGroupRule mockDataSourceGroupRule(final String name, final List<String> readDataSources, final Collection<String> disabledDataSourceNames) {
        ReadwriteSplittingDataSourceGroupRule result = mock(ReadwriteSplittingDataSourceGroupRule.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn(name);
        when(result.getReadwriteSplittingGroup().getReadDataSources()).thenReturn(readDataSources);
        when(result.getDisabledDataSourceNames()).thenReturn(disabledDataSourceNames);
        return result;
    }
    
    private void assertRow(final LocalDataQueryResultRow row, final String expectedRuleName, final String expectedStorageUnit, final String expectedStatus) {
        assertThat(row.getCell(1), is(expectedRuleName));
        assertThat(row.getCell(2), is(expectedStorageUnit));
        assertThat(row.getCell(3), is(expectedStatus));
    }
}
