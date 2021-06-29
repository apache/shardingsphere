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
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.InvalidLoadBalancersException;
import org.apache.shardingsphere.proxy.backend.exception.ReadwriteSplittingRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.spi.ReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterReadwriteSplittingRuleBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private AlterReadwriteSplittingRuleStatement sqlStatement;
    
    @Mock
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private TransactionContexts transactionContexts;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereRuleMetaData ruleMetaData;

    @Mock
    private ShardingSphereResource shardingSphereResource;
    
    @Mock
    private ReadwriteSplittingDataSourceRuleConfiguration readwriteSplittingDataSourceRuleConfiguration;
    
    private final RDLBackendHandler<AlterReadwriteSplittingRuleStatement> handler = new RDLBackendHandler<>(sqlStatement, backendConnection, ReadwriteSplittingRuleConfiguration.class);
    
    @Before
    public void setUp() {
        ShardingSphereServiceLoader.register(ReplicaLoadBalanceAlgorithm.class);
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singleton("test"));
        when(metaDataContexts.getMetaData(eq("test"))).thenReturn(shardingSphereMetaData);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
    }
    
    @Test
    public void assertExecute() {
        ReadwriteSplittingRuleSegment readwriteSplittingRuleSegment = new ReadwriteSplittingRuleSegment("pr_ds", "ds_write", Arrays.asList("ds_read_0", "ds_read_1"), "TEST", new Properties());
        when(sqlStatement.getRules()).thenReturn(Collections.singleton(readwriteSplittingRuleSegment));
        when(ruleMetaData.getConfigurations()).thenReturn(
                Collections.singleton(new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(readwriteSplittingDataSourceRuleConfiguration)), new LinkedHashMap<>())));
        when(readwriteSplittingDataSourceRuleConfiguration.getName()).thenReturn("pr_ds");
        when(shardingSphereMetaData.getResource()).thenReturn(shardingSphereResource);
        ResponseHeader responseHeader = handler.execute("test", sqlStatement);
        assertNotNull(responseHeader);
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
    
    @Test(expected = ReadwriteSplittingRuleNotExistedException.class)
    public void assertExecuteWithNotExistReadwriteSplittingRule() {
        when(ruleMetaData.getConfigurations()).thenReturn(Collections.emptyList());
        handler.execute("test", sqlStatement);
    }

    @Test(expected = ReadwriteSplittingRuleNotExistedException.class)
    public void assertExecuteWithNoAlteredReadwriteSplittingRules() {
        ReadwriteSplittingRuleSegment readwriteSplittingRuleSegment = new ReadwriteSplittingRuleSegment("pr_ds", "ds_write", Arrays.asList("ds_read_0", "ds_read_1"), "TEST", new Properties());
        when(sqlStatement.getRules()).thenReturn(Collections.singleton(readwriteSplittingRuleSegment));
        when(ruleMetaData.getConfigurations()).thenReturn(Collections.singleton(new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), Collections.emptyMap())));
        handler.execute("test", sqlStatement);
    }
    
    @Test(expected = ResourceNotExistedException.class)
    public void assertExecuteWithNotExistResources() {
        ReadwriteSplittingRuleSegment readwriteSplittingRuleSegment = new ReadwriteSplittingRuleSegment("pr_ds", "ds_write", Arrays.asList("ds_read_0", "ds_read_1"), null, new Properties());
        when(sqlStatement.getRules()).thenReturn(Collections.singleton(readwriteSplittingRuleSegment));
        when(ruleMetaData.getConfigurations()).thenReturn(
                Collections.singleton(new ReadwriteSplittingRuleConfiguration(Collections.singleton(readwriteSplittingDataSourceRuleConfiguration), Collections.emptyMap())));
        when(readwriteSplittingDataSourceRuleConfiguration.getName()).thenReturn("pr_ds");
        handler.execute("test", sqlStatement);
    }
    
    @Test(expected = InvalidLoadBalancersException.class)
    public void assertExecuteWithInvalidLoadBalancer() {
        ReadwriteSplittingRuleSegment readwriteSplittingRuleSegment = new ReadwriteSplittingRuleSegment(
                "pr_ds", "ds_write", Arrays.asList("ds_read_0", "ds_read_1"), "notExistLoadBalancer", new Properties());
        when(sqlStatement.getRules()).thenReturn(Collections.singleton(readwriteSplittingRuleSegment));
        when(ruleMetaData.getConfigurations()).thenReturn(
                Collections.singleton(new ReadwriteSplittingRuleConfiguration(Collections.singleton(readwriteSplittingDataSourceRuleConfiguration), Collections.emptyMap())));
        when(readwriteSplittingDataSourceRuleConfiguration.getName()).thenReturn("pr_ds");
        when(shardingSphereMetaData.getResource()).thenReturn(shardingSphereResource);
        handler.execute("test", sqlStatement);
    }
}
