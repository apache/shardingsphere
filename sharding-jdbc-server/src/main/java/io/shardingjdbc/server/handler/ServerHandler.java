package io.shardingjdbc.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.shardingjdbc.server.constant.StatusFlag;
import io.shardingjdbc.server.packet.MySQLPacketPayload;
import io.shardingjdbc.server.packet.MySQLSentPacket;
import io.shardingjdbc.server.packet.command.CommandPacket;
import io.shardingjdbc.server.packet.command.CommandPacketFactory;
import io.shardingjdbc.server.packet.handshake.AuthPluginData;
import io.shardingjdbc.server.packet.handshake.ConnectionIdGenerator;
import io.shardingjdbc.server.packet.handshake.HandshakePacket;
import io.shardingjdbc.server.packet.handshake.HandshakeResponse41Packet;
import io.shardingjdbc.server.packet.ok.OKPacket;

/**
 * Server handler.
 * 
 * @author zhangliang 
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {
    
    private AuthPluginData authPluginData;
    
    private boolean authorized;
    
    @Override
    public void channelActive(final ChannelHandlerContext context) throws Exception {
        authPluginData = new AuthPluginData();
        context.writeAndFlush(new HandshakePacket(ConnectionIdGenerator.getInstance().nextId(), authPluginData));
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) throws Exception {
        MySQLPacketPayload mysqlPacketPayload = new MySQLPacketPayload((ByteBuf) message);
        if (!authorized) {
            auth(context, mysqlPacketPayload);
        } else {
            executeCommand(context, mysqlPacketPayload);
        }
    }
    
    private void auth(final ChannelHandlerContext context, final MySQLPacketPayload mysqlPacketPayload) {
        HandshakeResponse41Packet response41 = new HandshakeResponse41Packet().read(mysqlPacketPayload);
        // TODO use authPluginData to auth
        authorized = true;
        context.writeAndFlush(new OKPacket(response41.getSequenceId() + 1, 0L, 0L, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
    }
    
    private void executeCommand(final ChannelHandlerContext context, final MySQLPacketPayload mysqlPacketPayload) {
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
