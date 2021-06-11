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

import org.apache.shardingsphere.distsql.parser.segment.FunctionSegment;
import org.apache.shardingsphere.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.impl.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DuplicateTablesException;
import org.apache.shardingsphere.proxy.backend.exception.InvalidShardingAlgorithmsException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterShardingTableRuleBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private AlterShardingTableRuleStatement sqlStatement;
    
    @Mock
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private TransactionContexts transactionContexts;
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereRuleMetaData ruleMetaData;
    
    @Mock
    private ShardingSphereResource shardingSphereResource;
    
    private final AlterShardingTableRuleBackendHandler handler = new AlterShardingTableRuleBackendHandler(sqlStatement, backendConnection);
    
    @Before
    public void setUp() {
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singletonList("test"));
        when(metaDataContexts.getMetaData(eq("test"))).thenReturn(shardingSphereMetaData);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
    }
    
    @Test(expected = ShardingTableRuleNotExistedException.class)
    public void assertExecuteWithoutShardingRule() {
        handler.execute("test", sqlStatement);
    }
    
    @Test
    public void assertExecute() {
        TableRuleSegment tableRuleSegment = new TableRuleSegment();
        tableRuleSegment.setLogicTable("t_order");
        FunctionSegment functionSegment = new FunctionSegment();
        functionSegment.setAlgorithmName("hash_mod");
        functionSegment.setAlgorithmProps(new Properties());
        tableRuleSegment.setTableStrategy(functionSegment);
        tableRuleSegment.setDataSources(Collections.singleton("ds_0"));
        tableRuleSegment.setTableStrategyColumn("order_id");
        when(ruleMetaData.getConfigurations()).thenReturn(buildShardingConfigurations());
        when(sqlStatement.getRules()).thenReturn(Collections.singleton(tableRuleSegment));
        when(shardingSphereMetaData.getResource()).thenReturn(shardingSphereResource);
        Map<String, DataSource> dataSourceMap = mock(Map.class);
        when(shardingSphereResource.getDataSources()).thenReturn(dataSourceMap);
        when(dataSourceMap.containsKey(anyString())).thenReturn(true);
        handler.execute("test", sqlStatement);
        ResponseHeader responseHeader = handler.execute("test", sqlStatement);
        assertNotNull(responseHeader);
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
    
    @Test(expected = DuplicateTablesException.class)
    public void assertExecuteWithDuplicateTablesInRDL() {
        TableRuleSegment tableRuleSegment = new TableRuleSegment();
        tableRuleSegment.setLogicTable("t_order");
        tableRuleSegment.setDataSources(Collections.emptyList());
        when(sqlStatement.getRules()).thenReturn(Arrays.asList(tableRuleSegment, tableRuleSegment));
        handler.execute("test", sqlStatement);
    }
    
    @Test(expected = ShardingTableRuleNotExistedException.class)
    public void assertExecuteWithoutExistTable() {
        TableRuleSegment tableRuleSegment = new TableRuleSegment();
        tableRuleSegment.setLogicTable("t_order_1");
        tableRuleSegment.setDataSources(Collections.emptyList());
        when(ruleMetaData.getConfigurations()).thenReturn(buildShardingConfigurations());
        when(sqlStatement.getRules()).thenReturn(Collections.singleton(tableRuleSegment));
        handler.execute("test", sqlStatement);
    }
    
    @Test(expected = InvalidShardingAlgorithmsException.class)
    public void assertExecuteWithInvalidAlgorithms() {
        TableRuleSegment tableRuleSegment = new TableRuleSegment();
        tableRuleSegment.setLogicTable("t_order_item");
        tableRuleSegment.setDataSources(Collections.emptyList());
        FunctionSegment shardingAlgorithm = new FunctionSegment();
        shardingAlgorithm.setAlgorithmName("algorithm-not-exist");
        tableRuleSegment.setTableStrategy(shardingAlgorithm);
        when(sqlStatement.getRules()).thenReturn(Collections.singleton(tableRuleSegment));
        when(ruleMetaData.getConfigurations()).thenReturn(buildShardingConfigurations());
        handler.execute("test", sqlStatement);
    }
    
    private Collection<RuleConfiguration> buildShardingConfigurations() {
        ShardingRuleConfiguration config = new ShardingRuleConfiguration();
        config.getTables().add(new ShardingTableRuleConfiguration("t_order_item"));
        ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfiguration = new ShardingAutoTableRuleConfiguration("t_order");
        shardingAutoTableRuleConfiguration.setShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "test"));
        config.getAutoTables().add(shardingAutoTableRuleConfiguration);
        return new ArrayList<>(Collections.singleton(config));
    }
}
