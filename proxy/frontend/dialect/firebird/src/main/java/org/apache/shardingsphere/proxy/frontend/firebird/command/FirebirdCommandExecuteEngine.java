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

package org.apache.shardingsphere.proxy.frontend.firebird.command;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdConnectionProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketFactory;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.commonresponses.generic.FirebirdErrorPacketFactory;
import org.apache.shardingsphere.database.protocol.firebird.packet.statements.execute.FirebirdExecuteStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.statements.fetch.FirebirdFetchStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.statements.prepare.FirebirdPrepareStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacketType;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Command execute engine for Firebird.
 */
@Slf4j
public final class FirebirdCommandExecuteEngine implements CommandExecuteEngine {
    
    @Override
    public FirebirdCommandPacketType getCommandPacketType(final PacketPayload payload) {
        return FirebirdCommandPacketType.valueOf(payload.getByteBuf().getInt(payload.getByteBuf().readerIndex()));
    }
    
    @Override
    public FirebirdCommandPacket getCommandPacket(final PacketPayload payload, final CommandPacketType type, final ConnectionSession connectionSession) {
        return FirebirdCommandPacketFactory.newInstance((FirebirdCommandPacketType) type, (FirebirdPacketPayload) payload,
                FirebirdConnectionProtocolVersion.getInstance().getProtocolVersion(connectionSession.getConnectionId()), connectionSession.getConnectionId());
    }
    
    @Override
    @HighFrequencyInvocation
    public CommandExecutor getCommandExecutor(final CommandPacketType type, final CommandPacket packet, final ConnectionSession connectionSession) {
        logCommand(type, packet, connectionSession);
        return FirebirdCommandExecutorFactory.newInstance((FirebirdCommandPacketType) type, packet, connectionSession);
    }
    
    private void logCommand(final CommandPacketType type, final CommandPacket packet, final ConnectionSession connectionSession) {
        if (!log.isDebugEnabled()) {
            return;
        }
        int connectionId = connectionSession.getConnectionId();
        if (packet instanceof FirebirdPrepareStatementPacket) {
            FirebirdPrepareStatementPacket preparePacket = (FirebirdPrepareStatementPacket) packet;
            log.debug("Firebird command: connectionId={}, operation={}, statementId={}, transactionId={}, sqlDialect={}", connectionId, type, preparePacket.getStatementId(),
                    preparePacket.getTransactionId(), preparePacket.getSqlDialect());
        } else if (packet instanceof FirebirdExecuteStatementPacket) {
            FirebirdExecuteStatementPacket executePacket = (FirebirdExecuteStatementPacket) packet;
            log.debug("Firebird command: connectionId={}, operation={}, statementId={}, transactionId={}, parameterCount={}", connectionId, type, executePacket.getStatementId(),
                    executePacket.getTransactionId(), executePacket.getParameterValues().size());
        } else if (packet instanceof FirebirdFetchStatementPacket) {
            FirebirdFetchStatementPacket fetchPacket = (FirebirdFetchStatementPacket) packet;
            log.debug("Firebird command: connectionId={}, operation={}, statementId={}, fetchSize={}", connectionId, type, fetchPacket.getStatementId(), fetchPacket.getFetchSize());
        } else {
            log.debug("Firebird command: connectionId={}, operation={}", connectionId, type);
        }
    }
    
    @Override
    public FirebirdPacket getErrorPacket(final Exception cause) {
        return FirebirdErrorPacketFactory.newInstance(cause);
    }
    
    @Override
    public Optional<DatabasePacket> getOtherPacket(final ConnectionSession connectionSession) {
        return Optional.empty();
    }
    
    @Override
    public void writeQueryData(final ChannelHandlerContext context,
                               final ProxyDatabaseConnectionManager databaseConnectionManager, final QueryCommandExecutor queryCommandExecutor, final int headerPackagesCount) throws SQLException {
        if (ResponseType.QUERY != queryCommandExecutor.getResponseType() || !context.channel().isActive()) {
            return;
        }
        int count = 0;
        int flushThreshold = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD);
        while (queryCommandExecutor.next()) {
            count++;
            databaseConnectionManager.getConnectionResourceLock().doAwait(context);
            context.write(queryCommandExecutor.getQueryRowPacket());
            if (flushThreshold == count) {
                context.flush();
                count = 0;
            }
        }
        context.flush();
    }
}
