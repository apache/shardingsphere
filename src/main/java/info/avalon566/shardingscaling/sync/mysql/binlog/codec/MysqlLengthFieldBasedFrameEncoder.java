package info.avalon566.shardingscaling.sync.mysql.binlog.codec;

import info.avalon566.shardingscaling.sync.mysql.binlog.packet.AbstractPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.HeaderPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author avalon566
 */
public class MysqlLengthFieldBasedFrameEncoder extends MessageToByteEncoder {

    private Logger LOGGER = LoggerFactory.getLogger(MysqlLengthFieldBasedFrameEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        var bb = ((AbstractPacket)msg).toByteBuf();
        HeaderPacket h = new HeaderPacket();
        h.setPacketBodyLength(bb.readableBytes());
        h.setPacketSequenceNumber(((AbstractPacket)msg).getSequenceNumber());
        out.writeBytes(h.toByteBuf());
        out.writeBytes(bb);
    }
}
