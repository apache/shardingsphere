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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query;

import org.apache.shardingsphere.infra.util.close.QuietlyCloser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Pipeline database resources.
 */
public final class PipelineDatabaseResources implements AutoCloseable {
    
    private final AtomicReference<Connection> connection = new AtomicReference<>();
    
    private final AtomicReference<PreparedStatement> preparedStatement = new AtomicReference<>();
    
    private final AtomicReference<ResultSet> resultSet = new AtomicReference<>();
    
    private final AtomicBoolean ready = new AtomicBoolean(false);
    
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
     * Whether database resources are ready.
     *
     * @return true if database resources are ready, false otherwise
     */
    public boolean isReady() {
        return ready.get();
    }
    
    /**
     * Set database resources ready.
     *
     * @param ready true if database resources are ready, false otherwise
     */
    public void setReady(final boolean ready) {
        this.ready.set(ready);
    }
    
    @Override
    public void close() {
        reset();
    }
    
    /**
     * Reset database resources.
     */
    public void reset() {
        setReady(false);
        QuietlyCloser.close(resultSet.get());
        QuietlyCloser.close(preparedStatement.get());
        QuietlyCloser.close(connection.get());
        resultSet.set(null);
        preparedStatement.set(null);
        connection.set(null);
    }
}
