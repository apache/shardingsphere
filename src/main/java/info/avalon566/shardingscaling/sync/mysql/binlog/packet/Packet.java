package info.avalon566.shardingscaling.sync.mysql.binlog.packet;

import io.netty.buffer.ByteBuf;

/**
 * @author avalon566
 */
public interface Packet {
    void fromByteBuf(ByteBuf data);

    ByteBuf toByteBuf();
}
