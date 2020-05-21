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

package org.apache.shardingsphere.proxy.frontend.postgresql.auth;

import com.google.common.base.Strings;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.BinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLReadyForQueryPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLAuthenticationOKPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLComStartupPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLParameterStatusPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLSSLNegativePacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.proxy.frontend.ConnectionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.engine.AuthenticationEngine;

/**
 * Authentication engine for PostgreSQL.
 */
public final class PostgreSQLAuthenticationEngine implements AuthenticationEngine {
    
    private static final int SSL_REQUEST_PAYLOAD_LENGTH = 8;
    
    private static final int SSL_REQUEST_CODE = 80877103;
    
    private static final String DATABASE_NAME_KEYWORD = "database";
    
    @Override
    public void handshake(final ChannelHandlerContext context, final BackendConnection backendConnection) {
        int connectionId = ConnectionIdGenerator.getInstance().nextId();
        backendConnection.setConnectionId(connectionId);
        BinaryStatementRegistry.getInstance().register(connectionId);
    }
    
    @Override
    public boolean auth(final ChannelHandlerContext context, final PacketPayload payload, final BackendConnection backendConnection) {
        if (SSL_REQUEST_PAYLOAD_LENGTH == payload.getByteBuf().markReaderIndex().readInt() && SSL_REQUEST_CODE == payload.getByteBuf().readInt()) {
            context.writeAndFlush(new PostgreSQLSSLNegativePacket());
            return false;
        }
        payload.getByteBuf().resetReaderIndex();
        PostgreSQLComStartupPacket comStartupPacket = new PostgreSQLComStartupPacket((PostgreSQLPacketPayload) payload);
        String databaseName = comStartupPacket.getParametersMap().get(DATABASE_NAME_KEYWORD);
        if (!Strings.isNullOrEmpty(databaseName) && !ProxySchemaContexts.getInstance().schemaExists(databaseName)) {
            PostgreSQLErrorResponsePacket responsePacket = new PostgreSQLErrorResponsePacket();
            responsePacket.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_SEVERITY, "FATAL");
            responsePacket.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_CODE, "3D000");
            responsePacket.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE, String.format("database \"%s\" does not exist", databaseName));
            context.writeAndFlush(responsePacket);
            return false;
        }
        backendConnection.setCurrentSchema(databaseName);
        // TODO send a md5 authentication request message
        context.write(new PostgreSQLAuthenticationOKPacket(true));
        context.write(new PostgreSQLParameterStatusPacket("server_version", "12.3"));
        context.write(new PostgreSQLParameterStatusPacket("client_encoding", "UTF8"));
        context.write(new PostgreSQLParameterStatusPacket("server_encoding", "UTF8"));
        context.writeAndFlush(new PostgreSQLReadyForQueryPacket());
        return true;
    }
}
