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

import org.apache.shardingsphere.distsql.parser.statement.rdl.RuleDefinitionStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContextFactory;
import org.apache.shardingsphere.infra.persist.PersistService;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.RDLBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.RQLBackendHandlerFactory;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DistSQLBackendHandlerFactoryTest {
    
    @Before
    public void setUp() throws IllegalAccessException, NoSuchFieldException {
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(PersistService.class), getMetaDataMap(),
                mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), new ConfigurationProperties(new Properties()), mock(OptimizeContextFactory.class));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        return Collections.singletonMap("schema", metaData);
    }
    
    @Test
    public void assertExecuteDataSourcesContext() throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        try {
            RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(AddResourceStatement.class), connection);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("No Registry center to execute `AddResourceStatement` SQL"));
        }
        setContextManager(true);
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(AddResourceStatement.class), connection).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteShardingTableRuleContext() throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        try {
            RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(CreateShardingTableRuleStatement.class), connection);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("No Registry center to execute `CreateShardingTableRuleStatement` SQL"));
        }
        setContextManager(true);
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(CreateShardingTableRuleStatement.class), connection).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteDropResourceContext() throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        try {
            RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(DropResourceStatement.class), connection);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("No Registry center to execute `DropResourceStatement` SQL"));
        }
        setContextManager(true);
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(DropResourceStatement.class), connection).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertExecuteDropReadwriteSplittingRuleContext() throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        try {
            RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(DropReadwriteSplittingRuleStatement.class), connection);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("No Registry center to execute `DropReadwriteSplittingRuleStatement` SQL"));
        }
        setContextManager(true);
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(DropReadwriteSplittingRuleStatement.class), connection).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }

    @Test
    public void assertExecuteCreateReadwriteSplittingRuleContext() throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        try {
            RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(CreateReadwriteSplittingRuleStatement.class), connection);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("No Registry center to execute `CreateReadwriteSplittingRuleStatement` SQL"));
        }
        setContextManager(true);
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(CreateReadwriteSplittingRuleStatement.class), connection).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertExecuteAlterReadwriteSplittingRuleContext() throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        try {
            RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(AlterReadwriteSplittingRuleStatement.class), connection);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("No Registry center to execute `AlterReadwriteSplittingRuleStatement` SQL"));
        }
        setContextManager(true);
        ResponseHeader response = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(AlterReadwriteSplittingRuleStatement.class), connection).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteShowResourceContext() throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        try {
            RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(RuleDefinitionStatement.class), connection);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), containsString("No Registry center to execute "));
        }
        setContextManager(true);
        ResponseHeader response = RQLBackendHandlerFactory.newInstance(mock(ShowResourcesStatement.class), connection).execute();
        assertThat(response, instanceOf(QueryResponseHeader.class));
    }
    
    private void setContextManager(final boolean isGovernance) {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = isGovernance ? mockMetaDataContexts() : new MetaDataContexts(mock(PersistService.class));
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
    
    @After
    public void setDown() {
        setContextManager(false);
    }
}
