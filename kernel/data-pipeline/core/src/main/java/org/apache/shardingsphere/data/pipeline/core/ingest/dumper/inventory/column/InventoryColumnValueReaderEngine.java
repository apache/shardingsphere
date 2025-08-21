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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.column;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

/**
 * Inventory column value reader engine.
 */
@RequiredArgsConstructor
public final class InventoryColumnValueReaderEngine {
    
    private final DatabaseType databaseType;
    
    /**
     * Read column value.
     *
     * @param resultSet result set
     * @param metaData result set meta data
     * @param columnIndex column index
     * @return column value
     * @throws SQLException SQL exception
     */
    public Object read(final ResultSet resultSet, final ResultSetMetaData metaData, final int columnIndex) throws SQLException {
        Optional<Object> dialectValue = readDialectValue(resultSet, metaData, columnIndex);
        Object result = dialectValue.isPresent() ? dialectValue.get() : readStandardValue(resultSet, metaData, columnIndex);
        return resultSet.wasNull() ? null : result;
    }
    
    private Optional<Object> readDialectValue(final ResultSet resultSet, final ResultSetMetaData metaData, final int columnIndex) throws SQLException {
        Optional<DialectInventoryColumnValueReader> dialectColumnReader = DatabaseTypedSPILoader.findService(DialectInventoryColumnValueReader.class, databaseType);
        return dialectColumnReader.isPresent() ? dialectColumnReader.get().read(resultSet, metaData, columnIndex) : Optional.empty();
    }
    
    private Object readStandardValue(final ResultSet resultSet, final ResultSetMetaData metaData, final int columnIndex) throws SQLException {
        int columnType = metaData.getColumnType(columnIndex);
        switch (columnType) {
            case Types.BOOLEAN:
                return resultSet.getBoolean(columnIndex);
            case Types.TINYINT:
                if (isSigned(metaData, columnIndex)) {
                    return resultSet.getByte(columnIndex);
                } else {
                    return resultSet.getShort(columnIndex);
                }
            case Types.SMALLINT:
                if (isSigned(metaData, columnIndex)) {
                    return resultSet.getShort(columnIndex);
                } else {
                    return resultSet.getInt(columnIndex);
                }
            case Types.INTEGER:
                if (isSigned(metaData, columnIndex)) {
                    return resultSet.getInt(columnIndex);
                } else {
                    return resultSet.getLong(columnIndex);
                }
            case Types.BIGINT:
                if (isSigned(metaData, columnIndex)) {
                    return resultSet.getLong(columnIndex);
                } else {
                    return resultSet.getBigDecimal(columnIndex);
                }
            case Types.NUMERIC:
            case Types.DECIMAL:
                return resultSet.getBigDecimal(columnIndex);
            case Types.REAL:
            case Types.FLOAT:
                return resultSet.getFloat(columnIndex);
            case Types.DOUBLE:
                return resultSet.getDouble(columnIndex);
            case Types.TIME:
                return resultSet.getTime(columnIndex);
            case Types.DATE:
                return resultSet.getDate(columnIndex);
            case Types.TIMESTAMP:
                return resultSet.getTimestamp(columnIndex);
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                return resultSet.getString(columnIndex);
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return resultSet.getBytes(columnIndex);
            case Types.BLOB:
                Blob blob = resultSet.getBlob(columnIndex);
                return null == blob ? null : blob.getBytes(1L, (int) blob.length());
            case Types.CLOB:
                Clob clob = resultSet.getClob(columnIndex);
                return null == clob ? null : clob.getSubString(1L, (int) clob.length());
            case Types.NCLOB:
                NClob nClob = resultSet.getNClob(columnIndex);
                return null == nClob ? null : nClob.getSubString(1L, (int) nClob.length());
            case Types.ARRAY:
                return resultSet.getArray(columnIndex);
            default:
                return resultSet.getObject(columnIndex);
        }
    }
    
    private boolean isSigned(final ResultSetMetaData metaData, final int columnIndex) throws SQLException {
        return metaData.isSigned(columnIndex);
    }
}
