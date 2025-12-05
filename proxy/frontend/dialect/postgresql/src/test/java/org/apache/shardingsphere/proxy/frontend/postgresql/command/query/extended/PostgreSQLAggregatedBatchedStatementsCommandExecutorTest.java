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
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLNoDataPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLBindCompletePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLComExecutePacket;
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
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.rule.builder.DefaultSQLTranslatorRuleConfigurationBuilder;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostgreSQLAggregatedBatchedStatementsCommandExecutorTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final String STATEMENT_ID = "S_1";
    
    private static final String SQL = "INSERT INTO t_order (id) VALUES (?)";
    
    private static final int BATCH_SIZE = 10;
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final ShardingSphereSQLParserEngine parserEngine = new ShardingSphereSQLParserEngine(databaseType, new CacheOption(2000, 65535L), new CacheOption(128, 1024L));
    
    @Test
    void assertExecute() throws SQLException {
        ConnectionSession connectionSession = mockConnectionSession();
        PostgreSQLAggregatedBatchedStatementsCommandExecutor executor = new PostgreSQLAggregatedBatchedStatementsCommandExecutor(connectionSession, createPackets());
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        List<DatabasePacket> actualPackets = new ArrayList<>(executor.execute());
        assertThat(actualPackets.size(), is(BATCH_SIZE * 3));
        for (int i = 0; i < BATCH_SIZE; i++) {
            assertThat(actualPackets.get(i * 3), is(PostgreSQLBindCompletePacket.getInstance()));
            assertThat(actualPackets.get(i * 3 + 1), is(PostgreSQLNoDataPacket.getInstance()));
            assertThat(actualPackets.get(i * 3 + 2), isA(PostgreSQLCommandCompletePacket.class));
        }
    }
    
    private ConnectionSession mockConnectionSession() throws SQLException {
        ConnectionSession result = mock(ConnectionSession.class);
        SQLStatementContext sqlStatementContext = mock(InsertStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(parserEngine.parse(SQL, false));
        when(result.getCurrentDatabaseName()).thenReturn("foo_db");
        when(result.getUsedDatabaseName()).thenReturn("foo_db");
        ConnectionContext connectionContext = new ConnectionContext(Collections::emptySet);
        connectionContext.setCurrentDatabaseName("foo_db");
        when(result.getConnectionContext()).thenReturn(connectionContext);
        when(result.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        result.getServerPreparedStatementRegistry().addPreparedStatement(STATEMENT_ID,
                new PostgreSQLServerPreparedStatement(SQL, sqlStatementContext, new HintValueContext(), Collections.singletonList(PostgreSQLColumnType.INT4), Collections.singletonList("int"),
                        Collections.singletonList(0)));
        when(result.getConnectionId()).thenReturn(CONNECTION_ID);
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:postgresql://127.0.0.1/db");
        when(databaseConnectionManager.getConnections(any(), nullable(String.class), anyInt(), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.getConnection()).thenReturn(connection);
        JDBCBackendStatement backendStatement = mock(JDBCBackendStatement.class);
        when(backendStatement.createStorageResource(any(ExecutionUnit.class), any(Connection.class), anyInt(), any(ConnectionMode.class), any(StatementOption.class), nullable(DatabaseType.class)))
                .thenReturn(preparedStatement);
        when(result.getStatementManager()).thenReturn(backendStatement);
        when(result.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        return result;
    }
    
    private List<PostgreSQLCommandPacket> createPackets() {
        List<PostgreSQLCommandPacket> result = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < BATCH_SIZE; i++) {
            PostgreSQLComBindPacket bindPacket = mock(PostgreSQLComBindPacket.class);
            when(bindPacket.getStatementId()).thenReturn(STATEMENT_ID);
            when(bindPacket.readParameters(Collections.singletonList(PostgreSQLColumnType.INT4))).thenReturn(Collections.singletonList(i));
            PostgreSQLComDescribePacket describePacket = mock(PostgreSQLComDescribePacket.class);
            PostgreSQLComExecutePacket executePacket = mock(PostgreSQLComExecutePacket.class);
            result.add(bindPacket);
            result.add(describePacket);
            result.add(executePacket);
        }
        return result;
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(0);
        when(result.getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY)).thenReturn(1);
        when(result.getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(new SQLTranslatorRule(new DefaultSQLTranslatorRuleConfigurationBuilder().build())));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.singletonList("foo_ds"));
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getStorageType()).thenReturn(databaseType);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        when(database.containsSchema("public")).thenReturn(true);
        when(database.getSchema("public").containsTable("t_order")).thenReturn(true);
        when(result.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(result.getMetaDataContexts().getMetaData().containsDatabase("foo_db")).thenReturn(true);
        when(database.getSchema("public").getTable("t_order").getAllColumns())
                .thenReturn(Collections.singleton(new ShardingSphereColumn("id", Types.VARCHAR, false, false, "varchar", false, true, false, false)));
        return result;
    }
}
