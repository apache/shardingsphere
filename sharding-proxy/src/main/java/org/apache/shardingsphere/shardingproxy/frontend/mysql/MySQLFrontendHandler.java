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

package org.apache.shardingsphere.shardingproxy.frontend.mysql;

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingproxy.frontend.common.FrontendHandler;
import org.apache.shardingsphere.shardingproxy.frontend.common.executor.CommandExecutorSelector;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.handshake.MySQLAuthenticationHandler;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.handshake.MySQLConnectionIdGenerator;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.handshake.MySQLHandshakePacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.handshake.MySQLHandshakeResponse41Packet;

/**
 * MySQL frontend handler.
 *
 * @author zhangliang
 * @author panjuan
 * @author wangkai
 * @author zhangyonglun
 */
@RequiredArgsConstructor
public final class MySQLFrontendHandler extends FrontendHandler {
    
    private final MySQLAuthenticationHandler mySQLAuthenticationHandler = new MySQLAuthenticationHandler();
    
    @Override
    protected void handshake(final ChannelHandlerContext context) {
        int connectionId = MySQLConnectionIdGenerator.getInstance().nextId();
        getBackendConnection().setConnectionId(connectionId);
        context.writeAndFlush(new MySQLHandshakePacket(connectionId, mySQLAuthenticationHandler.getMySQLAuthPluginData()));
    }
    
    @Override
    protected void auth(final ChannelHandlerContext context, final ByteBuf message) {
        try (MySQLPacketPayload payload = new MySQLPacketPayload(message)) {
            MySQLHandshakeResponse41Packet response41 = new MySQLHandshakeResponse41Packet(payload);
            if (mySQLAuthenticationHandler.login(response41.getUsername(), response41.getAuthResponse())) {
                if (!Strings.isNullOrEmpty(response41.getDatabase()) && !GlobalRegistry.getInstance().schemaExists(response41.getDatabase())) {
                    context.writeAndFlush(new MySQLErrPacket(response41.getSequenceId() + 1, MySQLServerErrorCode.ER_BAD_DB_ERROR, response41.getDatabase()));
                    return;
                }
                getBackendConnection().setCurrentSchema(response41.getDatabase());
                context.writeAndFlush(new MySQLOKPacket(response41.getSequenceId() + 1));
            } else {
                // TODO localhost should replace to real ip address
                context.writeAndFlush(new MySQLErrPacket(response41.getSequenceId() + 1,
                    MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR, response41.getUsername(), "localhost", 0 == response41.getAuthResponse().length ? "NO" : "YES"));
            }
        }
    }
    
    @Override
    protected void executeCommand(final ChannelHandlerContext context, final ByteBuf message) {
        CommandExecutorSelector.getExecutor(getBackendConnection().getTransactionType(), context.channel().id()).execute(new MySQLCommandExecutor(context, message, this));
    }
    
    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext context) {
        if (context.channel().isWritable()) {
            synchronized (this) {
                this.notifyAll();
            }
        }
    }
}
