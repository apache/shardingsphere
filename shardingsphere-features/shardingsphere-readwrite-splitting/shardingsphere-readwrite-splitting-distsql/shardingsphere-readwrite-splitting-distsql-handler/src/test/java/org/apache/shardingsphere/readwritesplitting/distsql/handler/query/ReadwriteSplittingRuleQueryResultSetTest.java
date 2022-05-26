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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.ExportableRule;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.fixture.ReadwriteSplittingRuleExportableFixture;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingRulesStatement;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ReadwriteSplittingRuleQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ExportableRule exportableRule = mock(ExportableRule.class);
        when(database.getRuleMetaData().findRules(any())).thenReturn(Collections.singletonList(exportableRule));
        when(exportableRule.export(anyCollection())).thenReturn(Collections.emptyMap());
        when(database.getRuleMetaData().findRuleConfigurations(any())).thenReturn(Collections.singleton(createRuleConfiguration()));
        ReadwriteSplittingRuleQueryResultSet resultSet = new ReadwriteSplittingRuleQueryResultSet();
        resultSet.init(database, mock(ShowReadwriteSplittingRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(6));
        assertTrue(actual.contains("readwrite_ds"));
        assertTrue(actual.contains("ds_primary"));
        assertTrue(actual.contains("ds_slave_0,ds_slave_1"));
        assertTrue(actual.contains("random"));
        assertTrue(actual.contains("read_weight=2:1"));
    }
    
    private RuleConfiguration createRuleConfiguration() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds", "Static", getProperties("ds_primary", "ds_slave_0,ds_slave_1"), "test");
        Properties props = new Properties();
        props.setProperty("read_weight", "2:1");
        return new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceRuleConfig), Collections.singletonMap("test", new ShardingSphereAlgorithmConfiguration("random", props)));
    }
    
    @Test
    public void assertGetRowDataWithoutLoadBalancer() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ExportableRule exportableRule = mock(ExportableRule.class);
        when(database.getRuleMetaData().findRules(any())).thenReturn(Collections.singletonList(exportableRule));
        when(exportableRule.export(anyCollection())).thenReturn(Collections.emptyMap());
        when(database.getRuleMetaData().findRuleConfigurations(any())).thenReturn(Collections.singleton(createRuleConfigurationWithoutLoadBalancer()));
        ReadwriteSplittingRuleQueryResultSet resultSet = new ReadwriteSplittingRuleQueryResultSet();
        resultSet.init(database, mock(ShowReadwriteSplittingRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(6));
        assertTrue(actual.contains("readwrite_ds"));
        assertTrue(actual.contains("write_ds"));
        assertTrue(actual.contains("read_ds_0,read_ds_1"));
    }
    
    private RuleConfiguration createRuleConfigurationWithoutLoadBalancer() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds", "Static", getProperties("write_ds", "read_ds_0,read_ds_1"), null);
        return new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceRuleConfig), null);
    }
    
    private Properties getProperties(final String writeDataSource, final String readDataSources) {
        Properties result = new Properties();
        result.setProperty("write-data-source-name", writeDataSource);
        result.setProperty("read-data-source-names", readDataSources);
        return result;
    }
    
    @Test
    public void assertGetRowDataWithAutoAwareDataSource() {
        ShardingSphereDatabase metaData = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(metaData.getRuleMetaData().findRules(ExportableRule.class)).thenReturn(Collections.singletonList(new ReadwriteSplittingRuleExportableFixture()));
        when(metaData.getRuleMetaData().findRuleConfigurations(any())).thenReturn(Collections.singleton(createRuleConfigurationWithAutoAwareDataSource()));
        ReadwriteSplittingRuleQueryResultSet resultSet = new ReadwriteSplittingRuleQueryResultSet();
        resultSet.init(metaData, mock(ShowReadwriteSplittingRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(6));
        assertTrue(actual.contains("readwrite_ds"));
        assertTrue(actual.contains("rd_rs"));
        assertTrue(actual.contains("write_ds"));
        assertTrue(actual.contains("read_ds_0,read_ds_1"));
    }
    
    private RuleConfiguration createRuleConfigurationWithAutoAwareDataSource() {
        Properties props = new Properties();
        props.setProperty("auto-aware-data-source-name", "rd_rs");
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig = new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds", "Dynamic", props, "");
        return new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceRuleConfig), null);
    }
}
