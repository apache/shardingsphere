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

package org.apache.shardingsphere.proxy.frontend.command;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.CommonConstants;
import org.apache.shardingsphere.db.protocol.packet.CommandPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacketType;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.util.exception.ShardingSphereInsideException;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.BackendConnectionException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.constant.LogMDCConstants;
import org.apache.shardingsphere.proxy.frontend.exception.ExpectedExceptions;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.slf4j.MDC;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Command executor task.
 */
@RequiredArgsConstructor
@Slf4j
public final class CommandExecutorTask implements Runnable {
    
    private final DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine;
    
    private final ConnectionSession connectionSession;
    
    private final ChannelHandlerContext context;
    
    private final Object message;
    
    /**
     * To make sure SkyWalking will be available at the next release of ShardingSphere,
     * a new plugin should be provided to SkyWalking project if this API changed.
     *
     * @see <a href="https://github.com/apache/skywalking/blob/master/docs/en/guides/Java-Plugin-Development-Guide.md#user-content-plugin-development-guide">Plugin Development Guide</a>
     */
    @Override
    public void run() {
        boolean isNeedFlush = false;
        boolean sqlShowEnabled = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.SQL_SHOW);
        try (PacketPayload payload = databaseProtocolFrontendEngine.getCodecEngine().createPacketPayload((ByteBuf) message, context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get())) {
            if (sqlShowEnabled) {
                fillLogMDC();
            }
            connectionSession.getBackendConnection().prepareForTaskExecution();
            isNeedFlush = executeCommand(context, payload);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            processException(ex);
            // CHECKSTYLE:OFF
        } catch (final Error error) {
            // CHECKSTYLE:ON
            processException(new RuntimeException(error));
        } finally {
            connectionSession.clearQueryContext();
            Collection<SQLException> exceptions = Collections.emptyList();
            try {
                connectionSession.getBackendConnection().closeExecutionResources();
            } catch (final BackendConnectionException ex) {
                exceptions = ex.getExceptions().stream().filter(SQLException.class::isInstance).map(SQLException.class::cast).collect(Collectors.toList());
            }
            if (isNeedFlush) {
                context.flush();
            }
            processClosedExceptions(exceptions);
            if (sqlShowEnabled) {
                clearLogMDC();
            }
        }
    }
    
    private boolean executeCommand(final ChannelHandlerContext context, final PacketPayload payload) throws SQLException {
        CommandExecuteEngine commandExecuteEngine = databaseProtocolFrontendEngine.getCommandExecuteEngine();
        CommandPacketType type = commandExecuteEngine.getCommandPacketType(payload);
        CommandPacket commandPacket = commandExecuteEngine.getCommandPacket(payload, type, connectionSession);
        CommandExecutor commandExecutor = commandExecuteEngine.getCommandExecutor(type, commandPacket, connectionSession);
        try {
            Collection<DatabasePacket<?>> responsePackets = commandExecutor.execute();
            if (responsePackets.isEmpty()) {
                return false;
            }
            responsePackets.forEach(context::write);
            if (commandExecutor instanceof QueryCommandExecutor) {
                commandExecuteEngine.writeQueryData(context, connectionSession.getBackendConnection(), (QueryCommandExecutor) commandExecutor, responsePackets.size());
            }
            return true;
        } catch (final SQLException | ShardingSphereInsideException ex) {
            databaseProtocolFrontendEngine.handleException(connectionSession, ex);
            throw ex;
        } finally {
            commandExecutor.close();
        }
    }
    
    private void processException(final Exception cause) {
        if (!ExpectedExceptions.isExpected(cause.getClass())) {
            log.error("Exception occur: ", cause);
        }
        context.write(databaseProtocolFrontendEngine.getCommandExecuteEngine().getErrorPacket(cause));
        Optional<DatabasePacket<?>> databasePacket = databaseProtocolFrontendEngine.getCommandExecuteEngine().getOtherPacket(connectionSession);
        databasePacket.ifPresent(context::write);
        context.flush();
    }
    
    private void processClosedExceptions(final Collection<SQLException> exceptions) {
        if (exceptions.isEmpty()) {
            return;
        }
        SQLException ex = new SQLException("");
        for (SQLException each : exceptions) {
            ex.setNextException(each);
        }
        processException(ex);
    }
    
    private void fillLogMDC() {
        MDC.put(LogMDCConstants.DATABASE_KEY, connectionSession.getDatabaseName());
        MDC.put(LogMDCConstants.USER_KEY, connectionSession.getGrantee().toString());
    }
    
    private void clearLogMDC() {
        MDC.clear();
    }
}
