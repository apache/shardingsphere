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

package org.apache.shardingsphere.test.e2e.operation.pipeline.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SQL builder utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLBuilderUtils {
    
    /**
     * Build insert SQL.
     *
     * @param columnNames column names
     * @param tableName table name
     * @return insert SQL
     */
    public static String buildInsertSQL(final List<String> columnNames, final String tableName) {
        StringBuilder result = new StringBuilder("INSERT INTO %s (");
        for (String each : columnNames) {
            result.append(each).append(",");
        }
        result.setLength(result.length() - 1);
        result.append(") ").append("VALUES").append("(");
        result.append("?,".repeat(columnNames.size()));
        result.setLength(result.length() - 1);
        result.append(")");
        return String.format(result.toString(), tableName);
    }
    
    /**
     * Build update SQL.
     *
     * @param columnNames column names
     * @param tableName table name
     * @param placeholder placeholder
     * @return update SQL
     */
    public static String buildUpdateSQL(final List<String> columnNames, final String tableName, final String placeholder) {
        StringBuilder result = new StringBuilder("UPDATE %s SET ");
        for (String each : columnNames) {
            result.append(each).append("=").append(placeholder).append(",");
        }
        result.setLength(result.length() - 1);
        result.append(" WHERE order_id=?");
        return String.format(result.toString(), tableName);
    }
    
    /**
     * Build delete SQL.
     *
     * @param tableName table name
     * @param primaryKeyName primary key name
     * @return delete SQL
     */
    public static String buildDeleteSQL(final String tableName, final String primaryKeyName) {
        return String.format("DELETE FROM %s WHERE %s = ?", tableName, primaryKeyName);
    }
}
