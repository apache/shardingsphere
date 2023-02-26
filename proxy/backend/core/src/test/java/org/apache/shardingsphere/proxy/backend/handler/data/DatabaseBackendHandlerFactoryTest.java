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

package org.apache.shardingsphere.proxy.backend.handler.data;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.BackendConnection;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnector;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.data.impl.UnicastDatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public final class DatabaseBackendHandlerFactoryTest {
    
    @Test
    public void assertNewInstanceReturnedUnicastDatabaseBackendHandlerWithDAL() {
        String sql = "DESC tbl";
        SQLStatementContext<DALStatement> sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(DALStatement.class));
        DatabaseBackendHandler actual = DatabaseBackendHandlerFactory.newInstance(new QueryContext(sqlStatementContext, sql, Collections.emptyList()), mock(ConnectionSession.class), false);
        assertThat(actual, instanceOf(UnicastDatabaseBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceReturnedUnicastDatabaseBackendHandlerWithQueryWithoutFrom() {
        String sql = "SELECT 1";
        SQLStatementContext<SelectStatement> sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        DatabaseBackendHandler actual = DatabaseBackendHandlerFactory.newInstance(new QueryContext(sqlStatementContext, sql, Collections.emptyList()), mock(ConnectionSession.class), false);
        assertThat(actual, instanceOf(UnicastDatabaseBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceReturnedSchemaAssignedDatabaseBackendHandler() {
        String sql = "SELECT 1 FROM user WHERE id = 1";
        SQLStatementContext<SQLStatement> sqlStatementContext = mockSQLStatementContext();
        ContextManager contextManager = mockContextManager();
        ConnectionSession connectionSession = mockConnectionSession();
        try (
                MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS);
                MockedConstruction<DatabaseConnector> ignored = mockConstruction(DatabaseConnector.class)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            DatabaseBackendHandler actual = DatabaseBackendHandlerFactory.newInstance(new QueryContext(sqlStatementContext, sql, Collections.emptyList()), connectionSession, false);
            assertThat(actual, instanceOf(DatabaseConnector.class));
        }
    }
    
    private static SQLStatementContext<SQLStatement> mockSQLStatementContext() {
        SQLStatementContext<SQLStatement> result = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getSqlStatement()).thenReturn(mock(SQLStatement.class));
        when(result.getTablesContext().getSchemaNames()).thenReturn(Collections.emptyList());
        return result;
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.isComplete()).thenReturn(true);
        when(database.containsDataSource()).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().getDatabase("db")).thenReturn(database);
        when(result.getMetaDataContexts().getMetaData().containsDatabase("db")).thenReturn(true);
        return result;
    }
    
    private static ConnectionSession mockConnectionSession() {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getDatabaseName()).thenReturn("db");
        when(result.getBackendConnection()).thenReturn(mock(BackendConnection.class));
        when(result.getBackendConnection().getConnectionSession()).thenReturn(result);
        return result;
    }
}
