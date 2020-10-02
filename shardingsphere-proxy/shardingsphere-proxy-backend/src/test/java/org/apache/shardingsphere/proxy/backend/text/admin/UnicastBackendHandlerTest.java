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
import org.apache.shardingsphere.infra.context.schema.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.rdl.parser.engine.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class UnicastBackendHandlerTest {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    @Spy
    private BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    @Mock
    private DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory;
    
    @Before
    public void setUp() throws SQLException, IllegalAccessException, NoSuchFieldException {
        Field schemaContexts = ProxyContext.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        schemaContexts.set(ProxyContext.getInstance(), new StandardSchemaContexts(getSchemas(), 
                mock(ShardingSphereSQLParserEngine.class), mock(ExecutorKernel.class), new Authentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
        setUnderlyingHandler(new UpdateResponse());
    }
    
    private Map<String, ShardingSphereSchema> getSchemas() {
        Map<String, ShardingSphereSchema> result = new HashMap<>(10);
        for (int i = 0; i < 10; i++) {
            result.put(String.format(SCHEMA_PATTERN, i), mock(ShardingSphereSchema.class));
        }
        return result;
    }
    
    @Test
    public void assertExecuteWhileSchemaIsNull() throws SQLException {
        UnicastBackendHandler backendHandler = new UnicastBackendHandler("show variable like %s", mock(SQLStatement.class), backendConnection);
        backendConnection.setCurrentSchema(String.format(SCHEMA_PATTERN, 8));
        setDatabaseCommunicationEngine(backendHandler);
        BackendResponse actual = backendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponse.class));
        backendHandler.execute();
    }
    
    @Test
    public void assertExecuteWhileSchemaNotNull() throws SQLException {
        backendConnection.setCurrentSchema(String.format(SCHEMA_PATTERN, 0));
        UnicastBackendHandler backendHandler = new UnicastBackendHandler("show variable like %s", mock(SQLStatement.class), backendConnection);
        setDatabaseCommunicationEngine(backendHandler);
        BackendResponse actual = backendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponse.class));
        backendHandler.execute();
    }
    
    private void setUnderlyingHandler(final BackendResponse backendResponse) throws SQLException {
        DatabaseCommunicationEngine databaseCommunicationEngine = mock(DatabaseCommunicationEngine.class);
        when(databaseCommunicationEngine.execute()).thenReturn(backendResponse);
        when(databaseCommunicationEngineFactory.newTextProtocolInstance(any(), anyString(), any())).thenReturn(databaseCommunicationEngine);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setDatabaseCommunicationEngine(final UnicastBackendHandler unicastSchemaBackendHandler) {
        Field field = unicastSchemaBackendHandler.getClass().getDeclaredField("databaseCommunicationEngineFactory");
        field.setAccessible(true);
        field.set(unicastSchemaBackendHandler, databaseCommunicationEngineFactory);
    }
}
