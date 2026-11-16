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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.MySQLServerVersion;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.PasswordEncryption;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLAuthenticationPlugin;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCapabilityFlag;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLAuthMoreDataPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLAuthSwitchRequestPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLAuthSwitchResponsePacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLHandshakePacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLHandshakeResponse41Packet;

import java.security.NoSuchAlgorithmException;

/**
 * MySQL negotiate handler.
 */
@RequiredArgsConstructor
public final class MySQLNegotiateHandler extends ChannelInboundHandlerAdapter {
    
    private static final int MAX_PACKET_SIZE = 1 << 24;
    
    private static final int CHARACTER_SET = 33;
    
    private static final int REQUEST_PUBLIC_KEY = 2;
    
    private static final int PERFORM_FULL_AUTHENTICATION = 4;
    
    private final String username;
    
    private final String password;
    
    private final Promise<Object> authResultCallback;
    
    private MySQLServerVersion serverVersion;
    
    private byte[] seed;
    
    private boolean publicKeyRequested;
    
    @SneakyThrows(NoSuchAlgorithmException.class)
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof MySQLHandshakePacket) {
            MySQLHandshakePacket handshake = (MySQLHandshakePacket) msg;
            MySQLHandshakeResponse41Packet handshakeResponsePacket = new MySQLHandshakeResponse41Packet(MAX_PACKET_SIZE, CHARACTER_SET, username);
            handshakeResponsePacket.setAuthResponse(generateAuthResponse(handshake.getAuthPluginData().getAuthenticationPluginData()));
            handshakeResponsePacket.setCapabilityFlags(generateClientCapability());
            handshakeResponsePacket.setAuthPluginName(MySQLAuthenticationMethod.NATIVE);
            ctx.channel().writeAndFlush(handshakeResponsePacket);
            serverVersion = new MySQLServerVersion(handshake.getServerVersion());
            return;
        }
        if (msg instanceof MySQLAuthSwitchRequestPacket) {
            MySQLAuthSwitchRequestPacket authSwitchRequest = (MySQLAuthSwitchRequestPacket) msg;
            ctx.channel().writeAndFlush(new MySQLAuthSwitchResponsePacket(getAuthPluginResponse(authSwitchRequest)));
            seed = authSwitchRequest.getAuthPluginData().getAuthenticationPluginData();
            return;
        }
        if (msg instanceof MySQLAuthMoreDataPacket) {
            MySQLAuthMoreDataPacket authMoreData = (MySQLAuthMoreDataPacket) msg;
            handleCachingSha2Auth(ctx, authMoreData);
            return;
        }
        if (msg instanceof MySQLOKPacket) {
            ctx.channel().pipeline().remove(this);
            authResultCallback.setSuccess(serverVersion);
            return;
        }
        MySQLErrPacket error = (MySQLErrPacket) msg;
        ctx.channel().close();
        throw new PipelineInternalException(error.getErrorMessage());
    }
    
    private byte[] getAuthPluginResponse(final MySQLAuthSwitchRequestPacket authSwitchRequest) throws NoSuchAlgorithmException {
        // TODO not support sha256_password now
        switch (MySQLAuthenticationPlugin.getPluginByName(authSwitchRequest.getAuthPluginName())) {
            case NATIVE:
                return PasswordEncryption.encryptWithMySQL41(password.getBytes(), authSwitchRequest.getAuthPluginData().getAuthenticationPluginData());
            case CACHING_SHA2:
                return PasswordEncryption.encryptWithSha2(password.getBytes(), authSwitchRequest.getAuthPluginData().getAuthenticationPluginData());
            default:
                return password.getBytes();
        }
    }
    
    private void handleCachingSha2Auth(final ChannelHandlerContext ctx, final MySQLAuthMoreDataPacket authMoreData) {
        if (publicKeyRequested) {
            ctx.channel().writeAndFlush(new MySQLAuthSwitchResponsePacket(PasswordEncryption.encryptWithRSAPublicKey(
                    password, seed, serverVersion.greaterThanOrEqualTo(8, 0, 5) ? "RSA/ECB/OAEPWithSHA-1AndMGF1Padding" : "RSA/ECB/PKCS1Padding", new String(authMoreData.getPluginData()))));
        } else {
            if (PERFORM_FULL_AUTHENTICATION == authMoreData.getPluginData()[0]) {
                publicKeyRequested = true;
                ctx.channel().writeAndFlush(new MySQLAuthSwitchResponsePacket(new byte[]{REQUEST_PUBLIC_KEY}));
            }
        }
    }
    
    private int generateClientCapability() {
        return MySQLCapabilityFlag.calculateCapabilityFlags(MySQLCapabilityFlag.CLIENT_LONG_PASSWORD, MySQLCapabilityFlag.CLIENT_LONG_FLAG,
                MySQLCapabilityFlag.CLIENT_PROTOCOL_41, MySQLCapabilityFlag.CLIENT_INTERACTIVE, MySQLCapabilityFlag.CLIENT_TRANSACTIONS,
                MySQLCapabilityFlag.CLIENT_SECURE_CONNECTION, MySQLCapabilityFlag.CLIENT_MULTI_STATEMENTS, MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH);
    }
    
    @SneakyThrows(NoSuchAlgorithmException.class)
    private byte[] generateAuthResponse(final byte[] authPluginData) {
        return null == password || password.isEmpty() ? new byte[0] : PasswordEncryption.encryptWithMySQL41(password.getBytes(), authPluginData);
    }
}
