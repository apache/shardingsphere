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
import org.apache.shardingsphere.infra.context.SchemaContext;
import org.apache.shardingsphere.infra.context.SchemaContexts;
import org.apache.shardingsphere.infra.context.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.exception.DBCreateExistsException;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.rdl.parser.statement.rdl.CreateDataSourcesStatement;
import org.apache.shardingsphere.rdl.parser.statement.rdl.CreateShardingRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RDLBackendHandlerTest {
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        Field schemaContexts = ProxySchemaContexts.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        schemaContexts.set(ProxySchemaContexts.getInstance(),
                new StandardSchemaContexts(getSchemaContextMap(), new Authentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
    }
    
    @Test
    public void assertExecuteCreateDatabaseContext() {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchema()).thenReturn("schema");
        RDLBackendHandler executeEngine = new RDLBackendHandler(connection, new CreateDatabaseStatement("new_db"));
        BackendResponse response = executeEngine.execute();
        assertThat(response, instanceOf(ErrorResponse.class));
        setGovernanceSchemaContexts(true);
        response = executeEngine.execute();
        assertThat(response, instanceOf(UpdateResponse.class));
    }
    
    @Test
    public void assertExecuteDropDatabaseContext() {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchema()).thenReturn("schema");
        RDLBackendHandler executeEngine = new RDLBackendHandler(connection, new DropDatabaseStatement("schema"));
        BackendResponse response = executeEngine.execute();
        assertThat(response, instanceOf(ErrorResponse.class));
        setGovernanceSchemaContexts(true);
        response = executeEngine.execute();
        assertThat(response, instanceOf(UpdateResponse.class));
    }
    
    @Test
    public void assertExecuteCreateDatabaseContextWithException() {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchema()).thenReturn("schema");
        RDLBackendHandler executeEngine = new RDLBackendHandler(connection, new CreateDatabaseStatement("schema"));
        BackendResponse response = executeEngine.execute();
        assertThat(response, instanceOf(ErrorResponse.class));
        setGovernanceSchemaContexts(true);
        response = executeEngine.execute();
        assertThat(response, instanceOf(ErrorResponse.class));
        assertThat(((ErrorResponse) response).getCause(), instanceOf(DBCreateExistsException.class));
    }
    
    private Map<String, SchemaContext> getSchemaContextMap() {
        SchemaContext result = new SchemaContext("schema", null, null);
        return Collections.singletonMap("schema", result);
    }
    
    @Test
    public void assertExecuteDataSourcesContext() {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchema()).thenReturn("schema");
        RDLBackendHandler executeEngine = new RDLBackendHandler(connection, mock(CreateDataSourcesStatement.class));
        BackendResponse response = executeEngine.execute();
        assertThat(response, instanceOf(ErrorResponse.class));
        setGovernanceSchemaContexts(true);
        response = executeEngine.execute();
        assertThat(response, instanceOf(UpdateResponse.class));
    }
    
    @Test
    public void assertExecuteShardingRuleContext() {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchema()).thenReturn("schema");
        RDLBackendHandler executeEngine = new RDLBackendHandler(connection, mock(CreateShardingRuleStatement.class));
        BackendResponse response = executeEngine.execute();
        assertThat(response, instanceOf(ErrorResponse.class));
        setGovernanceSchemaContexts(true);
        response = executeEngine.execute();
        assertThat(response, instanceOf(UpdateResponse.class));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setGovernanceSchemaContexts(final boolean isGovernance) {
        Field schemaContexts = ProxySchemaContexts.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        if (isGovernance) {
            SchemaContexts mockedSchemaContexts = mock(SchemaContexts.class);
            when(mockedSchemaContexts.getSchemaContexts()).thenReturn(Collections.singletonMap("schema", mock(SchemaContext.class)));
            schemaContexts.set(ProxySchemaContexts.getInstance(), mockedSchemaContexts);
        } else {
            schemaContexts.set(ProxySchemaContexts.getInstance(), new StandardSchemaContexts());
        }
    }
    
    @After
    public void setDown() {
        setGovernanceSchemaContexts(false);
    }
}
