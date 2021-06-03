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

package org.apache.shardingsphere.proxy.frontend.postgresql.authentication;

import com.google.common.base.Strings;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLErrorCode;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLServerInfo;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLReadyForQueryPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLAuthenticationMD5PasswordPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLAuthenticationOKPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLComStartupPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLParameterStatusPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLPasswordMessagePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLRandomGenerator;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLSSLNegativePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationResult;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationResultBuilder;
import org.apache.shardingsphere.proxy.frontend.connection.ConnectionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.exception.InvalidAuthorizationSpecificationException;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.exception.PostgreSQLAuthenticationException;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.exception.PostgreSQLProtocolViolationException;

/**
 * Authentication engine for PostgreSQL.
 */
public final class PostgreSQLAuthenticationEngine implements AuthenticationEngine {
    
    private static final int SSL_REQUEST_PAYLOAD_LENGTH = 8;
    
    private static final int SSL_REQUEST_CODE = 80877103;
    
    private boolean startupMessageReceived;
    
    private byte[] md5Salt;
    
    private AuthenticationResult currentAuthResult;
    
    @Override
    public int handshake(final ChannelHandlerContext context) {
        int result = ConnectionIdGenerator.getInstance().nextId();
        PostgreSQLBinaryStatementRegistry.getInstance().register(result);
        return result;
    }
    
    @Override
    public AuthenticationResult authenticate(final ChannelHandlerContext context, final PacketPayload payload) {
        if (SSL_REQUEST_PAYLOAD_LENGTH == payload.getByteBuf().markReaderIndex().readInt() && SSL_REQUEST_CODE == payload.getByteBuf().readInt()) {
            context.writeAndFlush(new PostgreSQLSSLNegativePacket());
            return AuthenticationResultBuilder.continued();
        }
        payload.getByteBuf().resetReaderIndex();
        return startupMessageReceived ? processPasswordMessage(context, (PostgreSQLPacketPayload) payload) : processStartupMessage(context, (PostgreSQLPacketPayload) payload);
    }
    
    private AuthenticationResult processStartupMessage(final ChannelHandlerContext context, final PostgreSQLPacketPayload payload) {
        startupMessageReceived = true;
        PostgreSQLComStartupPacket comStartupPacket = new PostgreSQLComStartupPacket(payload);
        String user = comStartupPacket.getUser();
        if (Strings.isNullOrEmpty(user)) {
            throw new InvalidAuthorizationSpecificationException("no PostgreSQL user name specified in startup packet");
        }
        md5Salt = PostgreSQLRandomGenerator.getInstance().generateRandomBytes(4);
        context.writeAndFlush(new PostgreSQLAuthenticationMD5PasswordPacket(md5Salt));
        currentAuthResult = AuthenticationResultBuilder.continued(user, "", comStartupPacket.getDatabase());
        return currentAuthResult;
    }
    
    private AuthenticationResult processPasswordMessage(final ChannelHandlerContext context, final PostgreSQLPacketPayload payload) {
        char messageType = (char) payload.readInt1();
        if (PostgreSQLMessagePacketType.PASSWORD_MESSAGE.getValue() != messageType) {
            throw new PostgreSQLProtocolViolationException("password", Character.toString(messageType));
        }
        PostgreSQLPasswordMessagePacket passwordMessagePacket = new PostgreSQLPasswordMessagePacket(payload);
        PostgreSQLLoginResult loginResult = PostgreSQLAuthenticationHandler.loginWithMd5Password(currentAuthResult.getUsername(), currentAuthResult.getDatabase(), md5Salt, passwordMessagePacket);
        if (PostgreSQLErrorCode.SUCCESSFUL_COMPLETION != loginResult.getErrorCode()) {
            throw new PostgreSQLAuthenticationException(loginResult.getErrorCode(), loginResult.getErrorMessage());
        }
        // TODO implement PostgreSQLServerInfo like MySQLServerInfo
        context.write(new PostgreSQLAuthenticationOKPacket());
        context.write(new PostgreSQLParameterStatusPacket("server_version", PostgreSQLServerInfo.getServerVersion()));
        context.write(new PostgreSQLParameterStatusPacket("client_encoding", "UTF8"));
        context.write(new PostgreSQLParameterStatusPacket("server_encoding", "UTF8"));
        context.writeAndFlush(new PostgreSQLReadyForQueryPacket(false));
        return AuthenticationResultBuilder.finished(currentAuthResult.getUsername(), "", currentAuthResult.getDatabase());
    }
}
