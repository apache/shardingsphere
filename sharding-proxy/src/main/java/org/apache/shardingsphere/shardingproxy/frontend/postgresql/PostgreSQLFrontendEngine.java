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

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.frontend.common.DatabaseFrontendEngine;
import org.apache.shardingsphere.shardingproxy.frontend.common.executor.CommandExecutorSelector;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.generic.PostgreSQLReadyForQueryPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.handshake.PostgreSQLAuthenticationOKPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.handshake.PostgreSQLComStartupPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.handshake.PostgreSQLConnectionIdGenerator;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.handshake.PostgreSQLSSLNegativePacket;

/**
 * PostgreSQL frontend engine.
 *
 * @author zhangyonglun
 */
@RequiredArgsConstructor
public final class PostgreSQLFrontendEngine implements DatabaseFrontendEngine {
    
    private static final int SSL_REQUEST_PAYLOAD_LENGTH = 8;
    
    private static final int SSL_REQUEST_CODE = 80877103;
    
    private static final String DATABASE_NAME_KEYWORD = "database";
    
    @Override
    public String getDatabaseType() {
        return DatabaseType.PostgreSQL.name();
    }
    
    @Override
    public void handshake(final ChannelHandlerContext context, final BackendConnection backendConnection) {
        int connectionId = PostgreSQLConnectionIdGenerator.getInstance().nextId();
        backendConnection.setConnectionId(connectionId);
    }
    
    @Override
    public boolean auth(final ChannelHandlerContext context, final ByteBuf message, final BackendConnection backendConnection) {
        if (SSL_REQUEST_PAYLOAD_LENGTH == message.markReaderIndex().readInt() && SSL_REQUEST_CODE == message.readInt()) {
            context.writeAndFlush(new PostgreSQLSSLNegativePacket());
            return false;
        }
        message.resetReaderIndex();
        try (PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(message)) {
            PostgreSQLComStartupPacket postgreSQLComStartupPacket = new PostgreSQLComStartupPacket(payload);
            String databaseName = postgreSQLComStartupPacket.getParametersMap().get(DATABASE_NAME_KEYWORD);
            if (!Strings.isNullOrEmpty(databaseName) && !GlobalRegistry.getInstance().schemaExists(databaseName)) {
                // TODO send an error message
                return true;
            }
            backendConnection.setCurrentSchema(databaseName);
            // TODO send a md5 authentication request message
            context.write(new PostgreSQLAuthenticationOKPacket(true));
            context.writeAndFlush(new PostgreSQLReadyForQueryPacket());
            return true;
        }
    }
    
    @Override
    public void executeCommand(final ChannelHandlerContext context, final ByteBuf message, final BackendConnection backendConnection) {
        CommandExecutorSelector.getExecutor(backendConnection.getTransactionType(), context.channel().id()).execute(new PostgreSQLCommandExecutor(context, message, backendConnection));
    }
}
