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

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.decider.context.SQLFederationDeciderContext;
import org.apache.shardingsphere.infra.binder.decider.engine.SQLFederationDeciderEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
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
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.ProxySQLExecutor;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.executor.callback.ProxyJDBCExecutorCallback;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.executor.callback.ProxyJDBCExecutorCallbackFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.JDBCBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilderEngine;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.sharding.merge.common.IteratorStreamMergedResult;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * JDBC database communication engine.
 */
public final class JDBCDatabaseCommunicationEngine extends DatabaseCommunicationEngine {
    
    private final ProxySQLExecutor proxySQLExecutor;
    
    private final Collection<Statement> cachedStatements = new CopyOnWriteArrayList<>();
    
    private final Collection<ResultSet> cachedResultSets = new CopyOnWriteArrayList<>();
    
    private final JDBCBackendConnection backendConnection;
    
    private volatile FederationExecutor federationExecutor;
    
    public JDBCDatabaseCommunicationEngine(final String driverType, final ShardingSphereDatabase database, final QueryContext queryContext, final JDBCBackendConnection backendConnection) {
        super(driverType, database, queryContext, backendConnection);
        proxySQLExecutor = new ProxySQLExecutor(driverType, backendConnection, this);
        this.backendConnection = backendConnection;
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
    @Override
    public ResponseHeader execute() throws SQLException {
        QueryContext queryContext = getQueryContext();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(backendConnection.getConnectionSession().getDatabaseName());
        SQLFederationDeciderContext deciderContext = decide(queryContext, metaDataContexts.getMetaData().getProps(), database);
        if (deciderContext.isUseSQLFederation()) {
            prepareFederationExecutor();
            ResultSet resultSet = doExecuteFederation(queryContext, metaDataContexts);
            return processExecuteFederation(resultSet, metaDataContexts);
        }
        ExecutionContext executionContext = getKernelProcessor().generateExecutionContext(queryContext, getDatabase(), metaDataContexts.getMetaData().getGlobalRuleMetaData(),
                metaDataContexts.getMetaData().getProps(), backendConnection.getConnectionSession().getConnectionContext());
        if (executionContext.getExecutionUnits().isEmpty()) {
            return new UpdateResponseHeader(executionContext.getSqlStatementContext().getSqlStatement());
        }
        proxySQLExecutor.checkExecutePrerequisites(executionContext);
        TransactionStatus transactionStatus = backendConnection.getConnectionSession().getTransactionStatus();
        SQLStatement sqlStatement = executionContext.getSqlStatementContext().getSqlStatement();
        JDBCBackendTransactionManager transactionManager = null;
        if (TransactionType.XA == transactionStatus.getTransactionType() && transactionStatus.isInTransaction()
                && sqlStatement instanceof DMLStatement && !(sqlStatement instanceof SelectStatement) && executionContext.getExecutionUnits().size() > 1 ) {
            proxySQLExecutor.getJdbcExecutor().getJdbcExecutor().setSerial(true);
            transactionManager = new JDBCBackendTransactionManager(backendConnection);
            transactionManager.begin();
        }
        ResponseHeader result;
        try {
            List executeResults = proxySQLExecutor.execute(executionContext);
            refreshMetaData(executionContext);
            Object executeResultSample = executeResults.iterator().next();
            result = executeResultSample instanceof QueryResult
                    ? processExecuteQuery(executionContext, executeResults, (QueryResult) executeResultSample)
                    : processExecuteUpdate(executionContext, executeResults);
            if (null != transactionManager) {
                transactionManager.commit();
            }
        } catch (final SQLException ex) {
            if (null != transactionManager) {
                transactionManager.rollback();
            }
            throw ex;
        }
        return result;
    }
    
    private static SQLFederationDeciderContext decide(final QueryContext queryContext, final ConfigurationProperties props, final ShardingSphereDatabase database) {
        SQLFederationDeciderEngine deciderEngine = new SQLFederationDeciderEngine(database.getRuleMetaData().getRules(), props);
        return deciderEngine.decide(queryContext, database);
    }
    
    private void prepareFederationExecutor() {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        String databaseName = backendConnection.getConnectionSession().getDatabaseName();
        DatabaseType databaseType = getQueryContext().getSqlStatementContext().getDatabaseType();
        String schemaName = getQueryContext().getSqlStatementContext().getTablesContext().getSchemaName().orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(databaseType, databaseName));
        OptimizerContext optimizerContext = OptimizerContextFactory.create(metaDataContexts.getMetaData().getDatabases(), metaDataContexts.getMetaData().getGlobalRuleMetaData());
        federationExecutor = FederationExecutorFactory.newInstance(databaseName, schemaName, optimizerContext, metaDataContexts.getMetaData().getGlobalRuleMetaData(),
                metaDataContexts.getMetaData().getProps(), new JDBCExecutor(BackendExecutorContext.getInstance().getExecutorEngine(), backendConnection.isSerialExecute()),
                ProxyContext.getInstance().getContextManager().getInstanceContext().getEventBusContext());
    }
    
    private ResultSet doExecuteFederation(final QueryContext queryContext, final MetaDataContexts metaDataContexts) throws SQLException {
        boolean isReturnGeneratedKeys = queryContext.getSqlStatementContext().getSqlStatement() instanceof MySQLInsertStatement;
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(backendConnection.getConnectionSession().getDatabaseName());
        DatabaseType protocolType = database.getProtocolType();
        DatabaseType databaseType = database.getResource().getDatabaseType();
        ProxyJDBCExecutorCallback callback = ProxyJDBCExecutorCallbackFactory.newInstance(getDriverType(), protocolType, databaseType,
                queryContext.getSqlStatementContext().getSqlStatement(), this, isReturnGeneratedKeys, SQLExecutorExceptionHandler.isExceptionThrown(), true);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine(isReturnGeneratedKeys, metaDataContexts);
        FederationContext context = new FederationContext(false, queryContext, metaDataContexts.getMetaData().getDatabases());
        return federationExecutor.executeQuery(prepareEngine, callback, context);
    }
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine(final boolean isReturnGeneratedKeys, final MetaDataContexts metaData) {
        int maxConnectionsSizePerQuery = metaData.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        JDBCBackendStatement statementManager = (JDBCBackendStatement) backendConnection.getConnectionSession().getStatementManager();
        return new DriverExecutionPrepareEngine<>(getDriverType(), maxConnectionsSizePerQuery, backendConnection, statementManager,
                new StatementOption(isReturnGeneratedKeys), metaData.getMetaData().getDatabase(backendConnection.getConnectionSession().getDatabaseName()).getRuleMetaData().getRules(),
                backendConnection.getConnectionSession().getDatabaseType());
    }
    
    private ResponseHeader processExecuteFederation(final ResultSet resultSet, final MetaDataContexts metaDataContexts) throws SQLException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        setQueryHeaders(new ArrayList<>(columnCount));
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(backendConnection.getConnectionSession().getDatabaseName());
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
    @Override
    public void close() throws SQLException {
        Collection<SQLException> result = new LinkedList<>();
        result.addAll(closeResultSets());
        result.addAll(closeStatements());
        closeFederationExecutor().ifPresent(result::add);
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
    
    private Optional<SQLException> closeFederationExecutor() {
        if (null != federationExecutor) {
            try {
                federationExecutor.close();
            } catch (final SQLException ex) {
                return Optional.of(ex);
            }
        }
        return Optional.empty();
    }
}
