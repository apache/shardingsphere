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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

/**
 * Result set mapper.
 */
@RequiredArgsConstructor
public final class ResultSetMapper {
    
    private final DatabaseType databaseType;
    
    /**
     * Load result set value.
     *
     * @param resultSet result set to be loaded
     * @param columnIndex column index
     * @return data value
     * @throws SQLException SQL exception
     */
    public Object load(final ResultSet resultSet, final int columnIndex) throws SQLException {
        Optional<DialectResultSetMapper> dialectResultSetMapper = DatabaseTypedSPILoader.findService(DialectResultSetMapper.class, databaseType);
        ResultSetMetaData metaData = resultSet.getMetaData();
        switch (metaData.getColumnType(columnIndex)) {
            case Types.BOOLEAN:
                return resultSet.getBoolean(columnIndex);
            case Types.TINYINT:
            case Types.SMALLINT:
                return dialectResultSetMapper.isPresent() ? dialectResultSetMapper.get().getSmallintValue(resultSet, columnIndex) : Integer.valueOf(resultSet.getInt(columnIndex));
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
                return dialectResultSetMapper.isPresent() ? dialectResultSetMapper.get().getDateValue(resultSet, columnIndex) : resultSet.getDate(columnIndex);
            case Types.TIME:
                return resultSet.getTime(columnIndex);
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
                return resultSet.getObject(columnIndex);
        }
    }
}
