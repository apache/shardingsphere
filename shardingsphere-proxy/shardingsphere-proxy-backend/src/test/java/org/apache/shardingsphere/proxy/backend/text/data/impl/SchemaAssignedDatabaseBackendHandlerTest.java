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

package org.apache.shardingsphere.proxy.backend.text.data.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.data.DatabaseBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaAssignedDatabaseBackendHandlerTest {
    
    private static final String EXECUTE_SQL = "USE test";
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    private SchemaAssignedDatabaseBackendHandler schemaAssignedDatabaseBackendHandler;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory;
    
    @Mock
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Before
    public void setUp() throws IllegalAccessException, NoSuchFieldException, SQLException {
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), getMetaDataMap(),
                mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), mock(OptimizerContext.class), new ConfigurationProperties(new Properties()));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
        when(connectionSession.getSchemaName()).thenReturn(String.format(SCHEMA_PATTERN, 0));
        mockDatabaseCommunicationEngine(new UpdateResponseHeader(mock(SQLStatement.class)));
        SQLStatementContext<?> sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(sqlStatementContext.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        schemaAssignedDatabaseBackendHandler = new SchemaAssignedDatabaseBackendHandler(sqlStatementContext, EXECUTE_SQL, connectionSession);
        setBackendHandlerFactory(schemaAssignedDatabaseBackendHandler);
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
            when(metaData.isComplete()).thenReturn(true);
            when(metaData.hasDataSource()).thenReturn(true);
            when(metaData.getResource().getDatabaseType()).thenReturn(new H2DatabaseType());
            result.put(String.format(SCHEMA_PATTERN, i), metaData);
        }
        return result;
    }
    
    private void mockDatabaseCommunicationEngine(final ResponseHeader responseHeader) {
        when(databaseCommunicationEngine.execute()).thenReturn(responseHeader);
        when(databaseCommunicationEngineFactory.newTextProtocolInstance(any(), anyString(), any())).thenReturn(databaseCommunicationEngine);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setBackendHandlerFactory(final DatabaseBackendHandler schemaDatabaseBackendHandler) {
        Field field = schemaDatabaseBackendHandler.getClass().getDeclaredField("databaseCommunicationEngineFactory");
        field.setAccessible(true);
        field.set(schemaDatabaseBackendHandler, databaseCommunicationEngineFactory);
    }
    
    @Test
    public void assertExecuteDatabaseBackendHandler() throws SQLException {
        ResponseHeader actual = schemaAssignedDatabaseBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertDatabaseUsingStream() throws SQLException {
        schemaAssignedDatabaseBackendHandler.execute();
        while (schemaAssignedDatabaseBackendHandler.next()) {
            assertThat(schemaAssignedDatabaseBackendHandler.getRowData().size(), is(1));
        }
    }
}
