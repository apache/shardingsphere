package info.avalon566.shardingscaling.sync.mysql.binlog.packet.command;

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
public class RegisterSlaveCommandPacket extends AbstractCommandPacket {
    public String reportHost;
    public short reportPort;
    public String reportUser;
    public String reportPasswd;
    public int serverId;

    public RegisterSlaveCommandPacket() {
        setCommand((byte) 0x15);
    }

    @Override
    public ByteBuf toByteBuf() {
        var out = ByteBufAllocator.DEFAULT.heapBuffer();
        DataTypesCodec.writeByte(getCommand(), out);
        DataTypesCodec.writeInt(serverId, out);
        DataTypesCodec.writeByte((byte) reportHost.getBytes().length, out);
        DataTypesCodec.writeBytes(reportHost.getBytes(), out);
        DataTypesCodec.writeByte((byte) reportUser.getBytes().length, out);
        DataTypesCodec.writeBytes(reportUser.getBytes(), out);
        DataTypesCodec.writeByte((byte) reportPasswd.getBytes().length, out);
        DataTypesCodec.writeBytes(reportPasswd.getBytes(), out);
        DataTypesCodec.writeShort(reportPort, out);
        DataTypesCodec.writeInt(0, out);
        DataTypesCodec.writeInt(0, out);
        return out;
    }
}
