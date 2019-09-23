package info.avalon566.shardingscaling.sync.mysql.binlog.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * @author avalon566
 * implement fromByteBuf and toByteBuf in here is a bad design
 * but we no need to implement both of method in this project
 */
@Data
public abstract class AbstractPacket implements Packet {
    private byte sequenceNumber;

    @Override
    public void fromByteBuf(ByteBuf data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteBuf toByteBuf() {
        throw new UnsupportedOperationException();
    }
}
