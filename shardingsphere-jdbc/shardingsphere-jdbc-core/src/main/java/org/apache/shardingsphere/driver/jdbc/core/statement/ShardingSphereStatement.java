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

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.driver.executor.DriverJDBCExecutor;
import org.apache.shardingsphere.driver.executor.callback.ExecuteCallback;
import org.apache.shardingsphere.driver.executor.callback.ExecuteUpdateCallback;
import org.apache.shardingsphere.driver.executor.callback.impl.StatementExecuteQueryCallback;
import org.apache.shardingsphere.driver.jdbc.adapter.AbstractStatementAdapter;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.exception.SQLExceptionErrorCode;
import org.apache.shardingsphere.driver.jdbc.core.resultset.GeneratedKeysResultSet;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.check.SQLCheckEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.executor.sql.federate.execute.FederationExecutor;
import org.apache.shardingsphere.infra.executor.sql.federate.execute.OriginalFederationExecutor;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.executor.sql.prepare.raw.RawExecutionPrepareEngine;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.RawExecutionRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;

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
public final class ShardingSphereStatement extends AbstractStatementAdapter {
    
    @Getter
    private final ShardingSphereConnection connection;
    
    private final MetaDataContexts metaDataContexts;
    
    private final List<Statement> statements;
    
    private final StatementOption statementOption;
    
    private final DriverJDBCExecutor driverJDBCExecutor;
    
    private final RawExecutor rawExecutor;
    
    @Getter(AccessLevel.PROTECTED)
    private final FederationExecutor federationExecutor;
    
    private final KernelProcessor kernelProcessor;
    
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
        super(Statement.class);
        this.connection = connection;
        metaDataContexts = connection.getContextManager().getMetaDataContexts();
        statements = new LinkedList<>();
        statementOption = new StatementOption(resultSetType, resultSetConcurrency, resultSetHoldability);
        JDBCExecutor jdbcExecutor = new JDBCExecutor(metaDataContexts.getExecutorEngine(), connection.isHoldTransaction());
        driverJDBCExecutor = new DriverJDBCExecutor(connection.getSchemaName(), metaDataContexts, jdbcExecutor);
        rawExecutor = new RawExecutor(metaDataContexts.getExecutorEngine(), connection.isHoldTransaction(), metaDataContexts.getProps());
        // TODO Consider FederateRawExecutor
        federationExecutor = new OriginalFederationExecutor(connection.getSchemaName(), metaDataContexts.getOptimizerContext(), metaDataContexts.getProps(), jdbcExecutor);
        kernelProcessor = new KernelProcessor();
    }
    
    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        if (Strings.isNullOrEmpty(sql)) {
            SQLExceptionErrorCode errorCode = SQLExceptionErrorCode.SQL_STRING_NULL_OR_EMPTY;
            throw new SQLException(errorCode.getErrorMessage(), errorCode.getSqlState(), errorCode.getErrorCode());
        }
        ResultSet result;
        try {
            executionContext = createExecutionContext(sql);
            List<QueryResult> queryResults = executeQuery0();
            MergedResult mergedResult = mergeQuery(queryResults);
            result = new ShardingSphereResultSet(getResultSetsForShardingSphereResultSet(), mergedResult, this, executionContext);
        } finally {
            currentResultSet = null;
        }
        currentResultSet = result;
        return result;
    }
    
    private List<ResultSet> getResultSetsForShardingSphereResultSet() throws SQLException {
        if (executionContext.getRouteContext().isFederated()) {
            return Collections.singletonList(federationExecutor.getResultSet());
        }
        return statements.stream().map(this::getResultSet).collect(Collectors.toList());
    }
    
    private List<QueryResult> executeQuery0() throws SQLException {
        if (metaDataContexts.getMetaData(connection.getSchemaName()).getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
            return rawExecutor.execute(createRawExecutionContext(), executionContext.getLogicSQL(),
                    new RawSQLExecutorCallback()).stream().map(each -> (QueryResult) each).collect(Collectors.toList());
        }
        if (executionContext.getRouteContext().isFederated()) {
            return executeFederatedQuery();
        }
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionContext();
        cacheStatements(executionGroupContext.getInputGroups());
        StatementExecuteQueryCallback callback = new StatementExecuteQueryCallback(metaDataContexts.getMetaData(connection.getSchemaName()).getResource().getDatabaseType(),
                executionContext.getSqlStatementContext().getSqlStatement(), SQLExecutorExceptionHandler.isExceptionThrown());
        return driverJDBCExecutor.executeQuery(executionGroupContext, executionContext.getLogicSQL(), callback);
    }
    
    private List<QueryResult> executeFederatedQuery() throws SQLException {
        if (executionContext.getExecutionUnits().isEmpty()) {
            return Collections.emptyList();
        }
        StatementExecuteQueryCallback callback = new StatementExecuteQueryCallback(metaDataContexts.getMetaData(connection.getSchemaName()).getResource().getDatabaseType(),
                executionContext.getSqlStatementContext().getSqlStatement(), SQLExecutorExceptionHandler.isExceptionThrown());
        return federationExecutor.executeQuery(executionContext, callback, createDriverExecutionPrepareEngine());
    }
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine() {
        int maxConnectionsSizePerQuery = metaDataContexts.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new DriverExecutionPrepareEngine<>(JDBCDriverType.STATEMENT, maxConnectionsSizePerQuery, connection, 
                statementOption, metaDataContexts.getMetaData(connection.getSchemaName()).getRuleMetaData().getRules());
    }
    
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        try {
            executionContext = createExecutionContext(sql);
            if (metaDataContexts.getMetaData(connection.getSchemaName()).getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                return accumulate(rawExecutor.execute(createRawExecutionContext(), executionContext.getLogicSQL(), new RawSQLExecutorCallback()));
            }
            ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionContext();
            cacheStatements(executionGroupContext.getInputGroups());
            return executeUpdate(executionGroupContext,
                (actualSQL, statement) -> statement.executeUpdate(actualSQL), executionContext.getSqlStatementContext(), executionContext.getRouteContext().getRouteUnits());
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
            executionContext = createExecutionContext(sql);
            if (metaDataContexts.getMetaData(connection.getSchemaName()).getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                return accumulate(rawExecutor.execute(createRawExecutionContext(), executionContext.getLogicSQL(), new RawSQLExecutorCallback()));
            }
            ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionContext();
            cacheStatements(executionGroupContext.getInputGroups());
            return executeUpdate(executionGroupContext,
                (actualSQL, statement) -> statement.executeUpdate(actualSQL, autoGeneratedKeys), executionContext.getSqlStatementContext(), executionContext.getRouteContext().getRouteUnits());
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        returnGeneratedKeys = true;
        try {
            executionContext = createExecutionContext(sql);
            if (metaDataContexts.getMetaData(connection.getSchemaName()).getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                return accumulate(rawExecutor.execute(createRawExecutionContext(), executionContext.getLogicSQL(), new RawSQLExecutorCallback()));
            }
            ExecutionGroupContext<JDBCExecutionUnit> executionGroups = createExecutionContext();
            cacheStatements(executionGroups.getInputGroups());
            return executeUpdate(executionGroups,
                (actualSQL, statement) -> statement.executeUpdate(actualSQL, columnIndexes), executionContext.getSqlStatementContext(), executionContext.getRouteContext().getRouteUnits());
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        returnGeneratedKeys = true;
        try {
            executionContext = createExecutionContext(sql);
            if (metaDataContexts.getMetaData(connection.getSchemaName()).getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                return accumulate(rawExecutor.execute(createRawExecutionContext(), executionContext.getLogicSQL(), new RawSQLExecutorCallback()));
            }
            ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionContext();
            cacheStatements(executionGroupContext.getInputGroups());
            return executeUpdate(executionGroupContext,
                (actualSQL, statement) -> statement.executeUpdate(actualSQL, columnNames), executionContext.getSqlStatementContext(), executionContext.getRouteContext().getRouteUnits());
        } finally {
            currentResultSet = null;
        }
    }
    
    private int executeUpdate(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext, final ExecuteUpdateCallback updater,
                              final SQLStatementContext<?> sqlStatementContext, final Collection<RouteUnit> routeUnits) throws SQLException {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        JDBCExecutorCallback<Integer> callback = new JDBCExecutorCallback<Integer>(
                metaDataContexts.getMetaData(connection.getSchemaName()).getResource().getDatabaseType(), sqlStatementContext.getSqlStatement(), isExceptionThrown) {
            
            @Override
            protected Integer executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return updater.executeUpdate(sql, statement);
            }
            
            @Override
            protected Optional<Integer> getSaneResult(final SQLStatement sqlStatement) {
                return Optional.empty();
            }
        };
        return driverJDBCExecutor.executeUpdate(executionGroupContext, executionContext.getLogicSQL(), routeUnits, callback);
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
        return execute0(sql, (actualSQL, statement) -> statement.execute(actualSQL));
    }
    
    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        if (RETURN_GENERATED_KEYS == autoGeneratedKeys) {
            returnGeneratedKeys = true;
        }
        return execute0(sql, (actualSQL, statement) -> statement.execute(actualSQL, autoGeneratedKeys));
    }
    
    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        returnGeneratedKeys = true;
        return execute0(sql, (actualSQL, statement) -> statement.execute(actualSQL, columnIndexes));
    }
    
    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        returnGeneratedKeys = true;
        return execute0(sql, (actualSQL, statement) -> statement.execute(actualSQL, columnNames));
    }
    
    private boolean execute(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext, final ExecuteCallback executor,
                            final SQLStatement sqlStatement, final Collection<RouteUnit> routeUnits) throws SQLException {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        JDBCExecutorCallback<Boolean> jdbcExecutorCallback = new JDBCExecutorCallback<Boolean>(
                metaDataContexts.getMetaData(connection.getSchemaName()).getResource().getDatabaseType(), sqlStatement, isExceptionThrown) {
            
            @Override
            protected Boolean executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return executor.execute(sql, statement);
            }
            
            @Override
            protected Optional<Boolean> getSaneResult(final SQLStatement sqlStatement) {
                return Optional.empty();
            }
        };
        return driverJDBCExecutor.execute(executionGroupContext, executionContext.getLogicSQL(), routeUnits, jdbcExecutorCallback);
    }
    
    private boolean execute0(final String sql, final ExecuteCallback callback) throws SQLException {
        try {
            executionContext = createExecutionContext(sql);
            if (metaDataContexts.getMetaData(connection.getSchemaName()).getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                // TODO process getStatement
                Collection<ExecuteResult> results = rawExecutor.execute(createRawExecutionContext(), executionContext.getLogicSQL(), new RawSQLExecutorCallback());
                return results.iterator().next() instanceof QueryResult;
            }
            if (executionContext.getRouteContext().isFederated()) {
                List<QueryResult> queryResults = executeFederatedQuery();
                return !queryResults.isEmpty();
            }
            ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionContext();
            cacheStatements(executionGroupContext.getInputGroups());
            return execute(executionGroupContext, callback, executionContext.getSqlStatementContext().getSqlStatement(), executionContext.getRouteContext().getRouteUnits());
        } finally {
            currentResultSet = null;
        }
    }
    
    private void clearStatements() throws SQLException {
        for (Statement each : statements) {
            each.close();
        }
        statements.clear();
    }
    
    private LogicSQL createLogicSQL(final String sql) {
        ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(
                DatabaseTypeRegistry.getTrunkDatabaseTypeName(metaDataContexts.getMetaData(connection.getSchemaName()).getResource().getDatabaseType()), metaDataContexts.getProps());
        SQLStatement sqlStatement = sqlParserEngine.parse(sql, false);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(metaDataContexts.getMetaDataMap(), Collections.emptyList(), sqlStatement,
                connection.getSchemaName());
        return new LogicSQL(sqlStatementContext, sql, Collections.emptyList());
    }
    
    private ExecutionContext createExecutionContext(final String sql) throws SQLException {
        clearStatements();
        LogicSQL logicSQL = createLogicSQL(sql);
        SQLCheckEngine.check(logicSQL.getSqlStatementContext().getSqlStatement(), logicSQL.getParameters(), 
                metaDataContexts.getMetaData(connection.getSchemaName()).getRuleMetaData().getRules(), connection.getSchemaName(), metaDataContexts.getMetaDataMap(), null);
        return kernelProcessor.generateExecutionContext(logicSQL, metaDataContexts.getMetaData(connection.getSchemaName()), metaDataContexts.getProps());
    }
    
    private ExecutionGroupContext<JDBCExecutionUnit> createExecutionContext() throws SQLException {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine();
        return prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
    }
    
    private ExecutionGroupContext<RawSQLExecutionUnit> createRawExecutionContext() throws SQLException {
        int maxConnectionsSizePerQuery = metaDataContexts.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new RawExecutionPrepareEngine(maxConnectionsSizePerQuery, metaDataContexts.getMetaData(connection.getSchemaName()).getRuleMetaData().getRules())
                .prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
    }
    
    private void cacheStatements(final Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups) {
        for (ExecutionGroup<JDBCExecutionUnit> each : executionGroups) {
            statements.addAll(each.getInputs().stream().map(JDBCExecutionUnit::getStorageResource).collect(Collectors.toList()));
        }
        replay();
    }
    
    private void replay() {
        statements.forEach(this::replayMethodsInvocation);
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        if (null != currentResultSet) {
            return currentResultSet;
        }
        if (executionContext.getSqlStatementContext() instanceof SelectStatementContext || executionContext.getSqlStatementContext().getSqlStatement() instanceof DALStatement) {
            List<ResultSet> resultSets = getResultSets();
            MergedResult mergedResult = mergeQuery(getQueryResults(resultSets));
            currentResultSet = new ShardingSphereResultSet(resultSets, mergedResult, this, executionContext);
        }
        return currentResultSet;
    }
    
    private ResultSet getResultSet(final Statement statement) {
        try {
            return statement.getResultSet();
        } catch (final SQLException ex) {
            throw new ShardingSphereException(ex);
        }
    }
    
    private List<ResultSet> getResultSets() throws SQLException {
        List<ResultSet> result = new ArrayList<>(statements.size());
        for (Statement each : statements) {
            result.add(each.getResultSet());
        }
        if (executionContext.getRouteContext().isFederated()) {
            result.add(federationExecutor.getResultSet());
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
    
    private MergedResult mergeQuery(final List<QueryResult> queryResults) throws SQLException {
        ShardingSphereMetaData metaData = metaDataContexts.getMetaData(connection.getSchemaName());
        MergeEngine mergeEngine = new MergeEngine(DefaultSchema.LOGIC_NAME, metaData.getResource().getDatabaseType(), metaData.getSchema(),
                metaDataContexts.getProps(), metaData.getRuleMetaData().getRules());
        return mergeEngine.merge(queryResults, executionContext.getSqlStatementContext());
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
        return metaDataContexts.getMetaData(connection.getSchemaName()).getRuleMetaData().getRules().stream().anyMatch(
            each -> each instanceof DataNodeContainedRule && ((DataNodeContainedRule) each).isNeedAccumulate(executionContext.getSqlStatementContext().getTablesContext().getTableNames()));
    }
    
    @Override
    public Collection<Statement> getRoutedStatements() {
        return statements;
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        Optional<GeneratedKeyContext> generatedKey = findGeneratedKey();
        if (returnGeneratedKeys && generatedKey.isPresent() && !generatedKey.get().getGeneratedValues().isEmpty()) {
            return new GeneratedKeysResultSet(generatedKey.get().getColumnName(), generatedKey.get().getGeneratedValues().iterator(), this);
        }
        Collection<Comparable<?>> generatedValues = new LinkedList<>();
        for (Statement statement : statements) {
            ResultSet resultSet = statement.getGeneratedKeys();
            while (resultSet.next()) {
                generatedValues.add((Comparable<?>) resultSet.getObject(1));
            }
        }
        return new GeneratedKeysResultSet(null, generatedValues.iterator(), this);
    }
    
    private Optional<GeneratedKeyContext> findGeneratedKey() {
        return executionContext.getSqlStatementContext() instanceof InsertStatementContext
                ? ((InsertStatementContext) executionContext.getSqlStatementContext()).getGeneratedKeyContext() : Optional.empty();
    }
}
