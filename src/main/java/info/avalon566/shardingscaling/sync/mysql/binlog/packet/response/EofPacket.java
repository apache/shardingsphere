package info.avalon566.shardingscaling.sync.mysql.binlog.packet.response;

import info.avalon566.shardingscaling.sync.mysql.binlog.codec.DataTypesCodec;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * @author avalon566
 */
@Data
public class EofPacket extends AbstractPacket {
    public byte fieldCount;
    public int warningCount;
    public int statusFlag;

    @Override
    public void fromByteBuf(ByteBuf data) {
        fieldCount = DataTypesCodec.readByte(data);
        warningCount = DataTypesCodec.readShort(data);
        this.statusFlag = DataTypesCodec.readShort(data);
    }
}
