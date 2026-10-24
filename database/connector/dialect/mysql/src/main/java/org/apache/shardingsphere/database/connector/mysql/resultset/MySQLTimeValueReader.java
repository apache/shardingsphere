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

package org.apache.shardingsphere.database.connector.mysql.resultset;

import org.apache.shardingsphere.database.connector.core.resultset.ResultSetTimeValueReader;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.BitSet;

/**
 * MySQL TIME value reader scoped to one JDBC result set.
 */
final class MySQLTimeValueReader implements ResultSetTimeValueReader {
    
    private static final LocalDate EPOCH_DATE = LocalDate.of(1970, 1, 1);
    
    private BitSet unsupportedTimeColumns;
    
    @Override
    @HighFrequencyInvocation
    public Object read(final ResultSet resultSet, final int columnIndex) throws SQLException {
        if (null != unsupportedTimeColumns && unsupportedTimeColumns.get(columnIndex)) {
            return getFallbackTimeValue(resultSet, columnIndex);
        }
        try {
            return resultSet.getObject(columnIndex, LocalTime.class);
        } catch (final SQLFeatureNotSupportedException ignored) {
            Object result = getFallbackTimeValue(resultSet, columnIndex);
            if (null == unsupportedTimeColumns) {
                unsupportedTimeColumns = new BitSet();
            }
            unsupportedTimeColumns.set(columnIndex);
            return result;
        }
    }
    
    /**
     * Read the legacy value, retaining SQL {@code NULL} and non-epoch TIME values.
     *
     * @param resultSet result set
     * @param columnIndex column index
     * @return TIME value, or {@code null} for SQL {@code NULL}
     * @throws SQLException SQL exception
     */
    private Object getFallbackTimeValue(final ResultSet resultSet, final int columnIndex) throws SQLException {
        Time value = resultSet.getTime(columnIndex);
        if (null == value) {
            return null;
        }
        LocalDateTime dateTime = new Timestamp(value.getTime()).toLocalDateTime();
        return EPOCH_DATE.equals(dateTime.toLocalDate()) ? dateTime.toLocalTime() : value;
    }
}
