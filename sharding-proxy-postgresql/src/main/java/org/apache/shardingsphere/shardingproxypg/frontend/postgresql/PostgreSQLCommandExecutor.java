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

package org.apache.shardingsphere.shardingproxypg.frontend.postgresql;

import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.shardingproxypg.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxypg.frontend.common.FrontendHandler;
import org.apache.shardingsphere.shardingproxypg.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxypg.transport.common.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.PostgreSQLPacketPayload;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.PostgreSQLCommandPacketFactory;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.PostgreSQLCommandResponsePackets;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.query.PostgreSQLQueryCommandPacket;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.generic.CommandCompletePacket;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.generic.ErrorResponsePacket;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.generic.ReadyForQuery;
import org.apache.shardingsphere.spi.root.RootInvokeHook;
import org.apache.shardingsphere.spi.root.SPIRootInvokeHook;

import java.sql.SQLException;

/**
 * PostgreSQL command executor.
 *
 * @author zhangyonglun
 */
@RequiredArgsConstructor
public final class PostgreSQLCommandExecutor implements Runnable {
    
    private final ChannelHandlerContext context;
    
    private final ByteBuf message;
    
    private final FrontendHandler frontendHandler;
    
    private final RootInvokeHook rootInvokeHook = new SPIRootInvokeHook();
    
    @Override
    public void run() {
        rootInvokeHook.start();
        int connectionSize = 0;
        try (PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(message);
             BackendConnection backendConnection = frontendHandler.getBackendConnection()) {
            backendConnection.getStateHandler().waitUntilConnectionReleasedIfNecessary();
            PostgreSQLCommandPacket commandPacket = getCommandPacket(payload, backendConnection);
            Optional<PostgreSQLCommandResponsePackets> responsePackets = commandPacket.execute();
            if (!responsePackets.isPresent()) {
                return;
            }
            for (DatabasePacket each : responsePackets.get().getPackets()) {
                context.write(each);
            }
            if (commandPacket instanceof PostgreSQLQueryCommandPacket && !(responsePackets.get().getHeadPacket() instanceof ReadyForQuery)
                && !(responsePackets.get().getHeadPacket() instanceof ErrorResponsePacket)) {
                writeMoreResults((PostgreSQLQueryCommandPacket) commandPacket);
            }
            connectionSize = backendConnection.getConnectionSize();
            context.write(new CommandCompletePacket("SELECT", 1));
        } catch (final SQLException ex) {
            context.write(new ErrorResponsePacket());
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            context.write(new ErrorResponsePacket());
        } finally {
            context.writeAndFlush(new ReadyForQuery());
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
                synchronized (frontendHandler) {
                    try {
                        frontendHandler.wait();
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
