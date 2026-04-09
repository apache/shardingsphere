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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.exception.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.PreparedStatementMetadataResolutionException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class PostgreSQLPreparedStatementMetadataFactoryTest {
    
    private static final List<Object> PARAMETERS = Collections.singletonList(1);
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(databaseType, new CacheOption(2000, 65535L), new CacheOption(128, 1024L));
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Test
    void assertLoad() throws SQLException {
        PreparedStatement expected = prepareJDBCBackendConnection(null);
        assertThat(PostgreSQLPreparedStatementMetadataFactory.load(connectionSession, createPreparedStatement(true), PARAMETERS), is(expected));
    }
    
    @Test
    void assertLoadWithSQLException() throws SQLException {
        PostgreSQLServerPreparedStatement preparedStatement = createPreparedStatement(true);
        SQLException expected = new SQLException("expected");
        prepareJDBCBackendConnection(expected);
        assertThat(assertThrows(SQLException.class, () -> PostgreSQLPreparedStatementMetadataFactory.load(connectionSession, preparedStatement, PARAMETERS)), is(expected));
    }
    
    @Test
    void assertLoadWithEmptyExecutionUnits() {
        PostgreSQLServerPreparedStatement preparedStatement = createPreparedStatement(false);
        ExecutionContext executionContext = mock(ExecutionContext.class);
        when(executionContext.getExecutionUnits()).thenReturn(Collections.emptyList());
        try (
                MockedConstruction<KernelProcessor> mockedConstruction = mockConstruction(KernelProcessor.class,
                        (mock, context) -> when(mock.generateExecutionContext(any(), any(), any())).thenReturn(executionContext))) {
            ShardingSphereSQLException actual = assertThrows(PreparedStatementMetadataResolutionException.class,
                    () -> PostgreSQLPreparedStatementMetadataFactory.load(connectionSession, preparedStatement, PARAMETERS));
            assertThat(actual.getMessage(), is("Can not resolve prepared statement metadata because no execution unit was generated."));
            assertThat(mockedConstruction.constructed().size(), is(1));
        }
    }
    
    private PostgreSQLServerPreparedStatement createPreparedStatement(final boolean withUsedDatabaseName) {
        SQLStatement sqlStatement = sqlParserEngine.parse("SELECT id FROM foo_tbl WHERE id=?", false);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("postgres");
        if (withUsedDatabaseName) {
            when(connectionSession.getUsedDatabaseName()).thenReturn("postgres");
        }
        when(connectionSession.getConnectionContext()).thenReturn(mock(ConnectionContext.class));
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        return new PostgreSQLServerPreparedStatement("SELECT id FROM foo_tbl WHERE id=?", sqlStatementContext, new HintValueContext(),
                Collections.singletonList(PostgreSQLBinaryColumnType.UNSPECIFIED), Collections.singletonList(0));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build())));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        lenient().when(result.getMetaDataContexts().getMetaData().containsDatabase("postgres")).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().containsDatabase(new IdentifierValue("postgres"))).thenReturn(true);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getProtocolType()).thenReturn(databaseType);
        when(database.getDefaultSchemaName()).thenReturn("public");
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getStorageType()).thenReturn(databaseType);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", storageUnit));
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        lenient().when(database.containsSchema("public")).thenReturn(true);
        when(database.containsSchema(new IdentifierValue("public"))).thenReturn(true);
        when(database.getAllSchemas()).thenReturn(Collections.singleton(schema));
        when(database.getSchema("public")).thenReturn(schema);
        when(database.getSchema(new IdentifierValue("public"))).thenReturn(schema);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.singletonList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList());
        lenient().when(schema.containsTable("foo_tbl")).thenReturn(true);
        when(schema.containsTable(new IdentifierValue("foo_tbl"))).thenReturn(true);
        lenient().when(schema.getTable("foo_tbl")).thenReturn(table);
        when(schema.getTable(new IdentifierValue("foo_tbl"))).thenReturn(table);
        when(result.getMetaDataContexts().getMetaData().getDatabase("postgres")).thenReturn(database);
        when(result.getMetaDataContexts().getMetaData().getDatabase(new IdentifierValue("postgres"))).thenReturn(database);
        when(result.getDatabase("postgres")).thenReturn(database);
        return result;
    }
    
    private PreparedStatement prepareJDBCBackendConnection(final SQLException ex) throws SQLException {
        PreparedStatement result = mock(PreparedStatement.class, RETURNS_DEEP_STUBS);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        if (null == ex) {
            when(connection.prepareStatement(anyString())).thenReturn(result);
        } else {
            when(connection.prepareStatement(anyString())).thenThrow(ex);
        }
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(databaseConnectionManager.getConnections(any(), nullable(String.class), anyInt(), anyInt(), any())).thenReturn(Collections.singletonList(connection));
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        return result;
    }
}
