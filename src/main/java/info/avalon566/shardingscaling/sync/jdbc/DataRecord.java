package info.avalon566.shardingscaling.sync.jdbc;

import info.avalon566.shardingscaling.sync.core.Record;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author avalon566
 */
@Data
public class DataRecord implements Record {
    private String type;
    private String tableName;
    private String fullTableName;
    private final List<Column> columns;

    public DataRecord(int columnCount) {
        columns = new ArrayList<Column>(columnCount);
    }

    public void addColumn(Column data) {
        columns.add(data);
    }

    public int getColumnCount() {
        return columns.size();
    }

    public Column getColumn(int index) {
        return columns.get(index);
    }

    public String getTableName() {
        return fullTableName.split("\\.")[1];
    }
}