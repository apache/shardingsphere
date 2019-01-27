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

package org.apache.shardingsphere.shardingproxy.frontend.postgresql;

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingproxy.frontend.common.FrontendHandler;
import org.apache.shardingsphere.shardingproxy.frontend.common.executor.CommandExecutorSelector;
import org.apache.shardingsphere.shardingproxy.frontend.mysql.CommandExecutor;
import org.apache.shardingsphere.shardingproxy.runtime.ChannelRegistry;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.generic.ReadyForQuery;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.handshake.AuthenticationOK;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.handshake.PostgreSQLConnectionIdGenerator;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.handshake.SSLNegative;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.handshake.StartupMessage;

/**
 * PostgreSQL frontend handler.
 *
 * @author zhangyonglun
 */
@RequiredArgsConstructor
public final class PostgreSQLFrontendHandler extends FrontendHandler {
    
    @Override
    protected void handshake(final ChannelHandlerContext context) {
        int connectionId = PostgreSQLConnectionIdGenerator.getInstance().nextId();
        ChannelRegistry.getInstance().putConnectionId(context.channel().id().asShortText(), connectionId);
        getBackendConnection().setConnectionId(connectionId);
    }
    
    @Override
    protected void auth(final ChannelHandlerContext context, final ByteBuf message) {
        if (8 == message.markReaderIndex().readInt() && 80877103 == message.readInt()) {
            setAuthorized(false);
            context.writeAndFlush(new SSLNegative());
            return;
        }
        message.resetReaderIndex();
        try (PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(message)) {
            StartupMessage startupMessage = new StartupMessage(payload);
            String databaseName = startupMessage.getParametersMap().get("database");
            if (!Strings.isNullOrEmpty(databaseName) && !GlobalRegistry.getInstance().schemaExists(databaseName)) {
                // TODO send an error message
                return;
            }
            getBackendConnection().setCurrentSchema(databaseName);
            // TODO send a md5 authentication request message
            context.writeAndFlush(new AuthenticationOK(true));
            context.writeAndFlush(new ReadyForQuery());
        }
    }
    
    @Override
    protected void executeCommand(final ChannelHandlerContext context, final ByteBuf message) {
        CommandExecutorSelector.getExecutor(getBackendConnection().getTransactionType(), context.channel().id()).execute(new CommandExecutor(context, message, this));
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
