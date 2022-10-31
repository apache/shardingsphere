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
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLReadyForQueryPacket;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.ResourceLock;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.postgresql.ProxyContextRestorer;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.simple.PostgreSQLComQueryExecutor;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLCommandExecuteEngineTest extends ProxyContextRestorer {
    
    @Mock
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private Channel channel;
    
    @Mock
    private PostgreSQLComQueryExecutor queryCommandExecutor;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Before
    public void setUp() {
        ProxyContext.init(new ContextManager(new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData()), mock(InstanceContext.class)));
        when(channelHandlerContext.channel()).thenReturn(channel);
        when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus(TransactionType.LOCAL));
    }
    
    @Test
    public void assertSimpleQueryWithUpdateResponseWriteQueryData() throws SQLException {
        PostgreSQLComQueryExecutor comQueryExecutor = mock(PostgreSQLComQueryExecutor.class);
        when(comQueryExecutor.getResponseType()).thenReturn(ResponseType.UPDATE);
        PostgreSQLCommandExecuteEngine commandExecuteEngine = new PostgreSQLCommandExecuteEngine();
        JDBCBackendConnection backendConnection = mock(JDBCBackendConnection.class);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
        commandExecuteEngine.writeQueryData(channelHandlerContext, backendConnection, comQueryExecutor, 0);
        verify(channelHandlerContext).write(any(PostgreSQLReadyForQueryPacket.class));
    }
    
    @Test
    public void assertWriteQueryDataWithUpdate() throws SQLException {
        PostgreSQLCommandExecuteEngine commandExecuteEngine = new PostgreSQLCommandExecuteEngine();
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.UPDATE);
        JDBCBackendConnection backendConnection = mock(JDBCBackendConnection.class, RETURNS_DEEP_STUBS);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
        commandExecuteEngine.writeQueryData(channelHandlerContext, backendConnection, queryCommandExecutor, 0);
        verify(channelHandlerContext).write(PostgreSQLReadyForQueryPacket.NOT_IN_TRANSACTION);
    }
    
    @Test
    public void assertWriteQueryDataWithInactiveChannel() throws SQLException {
        PostgreSQLCommandExecuteEngine commandExecuteEngine = new PostgreSQLCommandExecuteEngine();
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.QUERY);
        when(channel.isActive()).thenReturn(false);
        commandExecuteEngine.writeQueryData(channelHandlerContext, mock(JDBCBackendConnection.class), queryCommandExecutor, 0);
        verify(channelHandlerContext).write(isA(PostgreSQLCommandCompletePacket.class));
    }
    
    @Test
    public void assertWriteQueryDataWithHasNextResult() throws SQLException {
        PostgreSQLComQueryExecutor queryCommandExecutor = mock(PostgreSQLComQueryExecutor.class);
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.QUERY);
        when(channel.isActive()).thenReturn(true);
        when(queryCommandExecutor.next()).thenReturn(true, false);
        when(channel.isWritable()).thenReturn(false, true);
        ResourceLock resourceLock = mock(ResourceLock.class);
        JDBCBackendConnection backendConnection = mock(JDBCBackendConnection.class);
        when(backendConnection.getResourceLock()).thenReturn(resourceLock);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
        PostgreSQLPacket packet = mock(PostgreSQLPacket.class);
        when(queryCommandExecutor.getQueryRowPacket()).thenReturn(packet);
        PostgreSQLCommandExecuteEngine commandExecuteEngine = new PostgreSQLCommandExecuteEngine();
        commandExecuteEngine.writeQueryData(channelHandlerContext, backendConnection, queryCommandExecutor, 0);
        verify(resourceLock).doAwait();
        verify(channelHandlerContext).write(packet);
        verify(channelHandlerContext).write(isA(PostgreSQLCommandCompletePacket.class));
        verify(channelHandlerContext).flush();
        verify(channelHandlerContext).write(isA(PostgreSQLReadyForQueryPacket.class));
    }
}
