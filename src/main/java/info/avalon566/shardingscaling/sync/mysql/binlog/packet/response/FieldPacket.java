package info.avalon566.shardingscaling.sync.mysql.binlog.packet.response;

import info.avalon566.shardingscaling.sync.mysql.binlog.codec.DataTypesCodec;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * @author avalon566
 */
@Data
public class FieldPacket extends AbstractPacket {
    private String catalog;
    private String db;
    private String table;
    private String originalTable;
    private String name;
    private String originalName;
    private int character;
    private long length;
    private byte type;
    private int flags;
    private byte decimals;
    private String definition;

    @Override
    public void fromByteBuf(ByteBuf data) {
        catalog = DataTypesCodec.readLengthCodedString(data);
        db = DataTypesCodec.readLengthCodedString(data);
        table = DataTypesCodec.readLengthCodedString(data);
        originalTable = DataTypesCodec.readLengthCodedString(data);
        name = DataTypesCodec.readLengthCodedString(data);
        originalName = DataTypesCodec.readLengthCodedString(data);
        character = DataTypesCodec.readShort(data);
        length = DataTypesCodec.readInt(data);
        type = DataTypesCodec.readByte(data);
        flags = DataTypesCodec.readShort(data);
        decimals = DataTypesCodec.readByte(data);
        // fill
        data.readerIndex(data.readerIndex() + 2);
        definition = DataTypesCodec.readLengthCodedString(data);
    }
}
