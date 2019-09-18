package info.avalon566.shardingscaling.sync.jdbc;

import lombok.Data;

/**
 * @author avalon566
 */
@Data
public class ColumnMetaData {
    private String columnName;
    private int columnType;
    private String columnTypeName;
}
