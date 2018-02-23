package io.shardingjdbc.core.merger.show;

import io.shardingjdbc.core.merger.ResultSetMerger;

import java.io.InputStream;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Calendar;

/**
 * Show databases result set merger.
 *
 * @author zhangliang
 */
public final class ShowDatabasesResultSetMerger implements ResultSetMerger {
    
    private static final String LOGIC_DATABASE_NAME = "sharding_db";
    
    private boolean firstNext = true;
    
    @Override
    public boolean next() throws SQLException {
        if (firstNext) {
            firstNext = false;
            return true;
        }
        return false;
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        return LOGIC_DATABASE_NAME;
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) throws SQLException {
        return LOGIC_DATABASE_NAME;
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public InputStream getInputStream(final String columnLabel, final String type) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return false;
    }
}
