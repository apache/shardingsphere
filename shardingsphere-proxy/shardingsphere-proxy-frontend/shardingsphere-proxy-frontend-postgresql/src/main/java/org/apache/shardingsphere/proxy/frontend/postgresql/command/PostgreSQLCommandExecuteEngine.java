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

import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.packet.CommandPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacketType;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketFactory;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketTypeLoader;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLReadyForQueryPacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.engine.CommandExecuteEngine;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Command execute engine for PostgreSQL.
 */
public final class PostgreSQLCommandExecuteEngine implements CommandExecuteEngine {
    
    @Override
    public PostgreSQLCommandPacketType getCommandPacketType(final PacketPayload payload) {
        return PostgreSQLCommandPacketTypeLoader.getCommandPacketType((PostgreSQLPacketPayload) payload);
    }
    
    @Override
    public PostgreSQLCommandPacket getCommandPacket(final PacketPayload payload, final CommandPacketType type, final BackendConnection backendConnection) throws SQLException {
        return PostgreSQLCommandPacketFactory.newInstance((PostgreSQLCommandPacketType) type, (PostgreSQLPacketPayload) payload, backendConnection.getConnectionId());
    }
    
    @Override
    public CommandExecutor getCommandExecutor(final CommandPacketType type, final CommandPacket packet, final BackendConnection backendConnection) {
        return PostgreSQLCommandExecutorFactory.newInstance((PostgreSQLCommandPacketType) type, (PostgreSQLCommandPacket) packet, backendConnection);
    }
    
    @Override
    public DatabasePacket<?> getErrorPacket(final Exception cause) {
        PostgreSQLErrorResponsePacket errorResponsePacket = new PostgreSQLErrorResponsePacket();
        errorResponsePacket.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE, cause.getMessage());
        return errorResponsePacket;
    }
    
    @Override
    public Optional<DatabasePacket<?>> getOtherPacket() {
        return Optional.of(new PostgreSQLReadyForQueryPacket());
    }
    
    @Override
    @SneakyThrows
    public void writeQueryData(final ChannelHandlerContext context,
                               final BackendConnection backendConnection, final QueryCommandExecutor queryCommandExecutor, final int headerPackagesCount) {
        if (queryCommandExecutor.isQueryResponse() && !context.channel().isActive()) {
            context.write(new PostgreSQLCommandCompletePacket());
            context.write(new PostgreSQLReadyForQueryPacket());
            return;
        }
        if (queryCommandExecutor.isErrorResponse() || queryCommandExecutor.isUpdateResponse()) {
            context.write(new PostgreSQLReadyForQueryPacket());
            return;
        }
        int count = 0;
        int proxyFrontendFlushThreshold = ProxySchemaContexts.getInstance().getSchemaContexts().getProps().<Integer>getValue(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD);
        while (queryCommandExecutor.next()) {
            count++;
            while (!context.channel().isWritable() && context.channel().isActive()) {
                context.flush();
                backendConnection.getResourceSynchronizer().doAwaitUntil();
            }
            DatabasePacket<?> resultValue = queryCommandExecutor.getQueryData();
            context.write(resultValue);
            if (proxyFrontendFlushThreshold == count) {
                context.flush();
                count = 0;
            }
        }
        context.write(new PostgreSQLCommandCompletePacket());
        context.write(new PostgreSQLReadyForQueryPacket());
    }
}
