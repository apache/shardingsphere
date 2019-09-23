package info.avalon566.shardingscaling.sync.mysql.binlog.packet.command;

import com.google.common.base.Strings;
import info.avalon566.shardingscaling.sync.mysql.binlog.codec.DataTypesCodec;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.AbstractCommandPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;
import lombok.var;

/**
 * @author avalon566
 */
@Data
public class BinlogDumpCommandPacket extends AbstractCommandPacket {
    private static final int BINLOG_SEND_ANNOTATE_ROWS_EVENT = 2;
    private long binlogPosition;
    private int slaveServerId;
    private String binlogFileName;

    public BinlogDumpCommandPacket() {
        setCommand((byte) 0x12);
    }

    @Override
    public ByteBuf toByteBuf() {
        var out = ByteBufAllocator.DEFAULT.heapBuffer();
        DataTypesCodec.writeByte(getCommand(), out);
        DataTypesCodec.writeInt((int) binlogPosition, out);
        byte binlogFlags = 0;
        binlogFlags |= BINLOG_SEND_ANNOTATE_ROWS_EVENT;
        DataTypesCodec.writeByte(binlogFlags, out);
        DataTypesCodec.writeByte((byte) 0x00, out);
        DataTypesCodec.writeInt(this.slaveServerId, out);
        if (!Strings.isNullOrEmpty(this.binlogFileName)) {
            DataTypesCodec.writeBytes(this.binlogFileName.getBytes(), out);
        }
        return out;
    }
}
