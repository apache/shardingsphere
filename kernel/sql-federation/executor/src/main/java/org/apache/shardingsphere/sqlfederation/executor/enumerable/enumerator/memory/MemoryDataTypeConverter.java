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

package org.apache.shardingsphere.sqlfederation.executor.enumerable.enumerator.memory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.kernel.data.UnsupportedDataTypeConversionException;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.util.ResultSetUtils;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.sqlfederation.compiler.sql.type.SQLFederationDataTypeBuilder;

import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Enumerator utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MemoryDataTypeConverter {
    
    /**
     * Create column types.
     *
     * @param columns columns
     * @param databaseType database type
     * @return column types
     */
    public static Map<Integer, Class<?>> createColumnTypes(final List<ShardingSphereColumn> columns, final DatabaseType databaseType) {
        Map<Integer, Class<?>> result = new HashMap<>(columns.size(), 1F);
        for (int index = 0; index < columns.size(); index++) {
            int finalIndex = index;
            getSQLTypeClass(columns, databaseType, index).ifPresent(optional -> result.put(finalIndex, optional));
        }
        return result;
    }
    
    private static Optional<Class<?>> getSQLTypeClass(final List<ShardingSphereColumn> columns, final DatabaseType databaseType, final int index) {
        try {
            return Optional.of(SQLFederationDataTypeBuilder.getSqlTypeClass(databaseType, columns.get(index)));
        } catch (final IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
    
    /**
     * Convert to target type.
     *
     * @param columnTypes column types
     * @param rows rows
     * @return target type
     */
    public static Object[] convertToTargetType(final Map<Integer, Class<?>> columnTypes, final Object[] rows) {
        Object[] result = new Object[rows.length];
        for (int index = 0; index < rows.length; index++) {
            if (columnTypes.containsKey(index)) {
                result[index] = convertValue(rows, columnTypes, index);
            }
        }
        return result;
    }
    
    private static Object convertValue(final Object[] rows, final Map<Integer, Class<?>> columnTypes, final int index) {
        try {
            return ResultSetUtils.convertValue(rows[index], columnTypes.get(index));
        } catch (final SQLFeatureNotSupportedException | UnsupportedDataTypeConversionException ex) {
            return rows[index];
        }
    }
}
