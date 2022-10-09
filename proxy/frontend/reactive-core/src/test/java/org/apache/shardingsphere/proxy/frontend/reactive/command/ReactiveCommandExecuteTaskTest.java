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

package org.apache.shardingsphere.proxy.frontend.reactive.command;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.core.Future;
import org.apache.shardingsphere.db.protocol.packet.CommandPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacketType;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.exception.BackendConnectionException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.reactive.command.executor.ReactiveCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.reactive.spi.ReactiveDatabaseProtocolFrontendEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ReactiveCommandExecuteTaskTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReactiveDatabaseProtocolFrontendEngine frontendEngine;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private ByteBuf message;
    
    private ReactiveCommandExecuteTask reactiveCommandExecuteTask;
    
    @Mock
    private ReactiveCommandExecutor reactiveCommandExecutor;
    
    @Before
    public void setup() throws BackendConnectionException {
        reactiveCommandExecuteTask = new ReactiveCommandExecuteTask(frontendEngine, connectionSession, channelHandlerContext, message);
        when(connectionSession.getBackendConnection().prepareForTaskExecution()).thenReturn(Future.succeededFuture());
        when(connectionSession.getBackendConnection().closeExecutionResources()).thenReturn(Future.succeededFuture());
        when(frontendEngine.getReactiveCommandExecuteEngine().getReactiveCommandExecutor(nullable(CommandPacketType.class), nullable(CommandPacket.class), eq(connectionSession)))
                .thenReturn(reactiveCommandExecutor);
    }
    
    @Test
    public void assertExecuteAndWriteInEventLoop() {
        when(channelHandlerContext.executor().inEventLoop()).thenReturn(true);
        DatabasePacket<?> packet0 = mock(DatabasePacket.class);
        DatabasePacket<?> packet1 = mock(DatabasePacket.class);
        when(reactiveCommandExecutor.executeFuture()).thenReturn(Future.succeededFuture(Arrays.asList(packet0, packet1)));
        when(reactiveCommandExecutor.closeFuture()).thenReturn(Future.succeededFuture());
        reactiveCommandExecuteTask.run();
        verify(channelHandlerContext).write(packet0);
        verify(channelHandlerContext).write(packet1);
        verify(channelHandlerContext).flush();
    }
    
    @Test
    public void assertExecuteAndWriteNotInEventLoop() {
        when(channelHandlerContext.executor().inEventLoop()).thenReturn(false, true);
        DatabasePacket<?> packet0 = mock(DatabasePacket.class);
        DatabasePacket<?> packet1 = mock(DatabasePacket.class);
        when(reactiveCommandExecutor.executeFuture()).thenReturn(Future.succeededFuture(Arrays.asList(packet0, packet1)));
        when(reactiveCommandExecutor.closeFuture()).thenReturn(Future.succeededFuture());
        reactiveCommandExecuteTask.run();
        verify(channelHandlerContext).write(packet0);
        verify(channelHandlerContext).write(packet1);
        verify(channelHandlerContext.executor()).execute(any(Runnable.class));
    }
    
    @Test
    public void assertExecuteAndNoResponse() {
        when(reactiveCommandExecutor.executeFuture()).thenReturn(Future.succeededFuture(Collections.emptyList()));
        when(reactiveCommandExecutor.closeFuture()).thenReturn(Future.succeededFuture());
        reactiveCommandExecuteTask.run();
        verify(channelHandlerContext, never()).write(any());
    }
    
    @Test
    public void assertExecuteAndExceptionOccur() {
        RuntimeException ex = new RuntimeException("");
        when(connectionSession.getBackendConnection().prepareForTaskExecution()).thenThrow(ex);
        DatabasePacket errorPacket = mock(DatabasePacket.class);
        when(frontendEngine.getCommandExecuteEngine().getErrorPacket(ex)).thenReturn(errorPacket);
        DatabasePacket otherPacket = mock(DatabasePacket.class);
        when(frontendEngine.getCommandExecuteEngine().getOtherPacket(connectionSession)).thenReturn(Optional.of(otherPacket));
        reactiveCommandExecuteTask.run();
        verify(channelHandlerContext).write(errorPacket);
        verify(channelHandlerContext).write(otherPacket);
        verify(channelHandlerContext).flush();
    }
    
    @Test
    public void assertExecuteAndThrowableOccur() {
        Throwable throwable = mock(Throwable.class);
        when(reactiveCommandExecutor.executeFuture()).thenReturn(Future.failedFuture(throwable));
        DatabasePacket errorPacket = mock(DatabasePacket.class);
        when(frontendEngine.getCommandExecuteEngine().getErrorPacket(any(Exception.class))).thenReturn(errorPacket);
        DatabasePacket otherPacket = mock(DatabasePacket.class);
        when(frontendEngine.getCommandExecuteEngine().getOtherPacket(connectionSession)).thenReturn(Optional.of(otherPacket));
        reactiveCommandExecuteTask.run();
        verify(channelHandlerContext).write(errorPacket);
        verify(channelHandlerContext).write(otherPacket);
        verify(channelHandlerContext).flush();
    }
}
