/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.executor.sql.prepare;

import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.executor.StatementExecuteUnit;
import io.shardingsphere.core.routing.RouteUnit;

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
     * @param routeUnit route unit
     * @param connectionMode connection mode
     * @return SQL execute unit
     * @throws SQLException SQL exception
     */
    StatementExecuteUnit createStatementExecuteUnit(Connection connection, RouteUnit routeUnit, ConnectionMode connectionMode) throws SQLException;
}
