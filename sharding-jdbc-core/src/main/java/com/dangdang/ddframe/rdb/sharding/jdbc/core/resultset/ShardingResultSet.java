package com.dangdang.ddframe.rdb.sharding.jdbc.core.resultset;

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractResultSetAdapter;
import com.dangdang.ddframe.rdb.sharding.merger.core.MergeResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.util.ResultSetUtil;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 支持分片的结果集.
 *
 * @author zhangliang
 */
public final class ShardingResultSet extends AbstractResultSetAdapter {
    
    private final MergeResultSet mergeResultSet;
    
    private final SQLStatement sqlStatement;
    
    private final boolean skipAll;
    
    private int rowNumber;
    
    public ShardingResultSet(final List<ResultSet> resultSets, final SQLStatement sqlStatement, final MergeResultSet mergeResultSet) throws SQLException {
        super(resultSets);
        this.mergeResultSet = mergeResultSet;
        this.sqlStatement = sqlStatement;
        skipAll = skipOffset();
    }
    
    private boolean skipOffset() throws SQLException {
        if (null == sqlStatement.getLimit()) {
            return false;
        }
        for (int i = 0; i < sqlStatement.getLimit().getOffsetValue(); i++) {
            if (!mergeResultSet.next()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean next() throws SQLException {
        if (skipAll) {
            return false;
        }
        if (null != sqlStatement.getLimit() && sqlStatement.getLimit().getRowCountValue() > 0) {
            return ++rowNumber <= sqlStatement.getLimit().getRowCountValue() && mergeResultSet.next();
        }
        return mergeResultSet.next();
        
    }
    
    @Override
    // TODO
    public boolean wasNull() throws SQLException {
        return false;
    }
    
    @Override
    public String getString(final int columnIndex) throws SQLException {
        return (String) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, String.class), String.class);
    }
    
    @Override
    public String getString(final String columnLabel) throws SQLException {
        return (String) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, String.class), String.class);
    }
    
    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        return (boolean) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, boolean.class), boolean.class);
    }
    
    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        return (boolean) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, boolean.class), boolean.class);
    }
    
    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        return (byte) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, byte.class), byte.class);
    }
    
    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        return (byte) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, byte.class), byte.class);
    }
    
    @Override
    public short getShort(final int columnIndex) throws SQLException {
        return (short) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, short.class), short.class);
    }
    
    @Override
    public short getShort(final String columnLabel) throws SQLException {
        return (short) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, short.class), short.class);
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        return (int) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, int.class), int.class);
    }
    
    @Override
    public int getInt(final String columnLabel) throws SQLException {
        return (int) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, int.class), int.class);
    }
    
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        return (long) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, long.class), long.class);
    }
    
    @Override
    public long getLong(final String columnLabel) throws SQLException {
        return (long) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, long.class), long.class);
    }
    
    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        return (float) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, float.class), float.class);
    }
    
    @Override
    public float getFloat(final String columnLabel) throws SQLException {
        return (float) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, float.class), float.class);
    }
    
    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        return (double) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, double.class), double.class);
    }
    
    @Override
    public double getDouble(final String columnLabel) throws SQLException {
        return (double) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, double.class), double.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, BigDecimal.class), BigDecimal.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, BigDecimal.class), BigDecimal.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, BigDecimal.class), BigDecimal.class);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        return (BigDecimal) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, BigDecimal.class), BigDecimal.class);
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        return (byte[]) mergeResultSet.getValue(columnIndex, byte[].class);
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        return (byte[]) mergeResultSet.getValue(columnLabel, byte[].class);
    }
    
    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        return (Date) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, Date.class), Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel) throws SQLException {
        return (Date) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, Date.class), Date.class);
    }
    
    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        return (Date) ResultSetUtil.convertValue(mergeResultSet.getCalendarValue(columnIndex, Date.class, cal), Date.class);
    }
    
    @Override
    public Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        return (Date) ResultSetUtil.convertValue(mergeResultSet.getCalendarValue(columnLabel, Date.class, cal), Date.class);
    }
    
    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        return (Time) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, Time.class), Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel) throws SQLException {
        return (Time) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, Time.class), Time.class);
    }
    
    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        return (Time) ResultSetUtil.convertValue(mergeResultSet.getCalendarValue(columnIndex, Time.class, cal), Time.class);
    }
    
    @Override
    public Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        return (Time) ResultSetUtil.convertValue(mergeResultSet.getCalendarValue(columnLabel, Time.class, cal), Time.class);
    }
            
    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(mergeResultSet.getValue(columnIndex, Timestamp.class), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(mergeResultSet.getValue(columnLabel, Timestamp.class), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(mergeResultSet.getCalendarValue(columnIndex, Timestamp.class, cal), Timestamp.class);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        return (Timestamp) ResultSetUtil.convertValue(mergeResultSet.getCalendarValue(columnLabel, Timestamp.class, cal), Timestamp.class);
    }
    
    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        return mergeResultSet.getInputStream(columnIndex, "Ascii");
    }
    
    @Override
    public InputStream getAsciiStream(final String columnLabel) throws SQLException {
        return mergeResultSet.getInputStream(columnLabel, "Ascii");
    }
    
    @Override
    public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        return mergeResultSet.getInputStream(columnIndex, "Unicode");
    }
    
    @Override
    public InputStream getUnicodeStream(final String columnLabel) throws SQLException {
        return mergeResultSet.getInputStream(columnLabel, "Unicode");
    }
    
    @Override
    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        return mergeResultSet.getInputStream(columnIndex, "Binary");
    }
    
    @Override
    public InputStream getBinaryStream(final String columnLabel) throws SQLException {
        return mergeResultSet.getInputStream(columnLabel, "Binary");
    }
    
    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        return (Reader) mergeResultSet.getValue(columnIndex, Reader.class);
    }
    
    @Override
    public Reader getCharacterStream(final String columnLabel) throws SQLException {
        return (Reader) mergeResultSet.getValue(columnLabel, Reader.class);
    }
    
    @Override
    public Blob getBlob(final int columnIndex) throws SQLException {
        return (Blob) mergeResultSet.getValue(columnIndex, Blob.class);
    }
    
    @Override
    public Blob getBlob(final String columnLabel) throws SQLException {
        return (Blob) mergeResultSet.getValue(columnLabel, Blob.class);
    }
    
    @Override
    public Clob getClob(final int columnIndex) throws SQLException {
        return (Clob) mergeResultSet.getValue(columnIndex, Clob.class);
    }
        
    @Override
    public Clob getClob(final String columnLabel) throws SQLException {
        return (Clob) mergeResultSet.getValue(columnLabel, Clob.class);
    }
    
    @Override
    public URL getURL(final int columnIndex) throws SQLException {
        return (URL) mergeResultSet.getValue(columnIndex, URL.class);
    }
    
    @Override
    public URL getURL(final String columnLabel) throws SQLException {
        return (URL) mergeResultSet.getValue(columnLabel, URL.class);
    }
    
    @Override
    public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        return (SQLXML) mergeResultSet.getValue(columnIndex, SQLXML.class);
    }
    
    @Override
    public SQLXML getSQLXML(final String columnLabel) throws SQLException {
        return (SQLXML) mergeResultSet.getValue(columnLabel, SQLXML.class);
    }
    
    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        return mergeResultSet.getValue(columnIndex, Object.class);
    }
    
    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        return mergeResultSet.getValue(columnLabel, Object.class);
    }
    
    @Override
    // TODO should return sharding statement in future
    public Statement getStatement() throws SQLException {
        return getResultSets().get(0).getStatement();
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return getResultSets().get(0).getMetaData();
    }
    
    @Override
    public int findColumn(final String columnLabel) throws SQLException {
        return getResultSets().get(0).findColumn(columnLabel);
    }
    
    @Override
    public int getFetchDirection() throws SQLException {
        return getResultSets().get(0).getFetchDirection();
    }
    
    @Override
    public int getFetchSize() throws SQLException {
        return getResultSets().get(0).getFetchSize();
    }
    
    @Override
    public int getType() throws SQLException {
        return getResultSets().get(0).getType();
    }
    
    @Override
    public int getConcurrency() throws SQLException {
        return getResultSets().get(0).getConcurrency();
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return getResultSets().get(0).getWarnings();
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        for (ResultSet each : getResultSets()) {
            try {
                each.clearWarnings();
            } catch (final SQLException ex) {
                exceptions.add(ex);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
    }
}
