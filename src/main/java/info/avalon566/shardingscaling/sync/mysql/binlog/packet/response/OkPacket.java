package info.avalon566.shardingscaling.sync.mysql.binlog.packet.response;

import info.avalon566.shardingscaling.sync.mysql.binlog.codec.DataTypesCodec;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * @author avalon566
 */
@Data
public class OkPacket extends AbstractPacket {
    private byte fieldCount;
    private long affectedRows;
    private long insertId;
    private int serverStatus;
    private int warningCount;
    private String message;

    @Override
    public void fromByteBuf(ByteBuf data) {
        this.fieldCount =  DataTypesCodec.readByte(data);
        this.affectedRows = DataTypesCodec.readLengthCoded(data);
        this.insertId = DataTypesCodec.readLengthCoded(data);
        this.serverStatus = DataTypesCodec.readShort(data);
        this.warningCount = DataTypesCodec.readShort(data);
        this.message = new String(DataTypesCodec.readBytes(data.readableBytes(), data));
    }
}
