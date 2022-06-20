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
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
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
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CountInstanceRulesHandlerTest extends ProxyContextRestorer {
    
    @Mock
    private ShardingSphereDatabase database1;
    
    @Mock
    private ShardingSphereDatabase database2;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Before
    public void before() {
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        ShardingRule shardingRule = mockShardingRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(mockSingleTableRule(), shardingRule, mockReadwriteSplittingRule(), mockEncryptRule());
        when(ruleMetaData.getRules()).thenReturn(rules);
        when(database1.getRuleMetaData()).thenReturn(ruleMetaData);
        when(database2.getRuleMetaData()).thenReturn(ruleMetaData);
        Map<String, ShardingSphereDatabase> databases = new HashMap<>(2, 1);
        databases.put("db_1", database1);
        databases.put("db_2", database2);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(databases);
        ProxyContext.init(contextManager);
    }
    
    private SingleTableRule mockSingleTableRule() {
        SingleTableRule result = mock(SingleTableRule.class);
        when(result.getAllTables()).thenReturn(Arrays.asList("single_table_1", "single_table_2"));
        return result;
    }
    
    private ShardingRule mockShardingRule() {
        ShardingRule result = mock(ShardingRule.class);
        when(result.getConfiguration()).thenReturn(mock(ShardingRuleConfiguration.class));
        when(result.getTables()).thenReturn(Arrays.asList("sharding_table", "sharding_auto_table"));
        when(result.getBindingTableRules()).thenReturn(Collections.singletonMap("binding_table", mock(BindingTableRule.class)));
        when(result.getBroadcastTables()).thenReturn(Arrays.asList("broadcast_table_1", "broadcast_table_2"));
        return result;
    }
    
    private ReadwriteSplittingRule mockReadwriteSplittingRule() {
        ReadwriteSplittingRule result = mock(ReadwriteSplittingRule.class);
        when(result.getDataSourceMapper()).thenReturn(Collections.singletonMap("readwrite_splitting", Arrays.asList("write_ds", "read_ds")));
        return result;
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        when(result.getTables()).thenReturn(Collections.singleton("encrypt_table"));
        return result;
    }
    
    @Test
    public void assertGetRowData() throws SQLException {
        CountInstanceRulesHandler handler = new CountInstanceRulesHandler();
        handler.init(new CountInstanceRulesStatement(), null);
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
        CountInstanceRulesHandler handler = new CountInstanceRulesHandler();
        handler.init(new CountInstanceRulesStatement(), null);
        Collection<ShardingSphereRule> rules = Collections.singleton(mockSingleTableRule());
        when(database1.getRuleMetaData().getRules()).thenReturn(rules);
        when(database2.getRuleMetaData().getRules()).thenReturn(rules);
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
