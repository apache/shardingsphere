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
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLErrorCode;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.BinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLReadyForQueryPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLAuthenticationMD5PasswordPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLAuthenticationOKPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLComStartupPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLParameterStatusPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLPasswordMessagePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLRandomGenerator;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLSSLNegativePacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.proxy.frontend.ConnectionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.engine.AuthenticationEngine;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Authentication engine for PostgreSQL.
 */
public final class PostgreSQLAuthenticationEngine implements AuthenticationEngine {
    
    private static final int SSL_REQUEST_PAYLOAD_LENGTH = 8;
    
    private static final int SSL_REQUEST_CODE = 80877103;
    
    private static final String USER_NAME_KEYWORD = "user";
    
    private static final String DATABASE_NAME_KEYWORD = "database";
    
    private final AtomicBoolean startupMessageReceived = new AtomicBoolean(false);
    
    private volatile byte[] md5Salt;
    
    @Override
    public int handshake(final ChannelHandlerContext context) {
        int result = ConnectionIdGenerator.getInstance().nextId();
        BinaryStatementRegistry.getInstance().register(result);
        return result;
    }
    
    @Override
    public boolean auth(final ChannelHandlerContext context, final PacketPayload payload, final BackendConnection backendConnection) {
        if (SSL_REQUEST_PAYLOAD_LENGTH == payload.getByteBuf().markReaderIndex().readInt() && SSL_REQUEST_CODE == payload.getByteBuf().readInt()) {
            context.writeAndFlush(new PostgreSQLSSLNegativePacket());
            return false;
        }
        payload.getByteBuf().resetReaderIndex();
        if (!startupMessageReceived.get()) {
            PostgreSQLComStartupPacket comStartupPacket = new PostgreSQLComStartupPacket((PostgreSQLPacketPayload) payload);
            startupMessageReceived.set(true);
            String databaseName = comStartupPacket.getParametersMap().get(DATABASE_NAME_KEYWORD);
            if (!Strings.isNullOrEmpty(databaseName) && !ProxySchemaContexts.getInstance().schemaExists(databaseName)) {
                PostgreSQLErrorResponsePacket responsePacket = createPostgreSQLErrorResponsePacket(PostgreSQLErrorCode.INVALID_CATALOG_NAME,
                    String.format("database \"%s\" does not exist", databaseName));
                context.writeAndFlush(responsePacket);
                context.close();
                return false;
            }
            backendConnection.setCurrentSchema(databaseName);
            String userName = comStartupPacket.getParametersMap().get(USER_NAME_KEYWORD);
            if (null == userName || userName.isEmpty()) {
                PostgreSQLErrorResponsePacket responsePacket = createPostgreSQLErrorResponsePacket(PostgreSQLErrorCode.SQLSERVER_REJECTED_ESTABLISHMENT_OF_SQLCONNECTION,
                    "user not set in StartupMessage");
                context.writeAndFlush(responsePacket);
                context.close();
                return false;
            }
            backendConnection.setUserName(userName);
            md5Salt = PostgreSQLRandomGenerator.getInstance().generateRandomBytes(4);
            context.writeAndFlush(new PostgreSQLAuthenticationMD5PasswordPacket(md5Salt));
            return false;
        } else {
            char messageType = (char) ((PostgreSQLPacketPayload) payload).readInt1();
            if ('p' != messageType) {
                PostgreSQLErrorResponsePacket responsePacket = createPostgreSQLErrorResponsePacket(PostgreSQLErrorCode.SQLSERVER_REJECTED_ESTABLISHMENT_OF_SQLCONNECTION,
                    "PasswordMessage is expected, message type 'p', but not '" + messageType + "'");
                context.writeAndFlush(responsePacket);
                context.close();
                return false;
            }
            PostgreSQLPasswordMessagePacket passwordMessagePacket = new PostgreSQLPasswordMessagePacket((PostgreSQLPacketPayload) payload);
            PostgreSQLLoginResult loginResult = PostgreSQLAuthenticationHandler.loginWithMd5Password(
                backendConnection.getUserName(), backendConnection.getSchema(), md5Salt, passwordMessagePacket);
            if (PostgreSQLErrorCode.SUCCESSFUL_COMPLETION != loginResult.getErrorCode()) {
                PostgreSQLErrorResponsePacket responsePacket = createPostgreSQLErrorResponsePacket(loginResult.getErrorCode(),
                    loginResult.getErrorMessage());
                context.writeAndFlush(responsePacket);
                context.close();
                return false;
            } else {
                // TODO implement PostgreSQLServerInfo like MySQLServerInfo
                context.write(new PostgreSQLAuthenticationOKPacket(true));
                context.write(new PostgreSQLParameterStatusPacket("server_version", "12.3"));
                context.write(new PostgreSQLParameterStatusPacket("client_encoding", "UTF8"));
                context.write(new PostgreSQLParameterStatusPacket("server_encoding", "UTF8"));
                context.writeAndFlush(new PostgreSQLReadyForQueryPacket());
                return true;
            }
        }
    }
    
    private PostgreSQLErrorResponsePacket createPostgreSQLErrorResponsePacket(final PostgreSQLErrorCode errorCode, final String errorMessage) {
        PostgreSQLErrorResponsePacket result = new PostgreSQLErrorResponsePacket();
        result.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_SEVERITY, "FATAL");
        result.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_CODE, errorCode.getErrorCode());
        result.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE, Strings.isNullOrEmpty(errorMessage) ? errorCode.getConditionName() : errorMessage);
        return result;
    }
}
