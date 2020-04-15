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

package org.apache.shardingsphere.underlying.executor.sql.connection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Execution connection.
 */
public interface ExecutionConnection {
    
    /**
     * Get connections.
     *
     * @param dataSourceName data source name
     * @param connectionSize connection size
     * @param connectionMode connection mode
     * @return connections
     * @throws SQLException SQL exception
     */
    List<Connection> getConnections(String dataSourceName, int connectionSize, ConnectionMode connectionMode) throws SQLException;
    
    /**
     * Create statement.
     *
     * @param connection connection
     * @param connectionMode connection mode
     * @param statementOption statement option
     * @return SQL Statement
     * @throws SQLException SQL exception
     */
    Statement createStatement(Connection connection, ConnectionMode connectionMode, StatementOption statementOption) throws SQLException;
    
    /**
     * Create prepared statement.
     *
     * @param sql SQL
     * @param parameters SQL parameters
     * @param connection connection
     * @param connectionMode connection mode
     * @param statementOption statement option
     * @return SQL prepared statement
     * @throws SQLException SQL exception
     */
    PreparedStatement createPreparedStatement(String sql, List<Object> parameters, Connection connection, ConnectionMode connectionMode, StatementOption statementOption) throws SQLException;
}
