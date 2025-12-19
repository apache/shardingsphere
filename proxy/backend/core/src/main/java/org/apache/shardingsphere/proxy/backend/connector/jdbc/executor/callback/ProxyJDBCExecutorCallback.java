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

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.JDBCMemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnector;
import org.apache.shardingsphere.proxy.backend.connector.sane.DialectSaneQueryResultEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Optional;

/**
 * JDBC executor callback for proxy.
 */
public abstract class ProxyJDBCExecutorCallback extends JDBCExecutorCallback<ExecuteResult> {
    
    private final DatabaseProxyConnector databaseProxyConnector;
    
    private final boolean isReturnGeneratedKeys;
    
    private final boolean fetchMetaData;
    
    private boolean hasMetaData;
    
    protected ProxyJDBCExecutorCallback(final DatabaseType protocolType, final ResourceMetaData resourceMetaData, final SQLStatement sqlStatement,
                                        final DatabaseProxyConnector databaseProxyConnector,
                                        final boolean isReturnGeneratedKeys, final boolean isExceptionThrown, final boolean fetchMetaData) {
        super(protocolType, resourceMetaData, sqlStatement, isExceptionThrown);
        this.databaseProxyConnector = databaseProxyConnector;
        this.isReturnGeneratedKeys = isReturnGeneratedKeys;
        this.fetchMetaData = fetchMetaData;
    }
    
    @Override
    public ExecuteResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
        hasMetaData = fetchMetaData && !hasMetaData;
        databaseProxyConnector.add(statement);
        if (execute(sql, statement, isReturnGeneratedKeys)) {
            ResultSet resultSet = statement.getResultSet();
            databaseProxyConnector.add(resultSet);
            return createQueryResult(resultSet, connectionMode, storageType);
        }
        return new UpdateResult(Math.max(statement.getUpdateCount(), 0), isReturnGeneratedKeys ? getGeneratedKey(statement) : 0L);
    }
    
    protected abstract boolean execute(String sql, Statement statement, boolean isReturnGeneratedKeys) throws SQLException;
    
    private QueryResult createQueryResult(final ResultSet resultSet, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
        return ConnectionMode.MEMORY_STRICTLY == connectionMode ? new JDBCStreamQueryResult(resultSet) : new JDBCMemoryQueryResult(resultSet, storageType);
    }
    
    private long getGeneratedKey(final Statement statement) throws SQLException {
        try {
            ResultSet resultSet = statement.getGeneratedKeys();
            return resultSet.next() ? getGeneratedKeyIfInteger(resultSet) : 0L;
        } catch (final SQLFeatureNotSupportedException ignore) {
            return 0L;
        }
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
        return DatabaseTypedSPILoader.findService(DialectSaneQueryResultEngine.class, getProtocolTypeType()).flatMap(optional -> optional.getSaneQueryResult(sqlStatement, ex));
    }
    
    private DatabaseType getProtocolTypeType() {
        DatabaseType configuredDatabaseType = ProxyContext.getInstance()
                .getContextManager().getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE);
        if (null != configuredDatabaseType) {
            return configuredDatabaseType;
        }
        if (ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getAllDatabases().isEmpty()) {
            return TypedSPILoader.getService(DatabaseType.class, "MySQL");
        }
        return ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getAllDatabases().iterator().next().getProtocolType();
    }
}
