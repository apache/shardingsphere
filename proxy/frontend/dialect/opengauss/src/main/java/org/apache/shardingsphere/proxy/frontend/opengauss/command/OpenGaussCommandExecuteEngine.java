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
import org.apache.shardingsphere.database.protocol.opengauss.packet.command.OpenGaussCommandPacketFactory;
import org.apache.shardingsphere.database.protocol.opengauss.packet.command.OpenGaussCommandPacketType;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacketType;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.database.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.opengauss.err.OpenGaussErrorPacketFactory;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PortalContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLPortalContextRegistry;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Command execute engine for openGauss.
 */
public final class OpenGaussCommandExecuteEngine implements CommandExecuteEngine {
    
    private final PostgreSQLCommandExecuteEngine postgresqlCommandExecuteEngine = new PostgreSQLCommandExecuteEngine();
    
    @Override
    public CommandPacketType getCommandPacketType(final PacketPayload payload) {
        return OpenGaussCommandPacketType.valueOf(payload.getByteBuf().getByte(payload.getByteBuf().readerIndex()));
    }
    
    @Override
    public CommandPacket getCommandPacket(final PacketPayload payload, final CommandPacketType type, final ConnectionSession connectionSession) {
        return OpenGaussCommandPacketFactory.newInstance(type, (PostgreSQLPacketPayload) payload);
    }
    
    @Override
    public CommandExecutor getCommandExecutor(final CommandPacketType type, final CommandPacket packet, final ConnectionSession connectionSession) throws SQLException {
        PortalContext portalContext = PostgreSQLPortalContextRegistry.getInstance().get(connectionSession.getConnectionId());
        return OpenGaussCommandExecutorFactory.newInstance(type, (PostgreSQLCommandPacket) packet, connectionSession, portalContext);
    }
    
    @Override
    public PostgreSQLPacket getErrorPacket(final Exception cause) {
        return OpenGaussErrorPacketFactory.newInstance(cause);
    }
    
    @Override
    public Optional<DatabasePacket> getOtherPacket(final ConnectionSession connectionSession) {
        return postgresqlCommandExecuteEngine.getOtherPacket(connectionSession);
    }
    
    @Override
    public void writeQueryData(final ChannelHandlerContext context,
                               final ProxyDatabaseConnectionManager databaseConnectionManager, final QueryCommandExecutor queryCommandExecutor, final int headerPackagesCount) throws SQLException {
        postgresqlCommandExecuteEngine.writeQueryData(context, databaseConnectionManager, queryCommandExecutor, headerPackagesCount);
    }
}
