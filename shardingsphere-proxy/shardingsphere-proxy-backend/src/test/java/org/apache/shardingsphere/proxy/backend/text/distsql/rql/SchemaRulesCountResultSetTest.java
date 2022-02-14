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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql;

import org.apache.shardingsphere.distsql.parser.statement.rql.show.CountSchemaRulesStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.rule.SchemaRulesCountResultSet;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaRulesCountResultSetTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Before
    public void before() {
        Collection<ShardingSphereRule> rules = new LinkedList<>();
        rules.add(mockSingleTableRule());
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ruleMetaData.findRules(any())).thenReturn(rules);
        Collection<RuleConfiguration> ruleConfiguration = new LinkedList<>();
        ruleConfiguration.add(mockShardingTableRule());
        ruleConfiguration.add(mockReadwriteSplittingRule());
        ruleConfiguration.add(mockEncryptRule());
        when(ruleMetaData.getConfigurations()).thenReturn(ruleConfiguration);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
    }
    
    private SingleTableRule mockSingleTableRule() {
        SingleTableRule singleTableRule = mock(SingleTableRule.class);
        when(singleTableRule.export(ExportableConstants.EXPORTABLE_KEY_SINGLE_TABLES)).thenReturn(java.util.Optional.of(Arrays.asList("single_table_1", "single_table_2")));
        return singleTableRule;
    }
    
    private RuleConfiguration mockShardingTableRule() {
        ShardingRuleConfiguration shardingTableRule = mock(ShardingRuleConfiguration.class);
        when(shardingTableRule.getTables()).thenReturn(Collections.singletonList(new ShardingTableRuleConfiguration("sharding_table")));
        when(shardingTableRule.getAutoTables()).thenReturn(Collections.singletonList(new ShardingAutoTableRuleConfiguration("sharding_auto_table")));
        when(shardingTableRule.getBindingTableGroups()).thenReturn(Collections.singletonList("binding_table_1,binding_table_2"));
        when(shardingTableRule.getBroadcastTables()).thenReturn(Arrays.asList("broadcast_table_1", "broadcast_table_2"));
        return shardingTableRule;
    }
    
    private RuleConfiguration mockReadwriteSplittingRule() {
        ReadwriteSplittingRuleConfiguration configuration = mock(ReadwriteSplittingRuleConfiguration.class);
        when(configuration.getDataSources()).thenReturn(Collections.singletonList(new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_splitting", "", new Properties(), "")));
        return configuration;
    }
    
    private RuleConfiguration mockEncryptRule() {
        EncryptRuleConfiguration configuration = mock(EncryptRuleConfiguration.class);
        when(configuration.getTables()).thenReturn(Collections.singletonList(new EncryptTableRuleConfiguration("encrypt_table", Collections.emptyList(), false)));
        return configuration;
    }
    
    @Test
    public void assertGetRowData() {
        DistSQLResultSet resultSet = new SchemaRulesCountResultSet();
        resultSet.init(shardingSphereMetaData, mock(CountSchemaRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(3));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("single_table"));
        assertThat(rowData.next(), is("table"));
        assertThat(rowData.next(), is(2));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding"));
        assertThat(rowData.next(), is("sharding_table"));
        assertThat(rowData.next(), is(2));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding"));
        assertThat(rowData.next(), is("binding_table"));
        assertThat(rowData.next(), is(1));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding"));
        assertThat(rowData.next(), is("broadcast_table"));
        assertThat(rowData.next(), is(2));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("readwrite_splitting"));
        assertThat(rowData.next(), is("data_source"));
        assertThat(rowData.next(), is(1));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("db_discovery"));
        assertThat(rowData.next(), is("data_source"));
        assertThat(rowData.next(), is(0));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("encrypt"));
        assertThat(rowData.next(), is("table"));
        assertThat(rowData.next(), is(1));
    }
    
    @Test
    public void assertGetRowDataWithoutConfiguration() {
        DistSQLResultSet resultSet = new SchemaRulesCountResultSet();
        when(shardingSphereMetaData.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        resultSet.init(shardingSphereMetaData, mock(CountSchemaRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(3));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("single_table"));
        assertThat(rowData.next(), is("table"));
        assertThat(rowData.next(), is(2));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding"));
        assertThat(rowData.next(), is("sharding_table"));
        assertThat(rowData.next(), is(0));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding"));
        assertThat(rowData.next(), is("binding_table"));
        assertThat(rowData.next(), is(0));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding"));
        assertThat(rowData.next(), is("broadcast_table"));
        assertThat(rowData.next(), is(0));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("readwrite_splitting"));
        assertThat(rowData.next(), is("data_source"));
        assertThat(rowData.next(), is(0));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("db_discovery"));
        assertThat(rowData.next(), is("data_source"));
        assertThat(rowData.next(), is(0));
        resultSet.next();
        actual = resultSet.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("encrypt"));
        assertThat(rowData.next(), is("table"));
        assertThat(rowData.next(), is(0));
    }
}
