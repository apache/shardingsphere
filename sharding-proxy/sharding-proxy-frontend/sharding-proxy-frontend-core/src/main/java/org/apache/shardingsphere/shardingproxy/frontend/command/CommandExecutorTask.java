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

package org.apache.shardingsphere.shardingproxy.frontend.command;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.core.spi.hook.SPIRootInvokeHook;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.frontend.api.CommandExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.api.QueryCommandExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.spi.DatabaseFrontendEngine;
import org.apache.shardingsphere.shardingproxy.transport.api.packet.CommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.api.packet.CommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.api.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxy.transport.api.payload.PacketPayload;
import org.apache.shardingsphere.spi.hook.RootInvokeHook;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Command executor task.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Slf4j
public final class CommandExecutorTask implements Runnable {
    
    private final DatabaseFrontendEngine databaseFrontendEngine;
    
    private final BackendConnection backendConnection;
    
    private final ChannelHandlerContext context;
    
    private final Object message;
    
    @Override
    public void run() {
        RootInvokeHook rootInvokeHook = new SPIRootInvokeHook();
        rootInvokeHook.start();
        int connectionSize = 0;
        boolean isNeedFlush = false;
        try (BackendConnection backendConnection = this.backendConnection;
             PacketPayload payload = databaseFrontendEngine.createPacketPayload((ByteBuf) message)) {
            backendConnection.getStateHandler().waitUntilConnectionReleasedIfNecessary();
            isNeedFlush = executeCommand(context, payload, backendConnection);
            connectionSize = backendConnection.getConnectionSize();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("Exception occur: ", ex);
            context.write(databaseFrontendEngine.getCommandExecuteEngine().getErrorPacket(ex));
        } finally {
            if (isNeedFlush) {
                context.flush();
            }
            rootInvokeHook.finish(connectionSize);
        }
    }
    
    private boolean executeCommand(final ChannelHandlerContext context, final PacketPayload payload, final BackendConnection backendConnection) throws SQLException {
        CommandPacketType type = databaseFrontendEngine.getCommandExecuteEngine().getCommandPacketType(payload);
        CommandPacket commandPacket = databaseFrontendEngine.getCommandExecuteEngine().getCommandPacket(payload, type, backendConnection);
        CommandExecutor commandExecutor = databaseFrontendEngine.getCommandExecuteEngine().getCommandExecutor(type, commandPacket, backendConnection);
        Collection<DatabasePacket> responsePackets = commandExecutor.execute();
        if (responsePackets.isEmpty()) {
            return false;
        }
        for (DatabasePacket each : responsePackets) {
            context.write(each);
        }
        if (commandExecutor instanceof QueryCommandExecutor) {
            databaseFrontendEngine.getCommandExecuteEngine().writeQueryData(context, backendConnection, (QueryCommandExecutor) commandExecutor, responsePackets.size());
            return true;
        }
        return databaseFrontendEngine.getFrontendContext().isFlushForPerCommandPacket();
    }
}
