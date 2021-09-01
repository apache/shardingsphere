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

package org.apache.shardingsphere.proxy.frontend.opengauss.command;

import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.db.protocol.opengauss.packet.command.OpenGaussCommandPacketFactory;
import org.apache.shardingsphere.db.protocol.opengauss.packet.command.OpenGaussCommandPacketType;
import org.apache.shardingsphere.db.protocol.packet.CommandPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacketType;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.opengauss.err.OpenGaussErrorPacketFactory;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContextRegistry;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Command execute engine for openGauss.
 */
public final class OpenGaussCommandExecuteEngine implements CommandExecuteEngine {
    
    private final PostgreSQLCommandExecuteEngine postgreSQLCommandExecuteEngine = new PostgreSQLCommandExecuteEngine();
    
    @Override
    public CommandPacketType getCommandPacketType(final PacketPayload payload) {
        return OpenGaussCommandPacketType.valueOf(((PostgreSQLPacketPayload) payload).readInt1());
    }
    
    @Override
    public CommandPacket getCommandPacket(final PacketPayload payload, final CommandPacketType type, final BackendConnection backendConnection) {
        return OpenGaussCommandPacketFactory.newInstance(type, (PostgreSQLPacketPayload) payload, backendConnection.getConnectionId());
    }
    
    @Override
    public CommandExecutor getCommandExecutor(final CommandPacketType type, final CommandPacket packet, final BackendConnection backendConnection) throws SQLException {
        PostgreSQLConnectionContext connectionContext = PostgreSQLConnectionContextRegistry.getInstance().get(backendConnection.getConnectionId());
        return OpenGaussCommandExecutorFactory.newInstance(type, packet, backendConnection, connectionContext);
    }
    
    @Override
    public DatabasePacket<?> getErrorPacket(final Exception cause, final BackendConnection backendConnection) {
        PostgreSQLConnectionContextRegistry.getInstance().get(backendConnection.getConnectionId()).getPendingExecutors().clear();
        return OpenGaussErrorPacketFactory.newInstance(cause);
    }
    
    @Override
    public Optional<DatabasePacket<?>> getOtherPacket(final BackendConnection backendConnection) {
        return postgreSQLCommandExecuteEngine.getOtherPacket(backendConnection);
    }
    
    @Override
    public boolean writeQueryData(final ChannelHandlerContext context,
                                  final BackendConnection backendConnection, final QueryCommandExecutor queryCommandExecutor, final int headerPackagesCount) throws SQLException {
        return postgreSQLCommandExecuteEngine.writeQueryData(context, backendConnection, queryCommandExecutor, headerPackagesCount);
    }
}
