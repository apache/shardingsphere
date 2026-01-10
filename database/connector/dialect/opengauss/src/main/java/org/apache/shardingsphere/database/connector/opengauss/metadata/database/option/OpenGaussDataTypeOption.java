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

package org.apache.shardingsphere.database.connector.opengauss.metadata.database.option;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;
import org.apache.shardingsphere.database.connector.postgresql.metadata.database.option.PostgreSQLDataTypeOption;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Data type option for openGauss.
 */
public final class OpenGaussDataTypeOption implements DialectDataTypeOption {
    
    private final DialectDataTypeOption delegate = new PostgreSQLDataTypeOption();
    
    @Override
    public Map<String, Integer> getExtraDataTypes() {
        return delegate.getExtraDataTypes();
    }
    
    @Override
    public Optional<Class<?>> findExtraSQLTypeClass(final int dataType, final boolean unsigned) {
        return Optional.empty();
    }
    
    @Override
    public boolean isIntegerDataType(final int sqlType) {
        return delegate.isIntegerDataType(sqlType);
    }
    
    @Override
    public boolean isStringDataType(final int sqlType) {
        return delegate.isStringDataType(sqlType);
    }
    
    @Override
    public boolean isBinaryDataType(final int sqlType) {
        return delegate.isBinaryDataType(sqlType);
    }
    
    /**
     * Load user-defined data types from the database.
     *
     * @param connection database connection
     * @return mapping of UDT type name to JDBC type
     * @throws SQLException SQL exception
     */
    @Override
    public Map<String, Integer> loadUDTTypes(final Connection connection) throws SQLException {
        return new HashMap<>();
    }
}
