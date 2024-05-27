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

package org.apache.shardingsphere.driver.jdbc.core.statement;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.driver.executor.DriverExecutor;
import org.apache.shardingsphere.driver.executor.batch.BatchStatementExecutor;
import org.apache.shardingsphere.driver.executor.callback.ExecuteCallback;
import org.apache.shardingsphere.driver.executor.callback.ExecuteUpdateCallback;
import org.apache.shardingsphere.driver.executor.callback.impl.StatementExecuteQueryCallback;
import org.apache.shardingsphere.driver.jdbc.adapter.AbstractStatementAdapter;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.resultset.GeneratedKeysResultSet;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.dialect.SQLExceptionTransformEngine;
import org.apache.shardingsphere.infra.exception.kernel.syntax.EmptySQLException;
import org.apache.shardingsphere.infra.executor.audit.SQLAuditEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.executor.sql.prepare.raw.RawExecutionPrepareEngine;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.raw.RawExecutionRuleAttribute;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.traffic.executor.TrafficExecutorCallback;
import org.apache.shardingsphere.transaction.util.AutoCommitUtils;

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
import java.util.stream.Collectors;

/**
 * ShardingSphere statement.
 */
@HighFrequencyInvocation
public final class ShardingSphereStatement extends AbstractStatementAdapter {
    
    @Getter
    private final ShardingSphereConnection connection;
    
    private final MetaDataContexts metaDataContexts;
    
    private final List<Statement> statements;
    
    private final StatementOption statementOption;
    
    @Getter(AccessLevel.PROTECTED)
    private final DriverExecutor executor;
    
    private final KernelProcessor kernelProcessor;
    
    @Getter(AccessLevel.PROTECTED)
    private final StatementManager statementManager;
    
    private final BatchStatementExecutor batchStatementExecutor;
    
    private String databaseName;
    
    private boolean returnGeneratedKeys;
    
    private ExecutionContext executionContext;
    
    private ResultSet currentResultSet;
    
    public ShardingSphereStatement(final ShardingSphereConnection connection) {
        this(connection, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }
    
    public ShardingSphereStatement(final ShardingSphereConnection connection, final int resultSetType, final int resultSetConcurrency) {
        this(connection, resultSetType, resultSetConcurrency, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }
    
    public ShardingSphereStatement(final ShardingSphereConnection connection, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) {
        this.connection = connection;
        metaDataContexts = connection.getContextManager().getMetaDataContexts();
        statements = new LinkedList<>();
        statementOption = new StatementOption(resultSetType, resultSetConcurrency, resultSetHoldability);
        executor = new DriverExecutor(connection);
        kernelProcessor = new KernelProcessor();
        statementManager = new StatementManager();
        batchStatementExecutor = new BatchStatementExecutor(this);
        databaseName = connection.getDatabaseName();
    }
    
    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        ShardingSpherePreconditions.checkNotEmpty(sql, () -> new EmptySQLException().toSQLException());
        try {
            QueryContext queryContext = createQueryContext(sql);
            handleAutoCommit(queryContext.getSqlStatementContext().getSqlStatement());
            databaseName = queryContext.getDatabaseNameFromSQLStatement().orElse(connection.getDatabaseName());
            connection.getDatabaseConnectionManager().getConnectionContext().setCurrentDatabase(databaseName);
            ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
            Optional<ResultSet> advancedResultSet = executor.executeAdvanceQuery(metaDataContexts.getMetaData(), database, queryContext, createDriverExecutionPrepareEngine(database));
            if (advancedResultSet.isPresent()) {
                currentResultSet = advancedResultSet.get();
                return currentResultSet;
            }
            executionContext = createExecutionContext(queryContext);
            currentResultSet = doExecuteQuery(executionContext);
            return currentResultSet;
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            currentResultSet = null;
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType());
        }
    }
    
    private ShardingSphereResultSet doExecuteQuery(final ExecutionContext executionContext) throws SQLException {
        List<QueryResult> queryResults = executeQuery0(executionContext);
        MergedResult mergedResult = mergeQuery(queryResults, executionContext.getSqlStatementContext());
        boolean selectContainsEnhancedTable =
                executionContext.getSqlStatementContext() instanceof SelectStatementContext && ((SelectStatementContext) executionContext.getSqlStatementContext()).isContainsEnhancedTable();
        return new ShardingSphereResultSet(getResultSets(), mergedResult, this, selectContainsEnhancedTable, executionContext);
    }
    
    private List<QueryResult> executeQuery0(final ExecutionContext executionContext) throws SQLException {
        if (!metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getAttributes(RawExecutionRuleAttribute.class).isEmpty()) {
            return executor.getRawExecutor().execute(
                    createRawExecutionContext(executionContext), executionContext.getQueryContext(), new RawSQLExecutorCallback()).stream().map(QueryResult.class::cast).collect(Collectors.toList());
        }
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionGroupContext(executionContext);
        cacheStatements(executionGroupContext.getInputGroups());
        StatementExecuteQueryCallback callback = new StatementExecuteQueryCallback(metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType(),
                metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), executionContext.getSqlStatementContext().getSqlStatement(),
                SQLExecutorExceptionHandler.isExceptionThrown());
        return executor.getRegularExecutor().executeQuery(executionGroupContext, executionContext.getQueryContext(), callback);
    }
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine(final ShardingSphereDatabase database) {
        int maxConnectionsSizePerQuery = metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new DriverExecutionPrepareEngine<>(JDBCDriverType.STATEMENT, maxConnectionsSizePerQuery, connection.getDatabaseConnectionManager(), statementManager, statementOption,
                database.getRuleMetaData().getRules(), database.getResourceMetaData().getStorageUnits());
    }
    
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        try {
            return executeUpdate0(sql, (actualSQL, statement) -> statement.executeUpdate(actualSQL), Statement::executeUpdate);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType());
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        if (RETURN_GENERATED_KEYS == autoGeneratedKeys) {
            returnGeneratedKeys = true;
        }
        try {
            return executeUpdate0(sql, (actualSQL, statement) -> statement.executeUpdate(actualSQL, autoGeneratedKeys),
                    (statement, actualSQL) -> statement.executeUpdate(actualSQL, autoGeneratedKeys));
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType());
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        returnGeneratedKeys = true;
        try {
            return executeUpdate0(sql, (actualSQL, statement) -> statement.executeUpdate(actualSQL, columnIndexes), (statement, actualSQL) -> statement.executeUpdate(actualSQL, columnIndexes));
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType());
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        returnGeneratedKeys = true;
        try {
            return executeUpdate0(sql, (actualSQL, statement) -> statement.executeUpdate(actualSQL, columnNames), (statement, actualSQL) -> statement.executeUpdate(actualSQL, columnNames));
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType());
        } finally {
            currentResultSet = null;
        }
    }
    
    private int executeUpdate(final ExecuteUpdateCallback updateCallback, final SQLStatementContext sqlStatementContext, final ExecutionContext executionContext) throws SQLException {
        return isNeedImplicitCommitTransaction(connection, executionContext.getSqlStatementContext().getSqlStatement(), executionContext.getExecutionUnits().size() > 1)
                ? executeWithImplicitCommitTransaction(() -> useDriverToExecuteUpdate(updateCallback, sqlStatementContext, executionContext), connection,
                        metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType())
                : useDriverToExecuteUpdate(updateCallback, sqlStatementContext, executionContext);
    }
    
    private int executeUpdate0(final String sql, final ExecuteUpdateCallback updateCallback, final TrafficExecutorCallback<Integer> trafficCallback) throws SQLException {
        QueryContext queryContext = createQueryContext(sql);
        handleAutoCommit(queryContext.getSqlStatementContext().getSqlStatement());
        databaseName = queryContext.getDatabaseNameFromSQLStatement().orElse(connection.getDatabaseName());
        connection.getDatabaseConnectionManager().getConnectionContext().setCurrentDatabase(databaseName);
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        Optional<Integer> updatedCount = executor.executeAdvanceUpdate(metaDataContexts.getMetaData(), database, queryContext, createDriverExecutionPrepareEngine(database), trafficCallback);
        if (updatedCount.isPresent()) {
            return updatedCount.get();
        }
        executionContext = createExecutionContext(queryContext);
        if (!metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getAttributes(RawExecutionRuleAttribute.class).isEmpty()) {
            Collection<ExecuteResult> results = executor.getRawExecutor().execute(createRawExecutionContext(executionContext), executionContext.getQueryContext(), new RawSQLExecutorCallback());
            return accumulate(results);
        }
        return executeUpdate(updateCallback, queryContext.getSqlStatementContext(), executionContext);
    }
    
    private int useDriverToExecuteUpdate(final ExecuteUpdateCallback updateCallback, final SQLStatementContext sqlStatementContext,
                                         final ExecutionContext executionContext) throws SQLException {
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionGroupContext(executionContext);
        cacheStatements(executionGroupContext.getInputGroups());
        JDBCExecutorCallback<Integer> callback = createExecuteUpdateCallback(updateCallback, sqlStatementContext);
        return executor.getRegularExecutor().executeUpdate(executionGroupContext,
                executionContext.getQueryContext(), executionContext.getRouteContext().getRouteUnits(), callback);
    }
    
    private JDBCExecutorCallback<Integer> createExecuteUpdateCallback(final ExecuteUpdateCallback updateCallback, final SQLStatementContext sqlStatementContext) {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        return new JDBCExecutorCallback<Integer>(metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType(),
                metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), sqlStatementContext.getSqlStatement(), isExceptionThrown) {
            
            @Override
            protected Integer executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
                return updateCallback.executeUpdate(sql, statement);
            }
            
            @Override
            protected Optional<Integer> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
                return Optional.empty();
            }
        };
    }
    
    private int accumulate(final Collection<ExecuteResult> results) {
        int result = 0;
        for (ExecuteResult each : results) {
            result += ((UpdateResult) each).getUpdateCount();
        }
        return result;
    }
    
    @Override
    public boolean execute(final String sql) throws SQLException {
        try {
            return execute0(sql, (actualSQL, statement) -> statement.execute(actualSQL), Statement::execute);
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType());
        }
    }
    
    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        try {
            if (RETURN_GENERATED_KEYS == autoGeneratedKeys) {
                returnGeneratedKeys = true;
            }
            return execute0(sql, (actualSQL, statement) -> statement.execute(actualSQL, autoGeneratedKeys), (statement, actualSQL) -> statement.execute(actualSQL, autoGeneratedKeys));
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType());
        }
    }
    
    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        try {
            returnGeneratedKeys = true;
            return execute0(sql, (actualSQL, statement) -> statement.execute(actualSQL, columnIndexes), (statement, actualSQL) -> statement.execute(actualSQL, columnIndexes));
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType());
        }
    }
    
    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        try {
            returnGeneratedKeys = true;
            return execute0(sql, (actualSQL, statement) -> statement.execute(actualSQL, columnNames), (statement, actualSQL) -> statement.execute(actualSQL, columnNames));
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType());
        }
    }
    
    private boolean execute0(final String sql, final ExecuteCallback executeCallback, final TrafficExecutorCallback<Boolean> trafficCallback) throws SQLException {
        currentResultSet = null;
        QueryContext queryContext = createQueryContext(sql);
        handleAutoCommit(queryContext.getSqlStatementContext().getSqlStatement());
        databaseName = queryContext.getDatabaseNameFromSQLStatement().orElse(connection.getDatabaseName());
        connection.getDatabaseConnectionManager().getConnectionContext().setCurrentDatabase(databaseName);
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        Optional<Boolean> advancedResult = executor.executeAdvance(metaDataContexts.getMetaData(), database, queryContext, createDriverExecutionPrepareEngine(database), trafficCallback);
        if (advancedResult.isPresent()) {
            return advancedResult.get();
        }
        executionContext = createExecutionContext(queryContext);
        if (!metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getAttributes(RawExecutionRuleAttribute.class).isEmpty()) {
            Collection<ExecuteResult> results = executor.getRawExecutor().execute(createRawExecutionContext(executionContext), executionContext.getQueryContext(), new RawSQLExecutorCallback());
            return results.iterator().next() instanceof QueryResult;
        }
        return executeWithExecutionContext(executeCallback, executionContext);
    }
    
    private void handleAutoCommit(final SQLStatement sqlStatement) throws SQLException {
        if (AutoCommitUtils.needOpenTransaction(sqlStatement)) {
            connection.handleAutoCommit();
        }
    }
    
    private void clearStatements() throws SQLException {
        for (Statement each : statements) {
            each.close();
        }
        statements.clear();
    }
    
    @Override
    public void addBatch(final String sql) throws SQLException {
        batchStatementExecutor.addBatch(sql);
    }
    
    @Override
    public void clearBatch() {
        batchStatementExecutor.clear();
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        return batchStatementExecutor.executeBatch();
    }
    
    private QueryContext createQueryContext(final String originSQL) {
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        String sql = SQLHintUtils.removeHint(originSQL);
        HintValueContext hintValueContext = SQLHintUtils.extractHint(originSQL);
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType()).parse(sql, false);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(metaDataContexts.getMetaData(), databaseName, hintValueContext).bind(sqlStatement, Collections.emptyList());
        return new QueryContext(sqlStatementContext, sql, Collections.emptyList(), hintValueContext);
    }
    
    private ExecutionContext createExecutionContext(final QueryContext queryContext) throws SQLException {
        clearStatements();
        RuleMetaData globalRuleMetaData = metaDataContexts.getMetaData().getGlobalRuleMetaData();
        ShardingSphereDatabase currentDatabase = metaDataContexts.getMetaData().getDatabase(databaseName);
        SQLAuditEngine.audit(queryContext.getSqlStatementContext(), queryContext.getParameters(), globalRuleMetaData, currentDatabase, null, queryContext.getHintValueContext());
        return kernelProcessor.generateExecutionContext(queryContext, currentDatabase, globalRuleMetaData, metaDataContexts.getMetaData().getProps(),
                connection.getDatabaseConnectionManager().getConnectionContext());
    }
    
    private ExecutionGroupContext<JDBCExecutionUnit> createExecutionGroupContext(final ExecutionContext executionContext) throws SQLException {
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine(database);
        return prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits(),
                new ExecutionGroupReportContext(connection.getProcessId(), databaseName, new Grantee("", "")));
    }
    
    private ExecutionGroupContext<RawSQLExecutionUnit> createRawExecutionContext(final ExecutionContext executionContext) throws SQLException {
        int maxConnectionsSizePerQuery = metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new RawExecutionPrepareEngine(maxConnectionsSizePerQuery, metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getRules())
                .prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits(), new ExecutionGroupReportContext(connection.getProcessId(), databaseName, new Grantee("", "")));
    }
    
    private boolean executeWithExecutionContext(final ExecuteCallback executeCallback, final ExecutionContext executionContext) throws SQLException {
        return isNeedImplicitCommitTransaction(connection, executionContext.getSqlStatementContext().getSqlStatement(), executionContext.getExecutionUnits().size() > 1)
                ? executeWithImplicitCommitTransaction(() -> useDriverToExecute(executeCallback, executionContext), connection,
                        metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType())
                : useDriverToExecute(executeCallback, executionContext);
    }
    
    private boolean useDriverToExecute(final ExecuteCallback callback, final ExecutionContext executionContext) throws SQLException {
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionGroupContext(executionContext);
        cacheStatements(executionGroupContext.getInputGroups());
        JDBCExecutorCallback<Boolean> jdbcExecutorCallback = createExecuteCallback(callback, executionContext.getSqlStatementContext().getSqlStatement());
        return executor.getRegularExecutor().execute(executionGroupContext,
                executionContext.getQueryContext(), executionContext.getRouteContext().getRouteUnits(), jdbcExecutorCallback);
    }
    
    private void cacheStatements(final Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups) throws SQLException {
        for (ExecutionGroup<JDBCExecutionUnit> each : executionGroups) {
            statements.addAll(each.getInputs().stream().map(JDBCExecutionUnit::getStorageResource).collect(Collectors.toList()));
        }
        replay();
    }
    
    private JDBCExecutorCallback<Boolean> createExecuteCallback(final ExecuteCallback executeCallback, final SQLStatement sqlStatement) {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        return new JDBCExecutorCallback<Boolean>(metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType(),
                metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), sqlStatement, isExceptionThrown) {
            
            @Override
            protected Boolean executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
                return executeCallback.execute(sql, statement);
            }
            
            @Override
            protected Optional<Boolean> getSaneResult(final SQLStatement sqlStatement1, final SQLException ex) {
                return Optional.empty();
            }
        };
    }
    
    private void replay() throws SQLException {
        for (Statement each : statements) {
            getMethodInvocationRecorder().replay(each);
        }
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        if (null != currentResultSet) {
            return currentResultSet;
        }
        Optional<ResultSet> advancedResultSet = executor.getAdvancedResultSet();
        if (advancedResultSet.isPresent()) {
            return advancedResultSet.get();
        }
        if (executionContext.getSqlStatementContext() instanceof SelectStatementContext || executionContext.getSqlStatementContext().getSqlStatement() instanceof DALStatement) {
            List<ResultSet> resultSets = getResultSets();
            if (resultSets.isEmpty()) {
                return currentResultSet;
            }
            SQLStatementContext sqlStatementContext = executionContext.getSqlStatementContext();
            MergedResult mergedResult = mergeQuery(getQueryResults(resultSets), sqlStatementContext);
            boolean selectContainsEnhancedTable = sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isContainsEnhancedTable();
            currentResultSet = new ShardingSphereResultSet(resultSets, mergedResult, this, selectContainsEnhancedTable, executionContext);
        }
        return currentResultSet;
    }
    
    private List<ResultSet> getResultSets() throws SQLException {
        List<ResultSet> result = new ArrayList<>(statements.size());
        for (Statement each : statements) {
            if (null != each.getResultSet()) {
                result.add(each.getResultSet());
            }
        }
        return result;
    }
    
    private List<QueryResult> getQueryResults(final List<ResultSet> resultSets) throws SQLException {
        List<QueryResult> result = new ArrayList<>(resultSets.size());
        for (ResultSet each : resultSets) {
            if (null != each) {
                result.add(new JDBCStreamQueryResult(each));
            }
        }
        return result;
    }
    
    private MergedResult mergeQuery(final List<QueryResult> queryResults, final SQLStatementContext sqlStatementContext) throws SQLException {
        MergeEngine mergeEngine = new MergeEngine(metaDataContexts.getMetaData().getGlobalRuleMetaData(), metaDataContexts.getMetaData().getDatabase(databaseName),
                metaDataContexts.getMetaData().getProps(), connection.getDatabaseConnectionManager().getConnectionContext());
        return mergeEngine.merge(queryResults, sqlStatementContext);
    }
    
    @SuppressWarnings("MagicConstant")
    @Override
    public int getResultSetType() {
        return statementOption.getResultSetType();
    }
    
    @SuppressWarnings("MagicConstant")
    @Override
    public int getResultSetConcurrency() {
        return statementOption.getResultSetConcurrency();
    }
    
    @Override
    public int getResultSetHoldability() {
        return statementOption.getResultSetHoldability();
    }
    
    @Override
    public boolean isAccumulate() {
        for (DataNodeRuleAttribute each : metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getAttributes(DataNodeRuleAttribute.class)) {
            if (each.isNeedAccumulate(executionContext.getSqlStatementContext().getTablesContext().getTableNames())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Collection<Statement> getRoutedStatements() {
        return statements;
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        Optional<GeneratedKeyContext> generatedKey = findGeneratedKey();
        if (returnGeneratedKeys && generatedKey.isPresent() && !generatedKey.get().getGeneratedValues().isEmpty()) {
            return new GeneratedKeysResultSet(getGeneratedKeysColumnName(generatedKey.get().getColumnName()), generatedKey.get().getGeneratedValues().iterator(), this);
        }
        Collection<Comparable<?>> generatedValues = new LinkedList<>();
        for (Statement each : statements) {
            ResultSet resultSet = each.getGeneratedKeys();
            while (resultSet.next()) {
                generatedValues.add((Comparable<?>) resultSet.getObject(1));
            }
        }
        String columnName = generatedKey.map(GeneratedKeyContext::getColumnName).orElse(null);
        return new GeneratedKeysResultSet(getGeneratedKeysColumnName(columnName), generatedValues.iterator(), this);
    }
    
    private Optional<GeneratedKeyContext> findGeneratedKey() {
        return executionContext.getSqlStatementContext() instanceof InsertStatementContext
                ? ((InsertStatementContext) executionContext.getSqlStatementContext()).getGeneratedKeyContext()
                : Optional.empty();
    }
    
    private String getGeneratedKeysColumnName(final String columnName) {
        return metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType() instanceof MySQLDatabaseType ? "GENERATED_KEY" : columnName;
    }
}
