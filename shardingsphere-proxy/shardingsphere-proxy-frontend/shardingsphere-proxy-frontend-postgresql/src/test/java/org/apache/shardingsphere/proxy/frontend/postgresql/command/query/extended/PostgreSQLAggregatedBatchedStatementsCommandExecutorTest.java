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

import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLNoDataPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLPreparedStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLBindCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.parser.ParserConfiguration;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PostgreSQLAggregatedBatchedStatementsCommandExecutorTest {
    
    private static final ShardingSphereSQLParserEngine SQL_PARSER_ENGINE = new ShardingSphereSQLParserEngine("PostgreSQL", 
            new ParserConfiguration(new CacheOption(2000, 65535L, 4), new CacheOption(128, 1024L, 4), false));
    
    private static final int CONNECTION_ID = 1;
    
    private static final String STATEMENT_ID = "S_1";
    
    private static final String SQL = "insert into t_order (id) values (?)";
    
    private static final int BATCH_SIZE = 10;
    
    private ContextManager contextManagerBefore;
    
    @Before
    public void setup() {
        contextManagerBefore = ProxyContext.getInstance().getContextManager();
        ProxyContext.getInstance().init(mock(ContextManager.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    public void assertExecute() throws SQLException {
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE)).thenReturn(0);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY)).thenReturn(1);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)).thenReturn(false);
        PostgreSQLPreparedStatementRegistry.getInstance().register(CONNECTION_ID);
        PostgreSQLPreparedStatementRegistry.getInstance().register(CONNECTION_ID, STATEMENT_ID, SQL, SQL_PARSER_ENGINE.parse(SQL, false),
                Collections.singletonList(PostgreSQLColumnType.POSTGRESQL_TYPE_INT4));
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        JDBCBackendConnection backendConnection = mock(JDBCBackendConnection.class);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("");
        when(backendConnection.getConnections(nullable(String.class), anyInt(), any(ConnectionMode.class))).thenReturn(Collections.singletonList(connection));
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.getConnection()).thenReturn(connection);
        JDBCBackendStatement backendStatement = mock(JDBCBackendStatement.class);
        when(backendStatement.createStorageResource(any(ExecutionUnit.class), any(Connection.class), any(ConnectionMode.class), any(StatementOption.class))).thenReturn(preparedStatement);
        when(connectionSession.getStatementManager()).thenReturn(backendStatement);
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        PostgreSQLAggregatedBatchedStatementsCommandExecutor batchedStatementsCommandExecutor = new PostgreSQLAggregatedBatchedStatementsCommandExecutor(connectionSession, preparePackets());
        List<DatabasePacket<?>> actualPackets = new ArrayList<>(batchedStatementsCommandExecutor.execute());
        assertThat(actualPackets.size(), is(BATCH_SIZE * 3));
        for (int i = 0; i < BATCH_SIZE; i++) {
            assertThat(actualPackets.get(i * 3), is(PostgreSQLBindCompletePacket.getInstance()));
            assertThat(actualPackets.get(i * 3 + 1), is(PostgreSQLNoDataPacket.getInstance()));
            assertTrue(actualPackets.get(i * 3 + 2) instanceof PostgreSQLCommandCompletePacket);
        }
    }
    
    private List<PostgreSQLCommandPacket> preparePackets() {
        List<PostgreSQLCommandPacket> result = new ArrayList<>();
        for (int i = 0; i < BATCH_SIZE; i++) {
            PostgreSQLComBindPacket bindPacket = mock(PostgreSQLComBindPacket.class);
            when(bindPacket.getStatementId()).thenReturn(STATEMENT_ID);
            when(bindPacket.readParameters(Collections.singletonList(PostgreSQLColumnType.POSTGRESQL_TYPE_INT4))).thenReturn(Collections.singletonList(i));
            PostgreSQLComDescribePacket describePacket = mock(PostgreSQLComDescribePacket.class);
            PostgreSQLComExecutePacket executePacket = mock(PostgreSQLComExecutePacket.class);
            result.add(bindPacket);
            result.add(describePacket);
            result.add(executePacket);
        }
        return result;
    }
    
    @After
    public void tearDown() {
        ProxyContext.getInstance().init(contextManagerBefore);
    }
}
