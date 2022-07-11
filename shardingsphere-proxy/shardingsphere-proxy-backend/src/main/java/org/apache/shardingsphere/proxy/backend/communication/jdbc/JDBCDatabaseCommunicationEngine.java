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

package org.apache.shardingsphere.proxy.backend.communication.jdbc;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.metadata.JDBCQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.federation.executor.FederationContext;
import org.apache.shardingsphere.infra.federation.executor.FederationExecutor;
import org.apache.shardingsphere.infra.federation.executor.FederationExecutorFactory;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtil;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.ProxySQLExecutor;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.executor.callback.ProxyJDBCExecutorCallback;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.executor.callback.ProxyJDBCExecutorCallbackFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilderEngine;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.sharding.merge.common.IteratorStreamMergedResult;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * JDBC database communication engine.
 */
public final class JDBCDatabaseCommunicationEngine extends DatabaseCommunicationEngine<ResponseHeader> {
    
    private final ProxySQLExecutor proxySQLExecutor;
    
    private final Collection<Statement> cachedStatements = new CopyOnWriteArrayList<>();
    
    private final Collection<ResultSet> cachedResultSets = new CopyOnWriteArrayList<>();
    
    private final FederationExecutor federationExecutor;
    
    private final JDBCBackendConnection backendConnection;
    
    public JDBCDatabaseCommunicationEngine(final String driverType, final ShardingSphereDatabase database, final LogicSQL logicSQL, final JDBCBackendConnection backendConnection) {
        super(driverType, database, logicSQL, backendConnection);
        proxySQLExecutor = new ProxySQLExecutor(driverType, backendConnection, this);
        this.backendConnection = backendConnection;
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        String databaseName = backendConnection.getConnectionSession().getDatabaseName();
        DatabaseType databaseType = logicSQL.getSqlStatementContext().getDatabaseType();
        String schemaName = logicSQL.getSqlStatementContext().getTablesContext().getSchemaName().orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(databaseType, databaseName));
        federationExecutor = FederationExecutorFactory.newInstance(databaseName, schemaName, metaDataContexts.getOptimizerContext(), metaDataContexts.getMetaData().getGlobalRuleMetaData(),
                metaDataContexts.getMetaData().getProps(), new JDBCExecutor(BackendExecutorContext.getInstance().getExecutorEngine(), backendConnection.isSerialExecute()),
                ProxyContext.getInstance().getContextManager().getInstanceContext().getEventBusContext());
    }
    
    /**
     * Add statement.
     *
     * @param statement statement to be added
     */
    public void add(final Statement statement) {
        cachedStatements.add(statement);
    }
    
    /**
     * Add result set.
     *
     * @param resultSet result set to be added
     */
    public void add(final ResultSet resultSet) {
        cachedResultSets.add(resultSet);
    }
    
    /**
     * Execute to database.
     *
     * @return backend response
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @SneakyThrows(SQLException.class)
    public ResponseHeader execute() {
        LogicSQL logicSQL = getLogicSQL();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        ExecutionContext executionContext = getKernelProcessor().generateExecutionContext(
                logicSQL, getDatabase(), metaDataContexts.getMetaData().getGlobalRuleMetaData(), metaDataContexts.getMetaData().getProps());
        // TODO move federation route logic to binder
        SQLStatementContext<?> sqlStatementContext = logicSQL.getSqlStatementContext();
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabases().get(backendConnection.getConnectionSession().getDatabaseName());
        if (executionContext.getRouteContext().isFederated() || (sqlStatementContext instanceof SelectStatementContext
                && SystemSchemaUtil.containsSystemSchema(sqlStatementContext.getDatabaseType(), sqlStatementContext.getTablesContext().getSchemaNames(), database))) {
            ResultSet resultSet = doExecuteFederation(logicSQL, metaDataContexts);
            return processExecuteFederation(resultSet, metaDataContexts);
        }
        if (executionContext.getExecutionUnits().isEmpty()) {
            return new UpdateResponseHeader(executionContext.getSqlStatementContext().getSqlStatement());
        }
        proxySQLExecutor.checkExecutePrerequisites(executionContext);
        checkLockedDatabase(executionContext);
        List result = proxySQLExecutor.execute(executionContext);
        refreshMetaData(executionContext);
        Object executeResultSample = result.iterator().next();
        return executeResultSample instanceof QueryResult
                ? processExecuteQuery(executionContext, result, (QueryResult) executeResultSample)
                : processExecuteUpdate(executionContext, result);
    }
    
    private ResultSet doExecuteFederation(final LogicSQL logicSQL, final MetaDataContexts metaDataContexts) throws SQLException {
        boolean isReturnGeneratedKeys = logicSQL.getSqlStatementContext().getSqlStatement() instanceof MySQLInsertStatement;
        DatabaseType databaseType = metaDataContexts.getMetaData().getDatabases().get(backendConnection.getConnectionSession().getDatabaseName()).getResource().getDatabaseType();
        ProxyJDBCExecutorCallback callback = ProxyJDBCExecutorCallbackFactory.newInstance(getDriverType(), databaseType,
                logicSQL.getSqlStatementContext().getSqlStatement(), this, isReturnGeneratedKeys, SQLExecutorExceptionHandler.isExceptionThrown(), true);
        backendConnection.setFederationExecutor(federationExecutor);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine(isReturnGeneratedKeys, metaDataContexts);
        FederationContext context = new FederationContext(false, logicSQL, metaDataContexts.getMetaData().getDatabases());
        return federationExecutor.executeQuery(prepareEngine, callback, context);
    }
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine(final boolean isReturnGeneratedKeys, final MetaDataContexts metaData) {
        int maxConnectionsSizePerQuery = metaData.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        JDBCBackendStatement statementManager = (JDBCBackendStatement) backendConnection.getConnectionSession().getStatementManager();
        return new DriverExecutionPrepareEngine<>(getDriverType(), maxConnectionsSizePerQuery, backendConnection, statementManager,
                new StatementOption(isReturnGeneratedKeys), metaData.getMetaData().getDatabases().get(backendConnection.getConnectionSession().getDatabaseName()).getRuleMetaData().getRules());
    }
    
    private ResponseHeader processExecuteFederation(final ResultSet resultSet, final MetaDataContexts metaDataContexts) throws SQLException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        setQueryHeaders(new ArrayList<>(columnCount));
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabases().get(backendConnection.getConnectionSession().getDatabaseName());
        QueryHeaderBuilderEngine queryHeaderBuilderEngine = new QueryHeaderBuilderEngine(null == database ? null : database.getProtocolType());
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            getQueryHeaders().add(queryHeaderBuilderEngine.build(new JDBCQueryResultMetaData(resultSet.getMetaData()), database, columnIndex));
        }
        setMergedResult(new IteratorStreamMergedResult(Collections.singletonList(new JDBCStreamQueryResult(resultSet))));
        return new QueryResponseHeader(getQueryHeaders());
    }
    
    /**
     * Close database communication engine.
     *
     * @throws SQLException SQL exception
     */
    public void close() throws SQLException {
        Collection<SQLException> result = new LinkedList<>();
        result.addAll(closeResultSets());
        result.addAll(closeStatements());
        if (result.isEmpty()) {
            return;
        }
        SQLException ex = new SQLException();
        result.forEach(ex::setNextException);
        throw ex;
    }
    
    private Collection<SQLException> closeResultSets() {
        Collection<SQLException> result = new LinkedList<>();
        for (ResultSet each : cachedResultSets) {
            try {
                each.close();
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        cachedResultSets.clear();
        return result;
    }
    
    private Collection<SQLException> closeStatements() {
        Collection<SQLException> result = new LinkedList<>();
        for (Statement each : cachedStatements) {
            try {
                each.cancel();
                each.close();
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        cachedStatements.clear();
        return result;
    }
}
