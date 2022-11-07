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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper;

import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.ColumnValueReader;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Basic column value reader.
 */
public class BasicColumnValueReader implements ColumnValueReader {
    
    @Override
    public Object readValue(final ResultSet resultSet, final ResultSetMetaData metaData, final int columnIndex) throws SQLException {
        Object result = readValue0(resultSet, metaData, columnIndex);
        return resultSet.wasNull() ? null : result;
    }
    
    private Object readValue0(final ResultSet resultSet, final ResultSetMetaData metaData, final int columnIndex) throws SQLException {
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
