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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare;

import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.exception.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.PreparedStatementMetadataResolutionException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class MySQLPreparedStatementMetadataFactoryTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Test
    void assertLoad() throws SQLException {
        PreparedStatement expected = mockPreparedStatement();
        mockMetaDataWithDatabase();
        mockDatabaseConnectionManager(Collections.singletonList(mockBackendConnection(expected, null)));
        try (MockedConstruction<KernelProcessor> ignored = mockKernelProcessor(createExecutionContext(createExecutionUnits()))) {
            assertThat(MySQLPreparedStatementMetadataFactory.load(connectionSession, createPreparedStatement()), is(expected));
        }
    }
    
    @Test
    void assertLoadWithNoExecutionUnit() {
        mockMetaData();
        try (MockedConstruction<KernelProcessor> ignored = mockKernelProcessor(createExecutionContext(Collections.emptyList()))) {
            ShardingSphereSQLException actual = assertThrows(PreparedStatementMetadataResolutionException.class,
                    () -> MySQLPreparedStatementMetadataFactory.load(connectionSession, createPreparedStatement()));
            assertThat(actual.getMessage(), is("Can not resolve prepared statement metadata because no execution unit was generated."));
        }
    }
    
    @Test
    void assertLoadWithNoBackendConnection() throws SQLException {
        mockMetaDataWithDatabase();
        mockDatabaseConnectionManager(Collections.emptyList());
        try (MockedConstruction<KernelProcessor> ignored = mockKernelProcessor(createExecutionContext(createExecutionUnits()))) {
            ShardingSphereSQLException actual = assertThrows(PreparedStatementMetadataResolutionException.class,
                    () -> MySQLPreparedStatementMetadataFactory.load(connectionSession, createPreparedStatement()));
            assertThat(actual.getMessage(), is("Can not resolve prepared statement metadata because no backend connection was acquired."));
        }
    }
    
    @Test
    void assertLoadWithSQLException() throws SQLException {
        SQLException expected = new SQLException("expected");
        mockMetaDataWithDatabase();
        mockDatabaseConnectionManager(Collections.singletonList(mockBackendConnection(null, expected)));
        try (MockedConstruction<KernelProcessor> ignored = mockKernelProcessor(createExecutionContext(createExecutionUnits()))) {
            assertThat(assertThrows(SQLException.class, () -> MySQLPreparedStatementMetadataFactory.load(connectionSession, createPreparedStatement())), is(expected));
        }
    }
    
    private MySQLServerPreparedStatement createPreparedStatement() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getDatabaseNames()).thenReturn(Collections.singleton("foo_db"));
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        return new MySQLServerPreparedStatement("SELECT 1", sqlStatementContext, new HintValueContext(), Collections.emptyList());
    }
    
    private void mockMetaData() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(contextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        when(metaData.getGlobalRuleMetaData()).thenReturn(mock(RuleMetaData.class));
        when(metaData.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    private void mockMetaDataWithDatabase() {
        mockMetaData();
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData();
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(metaData.containsDatabase("foo_db")).thenReturn(true);
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        when(database.getName()).thenReturn("foo_db");
    }
    
    private MockedConstruction<KernelProcessor> mockKernelProcessor(final ExecutionContext executionContext) {
        return mockConstruction(KernelProcessor.class, (mock, context) -> when(mock.generateExecutionContext(any(), any(), any())).thenReturn(executionContext));
    }
    
    private ExecutionContext createExecutionContext(final Collection<ExecutionUnit> executionUnits) {
        return new ExecutionContext(mock(), executionUnits, mock(RouteContext.class));
    }
    
    private Collection<ExecutionUnit> createExecutionUnits() {
        return Collections.singletonList(new ExecutionUnit("ds_0", new SQLUnit("SELECT 1", Collections.emptyList())));
    }
    
    private void mockDatabaseConnectionManager(final List<Connection> connections) throws SQLException {
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(databaseConnectionManager.getConnections(anyString(), anyString(), anyInt(), anyInt(), any())).thenReturn(connections);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
    }
    
    private Connection mockBackendConnection(final PreparedStatement preparedStatement, final SQLException ex) throws SQLException {
        Connection result = mock(Connection.class);
        if (null == ex) {
            when(result.prepareStatement(anyString())).thenReturn(preparedStatement);
        } else {
            when(result.prepareStatement(anyString())).thenThrow(ex);
        }
        return result;
    }
    
    private PreparedStatement mockPreparedStatement() {
        return mock(PreparedStatement.class);
    }
}
