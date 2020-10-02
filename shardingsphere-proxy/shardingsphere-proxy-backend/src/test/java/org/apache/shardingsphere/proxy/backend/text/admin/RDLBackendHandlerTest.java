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

package org.apache.shardingsphere.proxy.backend.text.admin;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.schema.SchemaContexts;
import org.apache.shardingsphere.infra.context.schema.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DBCreateExistsException;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.rdl.parser.engine.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.rdl.parser.statement.rdl.CreateDataSourcesStatement;
import org.apache.shardingsphere.rdl.parser.statement.rdl.CreateShardingRuleStatement;
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
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RDLBackendHandlerTest {
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        Field schemaContexts = ProxyContext.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        schemaContexts.set(ProxyContext.getInstance(), new StandardSchemaContexts(getSchemas(), 
                mock(ShardingSphereSQLParserEngine.class), mock(ExecutorKernel.class), new Authentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
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
        RDLBackendHandler executeEngine = new RDLBackendHandler(connection, sqlStatement);
        try {
            executeEngine.execute();
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("No Registry center to execute `CreateDatabaseStatementContext` SQL"));
        }
        setGovernanceSchemaContexts(true);
        BackendResponse response = executeEngine.execute();
        assertThat(response, instanceOf(UpdateResponse.class));
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
        RDLBackendHandler executeEngine = new RDLBackendHandler(connection, sqlStatement);
        try {
            executeEngine.execute();
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("No Registry center to execute `DropDatabaseStatementContext` SQL"));
        }
        setGovernanceSchemaContexts(true);
        BackendResponse response = executeEngine.execute();
        assertThat(response, instanceOf(UpdateResponse.class));
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
        RDLBackendHandler executeEngine = new RDLBackendHandler(connection, sqlStatement);
        try {
            executeEngine.execute();
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("No Registry center to execute `CreateDatabaseStatementContext` SQL"));
        }
        setGovernanceSchemaContexts(true);
        try {
            executeEngine.execute();
        } catch (final DBCreateExistsException ex) {
            assertNull(ex.getMessage());
        }
    }
    
    private Map<String, ShardingSphereSchema> getSchemas() {
        return Collections.singletonMap("schema", null);
    }
    
    @Test
    public void assertExecuteDataSourcesContext() throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        RDLBackendHandler executeEngine = new RDLBackendHandler(connection, mock(CreateDataSourcesStatement.class));
        try {
            executeEngine.execute();
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("No Registry center to execute `CreateDataSourcesStatementContext` SQL"));
        }
        setGovernanceSchemaContexts(true);
        BackendResponse response = executeEngine.execute();
        assertThat(response, instanceOf(UpdateResponse.class));
    }
    
    @Test
    public void assertExecuteShardingRuleContext() throws SQLException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        RDLBackendHandler executeEngine = new RDLBackendHandler(connection, mock(CreateShardingRuleStatement.class));
        try {
            executeEngine.execute();
        } catch (final SQLException ex) {
            assertThat(ex.getMessage(), is("No Registry center to execute `CreateShardingRuleStatementContext` SQL"));
        }
        setGovernanceSchemaContexts(true);
        BackendResponse response = executeEngine.execute();
        assertThat(response, instanceOf(UpdateResponse.class));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setGovernanceSchemaContexts(final boolean isGovernance) {
        Field schemaContexts = ProxyContext.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        if (isGovernance) {
            SchemaContexts mockedSchemaContexts = mock(SchemaContexts.class);
            when(mockedSchemaContexts.getSchemas()).thenReturn(Collections.singletonMap("schema", mock(ShardingSphereSchema.class)));
            schemaContexts.set(ProxyContext.getInstance(), mockedSchemaContexts);
        } else {
            schemaContexts.set(ProxyContext.getInstance(), new StandardSchemaContexts());
        }
    }
    
    @After
    public void setDown() {
        setGovernanceSchemaContexts(false);
    }
}
