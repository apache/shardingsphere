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

package org.apache.shardingsphere.proxy.backend.handler.data.type;

import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnector;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnectorFactory;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseProxyConnectorFactory.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnicastDatabaseProxyBackendHandlerTest {
    
    private static final String EXECUTE_SQL = "SELECT 1 FROM user WHERE id = 1";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private UnicastDatabaseProxyBackendHandler unicastDatabaseProxyBackendHandler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() throws SQLException {
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        mockDatabaseProxyConnector(new UpdateResponseHeader(mock()));
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        unicastDatabaseProxyBackendHandler = new UnicastDatabaseProxyBackendHandler(
                new QueryContext(sqlStatementContext, EXECUTE_SQL, Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock()), mockContextManager(), connectionSession);
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), mock(), Collections.emptyList());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock());
        when(result.getMetaDataContexts()).thenReturn(new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics())));
        when(result.getDatabase("foo_db")).thenReturn(Collections.singleton(database).iterator().next());
        return result;
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        return result;
    }
    
    private void mockDatabaseProxyConnector(final ResponseHeader responseHeader) throws SQLException {
        DatabaseProxyConnector databaseProxyConnector = mock(DatabaseProxyConnector.class);
        when(databaseProxyConnector.execute()).thenReturn(responseHeader);
        when(DatabaseProxyConnectorFactory.newInstance(any(QueryContext.class), any(ProxyDatabaseConnectionManager.class), eq(false))).thenReturn(databaseProxyConnector);
    }
    
    @Test
    void assertExecuteDatabaseProxyBackendHandler() throws SQLException {
        ResponseHeader actual = unicastDatabaseProxyBackendHandler.execute();
        assertThat(actual, isA(UpdateResponseHeader.class));
    }
    
    @Test
    void assertDatabaseUsingStream() throws SQLException {
        unicastDatabaseProxyBackendHandler.execute();
        while (unicastDatabaseProxyBackendHandler.next()) {
            assertThat(unicastDatabaseProxyBackendHandler.getRowData().getData().size(), is(1));
        }
    }
    
    @Test
    void assertExecuteWithNullCurrentDatabaseChoosesFirstAvailable() throws SQLException {
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(connectionSession.getConnectionContext().getCurrentDatabaseName()).thenReturn(Optional.empty());
        AuthorityRule authorityRule = mock(AuthorityRule.class, RETURNS_DEEP_STUBS);
        ShardingSpherePrivileges privileges = mock(ShardingSpherePrivileges.class);
        when(privileges.hasPrivileges("bar_db")).thenReturn(true);
        when(authorityRule.findPrivileges(any())).thenReturn(Optional.of(privileges));
        ContextManager contextManager = mockContextManagerWithAuthority(authorityRule, Arrays.asList("foo_db", "bar_db"), Collections.singletonList("bar_db"));
        when(DatabaseProxyConnectorFactory.newInstance(any(QueryContext.class), any(ProxyDatabaseConnectionManager.class), eq(false))).thenReturn(mock(DatabaseProxyConnector.class, RETURNS_DEEP_STUBS));
        QueryContext queryContext = new QueryContext(mock(SQLStatementContext.class, RETURNS_DEEP_STUBS), EXECUTE_SQL, Collections.emptyList(), new HintValueContext(),
                connectionSession.getConnectionContext(), contextManager.getMetaDataContexts().getMetaData());
        new UnicastDatabaseProxyBackendHandler(queryContext, contextManager, connectionSession).execute();
        verify(connectionSession).setCurrentDatabaseName("bar_db");
    }
    
    @Test
    void assertCloseWithoutExecute() throws SQLException {
        AuthorityRule authorityRule = mock(AuthorityRule.class, RETURNS_DEEP_STUBS);
        ContextManager contextManager = mockContextManagerWithAuthority(authorityRule, Collections.singletonList("foo_db"), Collections.singletonList("foo_db"));
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        QueryContext queryContext = new QueryContext(mock(SQLStatementContext.class, RETURNS_DEEP_STUBS), EXECUTE_SQL, Collections.emptyList(), new HintValueContext(),
                connectionSession.getConnectionContext(), contextManager.getMetaDataContexts().getMetaData());
        DatabaseProxyConnector connector = mock(DatabaseProxyConnector.class);
        when(DatabaseProxyConnectorFactory.newInstance(any(QueryContext.class), any(ProxyDatabaseConnectionManager.class), eq(false))).thenReturn(connector);
        new UnicastDatabaseProxyBackendHandler(queryContext, contextManager, connectionSession).close();
        verify(connector, never()).close();
    }
    
    private ContextManager mockContextManagerWithAuthority(final AuthorityRule authorityRule, final List<String> databaseNames, final List<String> databasesWithStorageUnit) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getAllDatabaseNames()).thenReturn(databaseNames);
        databaseNames.forEach(each -> {
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
            when(database.containsDataSource()).thenReturn(databasesWithStorageUnit.contains(each));
            when(result.getDatabase(each)).thenReturn(database);
        });
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(authorityRule));
        when(metaData.getGlobalRuleMetaData()).thenReturn(ruleMetaData);
        when(result.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(ruleMetaData);
        return result;
    }
}
