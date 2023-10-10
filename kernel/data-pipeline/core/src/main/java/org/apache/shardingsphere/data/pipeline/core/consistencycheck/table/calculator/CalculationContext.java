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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Calculation context.
 */
@RequiredArgsConstructor
public final class CalculationContext implements AutoCloseable {
    
    private final AtomicReference<Connection> connection = new AtomicReference<>();
    
    private final AtomicReference<PreparedStatement> preparedStatement = new AtomicReference<>();
    
    private final AtomicReference<ResultSet> resultSet = new AtomicReference<>();
    
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
     * Get result set.
     *
     * @return result set
     */
    public ResultSet getResultSet() {
        return resultSet.get();
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
     * Set result set.
     *
     * @param resultSet result set
     */
    public void setResultSet(final ResultSet resultSet) {
        this.resultSet.set(resultSet);
    }
    
    @Override
    public void close() {
        QuietlyCloser.close(resultSet.get());
        QuietlyCloser.close(preparedStatement.get());
        QuietlyCloser.close(connection.get());
    }
}
