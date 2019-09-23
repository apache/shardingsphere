package info.avalon566.shardingscaling.sync.mysql.binlog.packet.response;

import info.avalon566.shardingscaling.sync.mysql.binlog.codec.DataTypesCodec;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * @author avalon566
 */
@Data
public class ErrorPacket extends AbstractPacket {
    private byte fieldCount;
    private int errorNumber;
    private byte sqlStateMarker;
    private byte[] sqlState;
    private String message;

    @Override
    public void fromByteBuf(ByteBuf data) {
        this.fieldCount = DataTypesCodec.readByte(data);
        this.errorNumber = DataTypesCodec.readShort(data);
        this.sqlStateMarker = DataTypesCodec.readByte(data);
        this.sqlState = DataTypesCodec.readBytes(5, data);
        this.message = new String(DataTypesCodec.readBytes(data.readableBytes(), data));
    }
}
