package io.shardingjdbc.core.merger.show;

import io.shardingjdbc.core.merger.ResultSetMerger;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Calendar;

/**
 * Show other result set merger.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ShowOtherResultSetMerger implements ResultSetMerger {
    
    private final ResultSet resultSet;
    
    @Override
    public boolean next() throws SQLException {
        return resultSet.next();
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        return resultSet.getObject(columnIndex);
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) throws SQLException {
        return resultSet.getObject(columnLabel);
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
