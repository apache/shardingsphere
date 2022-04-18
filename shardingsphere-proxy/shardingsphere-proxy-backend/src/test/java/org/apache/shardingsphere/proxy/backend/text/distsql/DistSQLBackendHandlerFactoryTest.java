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

package org.apache.shardingsphere.proxy.backend.text.distsql;

import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowUnusedResourcesStatement;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.RALBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.RDLBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.RQLBackendHandlerFactory;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ApplyScalingStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingCheckAlgorithmsStatement;
import org.apache.shardingsphere.scaling.distsql.statement.StopScalingSourceWritingStatement;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.parser.statement.AlterShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.AlterShadowRuleStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowRuleStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.DropShadowRuleStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowAlgorithmsStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowRulesStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DistSQLBackendHandlerFactoryTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Before
    public void setUp() throws IllegalAccessException, NoSuchFieldException {
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), getMetaDataMap(),
                mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), mock(OptimizerContext.class), new ConfigurationProperties(new Properties()));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
        when(connectionSession.getSchemaName()).thenReturn("schema");
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        return Collections.singletonMap("schema", metaData);
    }
    
    @Test
    public void assertExecuteDataSourcesContext() throws SQLException {
        setContextManager(true);
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(AddResourceStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteShardingTableRuleContext() throws SQLException {
        setContextManager(true);
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getMetaData("schema");
        when(metaData.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(CreateShardingTableRuleStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteAddResourceContext() throws SQLException {
        setContextManager(true);
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(AddResourceStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteAlterResourceContext() throws SQLException {
        setContextManager(true);
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(AlterResourceStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteAlterShadowRuleContext() throws SQLException {
        setContextManager(true);
        mockShardingSphereRuleMetaData();
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(AlterShadowRuleStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteCreateShadowRuleContext() throws SQLException {
        setContextManager(true);
        mockShardingSphereRuleMetaData();
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(CreateShadowRuleStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteDropShadowRuleContext() throws SQLException {
        setContextManager(true);
        mockShardingSphereRuleMetaData();
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(DropShadowRuleStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteAlterShadowAlgorithm() throws SQLException {
        setContextManager(true);
        mockShardingSphereRuleMetaData();
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(AlterShadowAlgorithmStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteShowShadowRulesContext() throws SQLException {
        setContextManager(true);
        mockShardingSphereRuleMetaData();
        ResponseHeader response = RQLBackendHandlerFactory.newInstance(mock(ShowShadowRulesStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(QueryResponseHeader.class));
    }
    
    @Test
    public void assertExecuteShowShadowTableRulesContext() throws SQLException {
        setContextManager(true);
        mockShardingSphereRuleMetaData();
        ResponseHeader response = RQLBackendHandlerFactory.newInstance(mock(ShowShadowTableRulesStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(QueryResponseHeader.class));
    }
    
    @Test
    public void assertExecuteShowShadowAlgorithmsContext() throws SQLException {
        setContextManager(true);
        mockShardingSphereRuleMetaData();
        ResponseHeader response = RQLBackendHandlerFactory.newInstance(mock(ShowShadowAlgorithmsStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(QueryResponseHeader.class));
    }
    
    @Test
    public void assertExecuteDropShadowAlgorithmContext() throws SQLException {
        setContextManager(true);
        mockShardingSphereRuleMetaData();
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(DropShadowAlgorithmStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteDropResourceContext() throws SQLException {
        setContextManager(true);
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(DropResourceStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertExecuteDropReadwriteSplittingRuleContext() throws SQLException {
        setContextManager(true);
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(DropReadwriteSplittingRuleStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }

    @Test
    public void assertExecuteCreateReadwriteSplittingRuleContext() throws SQLException {
        setContextManager(true);
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(CreateReadwriteSplittingRuleStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertExecuteAlterReadwriteSplittingRuleContext() throws SQLException {
        setContextManager(true);
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(AlterReadwriteSplittingRuleStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteShowResourceContext() throws SQLException {
        setContextManager(true);
        ResponseHeader response = RQLBackendHandlerFactory.newInstance(mock(ShowResourcesStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(QueryResponseHeader.class));
    }
    
    @Test
    public void assertExecuteShowUnusedResourceContext() throws SQLException {
        setContextManager(true);
        ResponseHeader response = RQLBackendHandlerFactory.newInstance(mock(ShowUnusedResourcesStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(QueryResponseHeader.class));
    }
    
    @Test
    public void assertExecuteShowScalingCheckAlgorithmsContext() throws SQLException {
        mockScalingContext();
        ResponseHeader response = RALBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(ShowScalingCheckAlgorithmsStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(QueryResponseHeader.class));
    }
    
    //TODO assertExecuteStopScalingSourceWritingContext, assertExecuteCheckoutScalingContext throw exception
    @Ignore
    @Test
    public void assertExecuteStopScalingSourceWritingContext() throws SQLException {
        mockScalingContext();
        ResponseHeader response = RALBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(StopScalingSourceWritingStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Ignore
    @Test
    public void assertExecuteCheckoutScalingContext() throws SQLException {
        mockScalingContext();
        ResponseHeader response = RALBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(ApplyScalingStatement.class), connectionSession).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    private void setContextManager(final boolean isGovernance) {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = isGovernance ? mockMetaDataContexts() : new MetaDataContexts(mock(MetaDataPersistService.class));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.getInstance().init(contextManager);
    }
    
    private MetaDataContexts mockMetaDataContexts() {
        MetaDataContexts result = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(result.getAllSchemaNames()).thenReturn(Collections.singletonList("schema"));
        when(result.getMetaData("schema").getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(result.getMetaData("schema").getResource().getDataSources()).thenReturn(Collections.emptyMap());
        when(result.getMetaData("schema").getResource().getNotExistedResources(any())).thenReturn(Collections.emptyList());
        return result;
    }
    
    private void mockShardingSphereRuleMetaData() {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class);
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(metaDataContexts.getMetaData("schema")).thenReturn(shardingSphereMetaData);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
        when(shardingSphereMetaData.getResource()).thenReturn(mock(ShardingSphereResource.class));
        when(ruleMetaData.getConfigurations()).thenReturn(Collections.singletonList(mock(ShadowRuleConfiguration.class)));
    }
    
    private void mockScalingContext() {
        ModeConfiguration modeConfiguration = mock(ModeConfiguration.class);
        when(modeConfiguration.getType()).thenReturn("Cluster");
        PipelineContext.initModeConfig(modeConfiguration);
    }
    
    @After
    public void setDown() {
        setContextManager(false);
    }
}
