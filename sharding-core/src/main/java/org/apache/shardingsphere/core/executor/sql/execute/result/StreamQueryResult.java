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

package org.apache.shardingsphere.core.executor.sql.execute.result;

import com.google.common.base.Optional;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.merger.QueryResult;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.spi.algorithm.encrypt.ShardingEncryptor;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Query result for stream loading.
 *
 * @author zhangliang
 * @author panjuan
 */
public final class StreamQueryResult implements QueryResult {
    
    private final QueryResultMetaData metaData;
    
    private final ResultSet resultSet;
    
    @SneakyThrows
    public StreamQueryResult(final ResultSet resultSet, final ShardingRule shardingRule) {
        this.resultSet = resultSet;
        metaData = new QueryResultMetaData(resultSet.getMetaData(), shardingRule);
    }
    
    @Override
    public boolean next() throws SQLException {
        return resultSet.next();
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        Object result;
        if (Object.class == type) {
            result = decrypt(columnIndex, resultSet.getObject(columnIndex));
        } else if (boolean.class == type) {
            result = decrypt(columnIndex, resultSet.getBoolean(columnIndex));
        } else if (byte.class == type) {
            result = decrypt(columnIndex, resultSet.getByte(columnIndex));
        } else if (short.class == type) {
            result = decrypt(columnIndex, resultSet.getShort(columnIndex));
        } else if (int.class == type) {
            result = decrypt(columnIndex, resultSet.getInt(columnIndex));
        } else if (long.class == type) {
            result = decrypt(columnIndex, resultSet.getLong(columnIndex));
        } else if (float.class == type) {
            result = decrypt(columnIndex, resultSet.getFloat(columnIndex));
        } else if (double.class == type) {
            result = decrypt(columnIndex, resultSet.getDouble(columnIndex));
        } else if (String.class == type) {
            result = decrypt(columnIndex, resultSet.getString(columnIndex));
        } else if (BigDecimal.class == type) {
            result = decrypt(columnIndex, resultSet.getBigDecimal(columnIndex));
        } else if (byte[].class == type) {
            result = resultSet.getBytes(columnIndex);
        } else if (Date.class == type) {
            result = resultSet.getDate(columnIndex);
        } else if (Time.class == type) {
            result = resultSet.getTime(columnIndex);
        } else if (Timestamp.class == type) {
            result = resultSet.getTimestamp(columnIndex);
        } else if (URL.class == type) {
            result = resultSet.getURL(columnIndex);
        } else if (Blob.class == type) {
            result = resultSet.getBlob(columnIndex);
        } else if (Clob.class == type) {
            result = resultSet.getClob(columnIndex);
        } else if (SQLXML.class == type) {
            result = resultSet.getSQLXML(columnIndex);
        } else if (Reader.class == type) {
            result = resultSet.getCharacterStream(columnIndex);
        } else {
            result = decrypt(columnIndex, resultSet.getObject(columnIndex));
        }
        return result;
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) throws SQLException {
        Object result;
        if (Object.class == type) {
            result = decrypt(columnLabel, resultSet.getObject(columnLabel));
        } else if (boolean.class == type) {
            result = decrypt(columnLabel, resultSet.getBoolean(columnLabel));
        } else if (byte.class == type) {
            result = decrypt(columnLabel, resultSet.getByte(columnLabel));
        } else if (short.class == type) {
            result = decrypt(columnLabel, resultSet.getShort(columnLabel));
        } else if (int.class == type) {
            result = decrypt(columnLabel, resultSet.getInt(columnLabel));
        } else if (long.class == type) {
            result = decrypt(columnLabel, resultSet.getLong(columnLabel));
        } else if (float.class == type) {
            result = decrypt(columnLabel, resultSet.getFloat(columnLabel));
        } else if (double.class == type) {
            result = decrypt(columnLabel, resultSet.getDouble(columnLabel));
        } else if (String.class == type) {
            result = decrypt(columnLabel, resultSet.getString(columnLabel));
        } else if (BigDecimal.class == type) {
            result = decrypt(columnLabel, resultSet.getBigDecimal(columnLabel));
        } else if (byte[].class == type) {
            result = resultSet.getBytes(columnLabel);
        } else if (Date.class == type) {
            result = resultSet.getDate(columnLabel);
        } else if (Time.class == type) {
            result = resultSet.getTime(columnLabel);
        } else if (Timestamp.class == type) {
            result = resultSet.getTimestamp(columnLabel);
        } else if (URL.class == type) {
            result = resultSet.getURL(columnLabel);
        } else if (Blob.class == type) {
            result = resultSet.getBlob(columnLabel);
        } else if (Clob.class == type) {
            result = resultSet.getClob(columnLabel);
        } else if (SQLXML.class == type) {
            result = resultSet.getSQLXML(columnLabel);
        } else if (Reader.class == type) {
            result = resultSet.getCharacterStream(columnLabel);
        } else {
            result = decrypt(columnLabel, resultSet.getObject(columnLabel));
        }
        return result;
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        if (Date.class == type) {
            return resultSet.getDate(columnIndex, calendar);
        }
        if (Time.class == type) {
            return resultSet.getTime(columnIndex, calendar);
        }
        if (Timestamp.class == type) {
            return resultSet.getTimestamp(columnIndex, calendar);
        }
        throw new SQLException(String.format("Unsupported type: %s", type));
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) throws SQLException {
        if (Date.class == type) {
            return resultSet.getDate(columnLabel, calendar);
        }
        if (Time.class == type) {
            return resultSet.getTime(columnLabel, calendar);
        }
        if (Timestamp.class == type) {
            return resultSet.getTimestamp(columnLabel, calendar);
        }
        throw new SQLException(String.format("Unsupported type: %s", type));
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        switch (type) {
            case "Ascii":
                return resultSet.getAsciiStream(columnIndex);
            case "Unicode":
                return resultSet.getUnicodeStream(columnIndex);
            case "Binary":
                return resultSet.getBinaryStream(columnIndex);
            default:
                throw new SQLException(String.format("Unsupported type: %s", type));
        }
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream getInputStream(final String columnLabel, final String type) throws SQLException {
        switch (type) {
            case "Ascii":
                return resultSet.getAsciiStream(columnLabel);
            case "Unicode":
                return resultSet.getUnicodeStream(columnLabel);
            case "Binary":
                return resultSet.getBinaryStream(columnLabel);
            default:
                throw new SQLException(String.format("Unsupported type: %s", type));
        }
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return resultSet.wasNull();
    }
    
    @Override
    public int getColumnCount() {
        return metaData.getColumnCount();
    }
    
    @Override
    public String getColumnLabel(final int columnIndex) {
        return metaData.getColumnLabel(columnIndex);
    }
    
    @SneakyThrows
    private Object decrypt(final String columnLabel, final Object value) {
        return decrypt(metaData.getColumnIndex(columnLabel), value);
    }
    
    @SneakyThrows
    private Object decrypt(final int columnIndex, final Object value) {
        Optional<ShardingEncryptor> shardingEncryptor = metaData.getShardingEncryptor(columnIndex);
        return shardingEncryptor.isPresent() ? shardingEncryptor.get().decrypt(value.toString()) : value;
    }
}
