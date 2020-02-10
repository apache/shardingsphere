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

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.wrapper;

import org.apache.shardingsphere.underlying.executor.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.context.SQLUnit;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC executor wrapper.
 *
 * @author zhangliang
 */
public interface JDBCExecutorWrapper {
    
    /**
     * Route SQL.
     * 
     * @param sql SQL to be routed
     * @return execution context
     */
    ExecutionContext route(String sql);
    
    /**
     * Create statement.
     * 
     * @param connection connection
     * @param sqlUnit sql unit
     * @param isReturnGeneratedKeys is return generated keys
     * @return statement
     * @throws SQLException SQL exception
     */
    Statement createStatement(Connection connection, SQLUnit sqlUnit, boolean isReturnGeneratedKeys) throws SQLException;
    
    /**
     * Execute SQL.
     * 
     * @param statement statement
     * @param sql SQL to be executed
     * @param isReturnGeneratedKeys is return generated keys
     * @return {@code true} is for query, {@code false} is for update
     * @throws SQLException SQL exception
     */
    boolean executeSQL(Statement statement, String sql, boolean isReturnGeneratedKeys) throws SQLException;
}
