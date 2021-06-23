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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Connection savepoint manager for local transaction.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConnectionSavepointManager {
    
    private static final ConnectionSavepointManager INSTANCE = new ConnectionSavepointManager();
    
    private static final Map<Connection, Map<String, Savepoint>> CONNECTION_SAVEPOINT_MAP = new ConcurrentHashMap<>(128);
    
    /**
     * Get instance of connection savepoint manager.
     *
     * @return instance of connection savepoint manager
     */
    public static ConnectionSavepointManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Set savepoint.
     *
     * @param connection connection
     * @param savepointName savepoint name
     * @throws SQLException SQL Exception
     */
    public void setSavepoint(final Connection connection, final String savepointName) throws SQLException {
        Savepoint result = connection.setSavepoint(savepointName);
        CONNECTION_SAVEPOINT_MAP.computeIfAbsent(connection, unused -> new LinkedHashMap<>()).put(savepointName, result);
    }
    
    /**
     * Rollback to savepoint.
     *
     * @param connection connection
     * @param savepointName savepoint name
     * @throws SQLException SQL Exception
     */
    public void rollbackToSavepoint(final Connection connection, final String savepointName) throws SQLException {
        Optional<Savepoint> result = lookupSavepoint(connection, savepointName);
        if (result.isPresent()) {
            connection.rollback(result.get());
        }
    }
    
    /**
     * Release savepoint.
     *
     * @param connection connection
     * @param savepointName savepoint name
     * @throws SQLException SQL Exception
     */
    public void releaseSavepoint(final Connection connection, final String savepointName) throws SQLException {
        Optional<Savepoint> result = lookupSavepoint(connection, savepointName);
        if (result.isPresent()) {
            connection.releaseSavepoint(result.get());
        }
    }
    
    private Optional<Savepoint> lookupSavepoint(final Connection connection, final String savepointName) {
        return Optional.ofNullable(CONNECTION_SAVEPOINT_MAP.get(connection)).map(savepointMap -> savepointMap.get(savepointName));
    }
    
    /**
     * Transaction finished.
     *
     * @param connection connection
     */
    public void transactionFinished(final Connection connection) {
        CONNECTION_SAVEPOINT_MAP.remove(connection);
    }
}
