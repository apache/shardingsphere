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

import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ShardingBroadcastTableRulesNotExistsException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DropShardingBroadcastTableRulesBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private DropShardingBroadcastTableRulesStatement sqlStatement;
    
    @Mock
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private TransactionContexts transactionContexts;
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereRuleMetaData ruleMetaData;
    
    private DropShardingBroadcastTableRulesBackendHandler handler = new DropShardingBroadcastTableRulesBackendHandler(sqlStatement, backendConnection);
    
    @Before
    public void setUp() {
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singletonList("test"));
        when(metaDataContexts.getMetaData(eq("test"))).thenReturn(shardingSphereMetaData);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
    }
    
    @Test(expected = ShardingRuleNotExistedException.class)
    public void assertExecuteWithoutShardingRule() {
        handler.execute("test", sqlStatement);
    }

    @Test(expected = ShardingBroadcastTableRulesNotExistsException.class)
    public void assertExecuteWithNotExistBroadcastTableRule() {
        when(ruleMetaData.getConfigurations()).thenReturn(Arrays.asList(new ShardingRuleConfiguration()));
        handler.execute("test", sqlStatement);
    }

    @Test
    public void assertExecute() {
        when(ruleMetaData.getConfigurations()).thenReturn(buildShardingConfigurations());
        ResponseHeader responseHeader = handler.execute("test", sqlStatement);
        assertNotNull(responseHeader);
        assertTrue(responseHeader instanceof UpdateResponseHeader);
        ShardingRuleConfiguration shardingRuleConfiguration = (ShardingRuleConfiguration) ProxyContext.getInstance()
                .getMetaData("test").getRuleMetaData().getConfigurations().iterator().next();
        assertTrue(shardingRuleConfiguration.getBroadcastTables().isEmpty());
    }
    
    private Collection<RuleConfiguration> buildShardingConfigurations() {
        ShardingRuleConfiguration configuration = new ShardingRuleConfiguration();
        configuration.getTables().add(new ShardingTableRuleConfiguration("t_order_item"));
        configuration.getAutoTables().add(new ShardingAutoTableRuleConfiguration("t_order"));
        configuration.getBroadcastTables().add("t_order");
        return new ArrayList<>(Collections.singletonList(configuration));
    }
}
