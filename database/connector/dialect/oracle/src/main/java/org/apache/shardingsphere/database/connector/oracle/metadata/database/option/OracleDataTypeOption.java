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

package org.apache.shardingsphere.database.connector.oracle.metadata.database.option;

import com.cedarsoftware.util.CaseInsensitiveMap;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DefaultDataTypeOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Data type option for Oracle.
 */
public final class OracleDataTypeOption implements DialectDataTypeOption {
    
    private static final Map<String, Integer> EXTRA_DATA_TYPES;
    
    private final DialectDataTypeOption delegate = new DefaultDataTypeOption();
    
    static {
        EXTRA_DATA_TYPES = setUpExtraDataTypes();
    }
    
    private static Map<String, Integer> setUpExtraDataTypes() {
        Map<String, Integer> result = new CaseInsensitiveMap<>();
        result.put("SMALLINT", Types.SMALLINT);
        result.put("TINYINT", Types.TINYINT);
        result.put("INT", Types.INTEGER);
        result.put("TEXT", Types.LONGVARCHAR);
        result.put("CHARACTER", Types.CHAR);
        result.put("VARCHAR2", Types.VARCHAR);
        result.put("DATETIME", Types.TIMESTAMP);
        result.put("ROWID", Types.ROWID);
        result.put("BINARY_DOUBLE", Types.DOUBLE);
        result.put("BINARY_FLOAT", Types.FLOAT);
        result.put("NUMBER", Types.NUMERIC);
        return result;
    }
    
    @Override
    public Map<String, Integer> getExtraDataTypes() {
        return EXTRA_DATA_TYPES;
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
