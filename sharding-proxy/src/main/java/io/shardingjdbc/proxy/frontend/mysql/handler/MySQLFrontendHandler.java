/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.frontend.mysql.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.shardingjdbc.proxy.constant.StatusFlag;
import io.shardingjdbc.proxy.frontend.common.handler.FrontendHandler;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLSentPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.command.CommandPacketFactory;
import io.shardingjdbc.proxy.transport.mysql.packet.handshake.AuthPluginData;
import io.shardingjdbc.proxy.transport.mysql.packet.handshake.ConnectionIdGenerator;
import io.shardingjdbc.proxy.transport.mysql.packet.handshake.HandshakePacket;
import io.shardingjdbc.proxy.transport.mysql.packet.handshake.HandshakeResponse41Packet;
import io.shardingjdbc.proxy.transport.mysql.packet.generic.OKPacket;

/**
 * MySQL frontend handler.
 * 
 * @author zhangliang 
 */
public final class MySQLFrontendHandler extends FrontendHandler {
    
    private AuthPluginData authPluginData;
    
    @Override
    protected void handshake(final ChannelHandlerContext context) {
        authPluginData = new AuthPluginData();
        context.writeAndFlush(new HandshakePacket(ConnectionIdGenerator.getInstance().nextId(), authPluginData));
    }
    
    @Override
    protected void auth(final ChannelHandlerContext context, final ByteBuf message) {
        MySQLPacketPayload mysqlPacketPayload = new MySQLPacketPayload(message);
        // TODO use authPluginData to auth
        HandshakeResponse41Packet response41 = new HandshakeResponse41Packet().read(mysqlPacketPayload);
        context.writeAndFlush(new OKPacket(response41.getSequenceId() + 1, 0L, 0L, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
    }
    
    @Override
    protected void executeCommand(final ChannelHandlerContext context, final ByteBuf message) {
        MySQLPacketPayload mysqlPacketPayload = new MySQLPacketPayload(message);
        int sequenceId = mysqlPacketPayload.readInt1();
        CommandPacket commandPacket = CommandPacketFactory.getCommandPacket(mysqlPacketPayload.readInt1());
        commandPacket.setSequenceId(sequenceId);
        commandPacket.read(mysqlPacketPayload);
        for (MySQLSentPacket each : commandPacket.execute()) {
            context.write(each);
        }
        context.flush();
    }
}
