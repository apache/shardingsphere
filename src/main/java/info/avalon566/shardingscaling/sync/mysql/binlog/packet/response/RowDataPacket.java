package info.avalon566.shardingscaling.sync.mysql.binlog.packet.response;

import info.avalon566.shardingscaling.sync.mysql.binlog.codec.DataTypesCodec;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author avalon566
 */
@Data
public class RowDataPacket extends AbstractPacket {
    private List<String> columns = new ArrayList<>();

    @Override
    public void fromByteBuf(ByteBuf data) {
        while (data.isReadable()) {
            columns.add(DataTypesCodec.readLengthCodedString(data));
        }
    }
}
