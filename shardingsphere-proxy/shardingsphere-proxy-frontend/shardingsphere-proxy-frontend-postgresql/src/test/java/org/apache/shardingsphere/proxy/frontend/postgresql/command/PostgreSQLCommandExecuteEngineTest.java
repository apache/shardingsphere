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
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.ResourceLock;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.sync.PostgreSQLComSyncExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.text.PostgreSQLComQueryExecutor;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLCommandExecuteEngineTest {
    
    @Mock
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private Channel channel;
    
    @Mock
    private QueryCommandExecutor queryCommandExecutor;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Before
    public void setUp() {
        when(channelHandlerContext.channel()).thenReturn(channel);
        when(backendConnection.getTransactionStatus()).thenReturn(new TransactionStatus(TransactionType.LOCAL));
    }
    
    @Test
    public void assertWriteQueryDataWithUpdate() throws SQLException {
        PostgreSQLCommandExecuteEngine commandExecuteEngine = new PostgreSQLCommandExecuteEngine();
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.UPDATE);
        commandExecuteEngine.writeQueryData(channelHandlerContext, backendConnection, queryCommandExecutor, 0);
        verify(channelHandlerContext, times(1)).write(isA(PostgreSQLReadyForQueryPacket.class));
    }
    
    @Test
    public void assertWriteQueryDataWithComSync() throws SQLException {
        PostgreSQLCommandExecuteEngine commandExecuteEngine = new PostgreSQLCommandExecuteEngine();
        commandExecuteEngine.writeQueryData(channelHandlerContext, backendConnection, new PostgreSQLComSyncExecutor(backendConnection), 0);
        verify(channelHandlerContext, never()).write(any(Object.class));
    }
    
    @Test
    public void assertWriteQueryDataWithInactiveChannel() throws SQLException {
        PostgreSQLCommandExecuteEngine commandExecuteEngine = new PostgreSQLCommandExecuteEngine();
        when(queryCommandExecutor.getResponseType()).thenReturn(ResponseType.QUERY);
        when(channel.isActive()).thenReturn(false);
        commandExecuteEngine.writeQueryData(channelHandlerContext, backendConnection, queryCommandExecutor, 0);
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
        when(backendConnection.getResourceLock()).thenReturn(resourceLock);
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
