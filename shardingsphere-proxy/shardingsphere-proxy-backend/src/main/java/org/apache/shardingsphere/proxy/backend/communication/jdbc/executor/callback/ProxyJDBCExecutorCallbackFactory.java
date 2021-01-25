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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.executor.callback;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.executor.callback.impl.ProxyPreparedStatementExecutorCallback;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.executor.callback.impl.ProxyStatementExecutorCallback;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

/**
 * Proxy JDBC executor callback factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyJDBCExecutorCallbackFactory {
    
    /**
     * Create new instance of Proxy JDBC executor callback.
     * 
     * @param type driver type
     * @param databaseType database type
     * @param sqlStatement SQL statement
     * @param backendConnection backend connection
     * @param isReturnGeneratedKeys is return generated keys or not
     * @param isExceptionThrown is exception thrown or not
     * @param isFetchMetaData is fetch meta data or not
     * @return instance of Proxy JDBC executor callback
     */
    public static ProxyJDBCExecutorCallback newInstance(final String type, final DatabaseType databaseType, final SQLStatement sqlStatement, final BackendConnection backendConnection,
                                                        final boolean isReturnGeneratedKeys, final boolean isExceptionThrown, final boolean isFetchMetaData) {
        if (JDBCDriverType.STATEMENT.equals(type)) {
            return new ProxyStatementExecutorCallback(databaseType, sqlStatement, backendConnection, isReturnGeneratedKeys, isExceptionThrown, isFetchMetaData);
        }
        if (JDBCDriverType.PREPARED_STATEMENT.equals(type)) {
            return new ProxyPreparedStatementExecutorCallback(databaseType, sqlStatement, backendConnection, isReturnGeneratedKeys, isExceptionThrown, isFetchMetaData);
        }
        throw new UnsupportedOperationException(String.format("Unsupported driver type: `%s`", type));
    }
}
