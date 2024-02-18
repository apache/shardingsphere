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

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableConstants;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowReadwriteSplittingRuleExecutorTest {
    
    @Test
    void assertGetRowData() {
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfiguration());
        when(rule.getExportData()).thenReturn(createExportedData());
        ShowReadwriteSplittingRuleExecutor executor = new ShowReadwriteSplittingRuleExecutor();
        executor.setRule(rule);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowReadwriteSplittingRulesStatement.class), mock(ContextManager.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("readwrite_ds"));
        assertThat(row.getCell(2), is("ds_primary"));
        assertThat(row.getCell(3), is("ds_slave_0,ds_slave_1"));
        assertThat(row.getCell(4), is("DYNAMIC"));
        assertThat(row.getCell(5), is("random"));
        assertThat(row.getCell(6), is("{\"read_weight\":\"2:1\"}"));
    }
    
    @Test
    void assertGetRowDataWithSpecifiedRuleName() {
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfiguration());
        when(rule.getExportData()).thenReturn(createExportedData());
        ShowReadwriteSplittingRuleExecutor executor = new ShowReadwriteSplittingRuleExecutor();
        executor.setRule(rule);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowReadwriteSplittingRulesStatement("readwrite_ds", null), mock(ContextManager.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("readwrite_ds"));
        assertThat(row.getCell(2), is("ds_primary"));
        assertThat(row.getCell(3), is("ds_slave_0,ds_slave_1"));
        assertThat(row.getCell(4), is("DYNAMIC"));
        assertThat(row.getCell(5), is("random"));
        assertThat(row.getCell(6), is("{\"read_weight\":\"2:1\"}"));
    }
    
    private Map<String, Object> createExportedData() {
        Map<String, Object> result = new HashMap<>(2, 1F);
        result.put(ExportableConstants.EXPORT_DYNAMIC_READWRITE_SPLITTING_RULE, Collections.emptyMap());
        result.put(ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE, Collections.emptyMap());
        return result;
    }
    
    private ReadwriteSplittingRuleConfiguration createRuleConfiguration() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds", "ds_primary", Arrays.asList("ds_slave_0", "ds_slave_1"), "test");
        return new ReadwriteSplittingRuleConfiguration(
                Collections.singleton(dataSourceRuleConfig), Collections.singletonMap("test", new AlgorithmConfiguration("random", PropertiesBuilder.build(new Property("read_weight", "2:1")))));
    }
    
    @Test
    void assertGetRowDataWithoutLoadBalancer() {
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfigurationWithoutLoadBalancer());
        when(rule.getExportData()).thenReturn(createExportedData());
        ShowReadwriteSplittingRuleExecutor executor = new ShowReadwriteSplittingRuleExecutor();
        executor.setRule(rule);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowReadwriteSplittingRulesStatement.class), mock(ContextManager.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("readwrite_ds"));
        assertThat(row.getCell(2), is("write_ds"));
        assertThat(row.getCell(3), is("read_ds_0,read_ds_1"));
        assertThat(row.getCell(4), is("DYNAMIC"));
        assertThat(row.getCell(5), is(""));
        assertThat(row.getCell(6), is(""));
    }
    
    private ReadwriteSplittingRuleConfiguration createRuleConfigurationWithoutLoadBalancer() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), null);
        return new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceRuleConfig), null);
    }
}
