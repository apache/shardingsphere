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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.executor.callback;

import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.sane.JDBCSaneQueryResultEngineFactory;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.JDBCMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Optional;

/**
 * JDBC executor callback for proxy.
 */
public abstract class ProxyJDBCExecutorCallback extends JDBCExecutorCallback<ExecuteResult> {
    
    private final BackendConnection backendConnection;
    
    private final boolean isReturnGeneratedKeys;
    
    private final boolean fetchMetaData;
    
    private boolean hasMetaData;
    
    public ProxyJDBCExecutorCallback(final DatabaseType databaseType, final SQLStatement sqlStatement, final BackendConnection backendConnection,
                                     final boolean isReturnGeneratedKeys, final boolean isExceptionThrown, final boolean fetchMetaData) {
        super(databaseType, sqlStatement, isExceptionThrown);
        this.backendConnection = backendConnection;
        this.isReturnGeneratedKeys = isReturnGeneratedKeys;
        this.fetchMetaData = fetchMetaData;
    }
    
    @Override
    public ExecuteResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
        if (fetchMetaData && !hasMetaData) {
            hasMetaData = true;
            return executeSQL(sql, statement, connectionMode, true);
        }
        return executeSQL(sql, statement, connectionMode, false);
    }
    
    private ExecuteResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final boolean withMetadata) throws SQLException {
        backendConnection.add(statement);
        if (execute(sql, statement, isReturnGeneratedKeys)) {
            ResultSet resultSet = statement.getResultSet();
            backendConnection.add(resultSet);
            return createQueryResult(resultSet, connectionMode);
        }
        return new UpdateResult(statement.getUpdateCount(), isReturnGeneratedKeys ? getGeneratedKey(statement) : 0L);
    }
    
    protected abstract boolean execute(String sql, Statement statement, boolean isReturnGeneratedKeys) throws SQLException;
    
    private QueryResult createQueryResult(final ResultSet resultSet, final ConnectionMode connectionMode) throws SQLException {
        return ConnectionMode.MEMORY_STRICTLY == connectionMode ? new JDBCStreamQueryResult(resultSet) : new JDBCMemoryQueryResult(resultSet);
    }
    
    private long getGeneratedKey(final Statement statement) throws SQLException {
        ResultSet resultSet = statement.getGeneratedKeys();
        return resultSet.next() ? getGeneratedKeyIfInteger(resultSet) : 0L;
    }
    
    private long getGeneratedKeyIfInteger(final ResultSet resultSet) throws SQLException {
        switch (resultSet.getMetaData().getColumnType(1)) {
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                return resultSet.getLong(1);
            default:
                return 0L;
        }
    }
    
    @Override
    protected final Optional<ExecuteResult> getSaneResult(final SQLStatement sqlStatement) {
        return JDBCSaneQueryResultEngineFactory.newInstance(getFrontendDatabaseType()).getSaneQueryResult(sqlStatement);
    }
    
    private DatabaseType getFrontendDatabaseType() {
        Optional<DatabaseType> configuredDatabaseType = findConfiguredDatabaseType();
        if (configuredDatabaseType.isPresent()) {
            return configuredDatabaseType.get();
        }
        if (ProxyContext.getInstance().getMetaDataContexts().getAllSchemaNames().isEmpty()) {
            return DatabaseTypeRegistry.getTrunkDatabaseType("MySQL");
        }
        String schemaName = ProxyContext.getInstance().getMetaDataContexts().getAllSchemaNames().iterator().next();
        return ProxyContext.getInstance().getMetaDataContexts().getMetaData(schemaName).getResource().getDatabaseType();
    }
    
    private static Optional<DatabaseType> findConfiguredDatabaseType() {
        String configuredDatabaseType = ProxyContext.getInstance().getMetaDataContexts().getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE);
        return configuredDatabaseType.isEmpty() ? Optional.empty() : Optional.of(DatabaseTypeRegistry.getTrunkDatabaseType(configuredDatabaseType));
    }
}
