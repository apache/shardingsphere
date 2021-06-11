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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl;

import com.google.common.collect.Maps;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DatabaseDiscoveryRulesNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DropDatabaseDiscoveryRuleBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private DropDatabaseDiscoveryRuleStatement sqlStatement;
    
    @Mock
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private TransactionContexts transactionContexts;
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereRuleMetaData ruleMetaData;
    
    @Mock
    private DatabaseDiscoveryDataSourceRuleConfiguration databaseDiscoveryDataSourceRuleConfiguration;
    
    @Mock
    private ShardingSphereAlgorithmConfiguration shardingSphereAlgorithmConfiguration;
    
    private final DropDatabaseDiscoveryRuleBackendHandler handler = new DropDatabaseDiscoveryRuleBackendHandler(sqlStatement, backendConnection);
    
    @Before
    public void setUp() {
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singletonList("test"));
        when(metaDataContexts.getMetaData(eq("test"))).thenReturn(shardingSphereMetaData);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
    }
    
    @Test
    public void assertExecute() {
        when(sqlStatement.getRuleNames()).thenReturn(Collections.singletonList("ha_group"));
        Map<String, ShardingSphereAlgorithmConfiguration> discoveryTypes = new HashMap<>(1, 1);
        discoveryTypes.put("pr_ds_MGR", shardingSphereAlgorithmConfiguration);
        when(ruleMetaData.getConfigurations()).thenReturn(new LinkedList<>(
                Collections.singleton(new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Collections.singleton(databaseDiscoveryDataSourceRuleConfiguration)), discoveryTypes))));
        when(databaseDiscoveryDataSourceRuleConfiguration.getName()).thenReturn("ha_group");
        when(databaseDiscoveryDataSourceRuleConfiguration.getDiscoveryTypeName()).thenReturn("pr_ds_MGR");
        ResponseHeader responseHeader = handler.execute("test", sqlStatement);
        assertNotNull(responseHeader);
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
    
    @Test(expected = DatabaseDiscoveryRulesNotExistedException.class)
    public void assertExecuteWithNotExistDatabaseDiscoveryRule() {
        when(ruleMetaData.getConfigurations()).thenReturn(Collections.emptyList());
        handler.execute("test", sqlStatement);
    }
    
    @Test(expected = DatabaseDiscoveryRulesNotExistedException.class)
    public void assertExecuteWithNoDroppedDatabaseDiscoveryRules() {
        when(sqlStatement.getRuleNames()).thenReturn(Collections.singletonList("ha_group"));
        when(ruleMetaData.getConfigurations()).thenReturn(Collections.singletonList(new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Maps.newHashMap())));
        handler.execute("test", sqlStatement);
    }
}
