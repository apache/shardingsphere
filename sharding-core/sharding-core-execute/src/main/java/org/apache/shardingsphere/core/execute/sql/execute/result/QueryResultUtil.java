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
 * @author sunbufu
 */
public class QueryResultUtil {

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
     * Get value by column type.
     *
     * @param resultSet result set
     * @param columnIndex column index
     * @return column value
     * @throws SQLException SQL exception
     */
    public static Object getValueByColumnType(final ResultSet resultSet, final int columnIndex) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        switch (metaData.getColumnType(columnIndex)) {
            case Types.BIT:
                return resultSet.getBytes(columnIndex);
            case Types.BOOLEAN:
                return resultSet.getBoolean(columnIndex);
            case Types.TINYINT:
                return resultSet.getByte(columnIndex);
            case Types.SMALLINT:
                return resultSet.getShort(columnIndex);
            case Types.INTEGER:
                return resultSet.getInt(columnIndex);
            case Types.BIGINT:
                return resultSet.getLong(columnIndex);
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
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return resultSet.getBytes(columnIndex);
            case Types.DATE:
                return resultSet.getDate(columnIndex);
            case Types.TIME:
                return resultSet.getTime(columnIndex);
            case Types.TIMESTAMP:
                return resultSet.getTimestamp(columnIndex);
            case Types.CLOB:
                return resultSet.getClob(columnIndex);
            case Types.BLOB:
                return resultSet.getBlob(columnIndex);
            default:
                return resultSet.getObject(columnIndex);
        }
    }

    /**
     * Get value.
     *
     * @param resultSet result set
     * @param columnIndex column index of value
     * @param type class type of data value
     * @return {@code null} if the column is SQL {@code NULL}, otherwise the value of column
     * @throws SQLException SQL exception
     */
    public static Object getValue(final ResultSet resultSet, final int columnIndex, final Class<?> type) throws SQLException {
        Object result = getValueByExpectedType(resultSet, columnIndex, type);
        return resultSet.wasNull() ? null : result;
    }

    /**
     * Get value by expected type.
     *
     * @param resultSet result set
     * @param columnIndex column index
     * @param type class type of data value
     * @return column value
     * @throws SQLException SQL exception
     */
    public static Object getValueByExpectedType(final ResultSet resultSet, final int columnIndex, final Class<?> type) throws SQLException {

        if (byte.class == type) {
            return resultSet.getByte(columnIndex);
        } else if (boolean.class == type) {
            return resultSet.getBoolean(columnIndex);
        } else if (short.class == type) {
            return resultSet.getShort(columnIndex);
        } else if (int.class == type) {
            return resultSet.getInt(columnIndex);
        } else if (long.class == type) {
            return resultSet.getLong(columnIndex);
        } else if (float.class == type || double.class == type) {
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
        } else if (Clob.class == type) {
            return resultSet.getClob(columnIndex);
        } else if (Blob.class == type) {
            return resultSet.getBlob(columnIndex);
        } else {
            return resultSet.getObject(columnIndex);
        }
    }

}
