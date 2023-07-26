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

package org.apache.shardingsphere.infra.database.core.datatype;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Data type loader.
 */
public final class DataTypeLoader {
    
    /**
     * Load data type.
     *
     * @param databaseMetaData database meta data
     * @param databaseType database type
     * @return data type map
     * @throws SQLException SQL exception
     */
    public Map<String, Integer> load(final DatabaseMetaData databaseMetaData, final DatabaseType databaseType) throws SQLException {
        Map<String, Integer> result = loadStandardDataTypes(databaseMetaData);
        result.putAll(loadDialectDataTypes(databaseType));
        return result;
    }
    
    private Map<String, Integer> loadStandardDataTypes(final DatabaseMetaData databaseMetaData) throws SQLException {
        Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        try (ResultSet resultSet = databaseMetaData.getTypeInfo()) {
            while (resultSet.next()) {
                result.put(resultSet.getString("TYPE_NAME"), resultSet.getInt("DATA_TYPE"));
            }
        }
        return result;
    }
    
    private Map<String, Integer> loadDialectDataTypes(final DatabaseType databaseType) throws SQLException {
        Optional<DialectDataTypeLoader> loader = DatabaseTypedSPILoader.findService(DialectDataTypeLoader.class, databaseType);
        if (!loader.isPresent()) {
            return Collections.emptyMap();
        }
        Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        result.putAll(loader.get().load());
        return result;
    }
}
