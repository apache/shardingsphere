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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.CountInstanceRulesStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CountInstanceRulesTest extends ProxyContextRestorer {
    
    @Mock
    private ShardingSphereDatabase database1;
    
    @Mock
    private ShardingSphereDatabase database2;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Before
    public void before() {
        Collection<ShardingSphereRule> rules = new LinkedList<>();
        rules.add(mockSingleTableRule());
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ruleMetaData.findRules(any())).thenReturn(rules);
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>();
        ruleConfigs.add(mockShardingTableRule());
        ruleConfigs.add(mockReadwriteSplittingRule());
        ruleConfigs.add(mockEncryptRule());
        when(ruleMetaData.getConfigurations()).thenReturn(ruleConfigs);
        when(database1.getRuleMetaData()).thenReturn(ruleMetaData);
        when(database2.getRuleMetaData()).thenReturn(ruleMetaData);
        Map<String, ShardingSphereDatabase> databases = new HashMap<>(2, 1);
        databases.put("db_1", database1);
        databases.put("db_2", database2);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(databases);
        when(contextManager.getMetaDataContexts().getDatabase("db_1")).thenReturn(database1);
        when(contextManager.getMetaDataContexts().getDatabase("db_2")).thenReturn(database2);
        ProxyContext.init(contextManager);
    }
    
    private SingleTableRule mockSingleTableRule() {
        SingleTableRule result = mock(SingleTableRule.class);
        when(result.export(ExportableConstants.EXPORT_SINGLE_TABLES)).thenReturn(java.util.Optional.of(Arrays.asList("single_table_1", "single_table_2")));
        return result;
    }
    
    private RuleConfiguration mockShardingTableRule() {
        ShardingRuleConfiguration result = mock(ShardingRuleConfiguration.class);
        when(result.getTables()).thenReturn(Collections.singletonList(new ShardingTableRuleConfiguration("sharding_table")));
        when(result.getAutoTables()).thenReturn(Collections.singletonList(new ShardingAutoTableRuleConfiguration("sharding_auto_table")));
        when(result.getBindingTableGroups()).thenReturn(Collections.singletonList("binding_table_1,binding_table_2"));
        when(result.getBroadcastTables()).thenReturn(Arrays.asList("broadcast_table_1", "broadcast_table_2"));
        return result;
    }
    
    private RuleConfiguration mockReadwriteSplittingRule() {
        ReadwriteSplittingRuleConfiguration result = mock(ReadwriteSplittingRuleConfiguration.class);
        when(result.getDataSources()).thenReturn(Collections.singletonList(new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_splitting", "", new Properties(), "")));
        return result;
    }
    
    private RuleConfiguration mockEncryptRule() {
        EncryptRuleConfiguration result = mock(EncryptRuleConfiguration.class);
        when(result.getTables()).thenReturn(Collections.singletonList(new EncryptTableRuleConfiguration("encrypt_table", Collections.emptyList(), false)));
        return result;
    }
    
    @Test
    public void assertGetRowData() throws SQLException {
        CountInstanceRulesHandler handler = new CountInstanceRulesHandler().initStatement(new CountInstanceRulesStatement());
        handler.execute();
        handler.next();
        Collection<Object> actual = handler.getRowData();
        assertThat(actual.size(), is(2));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("single_table"));
        assertThat(rowData.next(), is(4));
        handler.next();
        actual = handler.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding_table"));
        assertThat(rowData.next(), is(4));
        handler.next();
        actual = handler.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding_binding_table"));
        assertThat(rowData.next(), is(2));
        handler.next();
        actual = handler.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding_broadcast_table"));
        assertThat(rowData.next(), is(4));
        handler.next();
        actual = handler.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding_scaling"));
        assertThat(rowData.next(), is(0));
        handler.next();
        actual = handler.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("readwrite_splitting"));
        assertThat(rowData.next(), is(2));
        handler.next();
        actual = handler.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("db_discovery"));
        assertThat(rowData.next(), is(0));
        handler.next();
        actual = handler.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("encrypt"));
        assertThat(rowData.next(), is(2));
    }
    
    @Test
    public void assertGetRowDataWithoutConfiguration() throws SQLException {
        CountInstanceRulesHandler handler = new CountInstanceRulesHandler().initStatement(new CountInstanceRulesStatement());
        when(database1.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        when(database2.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        handler.execute();
        handler.next();
        Collection<Object> actual = handler.getRowData();
        assertThat(actual.size(), is(2));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("single_table"));
        assertThat(rowData.next(), is(4));
        handler.next();
        actual = handler.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding_table"));
        assertThat(rowData.next(), is(0));
        handler.next();
        actual = handler.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding_binding_table"));
        assertThat(rowData.next(), is(0));
        handler.next();
        actual = handler.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding_broadcast_table"));
        assertThat(rowData.next(), is(0));
        handler.next();
        actual = handler.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("sharding_scaling"));
        assertThat(rowData.next(), is(0));
        handler.next();
        actual = handler.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("readwrite_splitting"));
        assertThat(rowData.next(), is(0));
        handler.next();
        actual = handler.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("db_discovery"));
        assertThat(rowData.next(), is(0));
        handler.next();
        actual = handler.getRowData();
        rowData = actual.iterator();
        assertThat(rowData.next(), is("encrypt"));
        assertThat(rowData.next(), is(0));
    }
}
