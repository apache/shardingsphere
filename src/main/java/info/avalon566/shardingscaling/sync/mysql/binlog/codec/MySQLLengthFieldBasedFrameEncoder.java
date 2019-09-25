package info.avalon566.shardingscaling.sync.mysql.binlog.codec;

import info.avalon566.shardingscaling.sync.mysql.binlog.packet.AbstractPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.HeaderPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

/**
 * MySQL length field based frame encoder.
 *
 * @author avalon566
 * @author yangyi
 */
@Slf4j
public final class MySQLLengthFieldBasedFrameEncoder extends MessageToByteEncoder {
    
    @Override
    protected void encode(final ChannelHandlerContext ctx, final Object msg, final ByteBuf out) {
        var bb = ((AbstractPacket)msg).toByteBuf();
        HeaderPacket h = new HeaderPacket();
        h.setPacketBodyLength(bb.readableBytes());
        h.setPacketSequenceNumber(((AbstractPacket)msg).getSequenceNumber());
        out.writeBytes(h.toByteBuf());
        out.writeBytes(bb);
    }
}
