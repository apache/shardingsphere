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

package org.apache.shardingsphere.proxy.backend.handler.distsql;

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.RegisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowStorageUnitsStatement;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.RDLBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rql.RQLBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.AlterDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.AlterShadowRuleStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowRuleStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowRuleStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowAlgorithmsStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowRulesStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DistSQLBackendHandlerFactoryTest extends ProxyContextRestorer {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Before
    public void setUp() {
        when(connectionSession.getDatabaseName()).thenReturn("db");
        ProxyContext.init(mockContextManager());
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class);
        MetaDataContexts metaDataContexts = mockMetaDataContexts();
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private MetaDataContexts mockMetaDataContexts() {
        MetaDataContexts result = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().containsDatabase("db")).thenReturn(true);
        ShardingSphereDatabase database = mockDatabase();
        when(result.getMetaData().getDatabase("db")).thenReturn(database);
        return result;
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResourceMetaData().getDataSources()).thenReturn(Collections.emptyMap());
        when(result.getResourceMetaData().getNotExistedResources(any())).thenReturn(Collections.emptyList());
        when(result.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
        return result;
    }
    
    @Test
    public void assertExecuteDataSourcesContext() throws SQLException {
        assertThat(RDLBackendHandlerFactory.newInstance(mock(RegisterStorageUnitStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteShardingTableRuleContext() throws SQLException {
        when(ProxyContext.getInstance().getDatabase("db").getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
        assertThat(RDLBackendHandlerFactory.newInstance(mock(CreateShardingTableRuleStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteAddResourceContext() throws SQLException {
        assertThat(RDLBackendHandlerFactory.newInstance(mock(RegisterStorageUnitStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteAlterResourceContext() throws SQLException {
        assertThat(RDLBackendHandlerFactory.newInstance(mock(AlterStorageUnitStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteAlterShadowRuleContext() throws SQLException {
        mockShardingSphereRuleMetaData();
        assertThat(RDLBackendHandlerFactory.newInstance(mock(AlterShadowRuleStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteCreateShadowRuleContext() throws SQLException {
        mockShardingSphereRuleMetaData();
        assertThat(RDLBackendHandlerFactory.newInstance(mock(CreateShadowRuleStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteDropShadowRuleContext() throws SQLException {
        mockShardingSphereRuleMetaData();
        assertThat(RDLBackendHandlerFactory.newInstance(mock(DropShadowRuleStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteAlterDefaultShadowAlgorithm() throws SQLException {
        mockShardingSphereRuleMetaData();
        Properties props = new Properties();
        props.setProperty("type", "value");
        AlterDefaultShadowAlgorithmStatement statement = new AlterDefaultShadowAlgorithmStatement(new ShadowAlgorithmSegment("foo", new AlgorithmSegment("SIMPLE_HINT", props)));
        assertThat(RDLBackendHandlerFactory.newInstance(statement, connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteShowShadowRulesContext() throws SQLException {
        mockShardingSphereRuleMetaData();
        assertThat(RQLBackendHandlerFactory.newInstance(mock(ShowShadowRulesStatement.class), connectionSession).execute(), instanceOf(QueryResponseHeader.class));
    }
    
    @Test
    public void assertExecuteShowShadowTableRulesContext() throws SQLException {
        mockShardingSphereRuleMetaData();
        assertThat(RQLBackendHandlerFactory.newInstance(mock(ShowShadowTableRulesStatement.class), connectionSession).execute(), instanceOf(QueryResponseHeader.class));
    }
    
    @Test
    public void assertExecuteShowShadowAlgorithmsContext() throws SQLException {
        mockShardingSphereRuleMetaData();
        assertThat(RQLBackendHandlerFactory.newInstance(mock(ShowShadowAlgorithmsStatement.class), connectionSession).execute(), instanceOf(QueryResponseHeader.class));
    }
    
    @Test
    public void assertExecuteDropShadowAlgorithmContext() throws SQLException {
        mockShardingSphereRuleMetaData();
        assertThat(RDLBackendHandlerFactory.newInstance(mock(DropShadowAlgorithmStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteDropResourceContext() throws SQLException {
        assertThat(RDLBackendHandlerFactory.newInstance(mock(UnregisterStorageUnitStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertExecuteDropReadwriteSplittingRuleContext() throws SQLException {
        assertThat(RDLBackendHandlerFactory.newInstance(mock(DropReadwriteSplittingRuleStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteCreateReadwriteSplittingRuleContext() throws SQLException {
        assertThat(RDLBackendHandlerFactory.newInstance(mock(CreateReadwriteSplittingRuleStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertExecuteAlterReadwriteSplittingRuleContext() throws SQLException {
        assertThat(RDLBackendHandlerFactory.newInstance(mock(AlterReadwriteSplittingRuleStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteShowResourceContext() throws SQLException {
        assertThat(RQLBackendHandlerFactory.newInstance(mock(ShowStorageUnitsStatement.class), connectionSession).execute(), instanceOf(QueryResponseHeader.class));
    }
    
    private void mockShardingSphereRuleMetaData() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("db");
        when(database.getResourceMetaData()).thenReturn(mock(ShardingSphereResourceMetaData.class));
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        ShadowRuleConfiguration ruleConfig = mockShadowRuleConfiguration();
        when(ruleMetaData.getConfigurations()).thenReturn(Collections.singleton(ruleConfig));
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase("db")).thenReturn(database);
    }
    
    private ShadowRuleConfiguration mockShadowRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.getShadowAlgorithms().put("default_shadow_algorithm", mock(AlgorithmConfiguration.class));
        return result;
    }
}
