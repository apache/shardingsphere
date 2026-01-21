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

package org.apache.shardingsphere.proxy.frontend.postgresql.command;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.database.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLReadyForQueryPacket;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.connection.ConnectionResourceLock;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.simple.PostgreSQLComQueryExecutor;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostgreSQLCommandExecuteEngineTest {
    
    @Mock
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private Channel channel;
    
    @Mock
    private PostgreSQLComQueryExecutor queryCommandExecutor;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        when(channelHandlerContext.channel()).thenReturn(channel);
        when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus());
    }
    
    @Test
    void assertSimpleQueryWithUpdateResponseWriteQueryData() throws SQLException {
        PostgreSQLComQueryExecutor comQueryExecutor = mock(PostgreSQLComQueryExecutor.class);
        when(comQueryExecutor.getResponseType()).thenReturn(ResponseType.UPDATE);
        PostgreSQLCommandExecuteEngine commandExecuteEngine = new PostgreSQLCommandExecuteEngine();
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        commandExecuteEngine.writeQueryData(channelHandlerContext, databaseConnectionManager, comQueryExecutor, 0);
        verify(channelHandlerContext).write(any(PostgreSQLReadyForQueryPacket.class));
    }
    
    @Test
    void assertWriteQueryDataWithUpdate() throws SQLException {
        PostgreSQLCommandExecuteEngine commandExecuteEngine = new PostgreSQLCommandExecuteEngine();
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.UPDATE);
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class, RETURNS_DEEP_STUBS);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        commandExecuteEngine.writeQueryData(channelHandlerContext, databaseConnectionManager, queryCommandExecutor, 0);
        verify(channelHandlerContext).write(PostgreSQLReadyForQueryPacket.NOT_IN_TRANSACTION);
    }
    
    @Test
    void assertWriteQueryDataWithInactiveChannel() throws SQLException {
        PostgreSQLCommandExecuteEngine commandExecuteEngine = new PostgreSQLCommandExecuteEngine();
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.QUERY);
        when(channel.isActive()).thenReturn(false);
        commandExecuteEngine.writeQueryData(channelHandlerContext, mock(ProxyDatabaseConnectionManager.class), queryCommandExecutor, 0);
        verify(channelHandlerContext).write(isA(PostgreSQLCommandCompletePacket.class));
    }
    
    @Test
    void assertWriteQueryDataWithHasNextResult() throws SQLException {
        PostgreSQLComQueryExecutor queryCommandExecutor = mock(PostgreSQLComQueryExecutor.class);
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.QUERY);
        when(channel.isActive()).thenReturn(true);
        when(queryCommandExecutor.next()).thenReturn(true, false);
        when(channel.isWritable()).thenReturn(false, true);
        ConnectionResourceLock connectionResourceLock = mock(ConnectionResourceLock.class);
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(databaseConnectionManager.getConnectionResourceLock()).thenReturn(connectionResourceLock);
        when(databaseConnectionManager.getConnectionSession()).thenReturn(connectionSession);
        PostgreSQLPacket packet = mock(PostgreSQLPacket.class);
        when(queryCommandExecutor.getQueryRowPacket()).thenReturn(packet);
        PostgreSQLCommandExecuteEngine commandExecuteEngine = new PostgreSQLCommandExecuteEngine();
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        when(computeNodeInstanceContext.getModeConfiguration()).thenReturn(mock(ModeConfiguration.class));
        ContextManager contextManager = new ContextManager(
                new MetaDataContexts(new ShardingSphereMetaData(Collections.emptyList(), new ResourceMetaData(Collections.emptyMap()),
                        new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties())),
                        new ShardingSphereStatistics()), computeNodeInstanceContext, mock(), mock());
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        commandExecuteEngine.writeQueryData(channelHandlerContext, databaseConnectionManager, queryCommandExecutor, 0);
        verify(connectionResourceLock).doAwait(channelHandlerContext);
        verify(channelHandlerContext).write(packet);
        verify(channelHandlerContext).write(isA(PostgreSQLCommandCompletePacket.class));
        verify(channelHandlerContext).write(isA(PostgreSQLReadyForQueryPacket.class));
    }
}
