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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select;

import org.apache.shardingsphere.authority.provider.database.DatabasePermittedPrivileges;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SelectInformationSchemataExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final Grantee grantee = new Grantee("root", "127.0.0.1");
    
    private final String sql = "SELECT SCHEMA_NAME, DEFAULT_COLLATION_NAME FROM information_schema.SCHEMATA";
    
    private SelectStatement statement;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getConnectionContext().getGrantee()).thenReturn(grantee);
        statement = (SelectStatement) new SQLParserRule(
                new DefaultSQLParserRuleConfigurationBuilder().build()).getSQLParserEngine(databaseType).parse(sql, false);
    }
    
    @Test
    void assertExecuteWithUnauthorizedDatabase() throws SQLException {
        ContextManager contextManager = mockContextManager(createDatabase("no_auth_db"));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        SelectInformationSchemataExecutor executor = new SelectInformationSchemataExecutor(statement, sql, Collections.emptyList());
        executor.execute(connectionSession, contextManager.getMetaDataContexts().getMetaData());
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(0));
        assertFalse(executor.getMergedResult().next());
    }
    
    @Test
    void assertExecuteWithAuthorizedDatabase() throws SQLException {
        Map<String, String> expectedResultSetMap = new HashMap<>(2, 1F);
        expectedResultSetMap.put("SCHEMA_NAME", "foo_ds");
        expectedResultSetMap.put("DEFAULT_COLLATION_NAME", "utf8mb4");
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, (mock, mockContext) -> {
            DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
            when(dialectDatabaseMetaData.getConnectionOption().isInstanceConnectionAvailable()).thenReturn(true);
            when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData);
        })) {
            ShardingSphereDatabase database = createDatabase(expectedResultSetMap);
            ContextManager contextManager = mockContextManager(database);
            when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            SelectInformationSchemataExecutor executor = new SelectInformationSchemataExecutor(statement, sql, Collections.emptyList());
            executor.execute(connectionSession, contextManager.getMetaDataContexts().getMetaData());
            assertThat(executor.getQueryResultMetaData().getColumnCount(), is(2));
            assertTrue(executor.getMergedResult().next());
            assertThat(executor.getMergedResult().getValue(1, String.class), is("auth_db"));
            assertThat(executor.getMergedResult().getValue(2, String.class), is("utf8mb4"));
            assertFalse(executor.getMergedResult().next());
            assertFalse(executor.getMergedResult().next());
        }
    }
    
    @Test
    void assertExecuteWithAuthorizedDatabaseAndEmptyResource() throws SQLException {
        ContextManager contextManager = mockContextManager(createDatabase("auth_db"));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        SelectInformationSchemataExecutor executor = new SelectInformationSchemataExecutor(statement, sql, Collections.emptyList());
        executor.execute(connectionSession, contextManager.getMetaDataContexts().getMetaData());
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(2));
        assertTrue(executor.getMergedResult().next());
        assertThat(executor.getMergedResult().getValue(1, String.class), is("auth_db"));
        assertThat(executor.getMergedResult().getValue(2, String.class), is(""));
        assertFalse(executor.getMergedResult().next());
    }
    
    @Test
    void assertExecuteWithoutDatabase() throws SQLException {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        SelectInformationSchemataExecutor executor = new SelectInformationSchemataExecutor(statement, sql, Collections.emptyList());
        executor.execute(connectionSession, contextManager.getMetaDataContexts().getMetaData());
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(0));
    }
    
    private ContextManager mockContextManager(final ShardingSphereDatabase... databases) {
        AuthorityRule authorityRule = mock(AuthorityRule.class);
        when(authorityRule.findPrivileges(grantee)).thenReturn(Optional.of(new DatabasePermittedPrivileges(Collections.singleton("auth_db"))));
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Arrays.stream(databases).collect(Collectors.toList()), mock(ResourceMetaData.class), new RuleMetaData(Collections.singleton(authorityRule)),
                new ConfigurationProperties(new Properties()));
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics()));
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        for (ShardingSphereDatabase each : databases) {
            when(result.getDatabase(each.getName())).thenReturn(each);
        }
        when(result.getAllDatabaseNames()).thenReturn(Arrays.stream(databases).map(ShardingSphereDatabase::getName).collect(Collectors.toList()));
        return result;
    }
    
    private ShardingSphereDatabase createDatabase(final String databaseName, final ResourceMetaData resourceMetaData) {
        return new ShardingSphereDatabase(databaseName, databaseType, resourceMetaData, mock(RuleMetaData.class), Collections.emptyList());
    }
    
    private ShardingSphereDatabase createDatabase(final String databaseName) {
        return createDatabase(databaseName, new ResourceMetaData(Collections.emptyMap()));
    }
    
    private ShardingSphereDatabase createDatabase(final Map<String, String> expectedResultSetMap) throws SQLException {
        return createDatabase("auth_db", new ResourceMetaData(Collections.singletonMap("foo_ds", new MockedDataSource(mockConnection(expectedResultSetMap)))));
    }
    
    @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
    private Connection mockConnection(final Map<String, String> expectedResultSetMap) throws SQLException {
        Connection result = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getURL()).thenReturn("jdbc:mysql://localhost:3306/foo_ds");
        when(result.getCatalog()).thenReturn("foo_ds");
        ResultSet resultSet = mockResultSet(expectedResultSetMap);
        when(result.prepareStatement(any(String.class)).executeQuery()).thenReturn(resultSet);
        return result;
    }
    
    private ResultSet mockResultSet(final Map<String, String> expectedResultSetMap) throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        List<String> keys = new ArrayList<>(expectedResultSetMap.keySet());
        for (int i = 0; i < keys.size(); i++) {
            when(result.getMetaData().getColumnName(i + 1)).thenReturn(keys.get(i));
            when(result.getMetaData().getColumnLabel(i + 1)).thenReturn(keys.get(i));
            when(result.getString(i + 1)).thenReturn(expectedResultSetMap.get(keys.get(i)));
        }
        when(result.next()).thenReturn(true, false);
        when(result.getMetaData().getColumnCount()).thenReturn(expectedResultSetMap.size());
        return result;
    }
}
