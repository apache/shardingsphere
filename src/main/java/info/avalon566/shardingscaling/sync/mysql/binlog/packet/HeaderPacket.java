package info.avalon566.shardingscaling.sync.mysql.binlog.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;
import lombok.var;

/**
 * @author avalon566
 */
@Data
public class HeaderPacket implements Packet {

    private int  packetBodyLength;
    private byte packetSequenceNumber;

    @Override
    public ByteBuf toByteBuf() {
        var data = ByteBufAllocator.DEFAULT.heapBuffer(4);
        data.writeByte((byte)(packetBodyLength & 0xFF));
        data.writeByte((byte)(packetBodyLength >>> 8));
        data.writeByte((byte)(packetBodyLength >>> 16));
        data.writeByte((byte)getPacketSequenceNumber());
        return data;
    }

    @Override
    public void fromByteBuf(ByteBuf data) {
        var buffer = new byte[4];
        buffer[0] = data.readByte();
        buffer[1] = data.readByte();
        buffer[2] = data.readByte();
        buffer[3] = data.readByte();
        this.packetBodyLength = (buffer[0] & 0xFF) | ((buffer[1] & 0xFF) << 8) | ((buffer[2] & 0xFF) << 16);
        this.setPacketSequenceNumber(buffer[3]);
    }
}
