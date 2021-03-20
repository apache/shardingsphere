package org.apache.shardingsphere.driver.jdbc.core.resultset;

import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationResultSet;
import org.apache.shardingsphere.infra.executor.exec.ExecContext;
import org.apache.shardingsphere.infra.executor.exec.Executor;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

public class SSResultSet extends AbstractUnsupportedOperationResultSet {
    
    private final QueryResultMetaData metaData;
    
    private final Executor executor;
    
    private final Statement statement;
    
    private final ExecContext execContext;
    
    private final Map<String, Integer> columnLabelAndIndexMap;
    
    private Row current;
    
    public SSResultSet(Executor executor, QueryResultMetaData metaData, Statement statement, ExecContext execContext) throws SQLException {
        this.executor = executor;
        this.metaData = metaData;
        this.statement = statement;
        this.execContext = execContext;
        this.columnLabelAndIndexMap = createColumnLabelAndIndexMap(metaData);
    }
    
    private Map<String, Integer> createColumnLabelAndIndexMap(final QueryResultMetaData resultSetMetaData) throws SQLException {
        Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int columnIndex = resultSetMetaData.getColumnCount(); columnIndex > 0; columnIndex--) {
            result.put(resultSetMetaData.getColumnLabel(columnIndex), columnIndex);
        }
        return result;
    }
    
    @Override
    public boolean next() throws SQLException {
        if (executor.moveNext()) {
            current = executor.current();
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public void close() throws SQLException {
        executor.close();
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        // TODO 
        return false;
    }
    
    @Override
    public String getString(final int i) throws SQLException {
        return (String) ResultSetUtil.convertValue(getColumnValue(i), String.class);
    }
    
    @Override
    public boolean getBoolean(final int i) throws SQLException {
        return (boolean) ResultSetUtil.convertValue(getColumnValue(i), boolean.class);
    }
    
    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        return getBoolean(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public byte getByte(final int i) throws SQLException {
        return (byte) ResultSetUtil.convertValue(getColumnValue(i), byte.class);
    }
    
    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        return getByte(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public short getShort(final int i) throws SQLException {
        return (short) ResultSetUtil.convertValue(getColumnValue(i), short.class);
    }
    
    @Override
    public short getShort(final String columnLabel) throws SQLException {
        return getShort(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public int getInt(final int i) throws SQLException {
        return 0;
    }
    
    @Override
    public long getLong(final int i) throws SQLException {
        return 0;
    }
    
    @Override
    public float getFloat(final int i) throws SQLException {
        return 0;
    }
    
    @Override
    public double getDouble(final int i) throws SQLException {
        return 0;
    }
    
    @Override
    public BigDecimal getBigDecimal(final int i, final int i1) throws SQLException {
        return null;
    }
    
    @Override
    public byte[] getBytes(final int i) throws SQLException {
        return new byte[0];
    }
    
    @Override
    public Date getDate(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public Time getTime(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public Timestamp getTimestamp(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public InputStream getAsciiStream(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public InputStream getUnicodeStream(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public InputStream getBinaryStream(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public String getString(final String s) throws SQLException {
        return null;
    }
    
    @Override
    public int getInt(final String s) throws SQLException {
        return 0;
    }
    
    @Override
    public long getLong(final String s) throws SQLException {
        return 0;
    }
    
    @Override
    public float getFloat(final String s) throws SQLException {
        return 0;
    }
    
    @Override
    public double getDouble(final String s) throws SQLException {
        return 0;
    }
    
    @Override
    public BigDecimal getBigDecimal(final String s, final int i) throws SQLException {
        return null;
    }
    
    @Override
    public byte[] getBytes(final String s) throws SQLException {
        return new byte[0];
    }
    
    @Override
    public Date getDate(final String s) throws SQLException {
        return null;
    }
    
    @Override
    public Time getTime(final String s) throws SQLException {
        return null;
    }
    
    @Override
    public Timestamp getTimestamp(final String s) throws SQLException {
        return null;
    }
    
    @Override
    public InputStream getAsciiStream(final String s) throws SQLException {
        return null;
    }
    
    @Override
    public InputStream getUnicodeStream(final String s) throws SQLException {
        return null;
    }
    
    @Override
    public InputStream getBinaryStream(final String s) throws SQLException {
        return null;
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        
    }
    
    @Override
    public Object getObject(final int i) throws SQLException {
        return ResultSetUtil.convertValue(getColumnValue(i), Object.class);
    }
    
    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        return getObject(getIndexFromColumnLabelAndIndexMap(columnLabel));
    }
    
    @Override
    public int findColumn(final String s) throws SQLException {
        return 0;
    }
    
    @Override
    public Reader getCharacterStream(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public Reader getCharacterStream(final String s) throws SQLException {
        return null;
    }
    
    @Override
    public BigDecimal getBigDecimal(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public BigDecimal getBigDecimal(final String s) throws SQLException {
        return null;
    }
    
    @Override
    public void setFetchDirection(final int i) throws SQLException {
        
    }
    
    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }
    
    @Override
    public void setFetchSize(final int i) throws SQLException {
        
    }
    
    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }
    
    @Override
    public int getType() throws SQLException {
        return 0;
    }
    
    @Override
    public int getConcurrency() throws SQLException {
        return 0;
    }
    
    @Override
    public Statement getStatement() throws SQLException {
        return null;
    }
    
    @Override
    public Blob getBlob(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public Clob getClob(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public Array getArray(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public Blob getBlob(final String s) throws SQLException {
        return null;
    }
    
    @Override
    public Clob getClob(final String s) throws SQLException {
        return null;
    }
    
    @Override
    public Array getArray(final String s) throws SQLException {
        return null;
    }
    
    @Override
    public Date getDate(final int i, final Calendar calendar) throws SQLException {
        return null;
    }
    
    @Override
    public Date getDate(final String s, final Calendar calendar) throws SQLException {
        return null;
    }
    
    @Override
    public Time getTime(final int i, final Calendar calendar) throws SQLException {
        return null;
    }
    
    @Override
    public Time getTime(final String s, final Calendar calendar) throws SQLException {
        return null;
    }
    
    @Override
    public Timestamp getTimestamp(final int i, final Calendar calendar) throws SQLException {
        return null;
    }
    
    @Override
    public Timestamp getTimestamp(final String s, final Calendar calendar) throws SQLException {
        return null;
    }
    
    @Override
    public URL getURL(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public URL getURL(final String s) throws SQLException {
        return null;
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }
    
    @Override
    public SQLXML getSQLXML(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public SQLXML getSQLXML(final String s) throws SQLException {
        return null;
    }
    
    @Override
    public String getNString(final int i) throws SQLException {
        return null;
    }
    
    @Override
    public String getNString(final String s) throws SQLException {
        return null;
    }
    
    
    private <T> T getColumnValue(final int i) throws SQLException {
        if(current == null) {
            throw new SQLException();
        }
        return current.getColumnValue(i);
    }
    
    private Integer getIndexFromColumnLabelAndIndexMap(final String columnLabel) throws SQLFeatureNotSupportedException {
        Integer columnIndex = columnLabelAndIndexMap.get(columnLabel);
        if (null == columnIndex) {
            throw new SQLFeatureNotSupportedException(String.format("can't get index from columnLabel[%s].", columnLabel));
        }
        return columnIndex;
    }
    
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return new SSResultSetMetaData(metaData);
    }
}
