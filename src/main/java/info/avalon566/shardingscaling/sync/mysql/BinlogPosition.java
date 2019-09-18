package info.avalon566.shardingscaling.sync.mysql;

import lombok.Data;

/**
 * @author avalon566
 */
@Data
public class BinlogPosition {
    private String serverId;
    private String filename;
    private long position;
}
