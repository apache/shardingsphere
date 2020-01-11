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

package org.apache.shardingsphere.sharding.execute.sql.prepare;

import org.apache.shardingsphere.underlying.executor.constant.ConnectionMode;
import org.apache.shardingsphere.sharding.execute.sql.StatementExecuteUnit;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * SQL execute prepare callback.
 *
 * @author zhangliang
 * @author panjuan
 */
public interface SQLExecutePrepareCallback {
    
    /**
     * Get connection.
     * 
     * @param connectionMode connection mode
     * @param dataSourceName data source name
     * @param connectionSize connection size
     * @return connection
     * @throws SQLException SQL exception
     */
    List<Connection> getConnections(ConnectionMode connectionMode, String dataSourceName, int connectionSize) throws SQLException;
    
    /**
     * Create SQL execute unit.
     * 
     * @param connection connection
     * @param executionUnit execution unit
     * @param connectionMode connection mode
     * @return SQL execute unit
     * @throws SQLException SQL exception
     */
    StatementExecuteUnit createStatementExecuteUnit(Connection connection, ExecutionUnit executionUnit, ConnectionMode connectionMode) throws SQLException;
}
