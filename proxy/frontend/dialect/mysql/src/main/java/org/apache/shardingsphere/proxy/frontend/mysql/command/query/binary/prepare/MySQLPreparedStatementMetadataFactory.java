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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.PreparedStatementMetadataResolutionException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Metadata factory for MySQL prepared statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLPreparedStatementMetadataFactory {
    
    private static final Optional<Method> SERVER_PREPARE_STATEMENT_METHOD = findServerPrepareStatementMethod();
    
    private static final Optional<Class<?>> SERVER_PREPARED_STATEMENT_CLASS = findServerPreparedStatementClass();
    
    /**
     * Load actual prepared statement for metadata access.
     *
     * @param connectionSession connection session
     * @param preparedStatement prepared statement
     * @return actual prepared statement
     * @throws SQLException SQL exception
     */
    public static PreparedStatement load(final ConnectionSession connectionSession, final MySQLServerPreparedStatement preparedStatement) throws SQLException {
        ExecutionContext executionContext = createExecutionContext(connectionSession, preparedStatement);
        ExecutionUnit executionUnit = executionContext.getExecutionUnits().iterator().next();
        return getConnection(connectionSession, executionContext, executionUnit).prepareStatement(executionUnit.getSqlUnit().getSql());
    }
    
    private static ExecutionContext createExecutionContext(final ConnectionSession connectionSession, final MySQLServerPreparedStatement preparedStatement) {
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData();
        QueryContext queryContext = new QueryContext(preparedStatement.getSqlStatementContext(), preparedStatement.getSql(),
                Collections.emptyList(), preparedStatement.getHintValueContext(), connectionSession.getConnectionContext(), metaData);
        ExecutionContext result = new KernelProcessor().generateExecutionContext(queryContext, metaData.getGlobalRuleMetaData(), metaData.getProps());
        ShardingSpherePreconditions.checkNotEmpty(result.getExecutionUnits(), () -> new PreparedStatementMetadataResolutionException("no execution unit was generated"));
        return result;
    }
    
    private static Connection getConnection(final ConnectionSession connectionSession, final ExecutionContext executionContext, final ExecutionUnit executionUnit) throws SQLException {
        ProxyDatabaseConnectionManager databaseConnectionManager = connectionSession.getDatabaseConnectionManager();
        Collection<Connection> connections = databaseConnectionManager.getConnections(
                executionContext.getQueryContext().getUsedDatabase().getName(), executionUnit.getDataSourceName(), 0, 1, ConnectionMode.CONNECTION_STRICTLY);
        ShardingSpherePreconditions.checkNotEmpty(connections, () -> new PreparedStatementMetadataResolutionException("no backend connection was acquired"));
        return connections.iterator().next();
    }
    
    /**
     * Load native MySQL metadata without executing the query.
     *
     * @param connectionSession connection session
     * @param preparedStatement prepared statement
     * @return native metadata, or {@code null} if not provided by MySQL storage
     * @throws SQLException SQL exception or native preparation is unavailable
     */
    @HighFrequencyInvocation
    public static ResultSetMetaData loadMetadataWithoutExecution(final ConnectionSession connectionSession, final MySQLServerPreparedStatement preparedStatement) throws SQLException {
        ShardingSpherePreconditions.checkState(SERVER_PREPARE_STATEMENT_METHOD.isPresent() && SERVER_PREPARED_STATEMENT_CLASS.isPresent(),
                () -> new SQLFeatureNotSupportedException("Native MySQL prepared statement metadata is unavailable."));
        ExecutionContext executionContext = createExecutionContext(connectionSession, preparedStatement);
        ExecutionUnit executionUnit = executionContext.getExecutionUnits().iterator().next();
        Connection connection = getConnection(connectionSession, executionContext, executionUnit);
        if (!"MySQL".equals(DatabaseTypeFactory.get(connection.getMetaData()).getType())) {
            return null;
        }
        return loadMetadataWithoutExecution(connection, executionUnit.getSqlUnit().getSql());
    }
    
    private static ResultSetMetaData loadMetadataWithoutExecution(final Connection connection, final String sql) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            if (preparedStatement.isWrapperFor(SERVER_PREPARED_STATEMENT_CLASS.get())) {
                return preparedStatement.getMetaData();
            }
        }
        try (PreparedStatement preparedStatement = prepareServerStatement(connection, sql)) {
            return preparedStatement.getMetaData();
        }
    }
    
    @SneakyThrows(Throwable.class)
    private static PreparedStatement prepareServerStatement(final Connection connection, final String sql) throws SQLException {
        Method method = SERVER_PREPARE_STATEMENT_METHOD.get();
        try {
            return (PreparedStatement) method.invoke(connection.unwrap(method.getDeclaringClass()), sql);
        } catch (final InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
    
    private static Optional<Method> findServerPrepareStatementMethod() {
        try {
            return Optional.of(Class.forName("com.mysql.cj.jdbc.JdbcConnection").getMethod("serverPrepareStatement", String.class));
        } catch (final ClassNotFoundException | NoSuchMethodException ignored) {
            return Optional.empty();
        }
    }
    
    private static Optional<Class<?>> findServerPreparedStatementClass() {
        try {
            return Optional.of(Class.forName("com.mysql.cj.jdbc.ServerPreparedStatement"));
        } catch (final ClassNotFoundException ignored) {
            return Optional.empty();
        }
    }
}
