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

package org.apache.shardingsphere.database.connector.firebird.metadata.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for Firebird sizes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdSizeRegistry {
    
    private static final Map<String, Map<String, Integer>> COLUMN_SIZES = new ConcurrentHashMap<>();
    
    /**
     * Refresh column sizes for a table.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnSizes column sizes map
     */
    public static void refreshTable(final String schemaName, final String tableName, final Map<String, Integer> columnSizes) {
        if (null == tableName) {
            return;
        }
        String tableKey = buildTableKey(schemaName, tableName);
        if (columnSizes.isEmpty()) {
            COLUMN_SIZES.remove(tableKey);
            return;
        }
        Map<String, Integer> normalizedColumnSizes = new HashMap<>(columnSizes.size(), 1F);
        for (Map.Entry<String, Integer> entry : columnSizes.entrySet()) {
            if (null == entry.getKey()) {
                continue;
            }
            normalizedColumnSizes.put(toKey(entry.getKey()), entry.getValue());
        }
        if (normalizedColumnSizes.isEmpty()) {
            COLUMN_SIZES.remove(tableKey);
            return;
        }
        COLUMN_SIZES.put(tableKey, Collections.unmodifiableMap(normalizedColumnSizes));
    }
    
    /**
     * Find registered column size.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnName column name
     * @return column size
     */
    public static OptionalInt findColumnSize(final String schemaName, final String tableName, final String columnName) {
        if (null == tableName || null == columnName) {
            return OptionalInt.empty();
        }
        Map<String, Integer> tableSizes = COLUMN_SIZES.get(buildTableKey(schemaName, tableName));
        if (null == tableSizes) {
            return OptionalInt.empty();
        }
        Integer columnSize = tableSizes.get(toKey(columnName));
        return null == columnSize ? OptionalInt.empty() : OptionalInt.of(columnSize);
    }
    
    private static String buildTableKey(final String schemaName, final String tableName) {
        String schemaKey = null == schemaName ? "" : toKey(schemaName);
        String logicTable = trimToLogicTableName(tableName);
        return schemaKey + "." + toKey(logicTable);
    }
    
    private static String trimToLogicTableName(final String tableName) {
        int end = tableName.length() - 1;
        while (end >= 0 && !Character.isLetter(tableName.charAt(end))) {
            end--;
        }
        return end < 0 ? tableName : tableName.substring(0, end + 1);
    }
    
    private static String toKey(final String value) {
        return value.toUpperCase(Locale.ENGLISH);
    }
}
