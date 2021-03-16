package org.apache.shardingsphere.driver.jdbc.core.resultset;

import org.apache.shardingsphere.driver.jdbc.adapter.WrapperAdapter;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class SSResultSetMetaData extends WrapperAdapter implements ResultSetMetaData {
    
    private final QueryResultMetaData metaData;
    
    public SSResultSetMetaData(QueryResultMetaData metaData) {
        this.metaData = metaData;
    }
    
    @Override
    public int getColumnCount() throws SQLException {
        return metaData.getColumnCount();
    }
    
    @Override
    public boolean isAutoIncrement(final int i) throws SQLException {
        return metaData.isAutoIncrement(i);
    }
    
    @Override
    public boolean isCaseSensitive(final int i) throws SQLException {
        // TODO 
        return false;
    }
    
    @Override
    public boolean isSearchable(final int i) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isCurrency(final int i) throws SQLException {
        return false;
    }
    
    @Override
    public int isNullable(final int i) throws SQLException {
        return 0;
    }
    
    @Override
    public boolean isSigned(final int i) throws SQLException {
        return metaData.isSigned(i);
    }
    
    @Override
    public int getColumnDisplaySize(final int i) throws SQLException {
        return 0;
    }
    
    @Override
    public String getColumnLabel(final int i) throws SQLException {
        return metaData.getColumnLabel(i);
    }
    
    @Override
    public String getColumnName(final int i) throws SQLException {
        return metaData.getColumnName(i);
    }
    
    @Override
    public String getSchemaName(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public int getPrecision(final int i) throws SQLException {
        return 0;
    }
    
    @Override
    public int getScale(final int i) throws SQLException {
        return 0;
    }
    
    @Override
    public String getTableName(final int i) throws SQLException {
        return metaData.getTableName(i);
    }
    
    @Override
    public String getCatalogName(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public int getColumnType(final int i) throws SQLException {
        return metaData.getColumnType(i);
    }
    
    @Override
    public String getColumnTypeName(final int i) throws SQLException {
        return metaData.getColumnTypeName(i);
    }
    
    @Override
    public boolean isReadOnly(final int i) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isWritable(final int i) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isDefinitelyWritable(final int i) throws SQLException {
        return false;
    }
    
    @Override
    public String getColumnClassName(final int i) throws SQLException {
        // TODO 
        return metaData.getColumnTypeName(i);
    }
}
