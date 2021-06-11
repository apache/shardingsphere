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
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.distsql.parser.segment.rdl.DatabaseDiscoveryRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.impl.AlterDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DatabaseDiscoveryRulesNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.InvalidDatabaseDiscoveryTypesException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterDatabaseDiscoveryRuleBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private AlterDatabaseDiscoveryRuleStatement sqlStatement;
    
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

    @Mock
    private DatabaseDiscoveryDataSourceRuleConfiguration databaseDiscoveryDataSourceRuleConfiguration;
    
    private AlterDatabaseDiscoveryRuleBackendHandler handler = new AlterDatabaseDiscoveryRuleBackendHandler(sqlStatement, backendConnection);
    
    @Before
    public void setUp() {
        ShardingSphereServiceLoader.register(DatabaseDiscoveryType.class);
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singletonList("test"));
        when(metaDataContexts.getMetaData(eq("test"))).thenReturn(shardingSphereMetaData);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
    }
    
    @Test
    public void assertExecute() {
        DatabaseDiscoveryRuleSegment databaseDiscoveryRuleSegment = new DatabaseDiscoveryRuleSegment();
        databaseDiscoveryRuleSegment.setName("ha_group");
        databaseDiscoveryRuleSegment.setDataSources(Arrays.asList("ds_0", "ds_1"));
        databaseDiscoveryRuleSegment.setDiscoveryTypeName("TEST");
        when(sqlStatement.getRules()).thenReturn(Collections.singletonList(databaseDiscoveryRuleSegment));
        when(ruleMetaData.getConfigurations()).thenReturn(Collections
                .singletonList(new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Collections
                        .singleton(databaseDiscoveryDataSourceRuleConfiguration)), Maps.newHashMap())));
        when(databaseDiscoveryDataSourceRuleConfiguration.getName()).thenReturn("ha_group");
        when(shardingSphereMetaData.getResource()).thenReturn(shardingSphereResource);
        Map<String, DataSource> dataSourceMap = mock(Map.class);
        when(shardingSphereResource.getDataSources()).thenReturn(dataSourceMap);
        when(dataSourceMap.containsKey(anyString())).thenReturn(true);
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
    public void assertExecuteWithNoAlteredDatabaseDiscoveryRule() {
        DatabaseDiscoveryRuleSegment databaseDiscoveryRuleSegment = new DatabaseDiscoveryRuleSegment();
        databaseDiscoveryRuleSegment.setName("ha_group");
        databaseDiscoveryRuleSegment.setDataSources(Arrays.asList("ds_0", "ds_1"));
        databaseDiscoveryRuleSegment.setDiscoveryTypeName("TEST");
        when(sqlStatement.getRules()).thenReturn(Collections.singletonList(databaseDiscoveryRuleSegment));
        when(ruleMetaData.getConfigurations()).thenReturn(Collections.singletonList(new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Maps.newHashMap())));
        handler.execute("test", sqlStatement);
    }
    
    @Test(expected = ResourceNotExistedException.class)
    public void assertExecuteWithNotExistResources() {
        DatabaseDiscoveryRuleSegment databaseDiscoveryRuleSegment = new DatabaseDiscoveryRuleSegment();
        databaseDiscoveryRuleSegment.setName("ha_group");
        databaseDiscoveryRuleSegment.setDataSources(Arrays.asList("ds_0", "ds_1"));
        databaseDiscoveryRuleSegment.setDiscoveryTypeName("TEST");
        when(sqlStatement.getRules()).thenReturn(Collections.singletonList(databaseDiscoveryRuleSegment));
        when(ruleMetaData.getConfigurations()).thenReturn(Collections
                .singletonList(new DatabaseDiscoveryRuleConfiguration(Collections
                        .singleton(databaseDiscoveryDataSourceRuleConfiguration), Maps.newHashMap())));
        when(databaseDiscoveryDataSourceRuleConfiguration.getName()).thenReturn("ha_group");
        handler.execute("test", sqlStatement);
    }

    @Test(expected = InvalidDatabaseDiscoveryTypesException.class)
    public void assertExecuteWithInvalidDiscoveryTypes() {
        DatabaseDiscoveryRuleSegment databaseDiscoveryRuleSegment = new DatabaseDiscoveryRuleSegment();
        databaseDiscoveryRuleSegment.setName("ha_group");
        databaseDiscoveryRuleSegment.setDataSources(Arrays.asList("ds_0", "ds_1"));
        databaseDiscoveryRuleSegment.setDiscoveryTypeName("notExistType");
        when(sqlStatement.getRules()).thenReturn(Collections.singletonList(databaseDiscoveryRuleSegment));
        when(ruleMetaData.getConfigurations()).thenReturn(Collections
                .singletonList(new DatabaseDiscoveryRuleConfiguration(Collections
                        .singleton(databaseDiscoveryDataSourceRuleConfiguration), Maps.newHashMap())));
        when(databaseDiscoveryDataSourceRuleConfiguration.getName()).thenReturn("ha_group");
        when(shardingSphereMetaData.getResource()).thenReturn(shardingSphereResource);
        Map<String, DataSource> dataSourceMap = mock(Map.class);
        when(shardingSphereResource.getDataSources()).thenReturn(dataSourceMap);
        when(dataSourceMap.containsKey(anyString())).thenReturn(true);
        handler.execute("test", sqlStatement);
    }
}
