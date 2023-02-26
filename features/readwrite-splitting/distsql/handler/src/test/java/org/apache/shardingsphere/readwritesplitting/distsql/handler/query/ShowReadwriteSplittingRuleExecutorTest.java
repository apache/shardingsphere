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

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableConstants;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableItemConstants;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.DynamicReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowReadwriteSplittingRuleExecutorTest {
    
    @Test
    public void assertGetEmptyRule() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
        RQLExecutor<ShowReadwriteSplittingRulesStatement> executor = new ShowReadwriteSplittingRuleExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(database, mock(ShowReadwriteSplittingRulesStatement.class));
        assertTrue(actual.isEmpty());
    }
    
    @Test
    public void assertGetRowData() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfiguration());
        when(rule.getExportData()).thenReturn(createExportedData());
        when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singleton(rule)));
        RQLExecutor<ShowReadwriteSplittingRulesStatement> executor = new ShowReadwriteSplittingRuleExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(database, mock(ShowReadwriteSplittingRulesStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("readwrite_ds"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
        assertThat(row.getCell(4), is("ds_primary"));
        assertThat(row.getCell(5), is("ds_slave_0,ds_slave_1"));
        assertThat(row.getCell(6), is("random"));
        assertThat(row.getCell(7), is("read_weight=2:1"));
    }
    
    @Test
    public void assertGetRowDataWithSpecifiedRuleName() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfiguration());
        when(rule.getExportData()).thenReturn(createExportedData());
        when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singleton(rule)));
        RQLExecutor<ShowReadwriteSplittingRulesStatement> executor = new ShowReadwriteSplittingRuleExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(database, new ShowReadwriteSplittingRulesStatement("readwrite_ds", null));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("readwrite_ds"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
        assertThat(row.getCell(4), is("ds_primary"));
        assertThat(row.getCell(5), is("ds_slave_0,ds_slave_1"));
        assertThat(row.getCell(6), is("random"));
        assertThat(row.getCell(7), is("read_weight=2:1"));
    }
    
    private Map<String, Object> createExportedData() {
        Map<String, Object> result = new HashMap<>(2, 1);
        result.put(ExportableConstants.EXPORT_DYNAMIC_READWRITE_SPLITTING_RULE, Collections.emptyMap());
        result.put(ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE, Collections.emptyMap());
        return result;
    }
    
    private RuleConfiguration createRuleConfiguration() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds",
                        new StaticReadwriteSplittingStrategyConfiguration("ds_primary", Arrays.asList("ds_slave_0", "ds_slave_1")), null, "test");
        return new ReadwriteSplittingRuleConfiguration(
                Collections.singleton(dataSourceRuleConfig), Collections.singletonMap("test", new AlgorithmConfiguration("random", PropertiesBuilder.build(new Property("read_weight", "2:1")))));
    }
    
    @Test
    public void assertGetRowDataWithoutLoadBalancer() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfigurationWithoutLoadBalancer());
        when(rule.getExportData()).thenReturn(createExportedData());
        when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singleton(rule)));
        RQLExecutor<ShowReadwriteSplittingRulesStatement> executor = new ShowReadwriteSplittingRuleExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(database, mock(ShowReadwriteSplittingRulesStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("readwrite_ds"));
        assertThat(row.getCell(2), is(""));
        assertThat(row.getCell(3), is(""));
        assertThat(row.getCell(4), is("write_ds"));
        assertThat(row.getCell(5), is("read_ds_0,read_ds_1"));
        assertThat(row.getCell(6), is(""));
        assertThat(row.getCell(7), is(""));
    }
    
    private RuleConfiguration createRuleConfigurationWithoutLoadBalancer() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds",
                        new StaticReadwriteSplittingStrategyConfiguration("write_ds", Arrays.asList("read_ds_0", "read_ds_1")), null, null);
        return new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceRuleConfig), null);
    }
    
    @Test
    public void assertGetRowDataWithAutoAwareDataSource() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfigurationWithAutoAwareDataSource());
        when(rule.getExportData()).thenReturn(getExportData());
        when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singleton(rule)));
        RQLExecutor<ShowReadwriteSplittingRulesStatement> executor = new ShowReadwriteSplittingRuleExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(database, mock(ShowReadwriteSplittingRulesStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("readwrite_ds"));
        assertThat(row.getCell(2), is("rd_rs"));
        assertThat(row.getCell(3), is("false"));
        assertThat(row.getCell(4), is("write_ds"));
        assertThat(row.getCell(5), is("read_ds_0,read_ds_1"));
        assertThat(row.getCell(6), is(""));
        assertThat(row.getCell(7), is(""));
    }
    
    @Test
    public void assertGetColumnNames() {
        RQLExecutor<ShowReadwriteSplittingRulesStatement> executor = new ShowReadwriteSplittingRuleExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(7));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("name"));
        assertThat(iterator.next(), is("auto_aware_data_source_name"));
        assertThat(iterator.next(), is("write_data_source_query_enabled"));
        assertThat(iterator.next(), is("write_storage_unit_name"));
        assertThat(iterator.next(), is("read_storage_unit_names"));
        assertThat(iterator.next(), is("load_balancer_type"));
        assertThat(iterator.next(), is("load_balancer_props"));
    }
    
    private Map<String, Object> getExportData() {
        Map<String, Object> result = new HashMap<>(2, 1);
        result.put(ExportableConstants.EXPORT_DYNAMIC_READWRITE_SPLITTING_RULE, exportDynamicDataSources());
        result.put(ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE, exportStaticDataSources());
        return result;
    }
    
    private Map<String, Map<String, String>> exportDynamicDataSources() {
        return Collections.singletonMap("readwrite_ds", getAutoAwareDataSources());
    }
    
    private Map<String, String> getAutoAwareDataSources() {
        Map<String, String> result = new HashMap<>(3, 1);
        result.put(ExportableItemConstants.AUTO_AWARE_DATA_SOURCE_NAME, "ha_group");
        result.put(ExportableItemConstants.PRIMARY_DATA_SOURCE_NAME, "write_ds");
        result.put(ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES, "read_ds_0,read_ds_1");
        return result;
    }
    
    private Map<String, Map<String, String>> exportStaticDataSources() {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        Map<String, String> staticRule = new LinkedHashMap<>(2, 1);
        staticRule.put(ExportableItemConstants.PRIMARY_DATA_SOURCE_NAME, "ds_0");
        staticRule.put(ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES, "ds_1");
        result.put("static_rule_1", staticRule);
        return result;
    }
    
    private RuleConfiguration createRuleConfigurationWithAutoAwareDataSource() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig = new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds", null,
                new DynamicReadwriteSplittingStrategyConfiguration("rd_rs", "false"), "");
        return new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceRuleConfig), null);
    }
}
