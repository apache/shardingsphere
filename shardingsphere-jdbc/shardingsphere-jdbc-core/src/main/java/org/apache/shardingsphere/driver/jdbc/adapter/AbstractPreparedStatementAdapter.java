/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.driver.jdbc.adapter;

import com.google.common.io.CharStreams;
import lombok.Getter;
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationPreparedStatement;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Adapter for {@code PreparedStatement}.
 */
public abstract class AbstractPreparedStatementAdapter extends AbstractUnsupportedOperationPreparedStatement {
    
    private final List<PreparedStatementInvocationReplayer> setParameterMethodInvocations = new LinkedList<>();
    
    @Getter
    private final List<Object> parameters = new ArrayList<>();
    
    @Override
    public final void setNull(final int parameterIndex, final int sqlType) {
        setParameter(parameterIndex, null);
    }
    
    @Override
    public final void setNull(final int parameterIndex, final int sqlType, final String typeName) {
        setParameter(parameterIndex, null);
    }
    
    @Override
    public final void setBoolean(final int parameterIndex, final boolean x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setByte(final int parameterIndex, final byte x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setShort(final int parameterIndex, final short x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setInt(final int parameterIndex, final int x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setLong(final int parameterIndex, final long x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setFloat(final int parameterIndex, final float x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setDouble(final int parameterIndex, final double x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setString(final int parameterIndex, final String x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setBigDecimal(final int parameterIndex, final BigDecimal x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setDate(final int parameterIndex, final Date x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setDate(final int parameterIndex, final Date x, final Calendar cal) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setTime(final int parameterIndex, final Time x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setTime(final int parameterIndex, final Time x, final Calendar cal) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setTimestamp(final int parameterIndex, final Timestamp x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setBytes(final int parameterIndex, final byte[] x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setBlob(final int parameterIndex, final Blob x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setBlob(final int parameterIndex, final InputStream x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setBlob(final int parameterIndex, final InputStream x, final long length) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Clob x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Reader x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setClob(final int parameterIndex, final Reader x, final long length) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public void setArray(final int parameterIndex, final Array x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x, final int length) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setAsciiStream(final int parameterIndex, final InputStream x, final long length) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x, final int length) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setBinaryStream(final int parameterIndex, final InputStream x, final long length) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x) {
        try {
            setParameter(parameterIndex, CharStreams.toString(x));
        } catch (final IOException ex) {
            throw new ShardingSphereException(ex);
        }
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x, final int length) {
        try {
            setParameter(parameterIndex, CharStreams.toString(x));
        } catch (final IOException ex) {
            throw new ShardingSphereException(ex);
        }
    }
    
    @Override
    public final void setCharacterStream(final int parameterIndex, final Reader x, final long length) {
        try {
            setParameter(parameterIndex, CharStreams.toString(x));
        } catch (final IOException ex) {
            throw new ShardingSphereException(ex);
        }
    }
    
    @Override
    public final void setURL(final int parameterIndex, final URL x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setSQLXML(final int parameterIndex, final SQLXML x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setObject(final int parameterIndex, final Object x) {
        setParameter(parameterIndex, x);
    }
    
    @Override
    public final void setObject(final int parameterIndex, final Object x, final int targetSqlType) {
        setObject(parameterIndex, x, targetSqlType, -1);
    }
    
    @Override
    public final void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scaleOrLength) {
        if (x == null) {
            setNull(parameterIndex, targetSqlType);
            return;
        }
        try {
            switch (targetSqlType) {
                case Types.NULL:
                    setNull(parameterIndex, targetSqlType);
                    break;
                case Types.INTEGER:
                    setInt(parameterIndex, castToInt(x));
                    break;
                case Types.BIGINT:
                    setLong(parameterIndex, castToLong(x));
                    break;
                case Types.SMALLINT:
                case Types.TINYINT:
                    setShort(parameterIndex, castToShort(x));
                    break;
                case Types.LONGVARCHAR:
                case Types.VARCHAR:
                case Types.CHAR:
                    setString(parameterIndex, x instanceof Clob ? clobToString((Clob) x) : x.toString());
                    break;
                case Types.DOUBLE:
                case Types.FLOAT:
                    setDouble(parameterIndex, castToDouble(x));
                    break;
                case Types.DECIMAL:
                case Types.NUMERIC:
                    setBigDecimal(parameterIndex, castToBigDecimal(x, scaleOrLength));
                    break;
                case Types.DATE:
                    setDate(parameterIndex, castToDate(x));
                    break;
                case Types.TIME:
                    setTime(parameterIndex, castToTime(x));
                    break;
                case Types.TIMESTAMP:
                    setTimestamp(parameterIndex, castToTimestamp(x));
                    break;
                case Types.BIT:
                case Types.BOOLEAN:
                    setBoolean(parameterIndex, castToBoolean(x));
                    break;
                default:
                    setParameter(parameterIndex, x);
            }
        } catch (Exception ex) {
            setParameter(parameterIndex, x);
        }
    }
    
    private void setParameter(final int parameterIndex, final Object value) {
        if (parameters.size() == parameterIndex - 1) {
            parameters.add(value);
            return;
        }
        for (int i = parameters.size(); i <= parameterIndex - 1; i++) {
            parameters.add(null);
        }
        parameters.set(parameterIndex - 1, value);
    }
    
    protected final void replaySetParameter(final PreparedStatement preparedStatement, final List<Object> parameters) throws SQLException {
        setParameterMethodInvocations.clear();
        addParameters(parameters);
        for (PreparedStatementInvocationReplayer each : setParameterMethodInvocations) {
            each.replayOn(preparedStatement);
        }
    }
    
    private void addParameters(final List<Object> parameters) {
        int i = 0;
        for (Object each : parameters) {
            int index = ++i;
            setParameterMethodInvocations.add(preparedStatement -> preparedStatement.setObject(index, each));
        }
    }
    
    private int castToInt(Object obj) throws Exception {
        if (obj instanceof String) {
            return Integer.parseInt((String) obj);
        } else if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj instanceof Boolean) {
            return (Boolean) obj ? 1 : 0;
        } else if (obj instanceof java.util.Date) {
            return (int) ((java.util.Date) obj).getTime();
        } else if (obj instanceof Clob) {
            return Integer.parseInt(clobToString((Clob) obj));
        }else if (obj instanceof Character) {
            return Integer.parseInt(obj.toString());
        }
        throw new ClassCastException();
    }

    private long castToLong(Object obj) throws Exception {
        if (obj instanceof String) {
            return Long.parseLong((String) obj);
        } else if (obj instanceof Number) {
            return ((Number) obj).longValue();
        } else if (obj instanceof Boolean) {
            return (Boolean) obj ? 1L : 0L;
        } else if (obj instanceof java.util.Date) {
            return ((java.util.Date) obj).getTime();
        } else if (obj instanceof Clob) {
            return Long.parseLong(clobToString((Clob) obj));
        } else if (obj instanceof Character) {
            return Long.parseLong(obj.toString());
        }
        throw new ClassCastException();
    }

    private double castToDouble(Object obj) throws Exception {
        if (obj instanceof String) {
            return Double.parseDouble((String) obj);
        } else if (obj instanceof Number) {
            if (obj instanceof Float) {
                return Double.parseDouble(Float.toString((float) obj));
            }
            return ((Number) obj).doubleValue();
        } else if (obj instanceof Boolean) {
            return (Boolean) obj ? 1D : 0D;
        } else if (obj instanceof java.util.Date) {
            return ((java.util.Date) obj).getTime();
        } else if (obj instanceof Clob) {
            return Double.parseDouble(clobToString((Clob) obj));
        } else if (obj instanceof Character) {
            return Double.parseDouble(obj.toString());
        }
        throw new ClassCastException();
    }

    private short castToShort(Object obj) throws Exception {
        if (obj instanceof String) {
            return Short.parseShort((String) obj);
        } else if (obj instanceof Number) {
            return ((Number) obj).shortValue();
        } else if (obj instanceof Boolean) {
            return (Boolean) obj ? (short) 1 : (short) 0;
        } else if (obj instanceof java.util.Date) {
            return (short) ((java.util.Date) obj).getTime();
        } else if (obj instanceof Clob) {
            return Short.parseShort(clobToString((Clob) obj));
        } else if (obj instanceof Character) {
            return Short.parseShort(obj.toString());
        }
        throw new ClassCastException();
    }

    private BigDecimal castToBigDecimal(Object obj,int scale) throws Exception {
        BigDecimal result = null;
        if (obj instanceof String) {
            result = new BigDecimal((String) obj);
        } else if (obj instanceof BigInteger) {
            result = new BigDecimal((BigInteger) obj);
        } else if (obj instanceof Long || obj instanceof Integer || obj instanceof Short) {
            result = BigDecimal.valueOf(((Number) obj).longValue());
        } else if (obj instanceof Double || obj instanceof Float) {
            if (obj instanceof Float) {
                result = BigDecimal.valueOf(Double.parseDouble(Float.toString((float) obj)));
            } else {
                result = BigDecimal.valueOf(((Number) obj).doubleValue());
            }
        } else if (obj instanceof Boolean) {
            result = (Boolean) obj ? BigDecimal.ONE : BigDecimal.ZERO;
        } else if (obj instanceof java.util.Date) {
            result = BigDecimal.valueOf(((java.util.Date) obj).getTime());
        } else if (obj instanceof Clob) {
            result = new BigDecimal(clobToString((Clob) obj));
        } else if (obj instanceof Character) {
            result = new BigDecimal(obj.toString());
        }
        if (result != null) {
            if (scale>0) {
                result = result.setScale(scale, 1);
            }
            return result;
        }
        throw new ClassCastException();
    }

    private long getTimeOfDateObject(Object obj) {
        try {
            return castToLong(obj);
        } catch (Exception ex) {
            if (obj instanceof java.sql.Date) {
                return ((Date) obj).getTime();
            } else if (obj instanceof Time) {
                return ((Time) obj).getTime();
            } else if (obj instanceof Timestamp) {
                return ((Timestamp) obj).getTime();
            } else if (obj instanceof java.util.Date) {
                return ((java.util.Date) obj).getTime();
            } else if (obj instanceof LocalDate) {
                return LocalDateTime.of((LocalDate) obj, LocalTime.MIN).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } else if (obj instanceof LocalTime) {
                return LocalDateTime.of(LocalDate.now(), (LocalTime) obj).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } else if (obj instanceof LocalDateTime) {
                return ((LocalDateTime) obj).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } else if (obj instanceof OffsetDateTime) {
                return ((OffsetDateTime) obj).toInstant().toEpochMilli();
            } else if (obj instanceof OffsetTime) {
                return ((OffsetTime) obj).atDate(LocalDate.now()).toInstant().toEpochMilli();
            } else if (obj instanceof Calendar) {
                return ((Calendar) obj).getTimeInMillis();
            } else if (obj instanceof ZonedDateTime) {
                return ((ZonedDateTime) obj).toInstant().toEpochMilli();
            }
            throw new ClassCastException();
        }
    }

    private Date castToDate(Object obj) {
        try {
            return new Date(getTimeOfDateObject(obj));
        } catch (Exception ex) {
            throw new ClassCastException();
        }
    }

    public Time castToTime(Object obj) {
        try {
            return new Time(getTimeOfDateObject(obj));
        } catch (Exception ex) {
            throw new ClassCastException();
        }
    }

    public Timestamp castToTimestamp(Object obj) {
        try {
            return new Timestamp(getTimeOfDateObject(obj));
        } catch (Exception ex) {
            throw new ClassCastException();
        }
    }

    private boolean castToBoolean(Object obj) {
        Boolean result = null;
        if (obj instanceof Boolean) {
            result = (Boolean) obj;
        } else if (obj instanceof Number) {
            result = castBooleanFromNumber((Number) obj);
        } else if (obj instanceof String) {
            result = castBooleanFromString((String) obj);
        } else if (obj instanceof Character) {
            result = castBooleanFromChar((Character) obj);
        }
        if (result==null) {
            throw new ClassCastException();
        }
        return result;
    }

    private boolean castBooleanFromString(String str) {
        str = str.trim();
        if ("yes".equalsIgnoreCase(str)
                || "y".equalsIgnoreCase(str)
                || "true".equalsIgnoreCase(str)
                || "t".equalsIgnoreCase(str)
                || "1".equalsIgnoreCase(str)) {
            return true;
        } else if ("no".equalsIgnoreCase(str)
                || "n".equalsIgnoreCase(str)
                || "false".equalsIgnoreCase(str)
                || "f".equalsIgnoreCase(str)
                || "0".equalsIgnoreCase(str)) {
            return false;
        }
        throw new ClassCastException();
    }

    private boolean castBooleanFromChar(Character ch) {
        if ('Y'== ch || 'y' == ch || 't' == ch || 'T' == ch || '1' == ch) {
            return true;
        } else if ('N'== ch || 'n' == ch || 'f' == ch || 'F' == ch || '0' == ch) {
            return false;
        }
        throw new ClassCastException();
    }

    private boolean castBooleanFromNumber(Number num) {
        int val = num.intValue();
        if (val == 1) {
            return true;
        } else if(val == 0) {
            return false;
        }
        throw new ClassCastException();
    }

    private String clobToString(Clob clob) throws Exception {
        long length = clob.length();
        if (length>0) {
            return clob.getSubString(1, (int) length);
        } else {
            return "";
        }
    }
    
    @Override
    public final void clearParameters() {
        parameters.clear();
        setParameterMethodInvocations.clear();
    }
    
    @FunctionalInterface
    interface PreparedStatementInvocationReplayer {
        
        void replayOn(PreparedStatement preparedStatement) throws SQLException;
    }
}
