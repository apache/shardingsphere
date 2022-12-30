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

package org.apache.shardingsphere.proxy.backend.communication;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.aware.CursorDefinitionAware;
import org.apache.shardingsphere.infra.binder.decider.context.SQLFederationDeciderContext;
import org.apache.shardingsphere.infra.binder.decider.engine.SQLFederationDeciderEngine;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.type.CursorAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefreshEngine;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.metadata.JDBCQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtil;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.executor.callback.ProxyJDBCExecutorCallback;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.executor.callback.ProxyJDBCExecutorCallbackFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
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
import org.apache.shardingsphere.sharding.merge.common.IteratorStreamMergedResult;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecutor;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecutorContext;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Database communication engine.
 */
public final class DatabaseCommunicationEngine implements DatabaseBackendHandler {
    
    private final ProxySQLExecutor proxySQLExecutor;
    
    private final Collection<Statement> cachedStatements = new CopyOnWriteArrayList<>();
    
    private final Collection<ResultSet> cachedResultSets = new CopyOnWriteArrayList<>();
    
    private final String driverType;
    
    private final ShardingSphereDatabase database;
    
    private final QueryContext queryContext;
    
    private final BackendConnection backendConnection;
    
    private volatile SQLFederationExecutor federationExecutor;
    
    private List<QueryHeader> queryHeaders;
    
    private MergedResult mergedResult;
    
    public DatabaseCommunicationEngine(final String driverType, final ShardingSphereDatabase database, final QueryContext queryContext, final BackendConnection backendConnection) {
        SQLStatementContext<?> sqlStatementContext = queryContext.getSqlStatementContext();
        failedIfBackendNotReady(backendConnection.getConnectionSession(), sqlStatementContext);
        this.driverType = driverType;
        this.database = database;
        this.queryContext = queryContext;
        this.backendConnection = backendConnection;
        if (sqlStatementContext instanceof CursorAvailable) {
            DatabaseCommunicationEngine.this.prepareCursorStatementContext((CursorAvailable) sqlStatementContext, backendConnection.getConnectionSession());
        }
        proxySQLExecutor = new ProxySQLExecutor(driverType, backendConnection, this);
    }
    
    private void failedIfBackendNotReady(final ConnectionSession connectionSession, final SQLStatementContext<?> sqlStatementContext) {
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(connectionSession.getDatabaseName());
        boolean isSystemSchema = SystemSchemaUtil.containsSystemSchema(sqlStatementContext.getDatabaseType(), sqlStatementContext.getTablesContext().getSchemaNames(), database);
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
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ResponseHeader execute() throws SQLException {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        SQLFederationDeciderContext deciderContext = decide(queryContext, metaDataContexts.getMetaData().getProps(), database);
        if (deciderContext.isUseSQLFederation()) {
            prepareFederationExecutor();
            ResultSet resultSet = doExecuteFederation(queryContext, metaDataContexts);
            return processExecuteFederation(resultSet, metaDataContexts);
        }
        ExecutionContext executionContext = new KernelProcessor().generateExecutionContext(queryContext, database, metaDataContexts.getMetaData().getGlobalRuleMetaData(),
                metaDataContexts.getMetaData().getProps(), backendConnection.getConnectionSession().getConnectionContext());
        if (executionContext.getExecutionUnits().isEmpty()) {
            return new UpdateResponseHeader(executionContext.getSqlStatementContext().getSqlStatement());
        }
        proxySQLExecutor.checkExecutePrerequisites(executionContext);
        List result = proxySQLExecutor.execute(executionContext);
        refreshMetaData(executionContext);
        Object executeResultSample = result.iterator().next();
        return executeResultSample instanceof QueryResult
                ? processExecuteQuery(executionContext, result, (QueryResult) executeResultSample)
                : processExecuteUpdate(executionContext, result);
    }
    
    private static SQLFederationDeciderContext decide(final QueryContext queryContext, final ConfigurationProperties props, final ShardingSphereDatabase database) {
        SQLFederationDeciderEngine deciderEngine = new SQLFederationDeciderEngine(database.getRuleMetaData().getRules(), props);
        return deciderEngine.decide(queryContext, database);
    }
    
    private void prepareFederationExecutor() {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        String databaseName = backendConnection.getConnectionSession().getDatabaseName();
        DatabaseType databaseType = queryContext.getSqlStatementContext().getDatabaseType();
        String schemaName = queryContext.getSqlStatementContext().getTablesContext().getSchemaName().orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(databaseType, databaseName));
        SQLFederationRule sqlFederationRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLFederationRule.class);
        federationExecutor = sqlFederationRule.getSQLFederationExecutor(databaseName, schemaName, metaDataContexts.getMetaData(), metaDataContexts.getShardingSphereData(),
                new JDBCExecutor(BackendExecutorContext.getInstance().getExecutorEngine(), backendConnection.getConnectionSession().getConnectionContext()),
                ProxyContext.getInstance().getContextManager().getInstanceContext().getEventBusContext());
    }
    
    private ResultSet doExecuteFederation(final QueryContext queryContext, final MetaDataContexts metaDataContexts) throws SQLException {
        boolean isReturnGeneratedKeys = queryContext.getSqlStatementContext().getSqlStatement() instanceof MySQLInsertStatement;
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(backendConnection.getConnectionSession().getDatabaseName());
        DatabaseType protocolType = database.getProtocolType();
        Map<String, DatabaseType> storageTypes = database.getResourceMetaData().getStorageTypes();
        ProxyJDBCExecutorCallback callback = ProxyJDBCExecutorCallbackFactory.newInstance(driverType, protocolType, storageTypes,
                queryContext.getSqlStatementContext().getSqlStatement(), this, isReturnGeneratedKeys, SQLExecutorExceptionHandler.isExceptionThrown(), true);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine(isReturnGeneratedKeys, metaDataContexts);
        SQLFederationExecutorContext context = new SQLFederationExecutorContext(false, queryContext, metaDataContexts.getMetaData());
        return federationExecutor.executeQuery(prepareEngine, callback, context);
    }
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine(final boolean isReturnGeneratedKeys, final MetaDataContexts metaData) {
        int maxConnectionsSizePerQuery = metaData.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        JDBCBackendStatement statementManager = (JDBCBackendStatement) backendConnection.getConnectionSession().getStatementManager();
        return new DriverExecutionPrepareEngine<>(driverType, maxConnectionsSizePerQuery, backendConnection, statementManager,
                new StatementOption(isReturnGeneratedKeys), metaData.getMetaData().getDatabase(backendConnection.getConnectionSession().getDatabaseName()).getRuleMetaData().getRules(),
                metaData.getMetaData().getDatabase(backendConnection.getConnectionSession().getDatabaseName()).getResourceMetaData().getStorageTypes());
    }
    
    private ResponseHeader processExecuteFederation(final ResultSet resultSet, final MetaDataContexts metaDataContexts) throws SQLException {
        int columnCount = resultSet.getMetaData().getColumnCount();
        queryHeaders = new ArrayList<>(columnCount);
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(backendConnection.getConnectionSession().getDatabaseName());
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
            connectionSession.getConnectionContext().getCursorConnectionContext().getCursorDefinitions().put(cursorName, (CursorStatementContext) statementContext);
        }
        if (statementContext instanceof CursorDefinitionAware) {
            CursorStatementContext cursorStatementContext = (CursorStatementContext) connectionSession.getConnectionContext().getCursorConnectionContext().getCursorDefinitions().get(cursorName);
            Preconditions.checkArgument(null != cursorStatementContext, "Cursor %s does not exist.", cursorName);
            ((CursorDefinitionAware) statementContext).setUpCursorDefinition(cursorStatementContext);
        }
        if (statementContext instanceof CloseStatementContext) {
            connectionSession.getConnectionContext().getCursorConnectionContext().removeCursorName(cursorName);
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
    
    private QueryHeader createQueryHeader(final QueryHeaderBuilderEngine queryHeaderBuilderEngine, final ExecutionContext executionContext,
                                          final QueryResult queryResultSample, final ShardingSphereDatabase database, final int columnIndex) throws SQLException {
        return hasSelectExpandProjections(executionContext.getSqlStatementContext()) ? queryHeaderBuilderEngine.build(
                ((SelectStatementContext) executionContext.getSqlStatementContext()).getProjectionsContext(), queryResultSample.getMetaData(), database, columnIndex)
                : queryHeaderBuilderEngine.build(queryResultSample.getMetaData(), database, columnIndex);
    }
    
    private int getColumnCount(final ExecutionContext executionContext, final QueryResult queryResultSample) throws SQLException {
        return hasSelectExpandProjections(executionContext.getSqlStatementContext())
                ? ((SelectStatementContext) executionContext.getSqlStatementContext()).getProjectionsContext().getExpandProjections().size()
                : queryResultSample.getMetaData().getColumnCount();
    }
    
    private boolean hasSelectExpandProjections(final SQLStatementContext<?> sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && !((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections().isEmpty();
    }
    
    private MergedResult mergeQuery(final SQLStatementContext<?> sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        MergeEngine mergeEngine = new MergeEngine(database, ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps(),
                backendConnection.getConnectionSession().getConnectionContext());
        return mergeEngine.merge(queryResults, sqlStatementContext);
    }
    
    private UpdateResponseHeader processExecuteUpdate(final ExecutionContext executionContext, final Collection<UpdateResult> updateResults) {
        UpdateResponseHeader result = new UpdateResponseHeader(executionContext.getSqlStatementContext().getSqlStatement(), updateResults);
        mergeUpdateCount(executionContext.getSqlStatementContext(), result);
        return result;
    }
    
    private void mergeUpdateCount(final SQLStatementContext<?> sqlStatementContext, final UpdateResponseHeader response) {
        if (isNeedAccumulate(sqlStatementContext)) {
            response.mergeUpdateCount();
        }
    }
    
    private boolean isNeedAccumulate(final SQLStatementContext<?> sqlStatementContext) {
        Optional<DataNodeContainedRule> dataNodeContainedRule = findDataNodeContainedRule();
        return dataNodeContainedRule.isPresent() && dataNodeContainedRule.get().isNeedAccumulate(sqlStatementContext.getTablesContext().getTableNames());
    }
    
    private Optional<DataNodeContainedRule> findDataNodeContainedRule() {
        for (ShardingSphereRule each : database.getRuleMetaData().getRules()) {
            if (each instanceof DataNodeContainedRule) {
                return Optional.of((DataNodeContainedRule) each);
            }
        }
        return Optional.empty();
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
            cells.add(new QueryResponseCell(queryHeaders.get(columnIndex - 1).getColumnType(), data));
        }
        return new QueryResponseRow(cells);
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
