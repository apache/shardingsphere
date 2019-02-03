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
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.DataHeaderPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.generic.DatabaseFailurePacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.generic.DatabaseSuccessPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.MySQLCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.MySQLCommandPacketFactory;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLFieldCountPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLQueryCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.QueryResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.spi.root.RootInvokeHook;
import org.apache.shardingsphere.spi.root.SPIRootInvokeHook;

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
            MySQLCommandPacket mySQLCommandPacket = getCommandPacket(payload, backendConnection, frontendHandler);
            Optional<CommandResponsePackets> responsePackets = mySQLCommandPacket.execute();
            if (!responsePackets.isPresent()) {
                return;
            }
            if (responsePackets.get() instanceof QueryResponsePackets) {
                context.write(new MySQLFieldCountPacket(1, ((QueryResponsePackets) responsePackets.get()).getFieldCount()));
            }
            for (DatabasePacket each : responsePackets.get().getPackets()) {
                if (each instanceof DatabaseSuccessPacket) {
                    context.write(new MySQLOKPacket((DatabaseSuccessPacket) each));
                } else if (each instanceof DatabaseFailurePacket) {
                    context.write(new MySQLErrPacket((DatabaseFailurePacket) each));
                } else if (each instanceof DataHeaderPacket) {
                    context.write(new MySQLColumnDefinition41Packet((DataHeaderPacket) each));
                } else {
                    context.write(each);
                }
            }
            if (responsePackets.get() instanceof QueryResponsePackets) {
                context.write(new MySQLEofPacket(((QueryResponsePackets) responsePackets.get()).getSequenceId()));
            }
            if (mySQLCommandPacket instanceof MySQLQueryCommandPacket && !(responsePackets.get().getHeadPacket() instanceof DatabaseSuccessPacket)
                && !(responsePackets.get().getHeadPacket() instanceof DatabaseFailurePacket)) {
                writeMoreResults((MySQLQueryCommandPacket) mySQLCommandPacket, ((QueryResponsePackets) responsePackets.get()).getSequenceId());
            }
            connectionSize = backendConnection.getConnectionSize();
        } catch (final SQLException ex) {
            context.write(new MySQLErrPacket(++currentSequenceId, ex));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            context.write(new MySQLErrPacket(1, MySQLServerErrorCode.ER_STD_UNKNOWN_EXCEPTION, ex.getMessage()));
        } finally {
            context.flush();
            rootInvokeHook.finish(connectionSize);
        }
    }
    
    private MySQLCommandPacket getCommandPacket(final MySQLPacketPayload payload, final BackendConnection backendConnection, final FrontendHandler frontendHandler) throws SQLException {
        int sequenceId = payload.readInt1();
        return MySQLCommandPacketFactory.newInstance(sequenceId, payload, backendConnection);
    }
    
    private void writeMoreResults(final MySQLQueryCommandPacket mySQLQueryCommandPacket, final int headPacketsCount) throws SQLException {
        if (!context.channel().isActive()) {
            return;
        }
        currentSequenceId = headPacketsCount;
        int count = 0;
        int proxyFrontendFlushThreshold = GlobalRegistry.getInstance().getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.PROXY_FRONTEND_FLUSH_THRESHOLD);
        while (mySQLQueryCommandPacket.next()) {
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
            DatabasePacket resultValue = mySQLQueryCommandPacket.getResultValue();
            currentSequenceId = resultValue.getSequenceId();
            context.write(resultValue);
            if (proxyFrontendFlushThreshold == count) {
                context.flush();
                count = 0;
            }
        }
        context.write(new MySQLEofPacket(++currentSequenceId));
    }
}
