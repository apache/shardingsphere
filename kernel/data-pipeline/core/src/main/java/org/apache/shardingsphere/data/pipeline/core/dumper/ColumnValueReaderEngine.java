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

package org.apache.shardingsphere.data.pipeline.core.dumper;

import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.DialectColumnValueReader;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

/**
 * Column value reader engine.
 */
public final class ColumnValueReaderEngine {
    
    private final DialectColumnValueReader dialectColumnValueReader;
    
    public ColumnValueReaderEngine(final DatabaseType databaseType) {
        dialectColumnValueReader = DatabaseTypedSPILoader.findService(DialectColumnValueReader.class, databaseType).orElse(null);
    }
    
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
        return null == dialectColumnValueReader ? Optional.empty() : dialectColumnValueReader.read(resultSet, metaData, columnIndex);
    }
    
    private static Object readStandardValue(final ResultSet resultSet, final ResultSetMetaData metaData, final int columnIndex) throws SQLException {
        int columnType = metaData.getColumnType(columnIndex);
        switch (columnType) {
            case Types.BOOLEAN:
                return resultSet.getBoolean(columnIndex);
            case Types.TINYINT:
                return metaData.isSigned(columnIndex) ? resultSet.getByte(columnIndex) : resultSet.getShort(columnIndex);
            case Types.SMALLINT:
                return metaData.isSigned(columnIndex) ? resultSet.getShort(columnIndex) : resultSet.getInt(columnIndex);
            case Types.INTEGER:
                return metaData.isSigned(columnIndex) ? resultSet.getInt(columnIndex) : resultSet.getLong(columnIndex);
            case Types.BIGINT:
                return metaData.isSigned(columnIndex) ? resultSet.getLong(columnIndex) : resultSet.getBigDecimal(columnIndex);
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
            case Types.CLOB:
                return resultSet.getClob(columnIndex);
            case Types.NCLOB:
                return resultSet.getNClob(columnIndex);
            case Types.BLOB:
                return resultSet.getBlob(columnIndex);
            case Types.ARRAY:
                return resultSet.getArray(columnIndex);
            default:
                return resultSet.getObject(columnIndex);
        }
    }
}
