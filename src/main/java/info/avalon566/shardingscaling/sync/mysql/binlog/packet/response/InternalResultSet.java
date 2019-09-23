package info.avalon566.shardingscaling.sync.mysql.binlog.packet.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author avalon566
 */
@Data
public class InternalResultSet {
    private ResultSetHeaderPacket header;
    private List<FieldPacket> fieldDescriptors = new ArrayList<>();
    private List<RowDataPacket> fieldValues = new ArrayList<>();

    public InternalResultSet(ResultSetHeaderPacket header) {
        this.header = header;
    }
}