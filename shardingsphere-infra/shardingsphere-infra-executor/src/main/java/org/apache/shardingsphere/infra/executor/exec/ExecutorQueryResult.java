package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;

public class ExecutorQueryResult implements QueryResult {
    
    private Executor executor;
    
    @Override
    public boolean next() throws SQLException {
        return executor.moveNext();
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        // TODO 
        return executor.current();
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        throw new UnsupportedOperationException("getCalendarValue");
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        throw new UnsupportedOperationException("getCalendarValue");
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return false;
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return this.executor.getMetaData();
    }
    
    @Override
    public void close() throws SQLException {
        executor.close();
    }
}
