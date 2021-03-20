package org.apache.shardingsphere.infra.executor.exec.meta;

import org.apache.calcite.rel.core.JoinRelType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;

public class JoinColumnMetaData implements QueryResultMetaData {
    
    private final QueryResultMetaData left;
    
    private final QueryResultMetaData right;
    
    private final JoinRelType joinRelType;
    
    public JoinColumnMetaData(final QueryResultMetaData left, final QueryResultMetaData right, final JoinRelType joinRelType) {
        this.left = left;
        this.right = right;
        this.joinRelType = joinRelType;
    }
    
    @Override
    public int getColumnCount() throws SQLException {
        return left.getColumnCount() + right.getColumnCount();
    }
    
    @Override
    public String getTableName(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().getTableName(entry.getKey());
    }
    
    @Override
    public String getColumnName(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().getColumnName(entry.getKey());
    }
    
    @Override
    public String getColumnLabel(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().getColumnLabel(entry.getKey());
    }
    
    @Override
    public int getColumnType(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().getColumnType(entry.getKey());
    }
    
    @Override
    public String getColumnTypeName(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().getColumnTypeName(entry.getKey());
    }
    
    @Override
    public int getColumnLength(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().getColumnLength(entry.getKey());
    }
    
    @Override
    public int getDecimals(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().getDecimals(entry.getKey());
    }
    
    @Override
    public boolean isSigned(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().isSigned(entry.getKey());
    }
    
    @Override
    public boolean isNotNull(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().isNotNull(entry.getKey());
    }
    
    @Override
    public boolean isAutoIncrement(final int columnIndex) throws SQLException {
        Map.Entry<Integer, QueryResultMetaData> entry = getColumnMeta(columnIndex);
        return entry.getValue().isAutoIncrement(entry.getKey());
    }
    
    private Map.Entry<Integer, QueryResultMetaData> getColumnMeta(final int columnIndex) throws SQLException {
        if (columnIndex <= left.getColumnCount()) {
            return new SimpleEntry<>(columnIndex, left);
        }
        return new SimpleEntry<>(columnIndex - left.getColumnCount(), right);
    }
}
