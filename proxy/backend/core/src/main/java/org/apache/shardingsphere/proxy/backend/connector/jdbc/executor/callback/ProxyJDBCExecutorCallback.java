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

package org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.callback;

import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.JDBCMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnector;
import org.apache.shardingsphere.proxy.backend.connector.sane.SaneQueryResultEngine;
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
    
    private final DatabaseConnector databaseConnector;
    
    private final boolean isReturnGeneratedKeys;
    
    private final boolean fetchMetaData;
    
    private boolean hasMetaData;
    
    protected ProxyJDBCExecutorCallback(final DatabaseType protocolType, final ShardingSphereResourceMetaData resourceMetaData, final SQLStatement sqlStatement,
                                        final DatabaseConnector databaseConnector,
                                        final boolean isReturnGeneratedKeys, final boolean isExceptionThrown, final boolean fetchMetaData) {
        super(protocolType, resourceMetaData, sqlStatement, isExceptionThrown);
        this.databaseConnector = databaseConnector;
        this.isReturnGeneratedKeys = isReturnGeneratedKeys;
        this.fetchMetaData = fetchMetaData;
    }
    
    @Override
    public ExecuteResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
        hasMetaData = fetchMetaData && !hasMetaData;
        databaseConnector.add(statement);
        if (execute(sql, statement, isReturnGeneratedKeys)) {
            ResultSet resultSet = statement.getResultSet();
            databaseConnector.add(resultSet);
            return createQueryResult(resultSet, connectionMode, storageType);
        }
        return new UpdateResult(statement.getUpdateCount(), isReturnGeneratedKeys ? getGeneratedKey(statement) : 0L);
    }
    
    protected abstract boolean execute(String sql, Statement statement, boolean isReturnGeneratedKeys) throws SQLException;
    
    private QueryResult createQueryResult(final ResultSet resultSet, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
        return ConnectionMode.MEMORY_STRICTLY == connectionMode ? new JDBCStreamQueryResult(resultSet) : new JDBCMemoryQueryResult(resultSet, storageType);
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
    protected final Optional<ExecuteResult> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
        return TypedSPILoader.getService(SaneQueryResultEngine.class, getProtocolTypeType().getType()).getSaneQueryResult(sqlStatement, ex);
    }
    
    private DatabaseType getProtocolTypeType() {
        Optional<DatabaseType> configuredDatabaseType = findConfiguredDatabaseType();
        if (configuredDatabaseType.isPresent()) {
            return configuredDatabaseType.get();
        }
        if (ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabases().isEmpty()) {
            return DatabaseTypeEngine.getTrunkDatabaseType("MySQL");
        }
        return ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabases().values().iterator().next().getProtocolType();
    }
    
    private Optional<DatabaseType> findConfiguredDatabaseType() {
        String configuredDatabaseType = ProxyContext.getInstance()
                .getContextManager().getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE);
        return configuredDatabaseType.isEmpty() ? Optional.empty() : Optional.of(DatabaseTypeEngine.getTrunkDatabaseType(configuredDatabaseType));
    }
}
