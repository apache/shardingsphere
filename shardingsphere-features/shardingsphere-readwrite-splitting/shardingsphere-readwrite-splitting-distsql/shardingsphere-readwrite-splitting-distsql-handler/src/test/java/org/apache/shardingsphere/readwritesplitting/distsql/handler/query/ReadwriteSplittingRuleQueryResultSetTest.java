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

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.ExportableRule;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingRulesStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ReadwriteSplittingRuleQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        ExportableRule exportableRule = mock(ExportableRule.class);
        when(metaData.getRuleMetaData().getRules()).thenReturn(Arrays.asList(exportableRule));
        when(exportableRule.export()).thenReturn(Collections.emptyMap());
        when(metaData.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(createRuleConfiguration()));
        ReadwriteSplittingRuleQueryResultSet resultSet = new ReadwriteSplittingRuleQueryResultSet();
        resultSet.init(metaData, mock(ShowReadwriteSplittingRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(6));
        assertTrue(actual.contains("pr_ds"));
        assertTrue(actual.contains("ds_primary"));
        assertTrue(actual.contains("ds_slave_0,ds_slave_1"));
        assertTrue(actual.contains("random"));
        assertTrue(actual.contains("read_weight=2:1"));
    }
    
    private RuleConfiguration createRuleConfiguration() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("pr_ds", null, "ds_primary", Arrays.asList("ds_slave_0", "ds_slave_1"), "test");
        Properties props = new Properties();
        props.setProperty("read_weight", "2:1");
        ShardingSphereAlgorithmConfiguration shardingSphereAlgorithmConfiguration = new ShardingSphereAlgorithmConfiguration("random", props);
        return new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceRuleConfig), Collections.singletonMap("test", shardingSphereAlgorithmConfiguration));
    }
    
    @Test
    public void assertGetRowDataWithoutLoadBalancer() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        ExportableRule exportableRule = mock(ExportableRule.class);
        when(metaData.getRuleMetaData().getRules()).thenReturn(Arrays.asList(exportableRule));
        when(exportableRule.export()).thenReturn(Collections.emptyMap());
        when(metaData.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(createRuleConfigurationWithoutLoadBalancer()));
        ReadwriteSplittingRuleQueryResultSet resultSet = new ReadwriteSplittingRuleQueryResultSet();
        resultSet.init(metaData, mock(ShowReadwriteSplittingRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(6));
        assertTrue(actual.contains("pr_ds"));
        assertTrue(actual.contains("write_ds"));
        assertTrue(actual.contains("read_ds_0,read_ds_1"));
    }
    
    private RuleConfiguration createRuleConfigurationWithoutLoadBalancer() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("pr_ds", null, "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), null);
        return new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceRuleConfig), null);
    }

    @Test
    public void assertGetRowDataWithAutoAwareDataSource() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        ExportableRule exportableRule = mock(ExportableRule.class);
        when(metaData.getRuleMetaData().getRules()).thenReturn(Arrays.asList(exportableRule));
        when(exportableRule.export()).thenReturn(createAutoAwareDataSources());
        when(metaData.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(createRuleConfigurationWithAutoAwareDataSource()));
        ReadwriteSplittingRuleQueryResultSet resultSet = new ReadwriteSplittingRuleQueryResultSet();
        resultSet.init(metaData, mock(ShowReadwriteSplittingRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(6));
        assertTrue(actual.contains("pr_ds"));
        assertTrue(actual.contains("rd_rs"));
        assertTrue(actual.contains("write_ds"));
        assertTrue(actual.contains("read_ds_0,read_ds_1"));
    }

    private RuleConfiguration createRuleConfigurationWithAutoAwareDataSource() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("pr_ds", "rd_rs", null, Collections.emptyList(), null);
        return new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceRuleConfig), null);
    }
    
    private Map<String, Object> createAutoAwareDataSources() {
        Map<String, Object> result = new HashMap<>(1, 1);
        result.put(ExportableConstants.AUTO_AWARE_DATA_SOURCE_KEY, exportAutoAwareDataSourceMap());
        return result;
    }

    private Map<String, Map<String, String>> exportAutoAwareDataSourceMap() {
        Map<String, Map<String, String>> result = new HashMap<>(1, 1);
        result.put("pr_ds", getAutoAwareDataSources());
        return result;
    }
        
    private Map<String, String> getAutoAwareDataSources() {
        Map<String, String> result = new HashMap<>(2, 1);
        result.put(ExportableConstants.PRIMARY_DATA_SOURCE_NAME, "write_ds");
        result.put(ExportableConstants.REPLICA_DATA_SOURCE_NAMES, "read_ds_0,read_ds_1");
        return result;
    }
}
