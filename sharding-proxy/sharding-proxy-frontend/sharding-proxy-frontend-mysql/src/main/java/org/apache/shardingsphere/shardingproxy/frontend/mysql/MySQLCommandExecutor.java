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
import org.apache.shardingsphere.core.spi.hook.SPIRootInvokeHook;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.context.GlobalContext;
import org.apache.shardingsphere.shardingproxy.error.CommonErrorCode;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.CommandTransportResponse;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.TransportResponse;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.MySQLCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.MySQLCommandPacketFactory;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLQueryCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacketFactory;
import org.apache.shardingsphere.shardingproxy.transport.spi.DatabasePacket;
import org.apache.shardingsphere.spi.hook.RootInvokeHook;

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
    
    private final BackendConnection backendConnection;
    
    private final RootInvokeHook rootInvokeHook = new SPIRootInvokeHook();
    
    @Override
    public void run() {
        rootInvokeHook.start();
        int connectionSize = 0;
        try (MySQLPacketPayload payload = new MySQLPacketPayload(message);
             BackendConnection backendConnection = this.backendConnection) {
            backendConnection.getStateHandler().waitUntilConnectionReleasedIfNecessary();
            MySQLCommandPacket mysqlCommandPacket = MySQLCommandPacketFactory.newInstance(payload.readInt1(), payload, backendConnection);
            Optional<TransportResponse> transportResponse = mysqlCommandPacket.execute();
            if (!transportResponse.isPresent()) {
                return;
            }
            for (DatabasePacket each : ((CommandTransportResponse) transportResponse.get()).getPackets()) {
                context.write(each);
            }
            if (transportResponse.get().hasMoreData()) {
                writeMoreResults((MySQLQueryCommandPacket) mysqlCommandPacket);
            }
            connectionSize = backendConnection.getConnectionSize();
        } catch (final SQLException ex) {
            context.write(MySQLErrPacketFactory.newInstance(1, ex));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            context.write(new MySQLErrPacket(1, CommonErrorCode.UNKNOWN_EXCEPTION, ex.getMessage()));
        } finally {
            context.flush();
            rootInvokeHook.finish(connectionSize);
        }
    }
    
    private void writeMoreResults(final MySQLQueryCommandPacket mysqlQueryCommandPacket) throws SQLException {
        if (!context.channel().isActive()) {
            return;
        }
        int count = 0;
        int flushThreshold = GlobalContext.getInstance().getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.PROXY_FRONTEND_FLUSH_THRESHOLD);
        int lastSequenceId = 0;
        while (mysqlQueryCommandPacket.next()) {
            count++;
            while (!context.channel().isWritable() && context.channel().isActive()) {
                context.flush();
                backendConnection.getResourceSynchronizer().doAwait();
            }
            MySQLPacket dataValue = mysqlQueryCommandPacket.getQueryData();
            context.write(dataValue);
            if (flushThreshold == count) {
                context.flush();
                count = 0;
            }
            lastSequenceId = dataValue.getSequenceId();
        }
        context.write(new MySQLEofPacket(++lastSequenceId));
    }
}
