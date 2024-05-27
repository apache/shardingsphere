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

import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.statement.rdl.resource.unit.type.AlterStorageUnitStatement;
import org.apache.shardingsphere.distsql.statement.rdl.resource.unit.type.RegisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.statement.rdl.resource.unit.type.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.service.persist.MetaDataManagerPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.statement.AlterDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.statement.AlterShadowRuleStatement;
import org.apache.shardingsphere.shadow.distsql.statement.CreateShadowRuleStatement;
import org.apache.shardingsphere.shadow.distsql.statement.DropShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.distsql.statement.DropShadowRuleStatement;
import org.apache.shardingsphere.shadow.distsql.statement.ShowShadowAlgorithmsStatement;
import org.apache.shardingsphere.shadow.distsql.statement.ShowShadowRulesStatement;
import org.apache.shardingsphere.shadow.distsql.statement.ShowShadowTableRulesStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sharding.distsql.statement.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DistSQLBackendHandlerFactoryTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        ShardingSphereDatabase database = mockDatabase();
        ContextManager contextManager = mockContextManager(database);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().databaseExists("foo_db")).thenReturn(true);
        when(connectionSession.getDatabaseName()).thenReturn("foo_db");
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResourceMetaData().getNotExistedDataSources(any())).thenReturn(Collections.emptyList());
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        return result;
    }
    
    private ContextManager mockContextManager(final ShardingSphereDatabase database) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = mockMetaDataContexts(database);
        when(result.getDatabase("foo_db")).thenReturn(database);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(result.getPersistServiceFacade().getMetaDataManagerPersistService()).thenReturn(mock(MetaDataManagerPersistService.class));
        when(result.getComputeNodeInstanceContext().getModeConfiguration().getType()).thenReturn("Cluster");
        return result;
    }
    
    private MetaDataContexts mockMetaDataContexts(final ShardingSphereDatabase database) {
        MetaDataContexts result = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getDatabase("foo_db")).thenReturn(database);
        return result;
    }
    
    @Test
    void assertExecuteDataSourcesContext() throws SQLException {
        assertThat(new DistSQLUpdateBackendHandler(mock(RegisterStorageUnitStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteShardingTableRuleContext() throws SQLException {
        when(ProxyContext.getInstance().getContextManager().getDatabase("foo_db").getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        assertThat(new DistSQLUpdateBackendHandler(mock(CreateShardingTableRuleStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteAddResourceContext() throws SQLException {
        assertThat(new DistSQLUpdateBackendHandler(mock(RegisterStorageUnitStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteAlterResourceContext() throws SQLException {
        assertThat(new DistSQLUpdateBackendHandler(mock(AlterStorageUnitStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteAlterShadowRuleContext() throws SQLException {
        mockRuleMetaData();
        assertThat(new DistSQLUpdateBackendHandler(mock(AlterShadowRuleStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteCreateShadowRuleContext() throws SQLException {
        mockRuleMetaData();
        assertThat(new DistSQLUpdateBackendHandler(mock(CreateShadowRuleStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteDropShadowRuleContext() throws SQLException {
        mockRuleMetaData();
        assertThat(new DistSQLUpdateBackendHandler(mock(DropShadowRuleStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteAlterDefaultShadowAlgorithm() throws SQLException {
        mockRuleMetaData();
        AlterDefaultShadowAlgorithmStatement statement = new AlterDefaultShadowAlgorithmStatement(
                new ShadowAlgorithmSegment("foo", new AlgorithmSegment("SQL_HINT", PropertiesBuilder.build(new Property("type", "value")))));
        assertThat(new DistSQLUpdateBackendHandler(statement, connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteShowShadowRulesContext() throws SQLException {
        mockRuleMetaData();
        assertThat(new DistSQLQueryBackendHandler(mock(ShowShadowRulesStatement.class), connectionSession).execute(), instanceOf(QueryResponseHeader.class));
    }
    
    @Test
    void assertExecuteShowShadowTableRulesContext() throws SQLException {
        mockRuleMetaData();
        assertThat(new DistSQLQueryBackendHandler(mock(ShowShadowTableRulesStatement.class), connectionSession).execute(), instanceOf(QueryResponseHeader.class));
    }
    
    @Test
    void assertExecuteShowShadowAlgorithmsContext() throws SQLException {
        mockRuleMetaData();
        assertThat(new DistSQLQueryBackendHandler(mock(ShowShadowAlgorithmsStatement.class), connectionSession).execute(), instanceOf(QueryResponseHeader.class));
    }
    
    @Test
    void assertExecuteDropShadowAlgorithmContext() throws SQLException {
        mockRuleMetaData();
        assertThat(new DistSQLUpdateBackendHandler(mock(DropShadowAlgorithmStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteDropResourceContext() throws SQLException {
        assertThat(new DistSQLUpdateBackendHandler(mock(UnregisterStorageUnitStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteDropReadwriteSplittingRuleContext() {
        assertThrows(MissingRequiredRuleException.class, () -> new DistSQLUpdateBackendHandler(mock(DropReadwriteSplittingRuleStatement.class), connectionSession).execute());
    }
    
    @Test
    void assertExecuteCreateReadwriteSplittingRuleContext() throws SQLException {
        assertThat(new DistSQLUpdateBackendHandler(mock(CreateReadwriteSplittingRuleStatement.class), connectionSession).execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertExecuteAlterReadwriteSplittingRuleContext() {
        assertThrows(MissingRequiredRuleException.class, () -> new DistSQLUpdateBackendHandler(mock(AlterReadwriteSplittingRuleStatement.class), connectionSession).execute());
    }
    
    private void mockRuleMetaData() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getResourceMetaData()).thenReturn(mock(ResourceMetaData.class));
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        ShadowRuleConfiguration ruleConfig = mockShadowRuleConfiguration();
        when(ruleMetaData.getConfigurations()).thenReturn(Collections.singleton(ruleConfig));
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        when(ruleMetaData.findSingleRule(ShadowRule.class)).thenReturn(Optional.of(rule));
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        when(ProxyContext.getInstance().getContextManager().getDatabase("foo_db")).thenReturn(database);
    }
    
    private ShadowRuleConfiguration mockShadowRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.getShadowAlgorithms().put("default_shadow_algorithm", mock(AlgorithmConfiguration.class));
        return result;
    }
}
