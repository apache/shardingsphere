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

package org.apache.shardingsphere.traffic.executor;

import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Traffic executor.
 */
public final class TrafficExecutor implements AutoCloseable {
    
    private Statement statement;
    
    /**
     * Execute.
     * 
     * @param executionUnit execution unit
     * @param callback traffic executor callback
     * @param <T> return type
     * @return execute result
     * @throws SQLException SQL exception
     */
    public <T> T execute(final JDBCExecutionUnit executionUnit, final TrafficExecutorCallback<T> callback) throws SQLException {
        SQLUnit sqlUnit = executionUnit.getExecutionUnit().getSqlUnit();
        cacheStatement(sqlUnit.getParameters(), executionUnit.getStorageResource());
        return callback.execute(statement, sqlUnit.getSql());
    }
    
    private void cacheStatement(final List<Object> parameters, final Statement statement) throws SQLException {
        this.statement = statement;
        setParameters(statement, parameters);
    }
    
    private void setParameters(final Statement statement, final List<Object> parameters) throws SQLException {
        if (!(statement instanceof PreparedStatement)) {
            return;
        }
        int index = 1;
        for (Object each : parameters) {
            ((PreparedStatement) statement).setObject(index++, each);
        }
    }
    
    /**
     * Get result set.
     *
     * @return result set
     * @throws SQLException SQL exception
     */
    public ResultSet getResultSet() throws SQLException {
        return statement.getResultSet();
    }
    
    @Override
    public void close() throws SQLException {
        if (null != statement) {
            Connection connection = statement.getConnection();
            statement.close();
            connection.close();
        }
    }
}
