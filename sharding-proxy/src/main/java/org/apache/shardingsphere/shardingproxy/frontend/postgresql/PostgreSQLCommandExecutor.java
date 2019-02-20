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

package org.apache.shardingsphere.shardingproxy.frontend.postgresql;

import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.spi.hook.SPIRootInvokeHook;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.DataHeaderPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.generic.DatabaseFailurePacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.generic.DatabaseSuccessPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacketFactory;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.PostgreSQLQueryCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.generic.PostgreSQLReadyForQueryPacket;
import org.apache.shardingsphere.spi.hook.RootInvokeHook;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL command executor.
 *
 * @author zhangyonglun
 */
@RequiredArgsConstructor
public final class PostgreSQLCommandExecutor implements Runnable {
    
    private final ChannelHandlerContext context;
    
    private final ByteBuf message;
    
    private final BackendConnection backendConnection;
    
    private final RootInvokeHook rootInvokeHook = new SPIRootInvokeHook();
    
    @Override
    public void run() {
        rootInvokeHook.start();
        int connectionSize = 0;
        try (PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(message);
             BackendConnection backendConnection = this.backendConnection) {
            backendConnection.getStateHandler().waitUntilConnectionReleasedIfNecessary();
            PostgreSQLCommandPacket commandPacket = getCommandPacket(payload, backendConnection);
            Optional<CommandResponsePackets> responsePackets = commandPacket.execute();
            if (!responsePackets.isPresent()) {
                return;
            }
            List<PostgreSQLColumnDescription> postgreSQLColumnDescriptions = new ArrayList<>(128);
            int columnIndex = 1;
            for (DatabasePacket each : responsePackets.get().getPackets()) {
                if (each instanceof DatabaseSuccessPacket) {
                    context.write(new PostgreSQLCommandCompletePacket());
                } else if (each instanceof DatabaseFailurePacket) {
                    context.write(new PostgreSQLErrorResponsePacket());
                } else if (each instanceof DataHeaderPacket) {
                    postgreSQLColumnDescriptions.add(new PostgreSQLColumnDescription((DataHeaderPacket) each, columnIndex++));
                } else {
                    context.write(each);
                }
            }
            if (commandPacket instanceof PostgreSQLQueryCommandPacket && !(responsePackets.get().getHeadPacket() instanceof DatabaseSuccessPacket)
                && !(responsePackets.get().getHeadPacket() instanceof DatabaseFailurePacket)) {
                context.write(new PostgreSQLRowDescriptionPacket(postgreSQLColumnDescriptions.size(), postgreSQLColumnDescriptions));
                writeMoreResults((PostgreSQLQueryCommandPacket) commandPacket);
            }
            connectionSize = backendConnection.getConnectionSize();
            context.write(new PostgreSQLCommandCompletePacket());
        } catch (final SQLException ex) {
            context.write(new PostgreSQLErrorResponsePacket());
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            context.write(new PostgreSQLErrorResponsePacket());
        } finally {
            context.writeAndFlush(new PostgreSQLReadyForQueryPacket());
            rootInvokeHook.finish(connectionSize);
        }
    }
    
    private PostgreSQLCommandPacket getCommandPacket(final PostgreSQLPacketPayload payload, final BackendConnection backendConnection) throws SQLException {
        return PostgreSQLCommandPacketFactory.newInstance(payload, backendConnection);
    }
    
    private void writeMoreResults(final PostgreSQLQueryCommandPacket queryCommandPacket) throws SQLException {
        if (!context.channel().isActive()) {
            return;
        }
        int count = 0;
        int proxyFrontendFlushThreshold = GlobalRegistry.getInstance().getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.PROXY_FRONTEND_FLUSH_THRESHOLD);
        while (queryCommandPacket.next()) {
            count++;
            while (!context.channel().isWritable() && context.channel().isActive()) {
                context.flush();
                synchronized (backendConnection) {
                    try {
                        backendConnection.wait();
                    } catch (final InterruptedException ignored) {
                    }
                }
            }
            DatabasePacket resultValue = queryCommandPacket.getResultValue();
            context.write(resultValue);
            if (proxyFrontendFlushThreshold == count) {
                context.flush();
                count = 0;
            }
        }
    }
}
