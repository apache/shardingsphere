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

package org.apache.shardingsphere.proxy.frontend.opengauss.command.query.extended.bind;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.opengauss.packet.command.bind.OpenGaussComBatchBindPacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLBindCompletePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class OpenGaussComBatchBindExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private final ShardingSphereSQLParserEngine parserEngine = new ShardingSphereSQLParserEngine(databaseType, new CacheOption(2000, 65535L), new CacheOption(128, 1024L));
    
    @Test
    void assertExecute() throws SQLException {
        String statement = "S_1";
        String sql = "INSERT INTO bmsql (id) VALUES (?)";
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(sql);
        ConnectionSession connectionSession = mockConnectionSession();
        PostgreSQLServerPreparedStatement serverPreparedStatement = new PostgreSQLServerPreparedStatement(
                sql, sqlStatementContext, new HintValueContext(), Collections.emptyList(), Collections.emptyList());
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statement, serverPreparedStatement);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Iterator<DatabasePacket> actualPacketsIterator = new OpenGaussComBatchBindExecutor(mockComBatchBindPacket(), connectionSession).execute().iterator();
        assertThat(actualPacketsIterator.next(), is(PostgreSQLBindCompletePacket.getInstance()));
        assertThat(actualPacketsIterator.next(), isA(PostgreSQLCommandCompletePacket.class));
        assertFalse(actualPacketsIterator.hasNext());
    }
    
    @Test
    void assertExecuteWithUnspecifiedParameterTypes() throws SQLException {
        String statement = "S_1";
        String sql = "INSERT INTO bmsql (id) VALUES (?)";
        SQLStatementContext sqlStatementContext = mockSQLStatementContext(sql);
        ConnectionSession connectionSession = mockConnectionSessionWithParameterMetaData();
        List<PostgreSQLBinaryColumnType> parameterTypes = new ArrayList<>(Collections.singletonList(PostgreSQLBinaryColumnType.UNSPECIFIED));
        PostgreSQLServerPreparedStatement serverPreparedStatement = new PostgreSQLServerPreparedStatement(
                sql, sqlStatementContext, new HintValueContext(), parameterTypes, Collections.singletonList(0));
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statement, serverPreparedStatement);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        OpenGaussComBatchBindPacket packet = mockComBatchBindPacket();
        new OpenGaussComBatchBindExecutor(packet, connectionSession).execute();
        verify(packet).readParameterSets(Collections.singletonList(PostgreSQLBinaryColumnType.INT4));
    }
    
    private ConnectionSession mockConnectionSession() throws SQLException {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getConnectionContext()).thenReturn(new ConnectionContext(Collections::emptySet));
        when(result.getCurrentDatabaseName()).thenReturn("foo_db");
        when(result.getUsedDatabaseName()).thenReturn("foo_db");
        ConnectionContext connectionContext = mockConnectionContext();
        when(result.getConnectionContext()).thenReturn(connectionContext);
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(databaseConnectionManager.getConnections(any(), nullable(String.class), anyInt(), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        JDBCBackendStatement backendStatement = mock(JDBCBackendStatement.class);
        when(backendStatement.createStorageResource(any(ExecutionUnit.class), any(Connection.class), anyInt(), any(ConnectionMode.class), any(StatementOption.class), nullable(DatabaseType.class)))
                .thenReturn(preparedStatement);
        when(result.getStatementManager()).thenReturn(backendStatement);
        when(result.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        when(result.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        return result;
    }
    
    private ConnectionSession mockConnectionSessionWithParameterMetaData() throws SQLException {
        ConnectionSession result = mockConnectionSession();
        Connection connection = result.getDatabaseConnectionManager().getConnections("foo_db", null, 0, 1, ConnectionMode.CONNECTION_STRICTLY).iterator().next();
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ParameterMetaData parameterMetaData = mock(ParameterMetaData.class);
        when(parameterMetaData.getParameterType(1)).thenReturn(Types.INTEGER);
        when(parameterMetaData.getParameterTypeName(1)).thenReturn("int4");
        when(preparedStatement.getParameterMetaData()).thenReturn(parameterMetaData);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        return result;
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getTransactionContext()).thenReturn(mock(TransactionConnectionContext.class));
        return result;
    }
    
    private OpenGaussComBatchBindPacket mockComBatchBindPacket() {
        OpenGaussComBatchBindPacket result = mock(OpenGaussComBatchBindPacket.class);
        when(result.getStatementId()).thenReturn("S_1");
        when(result.readParameterSets(anyList())).thenReturn(Collections.singletonList(Collections.singletonList(0)));
        return result;
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        lenient().when(result.getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(0);
        when(result.getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY)).thenReturn(1);
        when(result.getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
        when(result.getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_UNION_SIZE_PER_DATASOURCE)).thenReturn(Integer.MAX_VALUE);
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(
                new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build()))));
        ShardingSphereDatabase database = mockDatabase();
        when(result.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(result.getMetaDataContexts().getMetaData().getDatabase(new IdentifierValue("foo_db"))).thenReturn(database);
        lenient().when(result.getMetaDataContexts().getMetaData().containsDatabase("foo_db")).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().containsDatabase(new IdentifierValue("foo_db"))).thenReturn(true);
        return result;
    }
    
    private SQLStatementContext mockSQLStatementContext(final String sql) {
        SQLStatement sqlStatement = parserEngine.parse(sql, false);
        SQLStatementContext result = mock(InsertStatementContext.class);
        when(result.getSqlStatement()).thenReturn(sqlStatement);
        return result;
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(result.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.singleton("foo_ds"));
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getStorageType()).thenReturn(databaseType);
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        when(result.getDefaultSchemaName()).thenReturn("public");
        when(result.getAllSchemas()).thenReturn(Collections.singleton(schema));
        when(result.containsSchema(new IdentifierValue("public"))).thenReturn(true);
        when(result.getSchema("public")).thenReturn(schema);
        when(result.getSchema(new IdentifierValue("public"))).thenReturn(schema);
        when(schema.containsTable(new IdentifierValue("bmsql"))).thenReturn(true);
        when(schema.getTable("bmsql").getAllColumns()).thenReturn(Collections.singleton(new ShardingSphereColumn("id", Types.VARCHAR, false, false, false, true, false, false)));
        when(schema.getTable(new IdentifierValue("bmsql")).getAllColumns()).thenReturn(Collections.singleton(new ShardingSphereColumn("id", Types.VARCHAR, false, false, false, true, false, false)));
        return result;
    }
}
