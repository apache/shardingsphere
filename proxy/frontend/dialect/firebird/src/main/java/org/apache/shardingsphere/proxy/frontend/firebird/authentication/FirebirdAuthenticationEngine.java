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

package org.apache.shardingsphere.proxy.frontend.firebird.authentication;

import com.google.common.base.Strings;
import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import org.apache.shardingsphere.authentication.AuthenticatorFactory;
import org.apache.shardingsphere.authentication.AuthenticatorType;
import org.apache.shardingsphere.authentication.result.AuthenticationResult;
import org.apache.shardingsphere.authentication.result.AuthenticationResultBuilder;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.exception.core.exception.connection.AccessDeniedException;
import org.apache.shardingsphere.database.exception.core.exception.data.InvalidParameterValueException;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdUserDataType;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdConnectionProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdAcceptPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdAttachPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdConnectPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdContinueAuthPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdSRPAuthenticationData;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.firebird.handler.admin.executor.variable.charset.FirebirdCharacterSets;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.connection.ConnectionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.authentication.authenticator.FirebirdAuthenticatorType;
import org.apache.shardingsphere.proxy.frontend.firebird.resource.FirebirdConnectionResourceManager;
import org.firebirdsql.gds.ISCConstants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

/**
 * Authentication engine for Firebird.
 */
public final class FirebirdAuthenticationEngine implements AuthenticationEngine {
    
    private FirebirdSRPAuthenticationData authData;
    
    private FirebirdAuthenticationMethod selectedPlugin;
    
    private FirebirdAttachPacket pendingAttach;
    
    private AuthenticationResult currentAuthResult;
    
    private int connectionId;
    
    @Override
    public int handshake(final ChannelHandlerContext context) {
        connectionId = ConnectionIdGenerator.getInstance().nextId();
        context.channel().attr(FirebirdConstant.CURRENT_CONNECTION).set(connectionId);
        FirebirdConnectionResourceManager.getInstance().registerConnection(connectionId);
        return connectionId;
    }
    
    @Override
    public AuthenticationResult authenticate(final ChannelHandlerContext context, final PacketPayload payload) {
        payload.getByteBuf().resetReaderIndex();
        AuthorityRule rule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        FirebirdPacketPayload fdbPacketPayload = (FirebirdPacketPayload) payload;
        FirebirdCommandPacketType type = FirebirdCommandPacketType.valueOf(fdbPacketPayload.readInt4());
        switch (type) {
            case CONNECT:
                return processConnect(context, fdbPacketPayload, rule);
            case ATTACH:
                return processAttach(context, fdbPacketPayload, rule);
            case CONT_AUTH:
                return processContinueAuth(context, fdbPacketPayload, rule);
            default:
                throw new DatabaseProtocolException("Wrong operation %s during authentication phase", type.name());
        }
    }
    
    private AuthenticationResult processConnect(final ChannelHandlerContext context, final FirebirdPacketPayload payload, final AuthorityRule rule) {
        FirebirdConnectPacket connectPacket = new FirebirdConnectPacket(payload);
        FirebirdAcceptPacket acceptPacket = new FirebirdAcceptPacket(connectPacket.getUserProtocols());
        context.channel().attr(FirebirdConstant.LAZY_SEND).set(acceptPacket.isLazySend());
        context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(FirebirdCharacterSets.findCharacterSet("NONE"));
        FirebirdConnectionProtocolVersion.getInstance().setProtocolVersion(connectionId, acceptPacket.getProtocol().getVersion());
        context.channel().attr(FirebirdConstant.CONNECTION_PROTOCOL_VERSION).set(acceptPacket.getProtocol().getVersion());
        String username = connectPacket.getLogin();
        ShardingSphereUser user = rule.findUser(new Grantee(username, "")).orElseThrow(() -> new AccessDeniedException(username, connectPacket.getHost(), true));
        selectedPlugin = FirebirdAuthenticationMethod.valueOf(getPluginName(rule, user));
        validateClientPlugins(connectPacket.getUserInfoMap().get(FirebirdUserDataType.CNCT_PLUGIN_LIST));
        if (!selectedPlugin.getMethodName().equalsIgnoreCase(connectPacket.getPluginName())) {
            acceptPacket.setAcceptDataPacket(new byte[0], "", selectedPlugin, 0, "");
        } else if (FirebirdAuthenticationMethod.LEGACY_AUTH != selectedPlugin) {
            authData = new FirebirdSRPAuthenticationData(selectedPlugin.getHashAlgorithm(), username, user.getPassword(), connectPacket.getAuthData());
            acceptPacket.setAcceptDataPacket(authData.getSalt(), authData.getPublicKeyHex(), selectedPlugin, 0, "");
        }
        context.writeAndFlush(acceptPacket);
        currentAuthResult = AuthenticationResultBuilder.continued(username, connectPacket.getHost(), connectPacket.getDatabase(), Collections.emptyMap());
        return currentAuthResult;
    }
    
    private String getPluginName(final AuthorityRule rule, final ShardingSphereUser user) {
        String pluginName = rule.getAuthenticatorType(user);
        return pluginName.isEmpty() ? Arrays.stream(FirebirdAuthenticatorType.values()).filter(AuthenticatorType::isDefault).findAny().orElseThrow(IllegalArgumentException::new).name() : pluginName;
    }
    
    @SneakyThrows(SQLException.class)
    private void validateClientPlugins(final String pluginList) {
        if (!Strings.isNullOrEmpty(pluginList) && Arrays.stream(pluginList.split("[ \t,;]+")).noneMatch(each -> selectedPlugin.getMethodName().equalsIgnoreCase(each))) {
            throw new SQLException("", "08006", ISCConstants.isc_login_error);
        }
    }
    
    private AuthenticationResult processAttach(final ChannelHandlerContext context, final FirebirdPacketPayload payload, final AuthorityRule rule) {
        FirebirdAttachPacket attachPacket = new FirebirdAttachPacket(payload);
        context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(parseAttachCharset(attachPacket.getEncoding()));
        validateDatabase(currentAuthResult.getDatabase());
        if (null == authData && FirebirdAuthenticationMethod.LEGACY_AUTH != selectedPlugin) {
            ShardingSphereUser user = rule.findUser(new Grantee(currentAuthResult.getUsername(), "")).orElseThrow(() -> new AccessDeniedException(currentAuthResult.getUsername(), "", true));
            authData = new FirebirdSRPAuthenticationData(selectedPlugin.getHashAlgorithm(), currentAuthResult.getUsername(), user.getPassword(), attachPacket.getAuthData());
            pendingAttach = attachPacket;
            context.writeAndFlush(new FirebirdContinueAuthPacket(createChallenge(), selectedPlugin.getMethodName(), "", new byte[0]));
            return currentAuthResult;
        }
        return finishAttach(context, attachPacket, attachPacket.getAuthData(), rule);
    }
    
    private Charset parseAttachCharset(final String encoding) {
        if (null == encoding) {
            return FirebirdCharacterSets.findCharacterSet("NONE");
        }
        if ("OCTETS".equalsIgnoreCase(encoding) || "BINARY".equalsIgnoreCase(encoding)) {
            throw new InvalidParameterValueException("lc_ctype", encoding);
        }
        try {
            return FirebirdCharacterSets.findCharacterSet(encoding);
        } catch (final IllegalArgumentException ex) {
            throw new InvalidParameterValueException("lc_ctype", encoding);
        }
    }
    
    private void validateDatabase(final String databaseName) {
        ShardingSpherePreconditions.checkState(
                Strings.isNullOrEmpty(databaseName) || ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().containsDatabase(databaseName),
                () -> new UnknownDatabaseException(databaseName));
    }
    
    private byte[] createChallenge() {
        byte[] salt = authData.getSalt();
        byte[] key = authData.getPublicKeyHex().getBytes(StandardCharsets.US_ASCII);
        return ByteBuffer.allocate(salt.length + key.length + 4).order(ByteOrder.LITTLE_ENDIAN).putShort((short) salt.length).put(salt).putShort((short) key.length).put(key).array();
    }
    
    private AuthenticationResult finishAttach(final ChannelHandlerContext context, final FirebirdAttachPacket attachPacket, final String proof, final AuthorityRule rule) {
        login(currentAuthResult.getDatabase(), currentAuthResult.getUsername(), attachPacket.getEncPassword(), proof, rule);
        context.channel().attr(FirebirdConstant.ATTACHMENT_CHARSET).set(null == attachPacket.getEncoding() ? "NONE" : attachPacket.getEncoding());
        context.writeAndFlush(new FirebirdGenericResponsePacket());
        pendingAttach = null;
        return AuthenticationResultBuilder.finished(currentAuthResult.getUsername(), "", currentAuthResult.getDatabase(), currentAuthResult.getConnectionAttributes());
    }
    
    private void login(final String databaseName, final String username, final String encryptedPassword, final String proof, final AuthorityRule rule) {
        validateDatabase(databaseName);
        ShardingSphereUser user = rule.findUser(new Grantee(username, "")).orElseThrow(() -> new AccessDeniedException(username, "", true));
        boolean authenticated = new AuthenticatorFactory<>(FirebirdAuthenticatorType.class, rule).newInstance(user).authenticate(user, new Object[]{encryptedPassword, authData, proof});
        ShardingSpherePreconditions.checkState(authenticated, () -> new AccessDeniedException(username, "", true));
    }
    
    private AuthenticationResult processContinueAuth(final ChannelHandlerContext context, final FirebirdPacketPayload payload, final AuthorityRule rule) {
        if (null == pendingAttach || null == authData) {
            throw new DatabaseProtocolException("Unexpected continue authentication packet");
        }
        FirebirdContinueAuthPacket packet = new FirebirdContinueAuthPacket(payload);
        return finishAttach(context, pendingAttach, new String(packet.getData(), StandardCharsets.US_ASCII), rule);
    }
}
