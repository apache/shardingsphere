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

import lombok.SneakyThrows;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.AlterReplicaQueryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateReplicaQueryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropReplicaQueryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.infra.auth.builtin.DefaultAuthentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DBCreateExistsException;
import org.apache.shardingsphere.proxy.backend.exception.ReplicaQueryRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.RDLBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.RQLBackendHandlerFactory;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropDatabaseStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RDLBackendHandlerFactoryTest {
    
    @Before
    public void setUp() throws IllegalAccessException, NoSuchFieldException {
        Field metaDataContexts = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        metaDataContexts.setAccessible(true);
        metaDataContexts.set(ProxyContext.getInstance(), 
                new StandardMetaDataContexts(getMetaDataMap(), mock(ExecutorEngine.class), new DefaultAuthentication(), new ConfigurationProperties(new Properties())));
    }
    
    @Test
    public void assertExecuteMySQLCreateDatabaseContext() throws SQLException {
        assertExecuteCreateDatabaseContext(new MySQLCreateDatabaseStatement());
    }

    @Test
    public void assertExecutePostgreSQLCreateDatabaseContext() throws SQLException {
        assertExecuteCreateDatabaseContext(new PostgreSQLCreateDatabaseStatement());
    }

    private void assertExecuteCreateDatabaseContext(final CreateDatabaseStatement sqlStatement) throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        sqlStatement.setDatabaseName("new_db");
        try {
            RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), sqlStatement, connection);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is(String.format("No Registry center to execute `%s` SQL", sqlStatement.getClass().getSimpleName())));
        }
        setGovernanceMetaDataContexts(true);
        Optional<TextProtocolBackendHandler> rdlBackendHandler = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), sqlStatement, connection);
        assertTrue(rdlBackendHandler.isPresent());
        ResponseHeader response = rdlBackendHandler.get().execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteMySQLDropDatabaseContext() throws SQLException {
        assertExecuteDropDatabaseContext(new MySQLDropDatabaseStatement());
    }
    
    @Test
    public void assertExecutePostgreSQLDropDatabaseContext() throws SQLException {
        assertExecuteDropDatabaseContext(new PostgreSQLDropDatabaseStatement());
    }
    
    private void assertExecuteDropDatabaseContext(final DropDatabaseStatement sqlStatement) throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        sqlStatement.setDatabaseName("schema");
        try {
            RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), sqlStatement, connection);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is(String.format("No Registry center to execute `%s` SQL", sqlStatement.getClass().getSimpleName())));
        }
        setGovernanceMetaDataContexts(true);
        Optional<TextProtocolBackendHandler> rdlBackendHandler = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), sqlStatement, connection);
        assertTrue(rdlBackendHandler.isPresent());
        ResponseHeader response = rdlBackendHandler.get().execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteMySQLCreateDatabaseContextWithException() throws SQLException {
        assertExecuteCreateDatabaseContextWithException(new MySQLCreateDatabaseStatement());
    }
    
    @Test
    public void assertExecutePostgreSQLCreateDatabaseContextWithException() throws SQLException {
        assertExecuteCreateDatabaseContextWithException(new PostgreSQLCreateDatabaseStatement());
    }
    
    public void assertExecuteCreateDatabaseContextWithException(final CreateDatabaseStatement sqlStatement) throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        sqlStatement.setDatabaseName("schema");
        try {
            RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), sqlStatement, connection);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is(String.format("No Registry center to execute `%s` SQL", sqlStatement.getClass().getSimpleName())));
        }
        setGovernanceMetaDataContexts(true);
        try {
            RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), sqlStatement, connection);
        } catch (final DBCreateExistsException ex) {
            assertNull(ex.getMessage());
        }
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
        setGovernanceMetaDataContexts(true);
        Optional<TextProtocolBackendHandler> rdlBackendHandler = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(AddResourceStatement.class), connection);
        assertTrue(rdlBackendHandler.isPresent());
        ResponseHeader response = rdlBackendHandler.get().execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertExecuteShardingRuleContext() throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        try {
            RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(CreateShardingRuleStatement.class), connection);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("No Registry center to execute `CreateShardingRuleStatement` SQL"));
        }
        setGovernanceMetaDataContexts(true);
        Optional<TextProtocolBackendHandler> rdlBackendHandler = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(CreateShardingRuleStatement.class), connection);
        assertTrue(rdlBackendHandler.isPresent());
        ResponseHeader response = rdlBackendHandler.get().execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }

    @Test(expected = ClassCastException.class)
    public void assertExecuteDropResourceContext() throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        try {
            RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(DropResourceStatement.class), connection);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("No Registry center to execute `DropResourceStatement` SQL"));
        }
        setGovernanceMetaDataContexts(true);
        Optional<TextProtocolBackendHandler> rdlBackendHandler = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(DropResourceStatement.class), connection);
        assertTrue(rdlBackendHandler.isPresent());
        ResponseHeader response = rdlBackendHandler.get().execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }

    @Test(expected = ReplicaQueryRuleNotExistedException.class)
    public void assertExecuteDropReplicaQueryRuleContext() throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        try {
            RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(DropReplicaQueryRuleStatement.class), connection);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("No Registry center to execute `DropReplicaQueryRuleStatement` SQL"));
        }
        setGovernanceMetaDataContexts(true);
        Optional<TextProtocolBackendHandler> rdlBackendHandler = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(DropReplicaQueryRuleStatement.class), connection);
        assertTrue(rdlBackendHandler.isPresent());
        ResponseHeader response = rdlBackendHandler.get().execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }

    @Test
    public void assertExecuteCreateReplicaQueryRuleContext() throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        try {
            RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(CreateReplicaQueryRuleStatement.class), connection);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("No Registry center to execute `CreateReplicaQueryRuleStatement` SQL"));
        }
        setGovernanceMetaDataContexts(true);
        Optional<TextProtocolBackendHandler> rdlBackendHandler = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(CreateReplicaQueryRuleStatement.class), connection);
        assertTrue(rdlBackendHandler.isPresent());
        ResponseHeader response = rdlBackendHandler.get().execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }

    @Test(expected = ReplicaQueryRuleNotExistedException.class)
    public void assertExecuteAlterReplicaQueryRuleContext() throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        try {
            RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(AlterReplicaQueryRuleStatement.class), connection);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("No Registry center to execute `AlterReplicaQueryRuleStatement` SQL"));
        }
        setGovernanceMetaDataContexts(true);
        Optional<TextProtocolBackendHandler> rdlBackendHandler = RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(AlterReplicaQueryRuleStatement.class), connection);
        assertTrue(rdlBackendHandler.isPresent());
        ResponseHeader response = rdlBackendHandler.get().execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test(expected = ClassCastException.class)
    public void assertExecuteShowResourceContext() throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        try {
            RDLBackendHandlerFactory.newInstance(new MySQLDatabaseType(), mock(ShowResourcesStatement.class), connection);
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("No Registry center to execute `ShowResourcesStatement` SQL"));
        }
        setGovernanceMetaDataContexts(true);
        Optional<TextProtocolBackendHandler> rdlBackendHandler = RQLBackendHandlerFactory.newInstance(mock(ShowResourcesStatement.class), connection);
        assertTrue(rdlBackendHandler.isPresent());
        ResponseHeader response = rdlBackendHandler.get().execute();
        assertThat(response, instanceOf(QueryResponseHeader.class));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setGovernanceMetaDataContexts(final boolean isGovernance) {
        Field metaDataContexts = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        metaDataContexts.setAccessible(true);
        if (isGovernance) {
            MetaDataContexts mockedMetaDataContexts = mock(MetaDataContexts.class);
            when(mockedMetaDataContexts.getAllSchemaNames()).thenReturn(Collections.singletonList("schema"));
            ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
            when(metaData.getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
            when(mockedMetaDataContexts.getMetaData("schema")).thenReturn(metaData);
            metaDataContexts.set(ProxyContext.getInstance(), mockedMetaDataContexts);
        } else {
            metaDataContexts.set(ProxyContext.getInstance(), new StandardMetaDataContexts());
        }
    }
    
    @After
    public void setDown() {
        setGovernanceMetaDataContexts(false);
    }
}
