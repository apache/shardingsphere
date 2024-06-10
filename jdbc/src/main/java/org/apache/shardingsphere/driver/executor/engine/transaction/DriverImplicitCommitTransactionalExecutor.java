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

package org.apache.shardingsphere.driver.executor.engine.transaction;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.exception.dialect.SQLExceptionTransformEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.transaction.implicit.ImplicitTransactionCallback;

import java.sql.SQLException;

/**
 * Driver implicit commit transactional executor.
 */
@RequiredArgsConstructor
public final class DriverImplicitCommitTransactionalExecutor {
    
    private final ShardingSphereConnection connection;
    
    /**
     * Execute.
     * 
     * @param database database
     * @param callback implicit transaction callback
     * @param <T> type of return value
     * @return execution result
     * @throws SQLException SQL exception
     */
    public <T> T execute(final ShardingSphereDatabase database, final ImplicitTransactionCallback<T> callback) throws SQLException {
        try {
            connection.setAutoCommit(false);
            T result = callback.execute();
            connection.commit();
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            connection.rollback();
            throw SQLExceptionTransformEngine.toSQLException(ex, database.getProtocolType());
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
