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

package org.apache.shardingsphere.database.connector.core.metadata.database.datatype;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data type registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class DataTypeRegistry {
    
    private static final Map<String, Map<String, Integer>> DATA_TYPES = new ConcurrentHashMap<>();
    
    /**
     * Load data types.
     *
     * @param dataSource data source to be loaded
     * @param databaseType database type
     * @throws SQLWrapperException SQL wrapper exception
     */
    public static void load(final DataSource dataSource, final String databaseType) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            if (!DATA_TYPES.containsKey(databaseType)) {
                DATA_TYPES.put(databaseType, new CaseInsensitiveMap<>(new DataTypeLoader().load(databaseMetaData, TypedSPILoader.getService(DatabaseType.class, databaseType))));
            }
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
    
    /**
     * Get data type.
     *
     * @param databaseType database type
     * @param dataTypeName data type name
     * @return data type
     */
    public static Optional<Integer> getDataType(final String databaseType, final String dataTypeName) {
        if (!DATA_TYPES.containsKey(databaseType)) {
            return Optional.empty();
        }
        if (!DATA_TYPES.get(databaseType).containsKey(dataTypeName)) {
            log.warn("Cannot find data type: {}.", dataTypeName);
            return Optional.empty();
        }
        return Optional.of(DATA_TYPES.get(databaseType).get(dataTypeName));
    }
}
