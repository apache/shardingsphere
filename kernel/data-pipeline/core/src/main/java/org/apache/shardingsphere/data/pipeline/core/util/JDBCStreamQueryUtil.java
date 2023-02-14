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

package org.apache.shardingsphere.data.pipeline.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JDBC stream query util.
 */
@Slf4j
public final class JDBCStreamQueryUtil {
    
    /**
     * Generate stream query prepared statement.
     *
     * @param connection connection
     * @param databaseType database type
     * @param sql sql
     * @return stream query prepared statement
     * @throws SQLException SQL exception
     */
    public static PreparedStatement generateStreamQueryPreparedStatement(final DatabaseType databaseType, final Connection connection, final String sql) throws SQLException {
        if (databaseType instanceof MySQLDatabaseType) {
            return generateMySQLStreamQueryPreparedStatement(connection, sql);
        }
        if (databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType) {
            return generatePostgreSQLStreamQueryPreparedStatement(connection, sql);
        }
        log.warn("not support {} streaming query now, pay attention to memory usage", databaseType.getType());
        return connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }
    
    private static PreparedStatement generateMySQLStreamQueryPreparedStatement(final Connection connection, final String sql) throws SQLException {
        PreparedStatement result = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        result.setFetchSize(Integer.MIN_VALUE);
        return result;
    }
    
    private static PreparedStatement generatePostgreSQLStreamQueryPreparedStatement(final Connection connection, final String sql) throws SQLException {
        PreparedStatement result = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
        connection.setAutoCommit(false);
        return result;
    }
}
