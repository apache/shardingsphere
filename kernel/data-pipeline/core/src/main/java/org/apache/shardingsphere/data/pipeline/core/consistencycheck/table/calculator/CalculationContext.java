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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator;

import lombok.Getter;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Calculation context.
 */
public final class CalculationContext implements AutoCloseable {
    
    private final AtomicReference<Connection> connection = new AtomicReference<>();
    
    private final AtomicReference<PreparedStatement> preparedStatement = new AtomicReference<>();
    
    private final AtomicReference<ResultSet> resultSet = new AtomicReference<>();
    
    private final AtomicBoolean databaseResourcesReady = new AtomicBoolean(false);
    
    @Getter
    private final Deque<Map<String, Object>> recordDeque = new LinkedList<>();
    
    /**
     * Get connection.
     *
     * @return connection
     */
    public Connection getConnection() {
        return connection.get();
    }
    
    /**
     * Set connection.
     *
     * @param connection connection
     */
    public void setConnection(final Connection connection) {
        this.connection.set(connection);
    }
    
    /**
     * Set prepared statement.
     *
     * @param preparedStatement prepared statement
     */
    public void setPreparedStatement(final PreparedStatement preparedStatement) {
        this.preparedStatement.set(preparedStatement);
    }
    
    /**
     * Get result set.
     *
     * @return result set
     */
    public ResultSet getResultSet() {
        return resultSet.get();
    }
    
    /**
     * Set result set.
     *
     * @param resultSet result set
     */
    public void setResultSet(final ResultSet resultSet) {
        this.resultSet.set(resultSet);
    }
    
    /**
     * Check if database resources are ready.
     *
     * @return true if database resources are ready, false otherwise
     */
    public boolean isDatabaseResourcesReady() {
        return databaseResourcesReady.get();
    }
    
    /**
     * Set database resources ready.
     *
     * @param databaseResourcesReady true if database resources are ready, false otherwise
     */
    public void setDatabaseResourcesReady(final boolean databaseResourcesReady) {
        this.databaseResourcesReady.set(databaseResourcesReady);
    }
    
    @Override
    public void close() {
        resetDatabaseResources();
        recordDeque.clear();
    }
    
    /**
     * Reset database resources.
     */
    public void resetDatabaseResources() {
        setDatabaseResourcesReady(false);
        QuietlyCloser.close(resultSet.get());
        QuietlyCloser.close(preparedStatement.get());
        QuietlyCloser.close(connection.get());
        resultSet.set(null);
        preparedStatement.set(null);
        connection.set(null);
    }
}
