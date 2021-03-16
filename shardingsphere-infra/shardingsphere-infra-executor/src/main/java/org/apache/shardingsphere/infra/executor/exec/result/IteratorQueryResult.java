package org.apache.shardingsphere.infra.executor.exec.result;

import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.stream.AbstractStreamQueryResult;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;

public class IteratorQueryResult extends AbstractStreamQueryResult implements QueryResult {
    
    public IteratorQueryResult(final QueryResultMetaData metaData) {
        super(metaData);
    }
    
    @Override
    public boolean next() throws SQLException {
        return false;
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        return null;
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        return null;
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        return null;
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return false;
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return null;
    }
}
