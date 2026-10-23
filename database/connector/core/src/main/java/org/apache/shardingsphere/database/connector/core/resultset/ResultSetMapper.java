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

package org.apache.shardingsphere.database.connector.core.resultset;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Result set mapper.
 */
public final class ResultSetMapper {
    
    /**
     * Dialect mapper, or {@code null} when no dialect-specific mapper is registered.
     */
    private final DialectResultSetMapper dialectResultSetMapper;
    
    private WeakReference<ResultSet> timeResultSet;
    
    /**
     * Custom TIME reader, or {@code null} when native JDBC TIME reading is used.
     */
    private ResultSetTimeValueReader timeValueReader;
    
    @HighFrequencyInvocation
    public ResultSetMapper(final DatabaseType databaseType) {
        dialectResultSetMapper = DatabaseTypedSPILoader.findService(DialectResultSetMapper.class, databaseType).orElse(null);
    }
    
    /**
     * Load result set value.
     *
     * @param resultSet result set to be loaded
     * @param columnIndex column index
     * @return data value
     * @throws SQLException SQL exception
     */
    @HighFrequencyInvocation
    public Object load(final ResultSet resultSet, final int columnIndex) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        switch (metaData.getColumnType(columnIndex)) {
            case Types.BOOLEAN:
                return resultSet.getBoolean(columnIndex);
            case Types.TINYINT:
            case Types.SMALLINT:
                return null == dialectResultSetMapper ? Integer.valueOf(resultSet.getInt(columnIndex)) : dialectResultSetMapper.getSmallintValue(resultSet, columnIndex);
            case Types.INTEGER:
                if (metaData.isSigned(columnIndex)) {
                    return resultSet.getInt(columnIndex);
                }
                return resultSet.getLong(columnIndex);
            case Types.BIGINT:
                if (metaData.isSigned(columnIndex)) {
                    return resultSet.getLong(columnIndex);
                }
                BigDecimal bigDecimal = resultSet.getBigDecimal(columnIndex);
                return null == bigDecimal ? null : bigDecimal.toBigInteger();
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
                return null == dialectResultSetMapper ? resultSet.getDate(columnIndex) : dialectResultSetMapper.getDateValue(resultSet, columnIndex);
            case Types.TIME:
                return getTimeValue(resultSet, columnIndex);
            case Types.TIMESTAMP:
                return resultSet.getTimestamp(columnIndex);
            case Types.BLOB:
                return resultSet.getBlob(columnIndex);
            case Types.CLOB:
                return resultSet.getClob(columnIndex);
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return resultSet.getBytes(columnIndex);
            case Types.ARRAY:
                return resultSet.getArray(columnIndex);
            default:
                return null == dialectResultSetMapper ? resultSet.getObject(columnIndex) : dialectResultSetMapper.getDefaultValue(resultSet, columnIndex, metaData.getColumnType(columnIndex));
        }
    }
    
    /**
     * Read a TIME value, retaining {@code null} for SQL {@code NULL}.
     *
     * @param resultSet result set
     * @param columnIndex column index
     * @return TIME value, or {@code null} for SQL {@code NULL}
     * @throws SQLException SQL exception
     */
    private Object getTimeValue(final ResultSet resultSet, final int columnIndex) throws SQLException {
        if (null == timeResultSet || resultSet != timeResultSet.get()) {
            timeResultSet = new WeakReference<>(resultSet);
            timeValueReader = null == dialectResultSetMapper ? null : dialectResultSetMapper.createTimeValueReader().orElse(null);
        }
        return null == timeValueReader ? resultSet.getTime(columnIndex) : timeValueReader.read(resultSet, columnIndex);
    }
}
