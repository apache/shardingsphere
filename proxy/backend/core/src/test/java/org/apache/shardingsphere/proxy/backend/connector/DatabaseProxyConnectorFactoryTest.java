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

package org.apache.shardingsphere.proxy.backend.connector;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
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
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class DatabaseProxyConnectorFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertNewDatabaseProxyConnectorWithoutParameter() {
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class, RETURNS_DEEP_STUBS);
        when(databaseConnectionManager.getConnectionSession().getUsedDatabaseName()).thenReturn("foo_db");
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        ShardingSphereDatabase database = mockDatabase();
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.containsDatabase("foo_db")).thenReturn(true);
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        QueryContext queryContext = new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), metaData);
        ContextManager contextManager = mockContextManager(database);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        DatabaseProxyConnector engine = DatabaseProxyConnectorFactory.newInstance(queryContext, databaseConnectionManager, false);
        assertThat(engine, isA(DatabaseProxyConnector.class));
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        return result;
    }
    
    @Test
    void assertNewDatabaseProxyConnectorWithParameters() {
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class, RETURNS_DEEP_STUBS);
        when(databaseConnectionManager.getConnectionSession().getUsedDatabaseName()).thenReturn("foo_db");
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        ShardingSphereDatabase database = mockDatabase();
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.containsDatabase("foo_db")).thenReturn(true);
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        ContextManager contextManager = mockContextManager(database);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertThat(DatabaseProxyConnectorFactory.newInstance(
                new QueryContext(sqlStatementContext, "schemaName", Collections.emptyList(), new HintValueContext(), mockConnectionContext(), metaData), databaseConnectionManager, false),
                isA(DatabaseProxyConnector.class));
    }
    
    @ParameterizedTest
    @MethodSource("driverTypeProvider")
    void assertDriverTypeSelection(final boolean preferPreparedStatement, final List<Object> parameters, final JDBCDriverType expected) throws ReflectiveOperationException {
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class, RETURNS_DEEP_STUBS);
        when(databaseConnectionManager.getConnectionSession().getUsedDatabaseName()).thenReturn("foo_db");
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        when(sqlStatementContext.getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        ShardingSphereDatabase database = mockDatabase();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), new ConfigurationProperties(new Properties()));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "schemaName", parameters, new HintValueContext(), mockConnectionContext(), metaData);
        ContextManager contextManager = mockContextManager(database);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        DatabaseProxyConnector connector = DatabaseProxyConnectorFactory.newInstance(queryContext, databaseConnectionManager, preferPreparedStatement);
        assertThat((JDBCDriverType) Plugins.getMemberAccessor().get(connector.getClass().getDeclaredField("driverType"), connector), is(expected));
    }
    
    private ContextManager mockContextManager(final ShardingSphereDatabase database) {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singleton(database), mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties()));
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics()));
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(result.getDatabase("foo_db")).thenReturn(database);
        return result;
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_db");
        when(result.containsDataSource()).thenReturn(true);
        when(result.isComplete()).thenReturn(true);
        when(result.getProtocolType()).thenReturn(databaseType);
        return result;
    }
    
    private static Stream<Arguments> driverTypeProvider() {
        return Stream.of(
                Arguments.of(true, Collections.emptyList(), JDBCDriverType.PREPARED_STATEMENT),
                Arguments.of(false, Collections.singletonList(1), JDBCDriverType.PREPARED_STATEMENT));
    }
}
