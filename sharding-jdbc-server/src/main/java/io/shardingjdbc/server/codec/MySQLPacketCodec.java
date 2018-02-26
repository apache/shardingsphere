package io.shardingjdbc.server.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.shardingjdbc.server.packet.AbstractMySQLPacket;
import io.shardingjdbc.server.packet.MySQLPacketPayload;
import io.shardingjdbc.server.packet.AbstractMySQLSentPacket;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * MySQL packet codec.
 * 
 * @author zhangliang 
 */
@Slf4j
public final class MySQLPacketCodec extends ByteToMessageCodec<AbstractMySQLSentPacket> {
    
    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) throws Exception {
        int readableBytes = in.readableBytes();
        if (readableBytes < AbstractMySQLPacket.PAYLOAD_LENGTH) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Read from client: \n {}", ByteBufUtil.prettyHexDump(in));
        }
        int payloadLength = in.markReaderIndex().readMediumLE();
        if (readableBytes < payloadLength) {
            in.resetReaderIndex();
            return;
        }
        out.add(in);
    }
    
    @Override
    protected void encode(final ChannelHandlerContext context, final AbstractMySQLSentPacket message, final ByteBuf out) throws Exception {
        MySQLPacketPayload mysqlPacketPayload = new MySQLPacketPayload(context.alloc().buffer());
        message.write(mysqlPacketPayload);
        out.writeMediumLE(mysqlPacketPayload.getByteBuf().readableBytes());
        out.writeByte(message.getSequenceId());
        out.writeBytes(mysqlPacketPayload.getByteBuf());
        if (log.isDebugEnabled()) {
            log.debug("Write to client: \n {}", ByteBufUtil.prettyHexDump(out));
        }
    }
}
