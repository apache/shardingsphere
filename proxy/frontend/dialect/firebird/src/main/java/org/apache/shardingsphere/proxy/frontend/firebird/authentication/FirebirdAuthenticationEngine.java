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
import org.apache.shardingsphere.authentication.AuthenticatorFactory;
import org.apache.shardingsphere.authentication.AuthenticatorType;
import org.apache.shardingsphere.authentication.result.AuthenticationResult;
import org.apache.shardingsphere.authentication.result.AuthenticationResultBuilder;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdConnectionProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdAcceptPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdAttachPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.handshake.FirebirdConnectPacket;
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
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;

import java.util.Arrays;
import java.util.Optional;

/**
 * Authentication engine for Firebird.
 */
public final class FirebirdAuthenticationEngine implements AuthenticationEngine {
    
    private FirebirdSRPAuthenticationData authData;
    
    private FirebirdAuthenticationMethod plugin;
    
    private AuthenticationResult currentAuthResult;
    
    private int connectionId;
    
    @Override
    public int handshake(final ChannelHandlerContext context) {
        connectionId = ConnectionIdGenerator.getInstance().nextId();
        FirebirdTransactionIdGenerator.getInstance().registerConnection(connectionId);
        FirebirdStatementIdGenerator.getInstance().registerConnection(connectionId);
        FirebirdFetchStatementCache.getInstance().registerConnection(connectionId);
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
                // TODO implement CONT_AUTH
            default:
                throw new FirebirdProtocolException("Wrong operation %s during authentication phase", type.name());
        }
    }
    
    private AuthenticationResult processAttach(final ChannelHandlerContext context, final FirebirdPacketPayload payload, final AuthorityRule rule) {
        FirebirdAttachPacket attachPacket = new FirebirdAttachPacket(payload);
        context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(FirebirdCharacterSets.findCharacterSet(attachPacket.getEncoding()));
        login(currentAuthResult.getDatabase(), currentAuthResult.getUsername(), attachPacket, rule);
        context.writeAndFlush(new FirebirdGenericResponsePacket());
        return AuthenticationResultBuilder.finished(currentAuthResult.getUsername(), "", currentAuthResult.getDatabase());
    }
    
    private void login(final String databaseName, final String username, final FirebirdAttachPacket attachPacket, final AuthorityRule rule) {
        ShardingSpherePreconditions.checkState(
                Strings.isNullOrEmpty(databaseName) || ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().containsDatabase(databaseName),
                () -> new UnknownDatabaseException(databaseName));
        Grantee grantee = new Grantee(username, "");
        Optional<ShardingSphereUser> user = rule.findUser(grantee);
        user.ifPresent(shardingSphereUser -> new AuthenticatorFactory<>(FirebirdAuthenticatorType.class, rule)
                .newInstance(shardingSphereUser).authenticate(shardingSphereUser, new Object[]{attachPacket.getEncPassword(), authData, attachPacket.getAuthData()}));
    }
    
    private AuthenticationResult processConnect(final ChannelHandlerContext context, final FirebirdPacketPayload payload, final AuthorityRule rule) {
        FirebirdConnectPacket connectPacket = new FirebirdConnectPacket(payload);
        FirebirdAcceptPacket acceptPacket = new FirebirdAcceptPacket(connectPacket.getUserProtocols());
        context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(FirebirdCharacterSets.findCharacterSet("NONE"));
        FirebirdConnectionProtocolVersion.getInstance().setProtocolVersion(connectionId, acceptPacket.getProtocol().getVersion());
        context.channel().attr(FirebirdConstant.CONNECTION_PROTOCOL_VERSION).set(acceptPacket.getProtocol().getVersion());
        String username = connectPacket.getLogin();
        Grantee grantee = new Grantee(username, "");
        Optional<ShardingSphereUser> user = rule.findUser(grantee);
        plugin = FirebirdAuthenticationMethod.valueOf(getPluginName(rule, user.get()));
        FirebirdAuthenticationMethod userPlugin = connectPacket.getPlugin();
        if (plugin != userPlugin) {
            acceptPacket.setAcceptDataPacket(new byte[0], "", plugin, 0, "");
        } else if (plugin != FirebirdAuthenticationMethod.LEGACY_AUTH) {
            authData = new FirebirdSRPAuthenticationData(plugin.getHashAlgorithm(), username, user.get().getPassword(), connectPacket.getAuthData());
            acceptPacket.setAcceptDataPacket(authData.getSalt(), authData.getPublicKeyHex(), plugin, 0, "");
        }
        context.writeAndFlush(acceptPacket);
        currentAuthResult = AuthenticationResultBuilder.continued(username, connectPacket.getHost(), connectPacket.getDatabase());
        return currentAuthResult;
    }
    
    private String getPluginName(final AuthorityRule rule, final ShardingSphereUser user) {
        String pluginName = rule.getAuthenticatorType(user);
        return pluginName.isEmpty()
                // default plugin name
                ? Arrays.stream(FirebirdAuthenticatorType.class.getEnumConstants()).filter(AuthenticatorType::isDefault).findAny().orElseThrow(IllegalArgumentException::new).name()
                : pluginName;
    }
}
