package info.avalon566.shardingscaling.sync.mysql.binlog.packet.response;

import info.avalon566.shardingscaling.sync.mysql.binlog.codec.DataTypesCodec;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * @author avalon566
 */
@Data
public class ResultSetHeaderPacket extends AbstractPacket {
    private long columnCount;
    private long extra;
    
    @Override
    public void fromByteBuf(ByteBuf data) {
        columnCount = DataTypesCodec.readLengthCoded(data);
        if (0 < data.readableBytes()) {
            extra = DataTypesCodec.readLengthCoded(data);
        }
    }
}
