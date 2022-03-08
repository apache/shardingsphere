package org.apache.shardingsphere.infra.federation.executor.customized;

import lombok.AllArgsConstructor;
import org.apache.shardingsphere.infra.merge.result.MergedResult;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public final class FederationResultSet extends AbstractUnsupportedOperationResultSet {

    private MergedResult mergedResult;

    @Override
    public boolean next() throws SQLException {
        return mergedResult.next();
    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public boolean wasNull() throws SQLException {
        return false;
    }

    @Override
    public String getString(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        return false;
    }

    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public short getShort(final int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public int getInt(final int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public long getLong(final int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        return null;
    }

    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        return new byte[0];
    }

    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public String getString(final String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        return false;
    }

    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public short getShort(final String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public int getInt(final String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public long getLong(final String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public float getFloat(final String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public double getDouble(final String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        return null;
    }

    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        return new byte[0];
    }

    @Override
    public Date getDate(final String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(final String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public InputStream getAsciiStream(final String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public InputStream getUnicodeStream(final String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public InputStream getBinaryStream(final String columnLabel) throws SQLException {
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
    public ResultSetMetaData getMetaData() throws SQLException {
        return null;
    }

    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public int findColumn(final String columnLabel) throws SQLException {
        return 0;
    }

    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Reader getCharacterStream(final String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public void setFetchDirection(final int direction) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(final int rows) throws SQLException {

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
    public Blob getBlob(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Clob getClob(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Array getArray(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Blob getBlob(final String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Clob getClob(final String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Array getArray(final String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public URL getURL(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public URL getURL(final String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public SQLXML getSQLXML(final String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public String getNString(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public String getNString(final String columnLabel) throws SQLException {
        return null;
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return false;
    }
}
