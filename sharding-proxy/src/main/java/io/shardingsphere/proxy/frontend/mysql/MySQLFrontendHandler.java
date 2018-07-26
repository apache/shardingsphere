/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.frontend.mysql;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.shardingsphere.proxy.backend.common.jdbc.BackendConnection;
import io.shardingsphere.proxy.frontend.common.FrontendHandler;
import io.shardingsphere.proxy.frontend.common.executor.ExecutorGroup;
import io.shardingsphere.proxy.transport.common.packet.DatabasePacket;
import io.shardingsphere.proxy.transport.mysql.constant.ServerErrorCode;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacketFactory;
import io.shardingsphere.proxy.transport.mysql.packet.command.QueryCommandPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.reponse.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import io.shardingsphere.proxy.transport.mysql.packet.handshake.AuthorityHandler;
import io.shardingsphere.proxy.transport.mysql.packet.handshake.ConnectionIdGenerator;
import io.shardingsphere.proxy.transport.mysql.packet.handshake.HandshakePacket;
import io.shardingsphere.proxy.transport.mysql.packet.handshake.HandshakeResponse41Packet;
import io.shardingsphere.proxy.util.MySQLResultCache;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

/**
 * MySQL frontend handler.
 *
 * @author zhangliang
 * @author panjuan
 * @author wangkai
 */
@RequiredArgsConstructor
public final class MySQLFrontendHandler extends FrontendHandler {
    
    private final AuthorityHandler authorityHandler = new AuthorityHandler();
    
    @Override
    protected void handshake(final ChannelHandlerContext context) {
        int connectionId = ConnectionIdGenerator.getInstance().nextId();
        MySQLResultCache.getInstance().putConnection(context.channel().id().asShortText(), connectionId);
        context.writeAndFlush(new HandshakePacket(connectionId, authorityHandler.getAuthPluginData()));
    }
    
    @Override
    protected void auth(final ChannelHandlerContext context, final ByteBuf message) {
        try (MySQLPacketPayload payload = new MySQLPacketPayload(message)) {
            HandshakeResponse41Packet response41 = new HandshakeResponse41Packet(payload);
            if (authorityHandler.login(response41.getUsername(), response41.getAuthResponse())) {
                context.writeAndFlush(new OKPacket(response41.getSequenceId() + 1));
            } else {
                // TODO localhost should replace to real ip address
                context.writeAndFlush(new ErrPacket(response41.getSequenceId() + 1, 
                        ServerErrorCode.ER_ACCESS_DENIED_ERROR, response41.getUsername(), "localhost", 0 == response41.getAuthResponse().length ? "NO" : "YES"));
            }
        }
    }
    
    @Override
    protected void executeCommand(final ChannelHandlerContext context, final ByteBuf message) {
        new ExecutorGroup(context.channel().id()).getExecutorService().execute(new CommandExecutor(context, message));
    }
    
    @RequiredArgsConstructor
    static class CommandExecutor implements Runnable {
        
        private final ChannelHandlerContext context;
        
        private final ByteBuf message;
        
        private int currentSequenceId;
        
        @Override
        public void run() {
            try (MySQLPacketPayload payload = new MySQLPacketPayload(message);
                 BackendConnection backendConnection = new BackendConnection()) {
                CommandPacket commandPacket = getCommandPacket(payload, backendConnection);
                CommandResponsePackets responsePackets = commandPacket.execute();
                for (DatabasePacket each : responsePackets.getPackets()) {
                    context.writeAndFlush(each);
                }
                if (commandPacket instanceof QueryCommandPacket && !(responsePackets.getHeadPacket() instanceof OKPacket) && !(responsePackets.getHeadPacket() instanceof ErrPacket)) {
                    writeMoreResults((QueryCommandPacket) commandPacket, responsePackets.getPackets().size());
                }
            } catch (final SQLException ex) {
                context.writeAndFlush(new ErrPacket(++currentSequenceId, ex));
            }
        }
        
        private CommandPacket getCommandPacket(final MySQLPacketPayload payload, final BackendConnection backendConnection) {
            int sequenceId = payload.readInt1();
            int connectionId = MySQLResultCache.getInstance().getConnection(context.channel().id().asShortText());
            return CommandPacketFactory.getCommandPacket(sequenceId, connectionId, payload, backendConnection);
        }
        
        private void writeMoreResults(final QueryCommandPacket queryCommandPacket, final int headPacketsCount) throws SQLException {
            currentSequenceId = headPacketsCount;
            while (queryCommandPacket.next()) {
                // TODO try to use wait notify
                while (!context.channel().isWritable()) {
                    continue;
                }
                DatabasePacket resultValue = queryCommandPacket.getResultValue();
                currentSequenceId = resultValue.getSequenceId();
                context.writeAndFlush(resultValue);
            }
            context.writeAndFlush(new EofPacket(++currentSequenceId));
        }
    }
}
