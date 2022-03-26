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

import org.apache.shardingsphere.db.protocol.opengauss.packet.command.query.extended.bind.OpenGaussComBatchBindPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLPreparedStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLBindCompletePacket;
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
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OpenGaussComBatchBindExecutorTest {
    
    private static final ShardingSphereSQLParserEngine SQL_PARSER_ENGINE = new ShardingSphereSQLParserEngine("openGauss", 
            new ParserConfiguration(new CacheOption(2000, 65535L, 4), new CacheOption(128, 1024L, 4), false));
    
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
        int connectionId = 1;
        String statement = "S_1";
        OpenGaussComBatchBindPacket packet = mock(OpenGaussComBatchBindPacket.class);
        when(packet.getStatementId()).thenReturn("S_1");
        when(packet.readParameterSets(anyList())).thenReturn(Collections.singletonList(Collections.singletonList(0)));
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getConnectionId()).thenReturn(1);
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
        PostgreSQLPreparedStatementRegistry.getInstance().register(connectionId);
        SQLStatement sqlStatement = SQL_PARSER_ENGINE.parse("insert into bmsql (id) values (?)", false);
        PostgreSQLPreparedStatementRegistry.getInstance().register(connectionId, statement, "", sqlStatement, Collections.emptyList());
        OpenGaussComBatchBindExecutor executor = new OpenGaussComBatchBindExecutor(packet, connectionSession);
        Iterator<DatabasePacket<?>> actualPacketsIterator = executor.execute().iterator();
        assertThat(actualPacketsIterator.next(), is(PostgreSQLBindCompletePacket.getInstance()));
        assertTrue(actualPacketsIterator.next() instanceof PostgreSQLCommandCompletePacket);
        assertFalse(actualPacketsIterator.hasNext());
    }
    
    @After
    public void tearDown() {
        ProxyContext.getInstance().init(contextManagerBefore);
    }
}
