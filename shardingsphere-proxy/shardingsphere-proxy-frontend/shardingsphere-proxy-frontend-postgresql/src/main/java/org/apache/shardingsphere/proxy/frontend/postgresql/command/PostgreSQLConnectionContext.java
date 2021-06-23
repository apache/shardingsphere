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
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.PostgreSQLPortal;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.describe.PostgreSQLComDescribeExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * PostgreSQL connection context.
 */
@Setter
public final class PostgreSQLConnectionContext {
    
    private final Map<String, PostgreSQLPortal> portals = new LinkedHashMap<>();
    
    @Getter
    private final Collection<CommandExecutor> pendingExecutors = new LinkedList<>();
    
    private SQLStatement sqlStatement;
    
    @Getter
    private long updateCount;
    
    /**
     * Create a portal.
     *
     * @param portal portal name
     * @param sql sql
     * @param parameters bind parameters
     * @param resultFormats result formats
     * @param backendConnection backend connection
     * @return a new portal
     * @throws SQLException SQL exception
     */
    public PostgreSQLPortal createPortal(final String portal, final String sql, final List<Object> parameters, final List<PostgreSQLValueFormat> resultFormats,
                                         final BackendConnection backendConnection) throws SQLException {
        if (!getSqlStatement().isPresent()) {
            SQLStatement result = parseSql(sql, backendConnection.getSchemaName());
            setSqlStatement(result);
        }
        PostgreSQLPortal result = new PostgreSQLPortal(sqlStatement, sql, parameters, resultFormats, backendConnection);
        portals.put(portal, result);
        return result;
    }
    
    private SQLStatement parseSql(final String sql, final String schemaName) {
        if (sql.isEmpty()) {
            return new EmptyStatement();
        }
        ShardingSphereSQLParserEngine sqlStatementParserEngine = new ShardingSphereSQLParserEngine(
                DatabaseTypeRegistry.getTrunkDatabaseTypeName(ProxyContext.getInstance().getMetaDataContexts().getMetaData(schemaName).getResource().getDatabaseType()));
        return sqlStatementParserEngine.parse(sql, true);
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
     * Get SQL statement.
     *
     * @return SQL statement
     */
    public Optional<SQLStatement> getSqlStatement() {
        return Optional.ofNullable(sqlStatement);
    }
    
    /**
     * Clear context.
     */
    public void clearContext() {
        pendingExecutors.clear();
        sqlStatement = null;
        updateCount = 0;
    }
}
