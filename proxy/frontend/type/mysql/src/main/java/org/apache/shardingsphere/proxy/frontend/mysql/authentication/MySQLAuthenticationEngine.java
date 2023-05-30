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

package org.apache.shardingsphere.proxy.frontend.mysql.authentication;

import com.google.common.base.Strings;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.authority.checker.AuthorityChecker;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.db.protocol.constant.CommonConstants;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLCapabilityFlag;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLCharacterSet;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConnectionPhase;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLStatusFlag;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLAuthSwitchRequestPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLAuthSwitchResponsePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLAuthenticationPluginData;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLHandshakePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLHandshakeResponse41Packet;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.db.protocol.payload.PacketPayload;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.dialect.mysql.exception.AccessDeniedException;
import org.apache.shardingsphere.dialect.mysql.exception.DatabaseAccessDeniedException;
import org.apache.shardingsphere.dialect.mysql.exception.HandshakeException;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationResult;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationResultBuilder;
import org.apache.shardingsphere.proxy.frontend.authentication.Authenticator;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticatorFactory;
import org.apache.shardingsphere.proxy.frontend.connection.ConnectionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.mysql.authentication.authenticator.MySQLAuthenticatorType;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLStatementIdGenerator;
import org.apache.shardingsphere.proxy.frontend.mysql.ssl.MySQLSSLRequestHandler;
import org.apache.shardingsphere.proxy.frontend.ssl.ProxySSLContext;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;

/**
 * Authentication engine for MySQL.
 */
@Slf4j
public final class MySQLAuthenticationEngine implements AuthenticationEngine {
    
    private final MySQLAuthenticationPluginData authPluginData = new MySQLAuthenticationPluginData();
    
    private MySQLConnectionPhase connectionPhase = MySQLConnectionPhase.INITIAL_HANDSHAKE;
    
    private byte[] authResponse;
    
    private AuthenticationResult currentAuthResult;
    
    @Override
    public int handshake(final ChannelHandlerContext context) {
        int result = ConnectionIdGenerator.getInstance().nextId();
        connectionPhase = MySQLConnectionPhase.AUTH_PHASE_FAST_PATH;
        boolean sslEnabled = ProxySSLContext.getInstance().isSSLEnabled();
        if (sslEnabled) {
            context.pipeline().addFirst(MySQLSSLRequestHandler.class.getSimpleName(), new MySQLSSLRequestHandler());
        }
        context.writeAndFlush(new MySQLHandshakePacket(result, sslEnabled, authPluginData));
        MySQLStatementIdGenerator.getInstance().registerConnection(result);
        return result;
    }
    
    @Override
    public AuthenticationResult authenticate(final ChannelHandlerContext context, final PacketPayload payload) {
        AuthorityRule rule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        if (MySQLConnectionPhase.AUTH_PHASE_FAST_PATH == connectionPhase) {
            currentAuthResult = authenticatePhaseFastPath(context, payload, rule);
            if (!currentAuthResult.isFinished()) {
                return currentAuthResult;
            }
        } else if (MySQLConnectionPhase.AUTHENTICATION_METHOD_MISMATCH == connectionPhase) {
            authenticateMismatchedMethod((MySQLPacketPayload) payload);
        }
        Grantee grantee = new Grantee(currentAuthResult.getUsername(), getHostAddress(context));
        if (!login(rule, grantee, authResponse)) {
            throw new AccessDeniedException(currentAuthResult.getUsername(), grantee.getHostname(), 0 != authResponse.length);
        }
        if (!authorizeDatabase(rule, grantee, currentAuthResult.getDatabase())) {
            throw new DatabaseAccessDeniedException(currentAuthResult.getUsername(), grantee.getHostname(), currentAuthResult.getDatabase());
        }
        writeOKPacket(context);
        return AuthenticationResultBuilder.finished(grantee.getUsername(), grantee.getHostname(), currentAuthResult.getDatabase());
    }
    
    private AuthenticationResult authenticatePhaseFastPath(final ChannelHandlerContext context, final PacketPayload payload, final AuthorityRule rule) {
        MySQLHandshakeResponse41Packet handshakeResponsePacket;
        try {
            handshakeResponsePacket = new MySQLHandshakeResponse41Packet((MySQLPacketPayload) payload);
        } catch (IndexOutOfBoundsException ex) {
            if (log.isWarnEnabled()) {
                log.warn("Received bad handshake from client {}: \n{}", context.channel(), ByteBufUtil.prettyHexDump(payload.getByteBuf().resetReaderIndex()));
            }
            throw new HandshakeException();
        }
        String database = handshakeResponsePacket.getDatabase();
        authResponse = handshakeResponsePacket.getAuthResponse();
        setCharacterSet(context, handshakeResponsePacket);
        if (!Strings.isNullOrEmpty(database) && !ProxyContext.getInstance().databaseExists(database)) {
            throw new UnknownDatabaseException(database);
        }
        String username = handshakeResponsePacket.getUsername();
        String hostname = getHostAddress(context);
        ShardingSphereUser user = rule.findUser(new Grantee(username, hostname)).orElseGet(() -> new ShardingSphereUser(username, "", hostname));
        Authenticator authenticator = new AuthenticatorFactory<>(MySQLAuthenticatorType.class, rule).newInstance(user);
        if (isClientPluginAuthenticate(handshakeResponsePacket) && !authenticator.getAuthenticationMethod().getMethodName().equals(handshakeResponsePacket.getAuthPluginName())) {
            connectionPhase = MySQLConnectionPhase.AUTHENTICATION_METHOD_MISMATCH;
            context.writeAndFlush(new MySQLAuthSwitchRequestPacket(authenticator.getAuthenticationMethod().getMethodName(), authPluginData));
            return AuthenticationResultBuilder.continued(username, hostname, database);
        }
        return AuthenticationResultBuilder.finished(username, hostname, database);
    }
    
    private void setCharacterSet(final ChannelHandlerContext context, final MySQLHandshakeResponse41Packet handshakeResponsePacket) {
        MySQLCharacterSet characterSet = MySQLCharacterSet.findById(handshakeResponsePacket.getCharacterSet());
        context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(characterSet.getCharset());
        context.channel().attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY).set(characterSet);
    }
    
    private boolean isClientPluginAuthenticate(final MySQLHandshakeResponse41Packet packet) {
        return 0 != (packet.getCapabilityFlags() & MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue());
    }
    
    private void authenticateMismatchedMethod(final MySQLPacketPayload payload) {
        authResponse = new MySQLAuthSwitchResponsePacket(payload).getAuthPluginResponse();
    }
    
    private boolean login(final AuthorityRule rule, final Grantee grantee, final byte[] authenticationResponse) {
        Optional<ShardingSphereUser> user = rule.findUser(grantee);
        return user.isPresent()
                && new AuthenticatorFactory<>(MySQLAuthenticatorType.class, rule).newInstance(user.get()).authenticate(user.get(), new Object[]{authenticationResponse, authPluginData});
    }
    
    private boolean authorizeDatabase(final AuthorityRule rule, final Grantee grantee, final String databaseName) {
        return null == databaseName || new AuthorityChecker(rule, grantee).isAuthorized(databaseName);
    }
    
    private String getHostAddress(final ChannelHandlerContext context) {
        if (context.channel() instanceof EpollDomainSocketChannel) {
            return context.channel().parent().localAddress().toString();
        }
        SocketAddress socketAddress = context.channel().remoteAddress();
        return socketAddress instanceof InetSocketAddress ? ((InetSocketAddress) socketAddress).getAddress().getHostAddress() : socketAddress.toString();
    }
    
    private void writeOKPacket(final ChannelHandlerContext context) {
        context.writeAndFlush(new MySQLOKPacket(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
    }
}
