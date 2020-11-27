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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.jdbc;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * JDBC execute query result for stream loading.
 */
public final class StreamJDBCExecuteQueryResult extends AbstractJDBCExecuteQueryResult {
    
    private final ResultSet resultSet;
    
    public StreamJDBCExecuteQueryResult(final ResultSet resultSet) throws SQLException {
        super(resultSet.getMetaData());
        this.resultSet = resultSet;
    }
    
    @Override
    public boolean next() throws SQLException {
        return resultSet.next();
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        if (boolean.class == type) {
            return resultSet.getBoolean(columnIndex);
        } else if (byte.class == type) {
            return resultSet.getByte(columnIndex);
        } else if (short.class == type) {
            return resultSet.getShort(columnIndex);
        } else if (int.class == type) {
            return resultSet.getInt(columnIndex);
        } else if (long.class == type) {
            return resultSet.getLong(columnIndex);
        } else if (float.class == type) {
            return resultSet.getFloat(columnIndex);
        } else if (double.class == type) {
            return resultSet.getDouble(columnIndex);
        } else if (String.class == type) {
            return resultSet.getString(columnIndex);
        } else if (BigDecimal.class == type) {
            return resultSet.getBigDecimal(columnIndex);
        } else if (byte[].class == type) {
            return resultSet.getBytes(columnIndex);
        } else if (Date.class == type) {
            return resultSet.getDate(columnIndex);
        } else if (Time.class == type) {
            return resultSet.getTime(columnIndex);
        } else if (Timestamp.class == type) {
            return resultSet.getTimestamp(columnIndex);
        } else if (Blob.class == type) {
            return resultSet.getBlob(columnIndex);
        } else if (Clob.class == type) {
            return resultSet.getClob(columnIndex);
        } else if (Array.class == type) {
            return resultSet.getArray(columnIndex);
        } else {
            return resultSet.getObject(columnIndex);
        }
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
    
    @Override
    public boolean wasNull() throws SQLException {
        return resultSet.wasNull();
    }
}
