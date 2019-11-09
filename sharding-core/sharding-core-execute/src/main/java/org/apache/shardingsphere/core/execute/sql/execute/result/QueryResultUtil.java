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

package org.apache.shardingsphere.core.execute.sql.execute.result;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * Query result util.
 *
 * @author yangyi
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryResultUtil {
    
    /**
     * Get value.
     *
     * @param resultSet result set
     * @param columnIndex column index of value
     * @return {@code null} if the column is SQL {@code NULL}, otherwise the value of column
     * @throws SQLException SQL exception
     */
    public static Object getValue(final ResultSet resultSet, final int columnIndex) throws SQLException {
        Object result = getValueByColumnType(resultSet, columnIndex);
        return resultSet.wasNull() ? null : result;
    }
    
    /**
     * Get value by target class.
     *
     * @param resultSet result set
     * @param columnIndex column index of value
     * @param targetClass target class
     * @return {@code null} if the column is SQL {@code NULL}, otherwise the value of column
     * @throws SQLException SQL exception
     */
    public static Object getValue(final ResultSet resultSet, final int columnIndex, final Class<?> targetClass) throws SQLException {
        if (boolean.class == targetClass) {
            return resultSet.getBoolean(columnIndex);
        } else if (byte.class == targetClass) {
            return resultSet.getByte(columnIndex);
        } else if (short.class == targetClass) {
            return resultSet.getShort(columnIndex);
        } else if (int.class == targetClass) {
            return resultSet.getInt(columnIndex);
        } else if (long.class == targetClass) {
            return resultSet.getLong(columnIndex);
        } else if (float.class == targetClass) {
            return resultSet.getFloat(columnIndex);
        } else if (double.class == targetClass) {
            return resultSet.getDouble(columnIndex);
        } else if (String.class == targetClass) {
            return resultSet.getString(columnIndex);
        } else if (BigDecimal.class == targetClass) {
            return resultSet.getBigDecimal(columnIndex);
        } else if (byte[].class == targetClass) {
            return resultSet.getBytes(columnIndex);
        } else if (Date.class == targetClass) {
            return resultSet.getDate(columnIndex);
        } else if (Time.class == targetClass) {
            return resultSet.getTime(columnIndex);
        } else if (Timestamp.class == targetClass) {
            return resultSet.getTimestamp(columnIndex);
        } else if (Blob.class == targetClass) {
            return resultSet.getBlob(columnIndex);
        } else if (Clob.class == targetClass) {
            return resultSet.getClob(columnIndex);
        } else {
            return resultSet.getObject(columnIndex);
        }
    }
    
    private static Object getValueByColumnType(final ResultSet resultSet, final int columnIndex) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        switch (metaData.getColumnType(columnIndex)) {
            case Types.BOOLEAN:
                return resultSet.getBoolean(columnIndex);
            case Types.TINYINT:
            case Types.SMALLINT:
                return resultSet.getInt(columnIndex);
            case Types.INTEGER:
                return metaData.isSigned(columnIndex) ? resultSet.getInt(columnIndex) : resultSet.getLong(columnIndex);
            case Types.BIGINT:
                return metaData.isSigned(columnIndex) ? resultSet.getLong(columnIndex) : resultSet.getBigDecimal(columnIndex).toBigInteger();
            case Types.NUMERIC:
            case Types.DECIMAL:
                return resultSet.getBigDecimal(columnIndex);
            case Types.FLOAT:
            case Types.DOUBLE:
                return resultSet.getDouble(columnIndex);
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                return resultSet.getString(columnIndex);
            case Types.DATE:
                return resultSet.getDate(columnIndex);
            case Types.TIME:
                return resultSet.getTime(columnIndex);
            case Types.TIMESTAMP:
                return resultSet.getTimestamp(columnIndex);
            case Types.CLOB:
                return resultSet.getClob(columnIndex);
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return resultSet.getBlob(columnIndex);
            default:
                return resultSet.getObject(columnIndex);
        }
    }
}
