package info.avalon566.shardingscaling.sync.mysql.binlog.packet;

import lombok.Data;

/**
 * @author avalon566
 */
@Data
public abstract class AbstractCommandPacket extends AbstractPacket {
    private byte command;
    public AbstractCommandPacket() {
        setSequenceNumber((byte) 0);
    }
}
