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

import org.apache.shardingsphere.infra.binder.context.aware.CursorAware;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.CursorAvailable;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.dialect.SQLExceptionTransformEngine;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.EmptyRuleException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.metadata.JDBCQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtils;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.session.connection.cursor.CursorConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.refresher.federation.FederationMetaDataRefreshEngine;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefreshEngine;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.callback.ProxyJDBCExecutorCallback;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.callback.ProxyJDBCExecutorCallbackFactory;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.BackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilderEngine;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.backend.util.TransactionUtils;
import org.apache.shardingsphere.sharding.merge.common.IteratorStreamMergedResult;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sqlfederation.executor.context.SQLFederationContext;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.implicit.ImplicitTransactionCallback;

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
import java.util.stream.Collectors;

/**
 * Standard database connector.
 */
public final class StandardDatabaseConnector implements DatabaseConnector {
    
    private final String driverType;
    
    private final QueryContext queryContext;
    
    private final ProxyDatabaseConnectionManager databaseConnectionManager;
    
    private final ContextManager contextManager;
    
    private final ShardingSphereDatabase database;
    
    private final boolean containsDerivedProjections;
    
    private final ProxySQLExecutor proxySQLExecutor;
    
    private final PushDownMetaDataRefreshEngine pushDownMetaDataRefreshEngine;
    
    private final FederationMetaDataRefreshEngine federationMetaDataRefreshEngine;
    
    private final Collection<Statement> cachedStatements = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    private final Collection<ResultSet> cachedResultSets = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    private List<QueryHeader> queryHeaders;
    
    private MergedResult mergedResult;
    
    public StandardDatabaseConnector(final String driverType, final QueryContext queryContext, final ProxyDatabaseConnectionManager databaseConnectionManager) {
        this.driverType = driverType;
        this.queryContext = queryContext;
        this.databaseConnectionManager = databaseConnectionManager;
        contextManager = ProxyContext.getInstance().getContextManager();
        database = queryContext.getUsedDatabase();
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        checkBackendReady(sqlStatementContext);
        containsDerivedProjections = sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).containsDerivedProjections();
        if (sqlStatementContext instanceof CursorAvailable) {
            prepareCursorStatementContext((CursorAvailable) sqlStatementContext);
        }
        proxySQLExecutor = new ProxySQLExecutor(driverType, databaseConnectionManager, this, queryContext);
        pushDownMetaDataRefreshEngine = new PushDownMetaDataRefreshEngine(
                contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService(), database, contextManager.getMetaDataContexts().getMetaData().getProps());
        federationMetaDataRefreshEngine = new FederationMetaDataRefreshEngine(contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService(), database);
    }
    
    private void checkBackendReady(final SQLStatementContext sqlStatementContext) {
        boolean isSystemSchema = SystemSchemaUtils.containsSystemSchema(sqlStatementContext.getDatabaseType(),
                sqlStatementContext instanceof TableAvailable ? ((TableAvailable) sqlStatementContext).getTablesContext().getSchemaNames() : Collections.emptyList(), database);
        ShardingSpherePreconditions.checkState(isSystemSchema || database.containsDataSource(), () -> new EmptyStorageUnitException(database.getName()));
        ShardingSpherePreconditions.checkState(isSystemSchema || database.isComplete(), () -> new EmptyRuleException(database.getName()));
    }
    
    private void prepareCursorStatementContext(final CursorAvailable statementContext) {
        if (statementContext.getCursorName().isPresent()) {
            prepareCursorStatementContext(statementContext, statementContext.getCursorName().get().getIdentifier().getValue().toLowerCase());
        }
        if (statementContext instanceof CloseStatementContext && ((CloseStatementContext) statementContext).getSqlStatement().isCloseAll()) {
            databaseConnectionManager.getConnectionSession().getConnectionContext().clearCursorContext();
        }
    }
    
    private void prepareCursorStatementContext(final CursorAvailable statementContext, final String cursorName) {
        CursorConnectionContext cursorContext = databaseConnectionManager.getConnectionSession().getConnectionContext().getCursorContext();
        if (statementContext instanceof CursorStatementContext) {
            cursorContext.getCursorStatementContexts().put(cursorName, (CursorStatementContext) statementContext);
        }
        if (statementContext instanceof CursorAware) {
            ShardingSpherePreconditions.checkContainsKey(
                    cursorContext.getCursorStatementContexts(), cursorName, () -> new IllegalArgumentException(String.format("Cursor %s does not exist.", cursorName)));
            ((CursorAware) statementContext).setCursorStatementContext(cursorContext.getCursorStatementContexts().get(cursorName));
        }
        if (statementContext instanceof CloseStatementContext) {
            cursorContext.removeCursor(cursorName);
        }
    }
    
    @Override
    public void add(final Statement statement) {
        cachedStatements.add(statement);
    }
    
    @Override
    public void add(final ResultSet resultSet) {
        cachedResultSets.add(resultSet);
    }
    
    @Override
    public ResponseHeader execute() throws SQLException {
        if (proxySQLExecutor.getSqlFederationEngine().decide(queryContext, contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData())) {
            return processExecuteFederation(doExecuteFederation());
        }
        if (proxySQLExecutor.getSqlFederationEngine().isSqlFederationEnabled() && federationMetaDataRefreshEngine.isNeedRefresh(queryContext.getSqlStatementContext())) {
            federationMetaDataRefreshEngine.refresh(queryContext.getSqlStatementContext());
            return new UpdateResponseHeader(queryContext.getSqlStatementContext().getSqlStatement());
        }
        ExecutionContext executionContext = generateExecutionContext();
        return isNeedImplicitCommitTransaction(queryContext.getSqlStatementContext().getSqlStatement(), executionContext.getExecutionUnits().size() > 1)
                ? doExecuteWithImplicitCommitTransaction(() -> doExecute(executionContext))
                : doExecute(executionContext);
    }
    
    private ExecutionContext generateExecutionContext() {
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
        return new KernelProcessor().generateExecutionContext(queryContext, metaData.getGlobalRuleMetaData(), metaData.getProps());
    }
    
    private boolean isNeedImplicitCommitTransaction(final SQLStatement sqlStatement, final boolean multiExecutionUnits) {
        if (!databaseConnectionManager.getConnectionSession().isAutoCommit()) {
            return false;
        }
        TransactionType transactionType = TransactionUtils.getTransactionType(databaseConnectionManager.getConnectionSession().getConnectionContext().getTransactionContext());
        TransactionStatus transactionStatus = databaseConnectionManager.getConnectionSession().getTransactionStatus();
        return multiExecutionUnits && TransactionType.isDistributedTransaction(transactionType) && !transactionStatus.isInTransaction() && isWriteDMLStatement(sqlStatement);
    }
    
    private boolean isWriteDMLStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof DMLStatement && !(sqlStatement instanceof SelectStatement);
    }
    
    private <T> T doExecuteWithImplicitCommitTransaction(final ImplicitTransactionCallback<T> callback) throws SQLException {
        T result;
        BackendTransactionManager transactionManager = new BackendTransactionManager(databaseConnectionManager);
        try {
            transactionManager.begin();
            result = callback.execute();
            transactionManager.commit();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            transactionManager.rollback();
            throw SQLExceptionTransformEngine.toSQLException(ex, database.getProtocolType());
        }
        return result;
    }
    
    private ResponseHeader doExecute(final ExecutionContext executionContext) throws SQLException {
        if (executionContext.getExecutionUnits().isEmpty()) {
            return new UpdateResponseHeader(queryContext.getSqlStatementContext().getSqlStatement());
        }
        proxySQLExecutor.checkExecutePrerequisites(executionContext);
        Collection<AdvancedProxySQLExecutor> advancedExecutors = ShardingSphereServiceLoader.getServiceInstances(AdvancedProxySQLExecutor.class);
        List<ExecuteResult> executeResults = advancedExecutors.isEmpty()
                ? proxySQLExecutor.execute(executionContext)
                : advancedExecutors.iterator().next().execute(executionContext, contextManager, database, this);
        pushDownMetaDataRefreshEngine.refresh(queryContext.getSqlStatementContext(), executionContext.getRouteContext().getRouteUnits());
        Object executeResultSample = executeResults.iterator().next();
        return executeResultSample instanceof QueryResult
                ? processExecuteQuery(queryContext.getSqlStatementContext(), executeResults.stream().map(QueryResult.class::cast).collect(Collectors.toList()), (QueryResult) executeResultSample)
                : processExecuteUpdate(executeResults.stream().map(UpdateResult.class::cast).collect(Collectors.toList()));
    }
    
    private ResultSet doExecuteFederation() {
        SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(sqlStatement.getDatabaseType()).getDialectDatabaseMetaData();
        boolean isReturnGeneratedKeys = sqlStatement instanceof InsertStatement && dialectDatabaseMetaData.getGeneratedKeyOption().isSupportReturnGeneratedKeys();
        DatabaseType protocolType = database.getProtocolType();
        ProxyJDBCExecutorCallback callback = ProxyJDBCExecutorCallbackFactory.newInstance(driverType, protocolType, database.getResourceMetaData(),
                sqlStatement, this, isReturnGeneratedKeys, SQLExecutorExceptionHandler.isExceptionThrown(), true);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine(isReturnGeneratedKeys, contextManager.getMetaDataContexts());
        SQLFederationContext context = new SQLFederationContext(
                false, queryContext, contextManager.getMetaDataContexts().getMetaData(), databaseConnectionManager.getConnectionSession().getProcessId());
        return proxySQLExecutor.getSqlFederationEngine().executeQuery(prepareEngine, callback, context);
    }
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine(final boolean isReturnGeneratedKeys, final MetaDataContexts metaData) {
        int maxConnectionsSizePerQuery = metaData.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        JDBCBackendStatement statementManager = (JDBCBackendStatement) databaseConnectionManager.getConnectionSession().getStatementManager();
        return new DriverExecutionPrepareEngine<>(driverType, maxConnectionsSizePerQuery, databaseConnectionManager, statementManager,
                new StatementOption(isReturnGeneratedKeys), database.getRuleMetaData().getRules(), metaData.getMetaData());
    }
    
    private ResponseHeader processExecuteFederation(final ResultSet resultSet) throws SQLException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        queryHeaders = new ArrayList<>(columnCount);
        QueryHeaderBuilderEngine queryHeaderBuilderEngine = new QueryHeaderBuilderEngine(null == database ? null : database.getProtocolType());
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            queryHeaders.add(queryHeaderBuilderEngine.build(new JDBCQueryResultMetaData(resultSet.getMetaData()), database, columnIndex));
        }
        mergedResult = new IteratorStreamMergedResult(Collections.singletonList(new JDBCStreamQueryResult(resultSet)));
        return new QueryResponseHeader(queryHeaders);
    }
    
    private QueryResponseHeader processExecuteQuery(final SQLStatementContext sqlStatementContext, final List<QueryResult> queryResults, final QueryResult queryResultSample) throws SQLException {
        queryHeaders = createQueryHeaders(sqlStatementContext, queryResultSample);
        mergedResult = mergeQuery(sqlStatementContext, queryResults);
        return new QueryResponseHeader(queryHeaders);
    }
    
    private List<QueryHeader> createQueryHeaders(final SQLStatementContext sqlStatementContext, final QueryResult queryResultSample) throws SQLException {
        int columnCount = getColumnCount(sqlStatementContext, queryResultSample);
        List<QueryHeader> result = new ArrayList<>(columnCount);
        QueryHeaderBuilderEngine queryHeaderBuilderEngine = new QueryHeaderBuilderEngine(database.getProtocolType());
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            result.add(createQueryHeader(queryHeaderBuilderEngine, sqlStatementContext, queryResultSample, database, columnIndex));
        }
        return result;
    }
    
    private int getColumnCount(final SQLStatementContext sqlStatementContext, final QueryResult queryResultSample) throws SQLException {
        return containsDerivedProjections
                ? ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections().size()
                : queryResultSample.getMetaData().getColumnCount();
    }
    
    private QueryHeader createQueryHeader(final QueryHeaderBuilderEngine queryHeaderBuilderEngine, final SQLStatementContext sqlStatementContext,
                                          final QueryResult queryResultSample, final ShardingSphereDatabase database, final int columnIndex) throws SQLException {
        return containsDerivedProjections
                ? queryHeaderBuilderEngine.build(((SelectStatementContext) sqlStatementContext).getProjectionsContext(), queryResultSample.getMetaData(), database, columnIndex)
                : queryHeaderBuilderEngine.build(queryResultSample.getMetaData(), database, columnIndex);
    }
    
    private MergedResult mergeQuery(final SQLStatementContext sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        MergeEngine mergeEngine = new MergeEngine(contextManager.getMetaDataContexts().getMetaData(),
                database, contextManager.getMetaDataContexts().getMetaData().getProps(), databaseConnectionManager.getConnectionSession().getConnectionContext());
        return mergeEngine.merge(queryResults, sqlStatementContext);
    }
    
    private UpdateResponseHeader processExecuteUpdate(final Collection<UpdateResult> updateResults) {
        Optional<GeneratedKeyContext> generatedKeyContext = queryContext.getSqlStatementContext() instanceof InsertStatementContext
                ? ((InsertStatementContext) queryContext.getSqlStatementContext()).getGeneratedKeyContext()
                : Optional.empty();
        Collection<Comparable<?>> autoIncrementGeneratedValues = generatedKeyContext.filter(GeneratedKeyContext::isSupportAutoIncrement)
                .map(GeneratedKeyContext::getGeneratedValues).orElseGet(Collections::emptyList);
        UpdateResponseHeader result = new UpdateResponseHeader(queryContext.getSqlStatementContext().getSqlStatement(), updateResults, autoIncrementGeneratedValues);
        if (isNeedAccumulate()) {
            result.mergeUpdateCount();
        }
        return result;
    }
    
    private boolean isNeedAccumulate() {
        Collection<String> tableNames = queryContext.getSqlStatementContext() instanceof TableAvailable
                ? ((TableAvailable) queryContext.getSqlStatementContext()).getTablesContext().getTableNames()
                : Collections.emptyList();
        for (DataNodeRuleAttribute each : database.getRuleMetaData().getAttributes(DataNodeRuleAttribute.class)) {
            if (each.isNeedAccumulate(tableNames)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    @Override
    public QueryResponseRow getRowData() throws SQLException {
        List<QueryResponseCell> cells = new ArrayList<>(queryHeaders.size());
        for (int columnIndex = 1; columnIndex <= queryHeaders.size(); columnIndex++) {
            Object data = mergedResult.getValue(columnIndex, Object.class);
            cells.add(new QueryResponseCell(queryHeaders.get(columnIndex - 1).getColumnType(), data, queryHeaders.get(columnIndex - 1).getColumnTypeName()));
        }
        return new QueryResponseRow(cells);
    }
    
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
