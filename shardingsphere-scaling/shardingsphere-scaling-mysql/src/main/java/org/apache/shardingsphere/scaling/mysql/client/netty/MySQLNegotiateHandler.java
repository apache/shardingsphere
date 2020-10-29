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

package org.apache.shardingsphere.scaling.mysql.client.netty;

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLCapabilityFlag;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLHandshakePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLHandshakeResponse41Packet;
import org.apache.shardingsphere.scaling.mysql.client.MySQLPasswordEncryptor;
import org.apache.shardingsphere.scaling.mysql.client.ServerInfo;
import org.apache.shardingsphere.scaling.mysql.client.ServerVersion;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;

import java.security.NoSuchAlgorithmException;

/**
 * MySQL Negotiate Handler.
 */
@RequiredArgsConstructor
public final class MySQLNegotiateHandler extends ChannelInboundHandlerAdapter {
    
    private static final int MAX_PACKET_SIZE = 1 << 24;
    
    private static final int CHARACTER_SET = 33;
    
    private final String username;
    
    private final String password;
    
    private final Promise<Object> authResultCallback;
    
    private ServerInfo serverInfo;
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof MySQLHandshakePacket) {
            MySQLHandshakePacket handshake = (MySQLHandshakePacket) msg;
            MySQLHandshakeResponse41Packet handshakeResponsePacket = new MySQLHandshakeResponse41Packet(1, MAX_PACKET_SIZE, CHARACTER_SET, username);
            handshakeResponsePacket.setAuthResponse(generateAuthResponse(handshake.getAuthPluginData().getAuthPluginData()));
            handshakeResponsePacket.setCapabilityFlags(generateClientCapability());
            handshakeResponsePacket.setDatabase("mysql");
            handshakeResponsePacket.setAuthPluginName(MySQLAuthenticationMethod.SECURE_PASSWORD_AUTHENTICATION);
            ctx.channel().writeAndFlush(handshakeResponsePacket);
            serverInfo = new ServerInfo();
            serverInfo.setServerVersion(new ServerVersion(handshake.getServerVersion()));
            return;
        }
        if (msg instanceof MySQLOKPacket) {
            ctx.channel().pipeline().remove(this);
            authResultCallback.setSuccess(serverInfo);
            return;
        }
        MySQLErrPacket error = (MySQLErrPacket) msg;
        ctx.channel().close();
        throw new RuntimeException(error.getErrorMessage());
    }
    
    private int generateClientCapability() {
        return MySQLCapabilityFlag.calculateCapabilityFlags(MySQLCapabilityFlag.CLIENT_LONG_PASSWORD, MySQLCapabilityFlag.CLIENT_LONG_FLAG,
            MySQLCapabilityFlag.CLIENT_PROTOCOL_41, MySQLCapabilityFlag.CLIENT_INTERACTIVE, MySQLCapabilityFlag.CLIENT_TRANSACTIONS,
            MySQLCapabilityFlag.CLIENT_SECURE_CONNECTION, MySQLCapabilityFlag.CLIENT_MULTI_STATEMENTS, MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH);
    }
    
    @SneakyThrows(NoSuchAlgorithmException.class)
    private byte[] generateAuthResponse(final byte[] authPluginData) {
        return (null == password || password.isEmpty()) ? new byte[0] : MySQLPasswordEncryptor.encryptWithMySQL41(password.getBytes(), authPluginData);
    }
}
