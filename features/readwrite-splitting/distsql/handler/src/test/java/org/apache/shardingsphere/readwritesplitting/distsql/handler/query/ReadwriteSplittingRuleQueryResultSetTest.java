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

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.constant.ExportableItemConstants;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.DynamicReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ReadwriteSplittingRuleQueryResultSetTest {
    
    @Test
    public void assertGetEmptyRule() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class)).thenReturn(Optional.empty());
        ReadwriteSplittingRuleQueryResultSet resultSet = new ReadwriteSplittingRuleQueryResultSet();
        resultSet.init(database, mock(ShowReadwriteSplittingRulesStatement.class));
        assertFalse(resultSet.next());
    }
    
    @Test
    public void assertGetRowData() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfiguration());
        when(rule.getExportData()).thenReturn(createExportedData());
        when(database.getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class)).thenReturn(Optional.of(rule));
        ReadwriteSplittingRuleQueryResultSet resultSet = new ReadwriteSplittingRuleQueryResultSet();
        resultSet.init(database, mock(ShowReadwriteSplittingRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(7));
        assertTrue(actual.contains("readwrite_ds"));
        assertTrue(actual.contains("ds_primary"));
        assertTrue(actual.contains("ds_slave_0,ds_slave_1"));
        assertTrue(actual.contains("random"));
        assertTrue(actual.contains("read_weight=2:1"));
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
        Properties props = new Properties();
        props.setProperty("read_weight", "2:1");
        return new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceRuleConfig), Collections.singletonMap("test", new AlgorithmConfiguration("random", props)));
    }
    
    @Test
    public void assertGetRowDataWithoutLoadBalancer() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfigurationWithoutLoadBalancer());
        when(rule.getExportData()).thenReturn(createExportedData());
        when(database.getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class)).thenReturn(Optional.of(rule));
        ReadwriteSplittingRuleQueryResultSet resultSet = new ReadwriteSplittingRuleQueryResultSet();
        resultSet.init(database, mock(ShowReadwriteSplittingRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(7));
        assertTrue(actual.contains("readwrite_ds"));
        assertTrue(actual.contains("write_ds"));
        assertTrue(actual.contains("read_ds_0,read_ds_1"));
    }
    
    private RuleConfiguration createRuleConfigurationWithoutLoadBalancer() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds",
                        new StaticReadwriteSplittingStrategyConfiguration("write_ds", Arrays.asList("read_ds_0", "read_ds_1")), null, null);
        return new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceRuleConfig), null);
    }
    
    @Test
    public void assertGetRowDataWithAutoAwareDataSource() {
        ShardingSphereDatabase metaData = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfigurationWithAutoAwareDataSource());
        when(rule.getExportData()).thenReturn(getExportData());
        when(metaData.getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class)).thenReturn(Optional.of(rule));
        ReadwriteSplittingRuleQueryResultSet resultSet = new ReadwriteSplittingRuleQueryResultSet();
        resultSet.init(metaData, mock(ShowReadwriteSplittingRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(7));
        assertTrue(actual.contains("readwrite_ds"));
        assertTrue(actual.contains("rd_rs"));
        assertTrue(actual.contains("false"));
        assertTrue(actual.contains("write_ds"));
        assertTrue(actual.contains("read_ds_0,read_ds_1"));
    }
    
    private Map<String, Object> getExportData() {
        Map<String, Object> result = new HashMap<>(2, 1);
        result.put(ExportableConstants.EXPORT_DYNAMIC_READWRITE_SPLITTING_RULE, exportDynamicDataSources());
        result.put(ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE, exportStaticDataSources());
        return result;
    }
    
    private Map<String, Map<String, String>> exportDynamicDataSources() {
        Map<String, Map<String, String>> result = new HashMap<>(1, 1);
        result.put("readwrite_ds", getAutoAwareDataSources());
        return result;
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
