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

package org.apache.shardingsphere.data.pipeline.core.query;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * JDBC stream query builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class JDBCStreamQueryBuilder {
    
    /**
     * Build streamed prepared statement.
     *
     * @param connection connection
     * @param databaseType database type
     * @param sql SQL to be queried
     * @param batchSize batch size
     * @return built prepared statement
     * @throws SQLException SQL exception
     */
    public static PreparedStatement build(final DatabaseType databaseType, final Connection connection, final String sql, final int batchSize) throws SQLException {
        Optional<DialectJDBCStreamQueryBuilder> dialectBuilder = DatabaseTypedSPILoader.findService(DialectJDBCStreamQueryBuilder.class, databaseType);
        if (dialectBuilder.isPresent()) {
            return dialectBuilder.get().build(connection, sql, batchSize);
        }
        log.warn("not support {} streaming query now, pay attention to memory usage", databaseType.getType());
        return connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }
}
