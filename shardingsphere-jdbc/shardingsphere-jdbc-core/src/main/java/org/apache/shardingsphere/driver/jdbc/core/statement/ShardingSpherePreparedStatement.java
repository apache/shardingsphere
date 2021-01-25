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
import org.apache.shardingsphere.driver.executor.batch.BatchExecutionUnit;
import org.apache.shardingsphere.driver.executor.batch.BatchPreparedStatementExecutor;
import org.apache.shardingsphere.driver.executor.callback.impl.PreparedStatementExecuteQueryCallback;
import org.apache.shardingsphere.driver.executor.callback.impl.StatementExecuteQueryCallback;
import org.apache.shardingsphere.driver.jdbc.adapter.AbstractPreparedStatementAdapter;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.constant.SQLExceptionConstant;
import org.apache.shardingsphere.driver.jdbc.core.resultset.GeneratedKeysResultSet;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;
import org.apache.shardingsphere.driver.jdbc.core.statement.metadata.ShardingSphereParameterMetaData;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
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
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.executor.sql.prepare.raw.RawExecutionPrepareEngine;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.optimize.execute.CalciteExecutor;
import org.apache.shardingsphere.infra.optimize.execute.CalciteJDBCExecutor;
import org.apache.shardingsphere.infra.optimize.schema.row.CalciteRowExecutor;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.type.RawExecutionRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
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
 * ShardingSphere prepared statement.
 */
public final class ShardingSpherePreparedStatement extends AbstractPreparedStatementAdapter {
    
    @Getter
    private final ShardingSphereConnection connection;
    
    private final MetaDataContexts metaDataContexts;
    
    private final String sql;
    
    private final List<PreparedStatement> statements;
    
    private final List<List<Object>> parameterSets;
    
    private final SQLStatement sqlStatement;
    
    private final StatementOption statementOption;
    
    @Getter
    private final ParameterMetaData parameterMetaData;
    
    private final RawExecutor rawExecutor;
    
    private final DriverJDBCExecutor driverJDBCExecutor;
    
    private final BatchPreparedStatementExecutor batchPreparedStatementExecutor;
    
    private final Collection<Comparable<?>> generatedValues = new LinkedList<>();
    
    private final KernelProcessor kernelProcessor;
    
    private ExecutionContext executionContext;
    
    private ResultSet currentResultSet;
    
    @Getter(AccessLevel.PROTECTED)
    private CalciteExecutor calciteExecutor;

    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql) throws SQLException {
        this(connection, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT, false);
    }
    
    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        this(connection, sql, resultSetType, resultSetConcurrency, ResultSet.HOLD_CURSORS_OVER_COMMIT, false);
    }
    
    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql, final int autoGeneratedKeys) throws SQLException {
        this(connection, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT, Statement.RETURN_GENERATED_KEYS == autoGeneratedKeys);
    }
    
    public ShardingSpherePreparedStatement(
            final ShardingSphereConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        this(connection, sql, resultSetType, resultSetConcurrency, resultSetHoldability, false);
    }
    
    private ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql,
                                            final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability, final boolean returnGeneratedKeys) throws SQLException {
        if (Strings.isNullOrEmpty(sql)) {
            throw new SQLException(SQLExceptionConstant.SQL_STRING_NULL_OR_EMPTY);
        }
        this.connection = connection;
        metaDataContexts = connection.getMetaDataContexts();
        this.sql = sql;
        statements = new ArrayList<>();
        parameterSets = new ArrayList<>();
        ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(
                DatabaseTypeRegistry.getTrunkDatabaseTypeName(metaDataContexts.getDefaultMetaData().getResource().getDatabaseType()));
        sqlStatement = sqlParserEngine.parse(sql, true);
        parameterMetaData = new ShardingSphereParameterMetaData(sqlStatement);
        statementOption = returnGeneratedKeys ? new StatementOption(true) : new StatementOption(resultSetType, resultSetConcurrency, resultSetHoldability);
        JDBCExecutor jdbcExecutor = new JDBCExecutor(metaDataContexts.getExecutorEngine(), connection.isHoldTransaction());
        rawExecutor = new RawExecutor(metaDataContexts.getExecutorEngine(), connection.isHoldTransaction());
        driverJDBCExecutor = new DriverJDBCExecutor(connection.getDataSourceMap(), metaDataContexts, jdbcExecutor);
        batchPreparedStatementExecutor = new BatchPreparedStatementExecutor(metaDataContexts, jdbcExecutor);
        kernelProcessor = new KernelProcessor();
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        ResultSet result;
        try {
            clearPrevious();
            executionContext = createExecutionContext();
            List<QueryResult> queryResults = executeQuery0();
            MergedResult mergedResult = mergeQuery(queryResults);
            result = new ShardingSphereResultSet(getResultSetsForShardingSphereResultSet(), mergedResult, this, executionContext);
        } finally {
            clearBatch();
        }
        currentResultSet = result;
        return result;
    }
    
    private List<ResultSet> getResultSetsForShardingSphereResultSet() throws SQLException {
        if (null != calciteExecutor) {
            return Collections.singletonList(calciteExecutor.getResultSet());
        }
        return statements.stream().map(this::getResultSet).collect(Collectors.toList());
    }
    
    private List<QueryResult> executeQuery0() throws SQLException {
        if (metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
            return rawExecutor.execute(createRawExecutionGroups(), new RawSQLExecutorCallback()).stream().map(each -> (QueryResult) each).collect(Collectors.toList());
        }
        if (executionContext.getRouteContext().isToCalcite()) {
            return executeQueryByCalcite();
        }
        Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups = createExecutionGroups();
        cacheStatements(executionGroups);
        return driverJDBCExecutor.executeQuery(executionGroups, 
                new PreparedStatementExecuteQueryCallback(metaDataContexts.getDefaultMetaData().getResource().getDatabaseType(), sqlStatement, SQLExecutorExceptionHandler.isExceptionThrown()));
    }
    
    private List<QueryResult> executeQueryByCalcite() throws SQLException {
        if (executionContext.getExecutionUnits().isEmpty()) {
            return Collections.emptyList();
        }
        calciteExecutor = createCalciteExecutor();
        SQLUnit sqlUnit = executionContext.getExecutionUnits().iterator().next().getSqlUnit();
        return calciteExecutor.executeQuery(sqlUnit.getSql(), sqlUnit.getParameters());
    }
    
    private CalciteExecutor createCalciteExecutor() {
        int maxConnectionsSizePerQuery = metaDataContexts.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        StatementExecuteQueryCallback callback = new StatementExecuteQueryCallback(metaDataContexts.getDefaultMetaData().getResource().getDatabaseType(),
                executionContext.getSqlStatementContext().getSqlStatement(), SQLExecutorExceptionHandler.isExceptionThrown());
        CalciteRowExecutor executor = new CalciteRowExecutor(metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules(),
                maxConnectionsSizePerQuery, connection, driverJDBCExecutor.getJdbcExecutor(), executionContext, callback);
        // TODO Consider CalciteRawExecutor
        return new CalciteJDBCExecutor(metaDataContexts.getCalciteContextFactory().create(DefaultSchema.LOGIC_NAME, executor));
    }
    
    @Override
    public int executeUpdate() throws SQLException {
        try {
            clearPrevious();
            executionContext = createExecutionContext();
            if (metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                Collection<ExecuteResult> executeResults = rawExecutor.execute(createRawExecutionGroups(), new RawSQLExecutorCallback());
                accumulate(executeResults);
            }
            Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups = createExecutionGroups();
            cacheStatements(executionGroups);
            return driverJDBCExecutor.executeUpdate(
                    executionGroups, executionContext.getSqlStatementContext(), executionContext.getRouteContext().getRouteUnits(), createExecuteUpdateCallback());
        } finally {
            clearBatch();
        }
    }
    
    private int accumulate(final Collection<ExecuteResult> results) {
        int result = 0;
        for (ExecuteResult each : results) {
            result += ((UpdateResult) each).getUpdateCount();
        }
        return result;
    }
    
    private JDBCExecutorCallback<Integer> createExecuteUpdateCallback() {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        return new JDBCExecutorCallback<Integer>(metaDataContexts.getDefaultMetaData().getResource().getDatabaseType(), sqlStatement, isExceptionThrown) {
            
            @Override
            protected Integer executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return ((PreparedStatement) statement).executeUpdate();
            }
            
            @Override
            protected Optional<Integer> getSaneResult(final SQLStatement sqlStatement) {
                return Optional.of(0);
            }
        };
    }
    
    @Override
    public boolean execute() throws SQLException {
        try {
            clearPrevious();
            executionContext = createExecutionContext();
            if (metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                // TODO process getStatement
                Collection<ExecuteResult> executeResults = rawExecutor.execute(createRawExecutionGroups(), new RawSQLExecutorCallback());
                return executeResults.iterator().next() instanceof QueryResult;
            }
            Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups = createExecutionGroups();
            cacheStatements(executionGroups);
            return driverJDBCExecutor.execute(
                    executionGroups, executionContext.getSqlStatementContext().getSqlStatement(), executionContext.getRouteContext().getRouteUnits(), createExecuteCallback());
        } finally {
            clearBatch();
        }
    }
    
    private Collection<ExecutionGroup<RawSQLExecutionUnit>> createRawExecutionGroups() throws SQLException {
        int maxConnectionsSizePerQuery = metaDataContexts.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new RawExecutionPrepareEngine(maxConnectionsSizePerQuery, metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules())
                .prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
    }
    
    private Collection<ExecutionGroup<JDBCExecutionUnit>> createExecutionGroups() throws SQLException {
        int maxConnectionsSizePerQuery = metaDataContexts.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = new DriverExecutionPrepareEngine<>(
                JDBCDriverType.PREPARED_STATEMENT, maxConnectionsSizePerQuery, connection, statementOption, metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules());
        return prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
    }
    
    private JDBCExecutorCallback<Boolean> createExecuteCallback() {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        return new JDBCExecutorCallback<Boolean>(metaDataContexts.getDefaultMetaData().getResource().getDatabaseType(), sqlStatement, isExceptionThrown) {
            
            @Override
            protected Boolean executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return ((PreparedStatement) statement).execute();
            }
            
            @Override
            protected Optional<Boolean> getSaneResult(final SQLStatement sqlStatement) {
                return Optional.of(sqlStatement instanceof SelectStatement);
            }
        };
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
    
    private ExecutionContext createExecutionContext() throws SQLException {
        LogicSQL logicSQL = createLogicSQL();
        ExecutionContext result = kernelProcessor.generateExecutionContext(logicSQL, metaDataContexts.getDefaultMetaData(), metaDataContexts.getProps());
        findGeneratedKey(result).ifPresent(generatedKey -> generatedValues.addAll(generatedKey.getGeneratedValues()));
        return result;
    }
    
    private LogicSQL createLogicSQL() {
        List<Object> parameters = new ArrayList<>(getParameters());
        ShardingSphereSchema schema = metaDataContexts.getDefaultMetaData().getSchema();
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(schema, parameters, sqlStatement);
        return new LogicSQL(sqlStatementContext, sql, parameters);
    }
    
    private MergedResult mergeQuery(final List<QueryResult> queryResults) throws SQLException {
        ShardingSphereMetaData metaData = metaDataContexts.getDefaultMetaData();
        MergeEngine mergeEngine = new MergeEngine(
                metaDataContexts.getDefaultMetaData().getResource().getDatabaseType(), metaData.getSchema(), metaDataContexts.getProps(), metaData.getRuleMetaData().getRules());
        return mergeEngine.merge(queryResults, executionContext.getSqlStatementContext());
    }
    
    private void cacheStatements(final Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups) {
        for (ExecutionGroup<JDBCExecutionUnit> each : executionGroups) {
            statements.addAll(each.getInputs().stream().map(jdbcExecutionUnit -> (PreparedStatement) jdbcExecutionUnit.getStorageResource()).collect(Collectors.toList()));
            parameterSets.addAll(each.getInputs().stream().map(input -> input.getExecutionUnit().getSqlUnit().getParameters()).collect(Collectors.toList()));
        }
        replay();
    }
    
    private void replay() {
        replaySetParameter();
        statements.forEach(this::replayMethodsInvocation);
    }
    
    private void replaySetParameter() {
        for (int i = 0; i < statements.size(); i++) {
            replaySetParameter(statements.get(i), parameterSets.get(i));
        }
    }
    
    private void clearPrevious() throws SQLException {
        clearStatements();
        parameterSets.clear();
    }
    
    private Optional<GeneratedKeyContext> findGeneratedKey(final ExecutionContext executionContext) {
        return executionContext.getSqlStatementContext() instanceof InsertStatementContext
                ? ((InsertStatementContext) executionContext.getSqlStatementContext()).getGeneratedKeyContext() : Optional.empty();
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        Optional<GeneratedKeyContext> generatedKey = findGeneratedKey(executionContext);
        if (statementOption.isReturnGeneratedKeys() && generatedKey.isPresent()) {
            return new GeneratedKeysResultSet(generatedKey.get().getColumnName(), generatedValues.iterator(), this);
        }
        if (1 == statements.size()) {
            return statements.iterator().next().getGeneratedKeys();
        }
        return new GeneratedKeysResultSet();
    }
    
    @Override
    public void addBatch() throws SQLException {
        try {
            executionContext = createExecutionContext();
            batchPreparedStatementExecutor.addBatchForExecutionUnits(executionContext.getExecutionUnits());
        } finally {
            currentResultSet = null;
            clearParameters();
        }
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        try {
            // TODO add raw SQL executor
            initBatchPreparedStatementExecutor();
            return batchPreparedStatementExecutor.executeBatch(executionContext.getSqlStatementContext());
        } finally {
            clearBatch();
        }
    }
    
    private void initBatchPreparedStatementExecutor() throws SQLException {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = new DriverExecutionPrepareEngine<>(
                JDBCDriverType.PREPARED_STATEMENT, metaDataContexts.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY),
                connection, statementOption, metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules());
        batchPreparedStatementExecutor.init(prepareEngine.prepare(executionContext.getRouteContext(),
                new ArrayList<>(batchPreparedStatementExecutor.getBatchExecutionUnits()).stream().map(BatchExecutionUnit::getExecutionUnit).collect(Collectors.toList())));
        setBatchParametersForStatements();
    }
    
    private void setBatchParametersForStatements() throws SQLException {
        for (Statement each : batchPreparedStatementExecutor.getStatements()) {
            List<List<Object>> parameterSet = batchPreparedStatementExecutor.getParameterSet(each);
            for (List<Object> parameters : parameterSet) {
                replaySetParameter((PreparedStatement) each, parameters);
                ((PreparedStatement) each).addBatch();
            }
        }
    }
    
    @Override
    public void clearBatch() throws SQLException {
        currentResultSet = null;
        batchPreparedStatementExecutor.clear();
        clearParameters();
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
        return metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().stream().anyMatch(
            each -> each instanceof DataNodeContainedRule && ((DataNodeContainedRule) each).isNeedAccumulate(executionContext.getSqlStatementContext().getTablesContext().getTableNames()));
    }
    
    @Override
    public Collection<PreparedStatement> getRoutedStatements() {
        return statements;
    }
    
    private void clearStatements() throws SQLException {
        for (Statement each : statements) {
            each.close();
        }
        statements.clear();
    }
}
