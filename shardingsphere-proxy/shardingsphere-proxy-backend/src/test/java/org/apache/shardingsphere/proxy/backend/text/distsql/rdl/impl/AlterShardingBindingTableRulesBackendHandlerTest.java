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

import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DuplicateBindingTablesException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.segment.BindingTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingBindingTableRulesStatement;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterShardingBindingTableRulesBackendHandlerTest {
    
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
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singletonList("test"));
        when(metaDataContexts.getMetaData(eq("test"))).thenReturn(shardingSphereMetaData);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(shardingSphereRuleMetaData);
    }
    
    @Test(expected = ShardingTableRuleNotExistedException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() {
        when(shardingSphereRuleMetaData.getConfigurations()).thenReturn(Collections.singletonList(new ShardingRuleConfiguration()));
        AlterShardingBindingTableRulesStatement sqlStatement = buildShardingTableRuleStatement();
        RDLBackendHandler<AlterShardingBindingTableRulesStatement> handler = new RDLBackendHandler<>(sqlStatement, backendConnection, ShardingRuleConfiguration.class);
        ResponseHeader responseHeader = handler.execute("test", sqlStatement);
        assertNotNull(responseHeader);
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
    
    @Test(expected = DuplicateBindingTablesException.class)
    public void assertCheckSQLStatementWithDuplicateTables() {
        when(shardingSphereRuleMetaData.getConfigurations()).thenReturn(Collections.singletonList(buildShardingRuleConfiguration()));
        AlterShardingBindingTableRulesStatement sqlStatement = buildDuplicateShardingTableRuleStatement();
        RDLBackendHandler<AlterShardingBindingTableRulesStatement> handler = new RDLBackendHandler<>(sqlStatement, backendConnection, ShardingRuleConfiguration.class);
        handler.execute("test", sqlStatement);
    }
    
    @Test
    public void assertUpdateCurrentRuleConfiguration() {
        when(shardingSphereRuleMetaData.getConfigurations()).thenReturn(Collections.singletonList(buildShardingRuleConfiguration()));
        AlterShardingBindingTableRulesStatement sqlStatement = buildShardingTableRuleStatement();
        RDLBackendHandler<AlterShardingBindingTableRulesStatement> handler = new RDLBackendHandler<>(sqlStatement, backendConnection, ShardingRuleConfiguration.class);
        ResponseHeader responseHeader = handler.execute("test", sqlStatement);
        assertNotNull(responseHeader);
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
    
    private ShardingRuleConfiguration buildShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order"));
        result.getTables().add(new ShardingTableRuleConfiguration("t_order_item"));
        result.getTables().add(new ShardingTableRuleConfiguration("t_1"));
        result.getTables().add(new ShardingTableRuleConfiguration("t_2"));
        result.getBindingTableGroups().addAll(Collections.singletonList("t_order,t_order_item"));
        return result;
    }
    
    private AlterShardingBindingTableRulesStatement buildShardingTableRuleStatement() {
        return new AlterShardingBindingTableRulesStatement(Arrays.asList(new BindingTableRuleSegment("t_order,t_order_item"), new BindingTableRuleSegment("t_1,t_2")));
    }
    
    private AlterShardingBindingTableRulesStatement buildDuplicateShardingTableRuleStatement() {
        return new AlterShardingBindingTableRulesStatement(Arrays.asList(new BindingTableRuleSegment("t_order,t_order_item"), new BindingTableRuleSegment("t_order,t_order_item")));
    }
}
