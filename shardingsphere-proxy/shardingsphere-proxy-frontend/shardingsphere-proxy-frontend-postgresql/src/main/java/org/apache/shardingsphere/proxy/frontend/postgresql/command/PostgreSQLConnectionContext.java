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

package org.apache.shardingsphere.proxy.frontend.postgresql.command;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLValueFormat;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.PostgreSQLPortal;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.describe.PostgreSQLComDescribeExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * PostgreSQL connection context.
 */
@Setter
public final class PostgreSQLConnectionContext {
    
    @Getter
    private final ConcurrentMap<String, PostgreSQLBinaryStatement> binaryStatements = new ConcurrentHashMap<>(65535, 1);
    
    private final Map<String, PostgreSQLPortal> portals = new LinkedHashMap<>();
    
    @Getter
    private final Collection<CommandExecutor> pendingExecutors = new LinkedList<>();
    
    @Getter
    private long updateCount;
    
    /**
     * Create a portal.
     *
     * @param portal portal name
     * @param binaryStatement binary statement
     * @param parameters bind parameters
     * @param resultFormats result formats
     * @param backendConnection backend connection
     * @return a new portal
     * @throws SQLException SQL exception
     */
    public PostgreSQLPortal createPortal(final String portal, final PostgreSQLBinaryStatement binaryStatement, final List<Object> parameters, final List<PostgreSQLValueFormat> resultFormats,
                                         final BackendConnection backendConnection) throws SQLException {
        PostgreSQLPortal result = new PostgreSQLPortal(binaryStatement, parameters, resultFormats, backendConnection);
        portals.put(portal, result);
        return result;
    }
    
    /**
     * Get portal.
     *
     * @param portal portal name
     * @return portal
     */
    public PostgreSQLPortal getPortal(final String portal) {
        return portals.get(portal);
    }
    
    /**
     * Close portal.
     *
     * @param portal portal name
     * @throws SQLException SQL exception
     */
    public void closePortal(final String portal) throws SQLException {
        PostgreSQLPortal result = portals.remove(portal);
        if (null != result) {
            result.close();
        }
    }
    
    /**
     * Close all portals.
     */
    public void closeAllPortals() {
        Collection<SQLException> result = new LinkedList<>();
        for (PostgreSQLPortal each : portals.values()) {
            try {
                each.close();
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        portals.clear();
        if (result.isEmpty()) {
            return;
        }
        SQLException ex = new SQLException("Close all portals failed.");
        result.forEach(ex::setNextException);
    }
    
    /**
     * Get describe command executor.
     *
     * @return describe command executor
     */
    public Optional<PostgreSQLComDescribeExecutor> getDescribeExecutor() {
        return pendingExecutors.stream().filter(PostgreSQLComDescribeExecutor.class::isInstance).map(PostgreSQLComDescribeExecutor.class::cast).findFirst();
    }
    
    /**
     * Clear context.
     */
    public void clearContext() {
        pendingExecutors.clear();
        updateCount = 0;
    }
    
    /**
     * Get postgreSQL binary statement.
     * 
     * @param statementId statement Id
     * @return postgreSQL binary statement
     */
    public PostgreSQLBinaryStatement getPostgreSQLBinaryStatement(final String statementId) {
        return binaryStatements.getOrDefault(statementId, new PostgreSQLBinaryStatement("", new EmptyStatement(), Collections.emptyList()));
    }
}
