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

package org.apache.shardingsphere.proxy.backend.connector;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.dialect.SQLExceptionTransformEngine;
import org.apache.shardingsphere.infra.binder.aware.CursorDefinitionAware;
import org.apache.shardingsphere.infra.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.type.CursorAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.connection.refresher.MetaDataRefreshEngine;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.metadata.JDBCQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtils;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.callback.ProxyJDBCExecutorCallback;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.callback.ProxyJDBCExecutorCallbackFactory;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.BackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.StorageUnitNotExistedException;
import org.apache.shardingsphere.proxy.backend.handler.data.DatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilderEngine;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.sharding.merge.common.IteratorStreamMergedResult;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sqlfederation.executor.SQLFederationExecutorContext;
import org.apache.shardingsphere.transaction.api.TransactionType;

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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database connector.
 */
public final class DatabaseConnector implements DatabaseBackendHandler {
    
    private final ProxySQLExecutor proxySQLExecutor;
    
    private final Collection<Statement> cachedStatements = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    private final Collection<ResultSet> cachedResultSets = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    private final String driverType;
    
    private final ShardingSphereDatabase database;
    
    private final boolean selectContainsEnhancedTable;
    
    private final QueryContext queryContext;
    
    private final ProxyDatabaseConnectionManager databaseConnectionManager;
    
    private List<QueryHeader> queryHeaders;
    
    private MergedResult mergedResult;
    
    public DatabaseConnector(final String driverType, final ShardingSphereDatabase database, final QueryContext queryContext, final ProxyDatabaseConnectionManager databaseConnectionManager) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        failedIfBackendNotReady(databaseConnectionManager.getConnectionSession(), sqlStatementContext);
        this.driverType = driverType;
        this.database = database;
        this.queryContext = queryContext;
        this.selectContainsEnhancedTable = sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isContainsEnhancedTable();
        this.databaseConnectionManager = databaseConnectionManager;
        if (sqlStatementContext instanceof CursorAvailable) {
            prepareCursorStatementContext((CursorAvailable) sqlStatementContext, databaseConnectionManager.getConnectionSession());
        }
        proxySQLExecutor = new ProxySQLExecutor(driverType, databaseConnectionManager, this, queryContext);
    }
    
    private void failedIfBackendNotReady(final ConnectionSession connectionSession, final SQLStatementContext sqlStatementContext) {
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(connectionSession.getDatabaseName());
        boolean isSystemSchema = SystemSchemaUtils.containsSystemSchema(sqlStatementContext.getDatabaseType(), sqlStatementContext.getTablesContext().getSchemaNames(), database);
        ShardingSpherePreconditions.checkState(isSystemSchema || database.containsDataSource(), () -> new StorageUnitNotExistedException(connectionSession.getDatabaseName()));
        if (!isSystemSchema && !database.isComplete()) {
            throw new RuleNotExistedException(connectionSession.getDatabaseName());
        }
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
     * @throws SQLException SQL exception
     */
    @Override
    public ResponseHeader execute() throws SQLException {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        if (proxySQLExecutor.getSqlFederationEngine().decide(queryContext.getSqlStatementContext(), queryContext.getParameters(), database, metaDataContexts.getMetaData().getGlobalRuleMetaData())) {
            ResultSet resultSet = doExecuteFederation(queryContext, metaDataContexts);
            return processExecuteFederation(resultSet, metaDataContexts);
        }
        Collection<ExecutionContext> executionContexts = generateExecutionContexts();
        return isNeedImplicitCommitTransaction(executionContexts) ? doExecuteWithImplicitCommitTransaction(executionContexts) : doExecute(executionContexts);
    }
    
    private Collection<ExecutionContext> generateExecutionContexts() {
        Collection<ExecutionContext> result = new LinkedList<>();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        ExecutionContext executionContext = new KernelProcessor().generateExecutionContext(queryContext, database, metaDataContexts.getMetaData().getGlobalRuleMetaData(),
                metaDataContexts.getMetaData().getProps(), databaseConnectionManager.getConnectionSession().getConnectionContext());
        result.add(executionContext);
        // TODO support logical SQL optimize to generate multiple logical SQL
        return result;
    }
    
    private boolean isNeedImplicitCommitTransaction(final Collection<ExecutionContext> executionContexts) {
        TransactionStatus transactionStatus = databaseConnectionManager.getConnectionSession().getTransactionStatus();
        if (!TransactionType.isDistributedTransaction(transactionStatus.getTransactionType()) || transactionStatus.isInTransaction()) {
            return false;
        }
        if (1 == executionContexts.size()) {
            SQLStatement sqlStatement = executionContexts.iterator().next().getSqlStatementContext().getSqlStatement();
            return isWriteDMLStatement(sqlStatement) && executionContexts.iterator().next().getExecutionUnits().size() > 1;
        }
        return executionContexts.stream().anyMatch(each -> isWriteDMLStatement(each.getSqlStatementContext().getSqlStatement()));
    }
    
    private boolean isWriteDMLStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof DMLStatement && !(sqlStatement instanceof SelectStatement);
    }
    
    private ResponseHeader doExecuteWithImplicitCommitTransaction(final Collection<ExecutionContext> executionContexts) throws SQLException {
        ResponseHeader result;
        BackendTransactionManager transactionManager = new BackendTransactionManager(databaseConnectionManager);
        try {
            transactionManager.begin();
            result = doExecute(executionContexts);
            transactionManager.commit();
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            transactionManager.rollback();
            String databaseName = databaseConnectionManager.getConnectionSession().getDatabaseName();
            throw SQLExceptionTransformEngine.toSQLException(ex, ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getProtocolType());
        }
        return result;
    }
    
    private ResponseHeader doExecute(final Collection<ExecutionContext> executionContexts) throws SQLException {
        ResponseHeader result = null;
        for (ExecutionContext each : executionContexts) {
            ResponseHeader responseHeader = doExecute(each);
            if (null == result) {
                result = responseHeader;
            }
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private ResponseHeader doExecute(final ExecutionContext executionContext) throws SQLException {
        if (executionContext.getExecutionUnits().isEmpty()) {
            return new UpdateResponseHeader(executionContext.getSqlStatementContext().getSqlStatement());
        }
        proxySQLExecutor.checkExecutePrerequisites(executionContext);
        List result = proxySQLExecutor.execute(executionContext);
        refreshMetaData(executionContext);
        Object executeResultSample = result.iterator().next();
        return executeResultSample instanceof QueryResult ? processExecuteQuery(executionContext, result, (QueryResult) executeResultSample) : processExecuteUpdate(executionContext, result);
    }
    
    private ResultSet doExecuteFederation(final QueryContext queryContext, final MetaDataContexts metaDataContexts) {
        boolean isReturnGeneratedKeys = queryContext.getSqlStatementContext().getSqlStatement() instanceof MySQLInsertStatement;
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseConnectionManager.getConnectionSession().getDatabaseName());
        DatabaseType protocolType = database.getProtocolType();
        ProxyJDBCExecutorCallback callback = ProxyJDBCExecutorCallbackFactory.newInstance(driverType, protocolType, database.getResourceMetaData(),
                queryContext.getSqlStatementContext().getSqlStatement(), this, isReturnGeneratedKeys, SQLExecutorExceptionHandler.isExceptionThrown(), true);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine(isReturnGeneratedKeys, metaDataContexts);
        SQLFederationExecutorContext context = new SQLFederationExecutorContext(false, queryContext, metaDataContexts.getMetaData());
        return proxySQLExecutor.getSqlFederationEngine().executeQuery(prepareEngine, callback, context);
    }
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine(final boolean isReturnGeneratedKeys, final MetaDataContexts metaData) {
        int maxConnectionsSizePerQuery = metaData.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        JDBCBackendStatement statementManager = (JDBCBackendStatement) databaseConnectionManager.getConnectionSession().getStatementManager();
        return new DriverExecutionPrepareEngine<>(driverType, maxConnectionsSizePerQuery, databaseConnectionManager, statementManager,
                new StatementOption(isReturnGeneratedKeys), metaData.getMetaData().getDatabase(databaseConnectionManager.getConnectionSession().getDatabaseName()).getRuleMetaData().getRules(),
                metaData.getMetaData().getDatabase(databaseConnectionManager.getConnectionSession().getDatabaseName()).getResourceMetaData().getStorageTypes());
    }
    
    private ResponseHeader processExecuteFederation(final ResultSet resultSet, final MetaDataContexts metaDataContexts) throws SQLException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        queryHeaders = new ArrayList<>(columnCount);
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseConnectionManager.getConnectionSession().getDatabaseName());
        QueryHeaderBuilderEngine queryHeaderBuilderEngine = new QueryHeaderBuilderEngine(null == database ? null : database.getProtocolType());
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            queryHeaders.add(queryHeaderBuilderEngine.build(new JDBCQueryResultMetaData(resultSet.getMetaData()), database, columnIndex));
        }
        mergedResult = new IteratorStreamMergedResult(Collections.singletonList(new JDBCStreamQueryResult(resultSet)));
        return new QueryResponseHeader(queryHeaders);
    }
    
    private void prepareCursorStatementContext(final CursorAvailable statementContext, final ConnectionSession connectionSession) {
        if (statementContext.getCursorName().isPresent()) {
            String cursorName = statementContext.getCursorName().get().getIdentifier().getValue().toLowerCase();
            prepareCursorStatementContext(statementContext, connectionSession, cursorName);
        }
        if (statementContext instanceof CloseStatementContext && ((CloseStatementContext) statementContext).getSqlStatement().isCloseAll()) {
            connectionSession.getConnectionContext().clearCursorConnectionContext();
        }
    }
    
    private void prepareCursorStatementContext(final CursorAvailable statementContext, final ConnectionSession connectionSession, final String cursorName) {
        if (statementContext instanceof CursorStatementContext) {
            connectionSession.getConnectionContext().getCursorContext().getCursorDefinitions().put(cursorName, (CursorStatementContext) statementContext);
        }
        if (statementContext instanceof CursorDefinitionAware) {
            CursorStatementContext cursorStatementContext = (CursorStatementContext) connectionSession.getConnectionContext().getCursorContext().getCursorDefinitions().get(cursorName);
            Preconditions.checkArgument(null != cursorStatementContext, "Cursor %s does not exist.", cursorName);
            ((CursorDefinitionAware) statementContext).setUpCursorDefinition(cursorStatementContext);
        }
        if (statementContext instanceof CloseStatementContext) {
            connectionSession.getConnectionContext().getCursorContext().removeCursor(cursorName);
        }
    }
    
    private void refreshMetaData(final ExecutionContext executionContext) throws SQLException {
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        new MetaDataRefreshEngine(contextManager.getInstanceContext().getModeContextManager(), database,
                contextManager.getMetaDataContexts().getMetaData().getProps()).refresh(executionContext.getSqlStatementContext(), executionContext.getRouteContext().getRouteUnits());
    }
    
    private QueryResponseHeader processExecuteQuery(final ExecutionContext executionContext, final List<QueryResult> queryResults, final QueryResult queryResultSample) throws SQLException {
        queryHeaders = createQueryHeaders(executionContext, queryResultSample);
        mergedResult = mergeQuery(executionContext.getSqlStatementContext(), queryResults);
        return new QueryResponseHeader(queryHeaders);
    }
    
    private List<QueryHeader> createQueryHeaders(final ExecutionContext executionContext, final QueryResult queryResultSample) throws SQLException {
        int columnCount = getColumnCount(executionContext, queryResultSample);
        List<QueryHeader> result = new ArrayList<>(columnCount);
        QueryHeaderBuilderEngine queryHeaderBuilderEngine = new QueryHeaderBuilderEngine(database.getProtocolType());
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            result.add(createQueryHeader(queryHeaderBuilderEngine, executionContext, queryResultSample, database, columnIndex));
        }
        return result;
    }
    
    private int getColumnCount(final ExecutionContext executionContext, final QueryResult queryResultSample) throws SQLException {
        return selectContainsEnhancedTable && hasSelectExpandProjections(executionContext.getSqlStatementContext())
                ? ((SelectStatementContext) executionContext.getSqlStatementContext()).getProjectionsContext().getExpandProjections().size()
                : queryResultSample.getMetaData().getColumnCount();
    }
    
    private boolean hasSelectExpandProjections(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && !((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections().isEmpty();
    }
    
    private QueryHeader createQueryHeader(final QueryHeaderBuilderEngine queryHeaderBuilderEngine, final ExecutionContext executionContext,
                                          final QueryResult queryResultSample, final ShardingSphereDatabase database, final int columnIndex) throws SQLException {
        return selectContainsEnhancedTable && hasSelectExpandProjections(executionContext.getSqlStatementContext())
                ? queryHeaderBuilderEngine.build(((SelectStatementContext) executionContext.getSqlStatementContext()).getProjectionsContext(), queryResultSample.getMetaData(), database, columnIndex)
                : queryHeaderBuilderEngine.build(queryResultSample.getMetaData(), database, columnIndex);
    }
    
    private MergedResult mergeQuery(final SQLStatementContext sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        MergeEngine mergeEngine = new MergeEngine(database, ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps(),
                databaseConnectionManager.getConnectionSession().getConnectionContext());
        return mergeEngine.merge(queryResults, sqlStatementContext);
    }
    
    private UpdateResponseHeader processExecuteUpdate(final ExecutionContext executionContext, final Collection<UpdateResult> updateResults) {
        Optional<GeneratedKeyContext> generatedKeyContext = executionContext.getSqlStatementContext() instanceof InsertStatementContext
                ? ((InsertStatementContext) executionContext.getSqlStatementContext()).getGeneratedKeyContext()
                : Optional.empty();
        Collection<Comparable<?>> autoIncrementGeneratedValues =
                generatedKeyContext.filter(GeneratedKeyContext::isSupportAutoIncrement).map(GeneratedKeyContext::getGeneratedValues).orElseGet(Collections::emptyList);
        UpdateResponseHeader result = new UpdateResponseHeader(executionContext.getSqlStatementContext().getSqlStatement(), updateResults, autoIncrementGeneratedValues);
        mergeUpdateCount(executionContext.getSqlStatementContext(), result);
        return result;
    }
    
    private void mergeUpdateCount(final SQLStatementContext sqlStatementContext, final UpdateResponseHeader response) {
        if (isNeedAccumulate(sqlStatementContext)) {
            response.mergeUpdateCount();
        }
    }
    
    private boolean isNeedAccumulate(final SQLStatementContext sqlStatementContext) {
        Optional<DataNodeContainedRule> dataNodeContainedRule = database.getRuleMetaData().findSingleRule(DataNodeContainedRule.class);
        return dataNodeContainedRule.isPresent() && dataNodeContainedRule.get().isNeedAccumulate(sqlStatementContext.getTablesContext().getTableNames());
    }
    
    /**
     * Goto next result value.
     *
     * @return has more result value or not
     * @throws SQLException SQL exception
     */
    @Override
    public boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    /**
     * Get query response row.
     *
     * @return query response row
     * @throws SQLException SQL exception
     */
    @Override
    public QueryResponseRow getRowData() throws SQLException {
        List<QueryResponseCell> cells = new ArrayList<>(queryHeaders.size());
        for (int columnIndex = 1; columnIndex <= queryHeaders.size(); columnIndex++) {
            Object data = mergedResult.getValue(columnIndex, Object.class);
            cells.add(new QueryResponseCell(queryHeaders.get(columnIndex - 1).getColumnType(), data, queryHeaders.get(columnIndex - 1).getColumnTypeName()));
        }
        return new QueryResponseRow(cells);
    }
    
    /**
     * Close database connector.
     *
     * @throws SQLException SQL exception
     */
    @Override
    public void close() throws SQLException {
        Collection<SQLException> result = new LinkedList<>();
        result.addAll(closeResultSets());
        result.addAll(closeStatements());
        closeSQLFederationEngine().ifPresent(result::add);
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
    
    private Optional<SQLException> closeSQLFederationEngine() {
        if (null != proxySQLExecutor.getSqlFederationEngine()) {
            try {
                proxySQLExecutor.getSqlFederationEngine().close();
            } catch (final SQLException ex) {
                return Optional.of(ex);
            }
        }
        return Optional.empty();
    }
}
