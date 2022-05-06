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
import org.apache.shardingsphere.db.protocol.packet.CommandPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacketType;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketFactory;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketTypeLoader;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLDataRowPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLReadyForQueryPacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.proxy.backend.communication.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.PostgreSQLCommand;
import org.apache.shardingsphere.proxy.frontend.postgresql.err.PostgreSQLErrPacketFactory;

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
    public PostgreSQLCommandPacket getCommandPacket(final PacketPayload payload, final CommandPacketType type, final ConnectionSession connectionSession) {
        return PostgreSQLCommandPacketFactory.newInstance((PostgreSQLCommandPacketType) type, (PostgreSQLPacketPayload) payload);
    }
    
    @Override
    public CommandExecutor getCommandExecutor(final CommandPacketType type, final CommandPacket packet, final ConnectionSession connectionSession) throws SQLException {
        PostgreSQLConnectionContext connectionContext = PostgreSQLConnectionContextRegistry.getInstance().get(connectionSession.getConnectionId());
        return PostgreSQLCommandExecutorFactory.newInstance((PostgreSQLCommandPacketType) type, (PostgreSQLCommandPacket) packet, connectionSession, connectionContext);
    }
    
    @Override
    public DatabasePacket<?> getErrorPacket(final Exception cause) {
        return PostgreSQLErrPacketFactory.newInstance(cause);
    }
    
    @Override
    public Optional<DatabasePacket<?>> getOtherPacket(final ConnectionSession connectionSession) {
        return Optional.of(connectionSession.getTransactionStatus().isInTransaction() ? PostgreSQLReadyForQueryPacket.TRANSACTION_FAILED : PostgreSQLReadyForQueryPacket.NOT_IN_TRANSACTION);
    }
    
    @Override
    public void writeQueryData(final ChannelHandlerContext context,
                               final BackendConnection backendConnection, final QueryCommandExecutor queryCommandExecutor, final int headerPackagesCount) throws SQLException {
        if (ResponseType.QUERY == queryCommandExecutor.getResponseType() && !context.channel().isActive()) {
            context.write(new PostgreSQLCommandCompletePacket(PostgreSQLCommand.SELECT.name(), 0));
            return;
        }
        processSimpleQuery(context, (JDBCBackendConnection) backendConnection, queryCommandExecutor);
    }
    
    private void processSimpleQuery(final ChannelHandlerContext context, final JDBCBackendConnection backendConnection, final QueryCommandExecutor queryExecutor) throws SQLException {
        if (ResponseType.UPDATE == queryExecutor.getResponseType()) {
            context.write(backendConnection.getConnectionSession().getTransactionStatus().isInTransaction() ? PostgreSQLReadyForQueryPacket.IN_TRANSACTION
                    : PostgreSQLReadyForQueryPacket.NOT_IN_TRANSACTION);
            return;
        }
        long dataRows = writeDataPackets(context, backendConnection, queryExecutor);
        if (ResponseType.QUERY == queryExecutor.getResponseType()) {
            context.write(new PostgreSQLCommandCompletePacket(PostgreSQLCommand.SELECT.name(), dataRows));
        }
        context.write(backendConnection.getConnectionSession().getTransactionStatus().isInTransaction() ? PostgreSQLReadyForQueryPacket.IN_TRANSACTION
                : PostgreSQLReadyForQueryPacket.NOT_IN_TRANSACTION);
    }
    
    private long writeDataPackets(final ChannelHandlerContext context, final JDBCBackendConnection backendConnection, final QueryCommandExecutor queryCommandExecutor) throws SQLException {
        long dataRows = 0;
        int flushCount = 0;
        int proxyFrontendFlushThreshold = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps().<Integer>getValue(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD);
        while (queryCommandExecutor.next()) {
            flushCount++;
            while (!context.channel().isWritable() && context.channel().isActive()) {
                context.flush();
                backendConnection.getResourceLock().doAwait();
            }
            DatabasePacket<?> resultValue = queryCommandExecutor.getQueryRowPacket();
            context.write(resultValue);
            if (proxyFrontendFlushThreshold == flushCount) {
                context.flush();
                flushCount = 0;
            }
            if (resultValue instanceof PostgreSQLDataRowPacket) {
                dataRows++;
            }
        }
        return dataRows;
    }
}
