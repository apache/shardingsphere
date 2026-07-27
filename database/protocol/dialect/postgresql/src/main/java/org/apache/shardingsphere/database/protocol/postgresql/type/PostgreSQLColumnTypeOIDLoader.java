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
import org.postgresql.core.BaseConnection;
import org.postgresql.core.Oid;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Loader for PostgreSQL column type OIDs.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLColumnTypeOIDLoader {
    
    /**
     * Load composite column type OIDs from result set metadata.
     *
     * @param connection database connection
     * @param metaData result set metadata
     * @return column indexes to type OIDs, or an empty map if no composite column type can be resolved
     * @throws SQLException SQL exception
     */
    public static Map<Integer, Integer> load(final Connection connection, final ResultSetMetaData metaData) throws SQLException {
        return connection.isWrapperFor(BaseConnection.class) ? getTypeOIDs(connection, metaData) : Collections.emptyMap();
    }
    
    private static Map<Integer, Integer> getTypeOIDs(final Connection connection, final ResultSetMetaData metaData) throws SQLException {
        BaseConnection baseConnection = connection.unwrap(BaseConnection.class);
        int columnCount = metaData.getColumnCount();
        Map<Integer, Integer> result = new HashMap<>();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            if (Types.STRUCT == metaData.getColumnType(columnIndex)) {
                int typeOID = baseConnection.getTypeInfo().getPGType(metaData.getColumnTypeName(columnIndex));
                if (Oid.UNSPECIFIED != typeOID) {
                    result.put(columnIndex, typeOID);
                }
            }
        }
        return result;
    }
    
    /**
     * Find type OID.
     *
     * @param connection database connection
     * @param columnTypeName column type name
     * @return type OID
     * @throws SQLException SQL exception
     */
    public static Optional<Integer> findTypeOID(final Connection connection, final String columnTypeName) throws SQLException {
        if (!connection.isWrapperFor(BaseConnection.class)) {
            return Optional.empty();
        }
        int typeOID = connection.unwrap(BaseConnection.class).getTypeInfo().getPGType(columnTypeName);
        return Oid.UNSPECIFIED == typeOID ? Optional.empty() : Optional.of(typeOID);
    }
}
