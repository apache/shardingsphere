package info.avalon566.shardingscaling.sync.mysql.binlog.packet.command;

import info.avalon566.shardingscaling.sync.mysql.binlog.codec.DataTypesCodec;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.AbstractCommandPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;
import lombok.var;

import java.io.UnsupportedEncodingException;

/**
 * @author avalon566
 */
@Data
public class QueryCommandPacket extends AbstractCommandPacket {

    private String queryString;

    public QueryCommandPacket() {
        setCommand((byte) 0x03);
    }

    @Override
    public ByteBuf toByteBuf() {
        var out = ByteBufAllocator.DEFAULT.heapBuffer();
        DataTypesCodec.writeByte(getCommand(), out);
        try {
            DataTypesCodec.writeBytes(getQueryString().getBytes("UTF-8"), out);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return out;
    }
}
