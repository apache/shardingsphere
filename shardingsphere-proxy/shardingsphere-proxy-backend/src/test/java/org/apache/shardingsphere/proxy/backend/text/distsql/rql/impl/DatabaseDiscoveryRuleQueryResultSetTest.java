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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.RuleQueryResultSet;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DatabaseDiscoveryRuleQueryResultSetTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereRuleMetaData ruleMetaData;
    
    @Before
    public void setUp() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        ProxyContext.getInstance().init(metaDataContexts, mock(TransactionContexts.class));
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singletonList("test"));
        when(metaDataContexts.getMetaData(eq("test"))).thenReturn(shardingSphereMetaData);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
        when(ruleMetaData.getConfigurations()).thenReturn(Collections.singleton(buildDatabaseDiscoveryRuleConfiguration()));
    }
    
    private DatabaseDiscoveryRuleConfiguration buildDatabaseDiscoveryRuleConfiguration() {
        DatabaseDiscoveryDataSourceRuleConfiguration databaseDiscoveryDataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("ms_group", Arrays.asList("ds_0", "ds_1"), "test");
        ShardingSphereAlgorithmConfiguration shardingSphereAlgorithmConfig = new ShardingSphereAlgorithmConfiguration("MGR", new Properties());
        Map<String, ShardingSphereAlgorithmConfiguration> discoverTypes = new HashMap<>(1, 1);
        discoverTypes.put("test", shardingSphereAlgorithmConfig);
        return new DatabaseDiscoveryRuleConfiguration(Collections.singleton(databaseDiscoveryDataSourceRuleConfig), discoverTypes);
    }
    
    @Test
    public void assertGetRowData() {
        RuleQueryResultSet resultSet = new DatabaseDiscoveryRuleQueryResultSet();
        resultSet.init("test", mock(ShowDatabaseDiscoveryRulesStatement.class));
        Collection<Object> rowData = resultSet.getRowData();
        assertThat(rowData.size(), is(4));
        assertTrue(rowData.contains("ms_group"));
        assertTrue(rowData.contains("ds_0,ds_1"));
        assertTrue(rowData.contains("MGR"));
    }
}
