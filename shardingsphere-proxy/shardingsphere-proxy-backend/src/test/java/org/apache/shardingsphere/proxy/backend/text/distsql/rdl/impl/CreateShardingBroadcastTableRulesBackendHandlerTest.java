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

import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ShardingBroadcastTableRulesExistsException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateShardingBroadcastTableRulesBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private TransactionContexts transactionContexts;
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereRuleMetaData shardingSphereRuleMetaData;
    
    @Before
    public void setUp() throws Exception {
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singleton("test"));
        when(metaDataContexts.getMetaData(eq("test"))).thenReturn(shardingSphereMetaData);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(shardingSphereRuleMetaData);
    }
    
    @Test
    public void assertExecuteWithoutShardingRuleConfiguration() {
        when(shardingSphereRuleMetaData.getConfigurations()).thenReturn(new ArrayList<>());
        CreateShardingBroadcastTableRulesStatement statement = new CreateShardingBroadcastTableRulesStatement(Collections.singleton("t_1"));
        CreateShardingBroadcastTableRulesBackendHandler handler = new CreateShardingBroadcastTableRulesBackendHandler(statement, backendConnection);
        ResponseHeader responseHeader = handler.execute("test", statement);
        assertNotNull(responseHeader);
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
    
    @Test(expected = ShardingBroadcastTableRulesExistsException.class)
    public void assertExecuteWithExistShardingBroadcastTableRules() {
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.getBroadcastTables().add("t_1");
        when(shardingSphereRuleMetaData.getConfigurations()).thenReturn(Collections.singleton(shardingRuleConfiguration));
        CreateShardingBroadcastTableRulesStatement statement = new CreateShardingBroadcastTableRulesStatement(Collections.singleton("t_1"));
        CreateShardingBroadcastTableRulesBackendHandler handler = new CreateShardingBroadcastTableRulesBackendHandler(statement, backendConnection);
        handler.execute("test", statement);
    }
    
    @Test
    public void assertExecuteWithNotExistShardingBroadcastTableRules() {
        when(shardingSphereRuleMetaData.getConfigurations()).thenReturn(Collections.singleton(new ShardingRuleConfiguration()));
        CreateShardingBroadcastTableRulesStatement statement = new CreateShardingBroadcastTableRulesStatement(Collections.singleton("t_1"));
        CreateShardingBroadcastTableRulesBackendHandler handler = new CreateShardingBroadcastTableRulesBackendHandler(statement, backendConnection);
        ResponseHeader responseHeader = handler.execute("test", statement);
        assertNotNull(responseHeader);
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
}
