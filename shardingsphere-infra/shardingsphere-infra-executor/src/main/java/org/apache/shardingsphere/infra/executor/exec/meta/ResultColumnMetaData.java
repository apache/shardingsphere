package org.apache.shardingsphere.infra.executor.exec.meta;

import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;

import java.sql.SQLException;
import java.util.List;

public class ResultColumnMetaData implements QueryResultMetaData {
    
    private final List<ColumnMetaData> columnMetaData;
    
    public ResultColumnMetaData(final List<ColumnMetaData> columnMetaData) {
        this.columnMetaData = columnMetaData;
    }
    
    @Override
    public int getColumnCount() throws SQLException {
        return columnMetaData.size();
    }
    
    @Override
    public String getTableName(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public String getColumnName(final int columnIndex) throws SQLException {
        return getColumnMetaData(columnIndex).getName();
    }
    
    @Override
    public String getColumnLabel(final int columnIndex) throws SQLException {
        return getColumnName(columnIndex);
    }
    
    @Override
    public int getColumnType(final int columnIndex) throws SQLException {
        return getColumnMetaData(columnIndex).getDataType();
    }
    
    @Override
    public String getColumnTypeName(final int columnIndex) throws SQLException {
        // TODO 
        int dataType = getColumnMetaData(columnIndex).getDataType();
        return null;
    }
    
    @Override
    public int getColumnLength(final int columnIndex) throws SQLException {
        return 0;
    }
    
    @Override
    public int getDecimals(final int columnIndex) throws SQLException {
        return 0;
    }
    
    @Override
    public boolean isSigned(final int columnIndex) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isNotNull(final int columnIndex) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isAutoIncrement(final int columnIndex) throws SQLException {
        return false;
    }
    
    private ColumnMetaData getColumnMetaData(final int columnIndex) {
        return columnMetaData.get(columnIndex - 1);
    }
}
