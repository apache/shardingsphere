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
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.builder.JDBCExecutionUnitBuilderType;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.executor.callback.impl.ProxyPreparedStatementExecutorCallback;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.executor.callback.impl.ProxyStatementExecutorCallback;

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
     * @param backendConnection backend connection
     * @param isExceptionThrown is exception thrown or not
     * @param isReturnGeneratedKeys is return generated keys or not
     * @param isFetchMetaData is fetch meta data or not
     * @return instance of Proxy JDBC executor callback
     */
    public static ProxyJDBCExecutorCallback newInstance(final String type, final DatabaseType databaseType, final BackendConnection backendConnection,
                                                        final boolean isExceptionThrown, final boolean isReturnGeneratedKeys, final boolean isFetchMetaData) {
        if (JDBCExecutionUnitBuilderType.STATEMENT.equals(type)) {
            return new ProxyStatementExecutorCallback(databaseType, backendConnection, isExceptionThrown, isReturnGeneratedKeys, isFetchMetaData);
        }
        if (JDBCExecutionUnitBuilderType.PREPARED_STATEMENT.equals(type)) {
        return new ProxyPreparedStatementExecutorCallback(databaseType, backendConnection, isExceptionThrown, isReturnGeneratedKeys, isFetchMetaData);
        }
        throw new UnsupportedOperationException(String.format("Unsupported driver type: `%s`", type));
    }
}
