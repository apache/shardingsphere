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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLPreparedStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussInsertStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLBatchedInsertsExecutorTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private JDBCBackendConnection backendConnection;
    
    private ContextManager previousContextManager;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Before
    public void setup() {
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        previousContextManager = ProxyContext.getInstance().getContextManager();
        ProxyContext.getInstance().init(contextManager);
    }
    
    @Test
    public void assertExecuteBatchWithEmptyParameterSets() throws SQLException {
        when(contextManager.getMetaDataContexts().getProps().getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(1);
        when(contextManager.getMetaDataContexts().getProps().getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY)).thenReturn(1);
        OpenGaussInsertStatement openGaussInsertStatement = mock(OpenGaussInsertStatement.class, RETURNS_DEEP_STUBS);
        when(openGaussInsertStatement.getTable().getTableName().getIdentifier().getValue()).thenReturn("");
        PostgreSQLPreparedStatement postgreSQLPreparedStatement = new PostgreSQLPreparedStatement("", openGaussInsertStatement, Collections.emptyList());
        PostgreSQLBatchedInsertsExecutor actual = new PostgreSQLBatchedInsertsExecutor(connectionSession, postgreSQLPreparedStatement, Collections.emptyList());
        ExecutionContext executionContext = mock(ExecutionContext.class);
        setAnyExecutionContext(actual, executionContext);
        actual.executeBatch();
        verify(backendConnection, never()).getConnections(nullable(String.class), anyInt(), any(ConnectionMode.class));
    }
    
    @SneakyThrows
    private void setAnyExecutionContext(final PostgreSQLBatchedInsertsExecutor executor, final ExecutionContext executionContext) {
        Field field = PostgreSQLBatchedInsertsExecutor.class.getDeclaredField("anyExecutionContext");
        field.setAccessible(true);
        field.set(executor, executionContext);
    }
    
    @After
    public void tearDown() {
        ProxyContext.getInstance().init(previousContextManager);
    }
}
