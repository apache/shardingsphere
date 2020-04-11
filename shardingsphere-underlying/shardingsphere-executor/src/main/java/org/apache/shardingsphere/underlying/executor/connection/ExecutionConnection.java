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

package org.apache.shardingsphere.underlying.executor.connection;

import org.apache.shardingsphere.underlying.executor.constant.ConnectionMode;

import java.sql.Connection;
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
     * Create SQL statement.
     *
     * @param connection connection
     * @param sql SQL
     * @param parameters SQL parameters
     * @param connectionMode connection mode
     * @param statementOption statement option
     * @return SQL execute unit
     * @throws SQLException SQL exception
     */
    Statement createStatement(Connection connection, String sql, List<Object> parameters, ConnectionMode connectionMode, StatementOption statementOption) throws SQLException;
}
