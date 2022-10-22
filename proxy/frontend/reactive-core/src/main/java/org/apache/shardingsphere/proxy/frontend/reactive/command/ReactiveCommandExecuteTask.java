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
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.CommonConstants;
import org.apache.shardingsphere.db.protocol.packet.CommandPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacketType;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.proxy.backend.exception.BackendConnectionException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.exception.ExpectedExceptions;
import org.apache.shardingsphere.proxy.frontend.reactive.command.executor.ReactiveCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.reactive.spi.ReactiveDatabaseProtocolFrontendEngine;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * Reactive command executor task.
 */
@SuppressWarnings("unchecked")
@RequiredArgsConstructor
@Slf4j
public final class ReactiveCommandExecuteTask implements Runnable {
    
    private final ReactiveDatabaseProtocolFrontendEngine reactiveDatabaseProtocolFrontendEngine;
    
    private final ConnectionSession connectionSession;
    
    private final ChannelHandlerContext context;
    
    private final Object message;
    
    private volatile boolean isNeedFlush;
    
    private volatile boolean writeInEventLoop;
    
    @Override
    public void run() {
        PacketPayload payload = reactiveDatabaseProtocolFrontendEngine.getCodecEngine().createPacketPayload((ByteBuf) message, context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get());
        try {
            ((Future<Void>) connectionSession.getBackendConnection().prepareForTaskExecution())
                    .compose(unused -> executeCommand(payload).eventually(unused0 -> closeResources(payload)))
                    .onFailure(this::processThrowable);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            processException(ex);
            closeResources(payload);
        }
    }
    
    @SneakyThrows(SQLException.class)
    private Future<Void> executeCommand(final PacketPayload payload) {
        CommandExecuteEngine commandExecuteEngine = reactiveDatabaseProtocolFrontendEngine.getCommandExecuteEngine();
        ReactiveCommandExecuteEngine reactiveCommandExecuteEngine = reactiveDatabaseProtocolFrontendEngine.getReactiveCommandExecuteEngine();
        CommandPacketType type = commandExecuteEngine.getCommandPacketType(payload);
        CommandPacket commandPacket = commandExecuteEngine.getCommandPacket(payload, type, connectionSession);
        ReactiveCommandExecutor commandExecutor = reactiveCommandExecuteEngine.getReactiveCommandExecutor(type, commandPacket, connectionSession);
        return commandExecutor.executeFuture()
                .compose(this::handleResponsePackets)
                .eventually(unused -> commandExecutor.closeFuture());
    }
    
    private Future<Void> handleResponsePackets(final Collection<DatabasePacket<?>> responsePackets) {
        responsePackets.forEach(context::write);
        writeInEventLoop = (isNeedFlush = !responsePackets.isEmpty()) && context.executor().inEventLoop();
        return Future.succeededFuture();
    }
    
    @SneakyThrows(BackendConnectionException.class)
    private Future<Void> closeResources(final PacketPayload payload) {
        try {
            payload.close();
            // CHECKSTYLE:OFF
        } catch (final Exception ignored) {
            // CHECKSTYLE:ON
        }
        connectionSession.clearQueryContext();
        return ((Future<Void>) connectionSession.getBackendConnection().closeExecutionResources()).onComplete(this::doFlushIfNecessary);
    }
    
    private void doFlushIfNecessary(final AsyncResult<Void> unused) {
        if (!isNeedFlush) {
            return;
        }
        if (!writeInEventLoop && context.executor().inEventLoop()) {
            context.executor().execute(context::flush);
        } else {
            context.flush();
        }
    }
    
    private void processThrowable(final Throwable throwable) {
        Exception ex = throwable instanceof Exception ? (Exception) throwable : new Exception(throwable);
        processException(ex);
    }
    
    private void processException(final Exception cause) {
        if (!ExpectedExceptions.isExpected(cause.getClass())) {
            log.error("Exception occur: ", cause);
        }
        context.write(reactiveDatabaseProtocolFrontendEngine.getCommandExecuteEngine().getErrorPacket(cause));
        Optional<DatabasePacket<?>> databasePacket = reactiveDatabaseProtocolFrontendEngine.getCommandExecuteEngine().getOtherPacket(connectionSession);
        databasePacket.ifPresent(context::write);
        context.flush();
    }
}
