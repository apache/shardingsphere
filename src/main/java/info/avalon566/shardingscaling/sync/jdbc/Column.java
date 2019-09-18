package info.avalon566.shardingscaling.sync.jdbc;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author avalon566
 */
@Data
@AllArgsConstructor
public class Column {
    private Object value;
    private boolean updated;
}