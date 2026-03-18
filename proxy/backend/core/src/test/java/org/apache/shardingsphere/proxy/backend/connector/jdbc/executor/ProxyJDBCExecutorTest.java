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

package org.apache.shardingsphere.proxy.backend.connector.jdbc.executor;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnector;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.callback.ProxyJDBCExecutorCallback;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.callback.ProxyJDBCExecutorCallbackFactory;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ProxyJDBCExecutorTest {
    
    @Test
    void assertExecuteWithCallbacks() throws SQLException, ReflectiveOperationException {
        final Object originalContextManager = Plugins.getMemberAccessor().get(ProxyContext.class.getDeclaredField("contextManager"), ProxyContext.getInstance());
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ProxyContext.init(contextManager);
        JDBCDriverType type = JDBCDriverType.STATEMENT;
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getUsedDatabaseName()).thenReturn("logic_db");
        DatabaseProxyConnector databaseProxyConnector = mock(DatabaseProxyConnector.class);
        JDBCExecutor jdbcExecutor = mock(JDBCExecutor.class);
        ProxyJDBCExecutor executor = new ProxyJDBCExecutor(type, connectionSession, databaseProxyConnector, jdbcExecutor);
        SQLStatement sqlStatement = mock(SQLStatement.class);
        QueryContext queryContext = mockQueryContext(sqlStatement);
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = new ExecutionGroupContext<>(Collections.emptyList(), new ExecutionGroupReportContext("pid", "logic_db"));
        ProxyJDBCExecutorCallback firstCallback = mock(ProxyJDBCExecutorCallback.class);
        ProxyJDBCExecutorCallback secondCallback = mock(ProxyJDBCExecutorCallback.class);
        List<ExecuteResult> expected = Collections.singletonList(mock(ExecuteResult.class));
        try (MockedStatic<ProxyJDBCExecutorCallbackFactory> mockedStatic = mockStatic(ProxyJDBCExecutorCallbackFactory.class)) {
            ResourceMetaData resourceMetaData = contextManager.getMetaDataContexts().getMetaData().getDatabase("logic_db").getResourceMetaData();
            DatabaseType protocolType = contextManager.getMetaDataContexts().getMetaData().getDatabase("logic_db").getProtocolType();
            mockedStatic.when(() -> ProxyJDBCExecutorCallbackFactory.newInstance(type, protocolType, resourceMetaData, sqlStatement, databaseProxyConnector, true, false, true))
                    .thenReturn(firstCallback);
            mockedStatic.when(() -> ProxyJDBCExecutorCallbackFactory.newInstance(type, protocolType, resourceMetaData, sqlStatement, databaseProxyConnector, true, false, false))
                    .thenReturn(secondCallback);
            when(jdbcExecutor.execute(executionGroupContext, firstCallback, secondCallback)).thenReturn(expected);
            assertThat(executor.execute(queryContext, executionGroupContext, true, false), is(expected));
            mockedStatic.verify(() -> ProxyJDBCExecutorCallbackFactory.newInstance(type, protocolType, resourceMetaData, sqlStatement, databaseProxyConnector, true, false, true));
            mockedStatic.verify(() -> ProxyJDBCExecutorCallbackFactory.newInstance(type, protocolType, resourceMetaData, sqlStatement, databaseProxyConnector, true, false, false));
        }
        ProcessRegistry.getInstance().remove("pid");
        Plugins.getMemberAccessor().set(ProxyContext.class.getDeclaredField("contextManager"), ProxyContext.getInstance(), originalContextManager);
    }
    
    private QueryContext mockQueryContext(final SQLStatement sqlStatement) {
        QueryContext result = mock(QueryContext.class);
        when(result.getSql()).thenReturn("SELECT 1");
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        when(result.getSqlStatementContext()).thenReturn(sqlStatementContext);
        return result;
    }
}
