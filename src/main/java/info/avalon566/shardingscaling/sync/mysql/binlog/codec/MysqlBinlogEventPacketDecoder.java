package info.avalon566.shardingscaling.sync.mysql.binlog.codec;

import info.avalon566.shardingscaling.sync.mysql.binlog.packet.binlog.EventHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author avalon566
 */
public class MysqlBinlogEventPacketDecoder extends ByteToMessageDecoder {

    private Logger LOGGER = LoggerFactory.getLogger(MysqlBinlogEventPacketDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List out) {
        in.readByte();
        EventHeader eventHeader = new EventHeader();
        eventHeader.fromBytes(in);
        LOGGER.info(Byte.toString(eventHeader.getTypeCode()));
        LOGGER.info("readable:{},length:{}", in.readableBytes(), eventHeader.getEventLength() - 19);
        in.readBytes(eventHeader.getEventLength() - 19);
    }
}
