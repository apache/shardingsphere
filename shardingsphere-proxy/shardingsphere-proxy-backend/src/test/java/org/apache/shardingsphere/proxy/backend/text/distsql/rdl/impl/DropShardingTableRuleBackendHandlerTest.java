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

import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropShardingTableRuleStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ShardingRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.TablesInUsedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
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
import java.util.LinkedList;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DropShardingTableRuleBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private DropShardingTableRuleStatement sqlStatement;
    
    @Mock
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private TransactionContexts transactionContexts;
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereRuleMetaData ruleMetaData;

    @Mock
    private ShardingSphereSchema shardingSphereSchema;
    
    private DropShardingTableRuleBackendHandler handler = new DropShardingTableRuleBackendHandler(sqlStatement, backendConnection);
    
    @Before
    public void setUp() {
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singletonList("test"));
        when(metaDataContexts.getMetaData(eq("test"))).thenReturn(shardingSphereMetaData);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
        when(shardingSphereMetaData.getSchema()).thenReturn(shardingSphereSchema);
    }
    
    @Test(expected = ShardingRuleNotExistedException.class)
    public void assertExecuteWithoutShardingRule() {
        handler.execute("test", sqlStatement);
    }

    @Test(expected = ShardingTableRuleNotExistedException.class)
    public void assertExecuteWithNotExistTableRule() {
        TableNameSegment tableRuleSegment = new TableNameSegment(0, 3, new IdentifierValue("t_order"));
        when(ruleMetaData.getConfigurations()).thenReturn(Arrays.asList(new ShardingRuleConfiguration()));
        when(sqlStatement.getTableNames()).thenReturn(Arrays.asList(tableRuleSegment));
        handler.execute("test", sqlStatement);
    }

    @Test(expected = TablesInUsedException.class)
    public void assertExecuteWithTableRuleInUsed() {
        TableNameSegment tableRuleSegment = new TableNameSegment(0, 3, new IdentifierValue("t_order"));
        when(ruleMetaData.getConfigurations()).thenReturn(buildShardingConfigurations());
        when(sqlStatement.getTableNames()).thenReturn(Arrays.asList(tableRuleSegment));
        when(shardingSphereSchema.containsTable(eq("t_order"))).thenReturn(true);
        handler.execute("test", sqlStatement);
    }

    @Test
    public void assertExecute() {
        TableNameSegment tableRuleSegment = new TableNameSegment(0, 3, new IdentifierValue("t_order"));
        when(ruleMetaData.getConfigurations()).thenReturn(buildShardingConfigurations());
        when(sqlStatement.getTableNames()).thenReturn(Arrays.asList(tableRuleSegment));
        ResponseHeader responseHeader = handler.execute("test", sqlStatement);
        assertNotNull(responseHeader);
        assertTrue(responseHeader instanceof UpdateResponseHeader);
        ShardingRuleConfiguration shardingRuleConfiguration = (ShardingRuleConfiguration) ProxyContext.getInstance()
                .getMetaData("test").getRuleMetaData().getConfigurations().iterator().next();
        Collection<String> shardingTables = getShardingTables(shardingRuleConfiguration);
        assertTrue(!shardingTables.contains("t_order"));
    }
    
    private Collection<RuleConfiguration> buildShardingConfigurations() {
        ShardingRuleConfiguration configuration = new ShardingRuleConfiguration();
        configuration.getTables().add(new ShardingTableRuleConfiguration("t_order_item"));
        configuration.getAutoTables().add(new ShardingAutoTableRuleConfiguration("t_order"));
        return new ArrayList<>(Collections.singletonList(configuration));
    }

    private Collection<String> getShardingTables(final ShardingRuleConfiguration shardingRuleConfiguration) {
        Collection<String> result = new LinkedList<>();
        result.addAll(shardingRuleConfiguration.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        result.addAll(shardingRuleConfiguration.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
        return result;
    }
}
