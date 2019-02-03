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

package org.apache.shardingsphere.shardingproxy.frontend.mysql;

import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.frontend.common.FrontendHandler;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.DataHeaderPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.QueryResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.generic.DatabaseFailurePacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.generic.DatabaseSuccessPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.ServerErrorCode;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandPacketFactory;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.ColumnDefinition41Packet;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.FieldCountPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.QueryCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.EofPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import org.apache.shardingsphere.spi.hook.root.RootInvokeHook;
import org.apache.shardingsphere.spi.hook.root.SPIRootInvokeHook;

import java.sql.SQLException;

/**
 * MySQL command executor.
 *
 * @author zhangyonglun
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class MySQLCommandExecutor implements Runnable {
    
    private final ChannelHandlerContext context;
    
    private final ByteBuf message;
    
    private final FrontendHandler frontendHandler;
    
    private int currentSequenceId;
    
    private final RootInvokeHook rootInvokeHook = new SPIRootInvokeHook();
    
    @Override
    public void run() {
        rootInvokeHook.start();
        int connectionSize = 0;
        try (MySQLPacketPayload payload = new MySQLPacketPayload(message);
             BackendConnection backendConnection = frontendHandler.getBackendConnection()) {
            backendConnection.getStateHandler().waitUntilConnectionReleasedIfNecessary();
            CommandPacket commandPacket = getCommandPacket(payload, backendConnection, frontendHandler);
            Optional<CommandResponsePackets> responsePackets = commandPacket.execute();
            if (!responsePackets.isPresent()) {
                return;
            }
            if (responsePackets.get() instanceof QueryResponsePackets) {
                context.write(new FieldCountPacket(1, ((QueryResponsePackets) responsePackets.get()).getFieldCount()));
            }
            for (DatabasePacket each : responsePackets.get().getPackets()) {
                if (each instanceof DatabaseSuccessPacket) {
                    context.write(new OKPacket((DatabaseSuccessPacket) each));
                } else if (each instanceof DatabaseFailurePacket) {
                    context.write(new ErrPacket((DatabaseFailurePacket) each));
                } else if (each instanceof DataHeaderPacket) {
                    context.write(new ColumnDefinition41Packet((DataHeaderPacket) each));
                } else {
                    context.write(each);
                }
            }
            if (responsePackets.get() instanceof QueryResponsePackets) {
                context.write(new EofPacket(((QueryResponsePackets) responsePackets.get()).getSequenceId()));
            }
            if (commandPacket instanceof QueryCommandPacket && !(responsePackets.get().getHeadPacket() instanceof DatabaseSuccessPacket)
                && !(responsePackets.get().getHeadPacket() instanceof DatabaseFailurePacket)) {
                writeMoreResults((QueryCommandPacket) commandPacket, ((QueryResponsePackets) responsePackets.get()).getSequenceId());
            }
            connectionSize = backendConnection.getConnectionSize();
        } catch (final SQLException ex) {
            context.write(new ErrPacket(++currentSequenceId, ex));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            context.write(new ErrPacket(1, ServerErrorCode.ER_STD_UNKNOWN_EXCEPTION, ex.getMessage()));
        } finally {
            context.flush();
            rootInvokeHook.finish(connectionSize);
        }
    }
    
    private CommandPacket getCommandPacket(final MySQLPacketPayload payload, final BackendConnection backendConnection, final FrontendHandler frontendHandler) throws SQLException {
        int sequenceId = payload.readInt1();
        return CommandPacketFactory.newInstance(sequenceId, payload, backendConnection);
    }
    
    private void writeMoreResults(final QueryCommandPacket queryCommandPacket, final int headPacketsCount) throws SQLException {
        if (!context.channel().isActive()) {
            return;
        }
        currentSequenceId = headPacketsCount;
        int count = 0;
        int proxyFrontendFlushThreshold = GlobalRegistry.getInstance().getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.PROXY_FRONTEND_FLUSH_THRESHOLD);
        while (queryCommandPacket.next()) {
            count++;
            while (!context.channel().isWritable() && context.channel().isActive()) {
                context.flush();
                synchronized (frontendHandler) {
                    try {
                        frontendHandler.wait();
                    } catch (final InterruptedException ignored) {
                    }
                }
            }
            DatabasePacket resultValue = queryCommandPacket.getResultValue();
            currentSequenceId = resultValue.getSequenceId();
            context.write(resultValue);
            if (proxyFrontendFlushThreshold == count) {
                context.flush();
                count = 0;
            }
        }
        context.write(new EofPacket(++currentSequenceId));
    }
}
