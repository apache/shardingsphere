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

package org.apache.shardingsphere.proxy.frontend.mysql.auth;

import com.google.common.base.Strings;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLCapabilityFlag;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConnectionPhase;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLAuthSwitchRequestPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLAuthSwitchResponsePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLHandshakePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLHandshakeResponse41Packet;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.proxy.frontend.ConnectionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.auth.AuthenticationResultBuilder;
import org.apache.shardingsphere.proxy.frontend.auth.AuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.auth.AuthenticationResult;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;

/**
 * Authentication engine for MySQL.
 */
public final class MySQLAuthenticationEngine implements AuthenticationEngine {
    
    private final MySQLAuthenticationHandler authenticationHandler = new MySQLAuthenticationHandler();
    
    private MySQLConnectionPhase connectionPhase = MySQLConnectionPhase.INITIAL_HANDSHAKE;
    
    private int sequenceId;
    
    private byte[] authResponse;
    
    private AuthenticationResult currentAuthResult;
    
    @Override
    public int handshake(final ChannelHandlerContext context) {
        int result = ConnectionIdGenerator.getInstance().nextId();
        connectionPhase = MySQLConnectionPhase.AUTH_PHASE_FAST_PATH;
        context.writeAndFlush(new MySQLHandshakePacket(result, authenticationHandler.getAuthPluginData()));
        return result;
    }
    
    @Override
    public AuthenticationResult auth(final ChannelHandlerContext context, final PacketPayload payload) {
        if (MySQLConnectionPhase.AUTH_PHASE_FAST_PATH == connectionPhase) {
            currentAuthResult = authPhaseFastPath(context, payload);
            if (!currentAuthResult.isFinished()) {
                return currentAuthResult;
            }
        } else if (MySQLConnectionPhase.AUTHENTICATION_METHOD_MISMATCH == connectionPhase) {
            authenticationMethodMismatch((MySQLPacketPayload) payload);
        }
        Optional<MySQLServerErrorCode> errorCode = authenticationHandler.login(currentAuthResult.getUsername(), authResponse, currentAuthResult.getDatabase());
        context.writeAndFlush(errorCode.isPresent() ? createErrorPacket(errorCode.get(), context) : new MySQLOKPacket(++sequenceId));
        return AuthenticationResultBuilder.finished(currentAuthResult.getUsername(), currentAuthResult.getDatabase());
    }
    
    private AuthenticationResult authPhaseFastPath(final ChannelHandlerContext context, final PacketPayload payload) {
        MySQLHandshakeResponse41Packet packet = new MySQLHandshakeResponse41Packet((MySQLPacketPayload) payload);
        authResponse = packet.getAuthResponse();
        sequenceId = packet.getSequenceId();
        if (!Strings.isNullOrEmpty(packet.getDatabase()) && !ProxySchemaContexts.getInstance().schemaExists(packet.getDatabase())) {
            context.writeAndFlush(new MySQLErrPacket(++sequenceId, MySQLServerErrorCode.ER_BAD_DB_ERROR, packet.getDatabase()));
            return AuthenticationResultBuilder.continued();
        }
        if (isClientPluginAuth(packet) && !MySQLAuthenticationMethod.SECURE_PASSWORD_AUTHENTICATION.getMethodName().equals(packet.getAuthPluginName())) {
            connectionPhase = MySQLConnectionPhase.AUTHENTICATION_METHOD_MISMATCH;
            context.writeAndFlush(new MySQLAuthSwitchRequestPacket(++sequenceId, MySQLAuthenticationMethod.SECURE_PASSWORD_AUTHENTICATION.getMethodName(), authenticationHandler.getAuthPluginData()));
            return AuthenticationResultBuilder.continued(packet.getUsername(), packet.getDatabase());
        }
        return AuthenticationResultBuilder.finished(packet.getUsername(), packet.getDatabase());
    }
    
    private boolean isClientPluginAuth(final MySQLHandshakeResponse41Packet packet) {
        return 0 != (packet.getCapabilityFlags() & MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue());
    }
    
    private void authenticationMethodMismatch(final MySQLPacketPayload payload) {
        MySQLAuthSwitchResponsePacket packet = new MySQLAuthSwitchResponsePacket(payload);
        sequenceId = packet.getSequenceId();
        authResponse = packet.getAuthPluginResponse();
    }
    
    private MySQLErrPacket createErrorPacket(final MySQLServerErrorCode errorCode, final ChannelHandlerContext context) {
        return MySQLServerErrorCode.ER_DBACCESS_DENIED_ERROR == errorCode
                ? new MySQLErrPacket(++sequenceId, MySQLServerErrorCode.ER_DBACCESS_DENIED_ERROR, currentAuthResult.getUsername(), getHostAddress(context), currentAuthResult.getDatabase())
                : new MySQLErrPacket(++sequenceId, MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR, currentAuthResult.getUsername(), getHostAddress(context), getErrorMessage());
    }
    
    private String getErrorMessage() {
        return 0 == authResponse.length ? "NO" : "YES";
    }
    
    private String getHostAddress(final ChannelHandlerContext context) {
        SocketAddress socketAddress = context.channel().remoteAddress();
        return socketAddress instanceof InetSocketAddress ? ((InetSocketAddress) socketAddress).getAddress().getHostAddress() : socketAddress.toString();
    }
}
