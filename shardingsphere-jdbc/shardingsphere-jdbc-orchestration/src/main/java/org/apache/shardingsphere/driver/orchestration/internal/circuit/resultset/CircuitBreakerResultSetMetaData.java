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

package org.apache.shardingsphere.driver.orchestration.internal.circuit.resultset;

import java.sql.ResultSetMetaData;

/**
 * Circuit breaker result set metadata.
 */
public final class CircuitBreakerResultSetMetaData implements ResultSetMetaData {
    
    @Override
    public int getColumnCount() {
        return 0;
    }
    
    @Override
    public boolean isAutoIncrement(final int column) {
        return false;
    }
    
    @Override
    public boolean isCaseSensitive(final int column) {
        return false;
    }
    
    @Override
    public boolean isSearchable(final int column) {
        return false;
    }
    
    @Override
    public boolean isCurrency(final int column) {
        return false;
    }
    
    @Override
    public int isNullable(final int column) {
        return ResultSetMetaData.columnNullable;
    }
    
    @Override
    public boolean isSigned(final int column) {
        return false;
    }
    
    @Override
    public int getColumnDisplaySize(final int column) {
        return 0;
    }
    
    @Override
    public String getColumnLabel(final int column) {
        return null;
    }
    
    @Override
    public String getColumnName(final int column) {
        return null;
    }
    
    @Override
    public String getSchemaName(final int column) {
        return null;
    }
    
    @Override
    public int getPrecision(final int column) {
        return 0;
    }
    
    @Override
    public int getScale(final int column) {
        return 0;
    }
    
    @Override
    public String getTableName(final int column) {
        return null;
    }
    
    @Override
    public String getCatalogName(final int column) {
        return null;
    }
    
    @Override
    public int getColumnType(final int column) {
        return 0;
    }
    
    @Override
    public String getColumnTypeName(final int column) {
        return null;
    }
    
    @Override
    public boolean isReadOnly(final int column) {
        return false;
    }
    
    @Override
    public boolean isWritable(final int column) {
        return false;
    }
    
    @Override
    public boolean isDefinitelyWritable(final int column) {
        return false;
    }
    
    @Override
    public String getColumnClassName(final int column) {
        return null;
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) {
        return null;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return false;
    }
}
