package info.avalon566.shardingscaling.sync.mysql.binlog.codec;

import info.avalon566.shardingscaling.sync.mysql.binlog.packet.binlog.EventHeader;
import lombok.extern.slf4j.Slf4j;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * MySQL binlog event packet decoder.
 *
 * @author avalon566
 * @author yangyi
 */
@Slf4j
public final class MySQLBinlogEventPacketDecoder extends ByteToMessageDecoder {
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        in.readByte();
        EventHeader eventHeader = new EventHeader();
        eventHeader.fromBytes(in);
        log.info(Byte.toString(eventHeader.getTypeCode()));
        log.info("readable:{},length:{}", in.readableBytes(), eventHeader.getEventLength() - 19);
        in.readBytes(eventHeader.getEventLength() - 19);
    }
}
