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

package org.apache.shardingsphere.database.protocol.postgresql.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Loader for column type OIDs.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnTypeOIDLoader {
    
    /**
     * Load composite column type OIDs from result set metadata.
     *
     * @param connection database connection
     * @param metaData result set metadata
     * @param typeOIDResolver column type OID resolver
     * @return column indexes to type OIDs, or an empty map if no composite column type can be resolved
     * @throws SQLException SQL exception
     */
    public static Map<Integer, Integer> load(final Connection connection, final ResultSetMetaData metaData, final ColumnTypeOIDResolver typeOIDResolver) throws SQLException {
        int columnCount = metaData.getColumnCount();
        Map<Integer, Integer> result = new HashMap<>();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            if (Types.STRUCT == metaData.getColumnType(columnIndex)) {
                int currentColumnIndex = columnIndex;
                typeOIDResolver.findTypeOID(connection, metaData.getColumnTypeName(columnIndex)).ifPresent(typeOID -> result.put(currentColumnIndex, typeOID));
            }
        }
        return result;
    }
}
