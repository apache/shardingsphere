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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.jdbc;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.row.QueryResultDataRow;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * JDBC rows loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JDBCRowsLoader {
    
    /**
     * Load rows.
     * 
     * @param columnCount column count
     * @param resultSet result set of JDBC
     * @return Query result data rows
     * @throws SQLException SQL exception
     */
    public static Collection<QueryResultDataRow> load(final int columnCount, final ResultSet resultSet) throws SQLException {
        Collection<QueryResultDataRow> result = new LinkedList<>();
        while (resultSet.next()) {
            List<Object> rowData = new ArrayList<>(columnCount);
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                Object rowValue = loadRowValue(resultSet, columnIndex);
                rowData.add(resultSet.wasNull() ? null : rowValue);
            }
            result.add(new QueryResultDataRow(rowData));
        }
        return result;
    }
    
    @SuppressWarnings("ReturnOfNull")
    private static Object loadRowValue(final ResultSet resultSet, final int columnIndex) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        switch (metaData.getColumnType(columnIndex)) {
            case Types.BOOLEAN:
                return resultSet.getBoolean(columnIndex);
            case Types.TINYINT:
            case Types.SMALLINT:
                return resultSet.getInt(columnIndex);
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
                return bigDecimal == null ? null : bigDecimal.toBigInteger();
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
            case Types.ARRAY:
                return resultSet.getArray(columnIndex);
            default:
                return resultSet.getObject(columnIndex);
        }
    }
}
