package org.apache.shardingsphere.infra.executor.exec.result;

import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.stream.AbstractStreamQueryResult;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;

public abstract class BaseQueryResult extends AbstractStreamQueryResult {
    
    public BaseQueryResult(final QueryResultMetaData metaData) {
        super(metaData);
    }
    
    @Override
    public boolean next() throws SQLException {
        return false;
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        throw new UnsupportedOperationException("unsupported operation getCalendarValue of " + this.getClass().getName());
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        throw new UnsupportedOperationException("unsupported operation getInputStream of " + this.getClass().getName());
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return false;
    }
    
    
}
