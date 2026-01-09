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

package org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * Dialect data type option.
 */
public interface DialectDataTypeOption {
    
    /**
     * Get extra data types.
     *
     * @return extra data type map
     */
    Map<String, Integer> getExtraDataTypes();
    
    /**
     * Find extra SQL type class.
     *
     * @param dataType data type
     * @param unsigned whether unsigned
     * @return extra SQL type class
     */
    Optional<Class<?>> findExtraSQLTypeClass(int dataType, boolean unsigned);
    
    /**
     * Whether data type is integer type.
     *
     * @param sqlType value of java.sql.Types
     * @return is integer type or not
     */
    boolean isIntegerDataType(int sqlType);
    
    /**
     * Whether data type is string column.
     *
     * @param sqlType value of java.sql.Types
     * @return is string type or not
     */
    boolean isStringDataType(int sqlType);
    
    /**
     * Whether data type is binary type.
     * <p>it doesn't include BLOB etc.</p>
     *
     * @param sqlType value of java.sql.Types
     * @return is binary type or not
     */
    boolean isBinaryDataType(int sqlType);
    
    /**
     * Load UDT types.
     *
     * @param connection database connection
     * @return UDT type map
     * @throws SQLException when SQL Exception occurs
     */
    Map<String, Integer> loadUDTTypes(Connection connection) throws SQLException;
    
}
