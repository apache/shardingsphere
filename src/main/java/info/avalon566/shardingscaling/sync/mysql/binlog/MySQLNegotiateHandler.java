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

package info.avalon566.shardingscaling.sync.mysql.binlog;

import info.avalon566.shardingscaling.sync.mysql.binlog.packet.auth.ClientAuthenticationPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.auth.HandshakeInitializationPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.response.ErrorPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.response.OkPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;
import lombok.var;

/**
 * MySQL Negotiate Handler.
 *
 * @author avalon566
 * @author yangyi
 */
public final class MySQLNegotiateHandler extends ChannelInboundHandlerAdapter {

    private final String username;
    
    private final String password;
    
    private final Promise<Object> authResultCallback;

    private ServerInfo serverInfo;

    public MySQLNegotiateHandler(final String username, final String password, final Promise<Object> authResultCallback) {
        this.username = username;
        this.password = password;
        this.authResultCallback = authResultCallback;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof HandshakeInitializationPacket) {
            var handshake = (HandshakeInitializationPacket) msg;
            var clientAuth = new ClientAuthenticationPacket();
            clientAuth.setSequenceNumber((byte) (handshake.getSequenceNumber() + 1));
            clientAuth.setCharsetNumber((byte) 33);
            clientAuth.setUsername(username);
            clientAuth.setPassword(password);
            clientAuth.setServerCapabilities(handshake.getServerCapabilities());
            // use default database
            clientAuth.setDatabaseName("mysql");
            clientAuth.setScrumbleBuff(joinAndCreateScrumbleBuff(handshake));
            clientAuth.setAuthPluginName(handshake.getAuthPluginName());
            ctx.channel().writeAndFlush(clientAuth);
            serverInfo = new ServerInfo();
            serverInfo.setServerVersion(new ServerInfo.ServerVersion(handshake.getServerVersion()));
            return;
        }
        if (msg instanceof OkPacket) {
            ctx.channel().pipeline().remove(this);
            authResultCallback.setSuccess(serverInfo);
            return;
        }
        var error = (ErrorPacket) msg;
        ctx.channel().close();
        throw new RuntimeException(error.getMessage());
    }

    private byte[] joinAndCreateScrumbleBuff(final HandshakeInitializationPacket handshakePacket) {
        byte[] result = new byte[handshakePacket.getScramble().length + handshakePacket.getRestOfScramble().length];
        System.arraycopy(handshakePacket.getScramble(), 0, result, 0, handshakePacket.getScramble().length);
        System.arraycopy(handshakePacket.getRestOfScramble(),
                0, result, handshakePacket.getScramble().length,
                handshakePacket.getRestOfScramble().length);
        return result;
    }
}
