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

package org.apache.shardingsphere.sqlfederation.executor.enumerable.enumerator.jdbc;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.util.ResultSetUtils;
import org.apache.shardingsphere.infra.merge.result.MergedResult;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;

/**
 * JDBC data row enumerator.
 */
@RequiredArgsConstructor
public final class JDBCDataRowEnumerator implements Enumerator<Object> {
    
    private final MergedResult queryResult;
    
    private final QueryResultMetaData metaData;
    
    private final Collection<Statement> statements;
    
    private Object currentRow;
    
    @Override
    public Object current() {
        return currentRow;
    }
    
    @SneakyThrows(SQLException.class)
    @Override
    public boolean moveNext() {
        if (queryResult.next()) {
            setCurrentRow();
            return true;
        }
        return false;
    }
    
    private void setCurrentRow() throws SQLException {
        Object[] rowValues = new Object[metaData.getColumnCount()];
        for (int i = 0; i < metaData.getColumnCount(); i++) {
            rowValues[i] = getValue(i);
        }
        currentRow = 1 == metaData.getColumnCount() ? rowValues[0] : rowValues;
    }
    
    @HighFrequencyInvocation
    private Object getValue(final int index) throws SQLException {
        int columnIndex = index + 1;
        int columnType = metaData.getColumnType(columnIndex);
        Object result = readColumnValue(columnIndex, columnType);
        return convertToCalciteValue(result, columnType);
    }
    
    /**
     * Read a column value, retaining {@code null} for SQL {@code NULL}.
     *
     * @param columnIndex column index
     * @param columnType JDBC column type
     * @return column value, or {@code null} for SQL {@code NULL}
     * @throws SQLException SQL exception
     */
    private Object readColumnValue(final int columnIndex, final int columnType) throws SQLException {
        Class<?> valueType = Types.TIME == columnType ? Time.class : Object.class;
        return queryResult.getValue(columnIndex, valueType);
    }
    
    /**
     * Adapt a JDBC value to Calcite, retaining {@code null} for SQL {@code NULL}.
     *
     * @param value original value, or {@code null} for SQL {@code NULL}
     * @param columnType JDBC column type
     * @return Calcite value, or {@code null} for SQL {@code NULL}
     * @throws SQLException SQL exception
     */
    private Object convertToCalciteValue(final Object value, final int columnType) throws SQLException {
        if (Types.TIME == columnType && value instanceof LocalTime) {
            return ResultSetUtils.convertTimeValue((LocalTime) value, Time.class);
        }
        return value instanceof LocalDateTime ? ResultSetUtils.convertValue(value, Timestamp.class) : value;
    }
    
    @Override
    public void reset() {
    }
    
    @Override
    public void close() {
        SQLException failure = null;
        for (Statement each : statements) {
            try {
                each.close();
            } catch (final SQLException ex) {
                failure = appendFailure(failure, ex);
            }
        }
        currentRow = null;
        if (null != failure) {
            throw new SQLWrapperException(failure);
        }
    }
    
    private SQLException appendFailure(final SQLException previousFailure, final SQLException failure) {
        if (null == previousFailure) {
            return failure;
        }
        if (previousFailure != failure) {
            previousFailure.addSuppressed(failure);
        }
        return previousFailure;
    }
}
