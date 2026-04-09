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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.bind;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLBindCompletePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PortalContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.Portal;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.EmptyStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, ProxyBackendHandlerFactory.class})
class PostgreSQLComBindExecutorTest {
    
    private static final String DATABASE_NAME = "postgres";
    
    private static final String SQL = "SELECT id FROM t_order WHERE id = ?";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final ShardingSphereSQLParserEngine parserEngine = new ShardingSphereSQLParserEngine(databaseType, new CacheOption(2000, 65535L), new CacheOption(128, 1024L));
    
    @Mock
    private PortalContext portalContext;
    
    @Mock
    private PostgreSQLComBindPacket bindPacket;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @InjectMocks
    private PostgreSQLComBindExecutor executor;
    
    @BeforeEach
    void setup() {
        connectionSession.setGrantee(mock(Grantee.class));
    }
    
    @Test
    void assertExecuteBind() throws SQLException {
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(connectionSession.getCurrentDatabaseName()).thenReturn(DATABASE_NAME);
        ConnectionContext connectionContext = mockConnectionContext();
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        String statementId = "S_1";
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, new PostgreSQLServerPreparedStatement(
                "", new CommonSQLStatementContext(new EmptyStatement(databaseType)), new HintValueContext(), Collections.emptyList(), Collections.emptyList()));
        when(bindPacket.getStatementId()).thenReturn(statementId);
        when(bindPacket.getPortal()).thenReturn("C_1");
        when(bindPacket.readParameters(anyList())).thenReturn(Collections.emptyList());
        when(bindPacket.readResultFormats()).thenReturn(Collections.emptyList());
        ContextManager contextManager = mock(ContextManager.class, Answers.RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getProtocolType()).thenReturn(databaseType);
        when(contextManager.getDatabase(DATABASE_NAME)).thenReturn(database);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(PostgreSQLBindCompletePacket.getInstance()));
        verify(portalContext).add(any(Portal.class));
    }
    
    @Test
    void assertExecuteBindWithResolvedParameterTypes() throws SQLException {
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(connectionSession.getCurrentDatabaseName()).thenReturn(DATABASE_NAME);
        when(connectionSession.getUsedDatabaseName()).thenReturn(DATABASE_NAME);
        ConnectionContext connectionContext = mockConnectionContext();
        when(connectionSession.getConnectionContext()).thenReturn(connectionContext);
        SQLStatement sqlStatement = parserEngine.parse(SQL, false);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class, Answers.RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        String statementId = "S_1";
        PostgreSQLServerPreparedStatement serverPreparedStatement = new PostgreSQLServerPreparedStatement(SQL, sqlStatementContext, new HintValueContext(),
                new ArrayList<>(Collections.singletonList(PostgreSQLBinaryColumnType.UNSPECIFIED)), Collections.singletonList(0));
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, serverPreparedStatement);
        when(bindPacket.getStatementId()).thenReturn(statementId);
        when(bindPacket.getPortal()).thenReturn("C_1");
        when(bindPacket.readParameters(Collections.singletonList(PostgreSQLBinaryColumnType.UNSPECIFIED))).thenReturn(Collections.singletonList(1));
        when(bindPacket.readResultFormats()).thenReturn(Collections.emptyList());
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        prepareJDBCBackendConnectionWithParamTypes(databaseConnectionManager);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(PostgreSQLBindCompletePacket.getInstance()));
        assertThat(serverPreparedStatement.getParameterTypes(), is(Collections.singletonList(PostgreSQLBinaryColumnType.INT4)));
        verify(bindPacket).readParameters(serverPreparedStatement.getParameterTypes());
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of(DATABASE_NAME));
        return result;
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build())));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(result.getMetaDataContexts().getMetaData().containsDatabase(DATABASE_NAME)).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().containsDatabase(new IdentifierValue(DATABASE_NAME))).thenReturn(true);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getProtocolType()).thenReturn(databaseType);
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getStorageType()).thenReturn(databaseType);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", storageUnit));
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(database.getDefaultSchemaName()).thenReturn("public");
        when(database.getAllSchemas()).thenReturn(Collections.singleton(schema));
        when(database.containsSchema(new IdentifierValue("public"))).thenReturn(true);
        when(database.getSchema("public")).thenReturn(schema);
        when(database.getSchema(new IdentifierValue("public"))).thenReturn(schema);
        ShardingSphereTable table = new ShardingSphereTable("t_order", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("k", Types.INTEGER, true, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList());
        when(schema.containsTable(new IdentifierValue("t_order"))).thenReturn(true);
        when(schema.getTable(new IdentifierValue("t_order"))).thenReturn(table);
        when(result.getMetaDataContexts().getMetaData().getDatabase(DATABASE_NAME)).thenReturn(database);
        when(result.getMetaDataContexts().getMetaData().getDatabase(new IdentifierValue(DATABASE_NAME))).thenReturn(database);
        when(result.getDatabase(DATABASE_NAME)).thenReturn(database);
        return result;
    }
    
    private void prepareJDBCBackendConnectionWithParamTypes(final ProxyDatabaseConnectionManager databaseConnectionManager) throws SQLException {
        ParameterMetaData parameterMetaData = mock(ParameterMetaData.class);
        when(parameterMetaData.getParameterType(1)).thenReturn(Types.INTEGER);
        when(parameterMetaData.getParameterTypeName(1)).thenReturn("int4");
        PreparedStatement preparedStatement = mock(PreparedStatement.class, RETURNS_DEEP_STUBS);
        when(preparedStatement.getParameterMetaData()).thenReturn(parameterMetaData);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(databaseConnectionManager.getConnections(any(), nullable(String.class), anyInt(), anyInt(), any())).thenReturn(Collections.singletonList(connection));
    }
}
