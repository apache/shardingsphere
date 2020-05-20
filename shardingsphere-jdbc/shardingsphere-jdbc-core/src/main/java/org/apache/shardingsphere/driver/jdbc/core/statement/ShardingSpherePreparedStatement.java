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
import lombok.Getter;
import org.apache.shardingsphere.driver.executor.PreparedStatementExecutor;
import org.apache.shardingsphere.driver.executor.batch.BatchExecutionUnit;
import org.apache.shardingsphere.driver.executor.batch.BatchPreparedStatementExecutor;
import org.apache.shardingsphere.driver.jdbc.adapter.AbstractPreparedStatementAdapter;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.constant.SQLExceptionConstant;
import org.apache.shardingsphere.driver.jdbc.core.resultset.GeneratedKeysResultSet;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;
import org.apache.shardingsphere.driver.jdbc.core.statement.metadata.ShardingSphereParameterMetaData;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.kernel.InputGroup;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContextBuilder;
import org.apache.shardingsphere.infra.executor.sql.execute.jdbc.StatementExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.jdbc.executor.SQLExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.jdbc.group.PreparedStatementExecuteGroupEngine;
import org.apache.shardingsphere.infra.executor.sql.execute.jdbc.group.StatementOption;
import org.apache.shardingsphere.infra.executor.sql.execute.jdbc.queryresult.StreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.log.SQLLogger;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.route.DataNodeRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.DataNodeRoutedRule;
import org.apache.shardingsphere.kernal.context.SchemaContext;
import org.apache.shardingsphere.kernal.context.SchemaContexts;
import org.apache.shardingsphere.sql.parser.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.DALStatement;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
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
    
    private final SchemaContexts schemaContexts;
    
    private final String sql;
    
    private final List<PreparedStatement> statements;
    
    private final List<List<Object>> parameterSets;
    
    private final SQLStatement sqlStatement;
    
    private final StatementOption statementOption;
    
    @Getter
    private final ParameterMetaData parameterMetaData;
    
    private final PreparedStatementExecutor preparedStatementExecutor;
    
    private final BatchPreparedStatementExecutor batchPreparedStatementExecutor;
    
    private final Collection<Comparable<?>> generatedValues = new LinkedList<>();
    
    private ExecutionContext executionContext;
    
    private ResultSet currentResultSet;
    
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
        schemaContexts = connection.getSchemaContexts();
        this.sql = sql;
        statements = new ArrayList<>();
        parameterSets = new ArrayList<>();
        sqlStatement = schemaContexts.getDefaultSchemaContext().getRuntimeContext().getSqlParserEngine().parse(sql, true);
        parameterMetaData = new ShardingSphereParameterMetaData(sqlStatement);
        statementOption = returnGeneratedKeys ? new StatementOption(true) : new StatementOption(resultSetType, resultSetConcurrency, resultSetHoldability);
        SQLExecutor sqlExecutor = new SQLExecutor(schemaContexts.getDefaultSchemaContext().getRuntimeContext().getExecutorKernel(), connection.isHoldTransaction());
        preparedStatementExecutor = new PreparedStatementExecutor(connection.getDataSourceMap(), schemaContexts, sqlExecutor);
        batchPreparedStatementExecutor = new BatchPreparedStatementExecutor(schemaContexts, sqlExecutor);
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        ResultSet result;
        try {
            clearPrevious();
            executionContext = createExecutionContext();
            PreparedStatementExecuteGroupEngine executeGroupEngine = new PreparedStatementExecuteGroupEngine(
                    schemaContexts.getProperties().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY), schemaContexts.getDefaultSchemaContext().getSchema().getRules());
            Collection<InputGroup<StatementExecuteUnit>> inputGroups = executeGroupEngine.generate(executionContext.getExecutionUnits(), connection, statementOption);
            cacheStatements(inputGroups);
            reply();
            MergedResult mergedResult = mergeQuery(preparedStatementExecutor.executeQuery(inputGroups));
            result = new ShardingSphereResultSet(statements.stream().map(this::getResultSet).collect(Collectors.toList()), mergedResult, this, executionContext);
        } finally {
            clearBatch();
        }
        currentResultSet = result;
        return result;
    }
    
    @Override
    public int executeUpdate() throws SQLException {
        try {
            clearPrevious();
            executionContext = createExecutionContext();
            PreparedStatementExecuteGroupEngine executeGroupEngine = new PreparedStatementExecuteGroupEngine(
                    schemaContexts.getProperties().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY), schemaContexts.getDefaultSchemaContext().getSchema().getRules());
            Collection<InputGroup<StatementExecuteUnit>> inputGroups = executeGroupEngine.generate(executionContext.getExecutionUnits(), connection, statementOption);
            cacheStatements(inputGroups);
            reply();
            return preparedStatementExecutor.executeUpdate(inputGroups, executionContext.getSqlStatementContext());
        } finally {
            clearBatch();
        }
    }
    
    @Override
    public boolean execute() throws SQLException {
        try {
            clearPrevious();
            executionContext = createExecutionContext();
            PreparedStatementExecuteGroupEngine executeGroupEngine = new PreparedStatementExecuteGroupEngine(
                    schemaContexts.getProperties().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY), schemaContexts.getDefaultSchemaContext().getSchema().getRules());
            Collection<InputGroup<StatementExecuteUnit>> inputGroups = executeGroupEngine.generate(executionContext.getExecutionUnits(), connection, statementOption);
            cacheStatements(inputGroups);
            reply();
            return preparedStatementExecutor.execute(inputGroups, executionContext.getSqlStatementContext());
        } finally {
            clearBatch();
        }
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
                result.add(new StreamQueryResult(each));
            }
        }
        return result;
    }
    
    private ExecutionContext createExecutionContext() {
        SchemaContext schemaContext = schemaContexts.getDefaultSchemaContext();
        RouteContext routeContext = 
                new DataNodeRouter(schemaContext.getSchema().getMetaData(), schemaContexts.getProperties(), schemaContext.getSchema().getRules()).route(sqlStatement, sql, getParameters());
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(schemaContext.getSchema().getMetaData().getSchema().getConfiguredSchemaMetaData(), 
                schemaContexts.getProperties(), schemaContext.getSchema().getRules()).rewrite(sql, new ArrayList<>(getParameters()), routeContext);
        ExecutionContext result = new ExecutionContext(routeContext.getSqlStatementContext(), ExecutionContextBuilder.build(schemaContext.getSchema().getMetaData(), sqlRewriteResult));
        findGeneratedKey(result).ifPresent(generatedKey -> generatedValues.add(generatedKey.getGeneratedValues().getLast()));
        logSQL(result);
        return result;
    }
    
    private MergedResult mergeQuery(final List<QueryResult> queryResults) throws SQLException {
        SchemaContext schemaContext = schemaContexts.getDefaultSchemaContext();
        MergeEngine mergeEngine = new MergeEngine(schemaContext.getSchema().getDatabaseType(), 
                schemaContext.getSchema().getMetaData().getSchema().getConfiguredSchemaMetaData(), schemaContexts.getProperties(), schemaContext.getSchema().getRules());
        return mergeEngine.merge(queryResults, executionContext.getSqlStatementContext());
    }
    
    private void reply() {
        setParametersForStatements();
        replayMethodForStatements();
    }
    
    private void cacheStatements(final Collection<InputGroup<StatementExecuteUnit>> inputGroups) {
        for (InputGroup<StatementExecuteUnit> each : inputGroups) {
            statements.addAll(each.getInputs().stream().map(statementExecuteUnit -> (PreparedStatement) statementExecuteUnit.getStorageResource()).collect(Collectors.toList()));
            parameterSets.addAll(each.getInputs().stream().map(input -> input.getExecutionUnit().getSqlUnit().getParameters()).collect(Collectors.toList()));
        }
    }
    
    private void setParametersForStatements() {
        for (int i = 0; i < statements.size(); i++) {
            replaySetParameter(statements.get(i), parameterSets.get(i));
        }
    }
    
    private void replayMethodForStatements() {
        statements.forEach(this::replayMethodsInvocation);
    }
    
    private void clearPrevious() throws SQLException {
        clearStatements();
        parameterSets.clear();
    }
    
    private Optional<GeneratedKeyContext> findGeneratedKey(final ExecutionContext executionContext) {
        return executionContext.getSqlStatementContext() instanceof InsertStatementContext
                ? ((InsertStatementContext) executionContext.getSqlStatementContext()).getGeneratedKeyContext() : Optional.empty();
    }
    
    private void logSQL(final ExecutionContext executionContext) {
        if (schemaContexts.getProperties().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)) {
            SQLLogger.logSQL(sql, schemaContexts.getProperties().<Boolean>getValue(ConfigurationPropertyKey.SQL_SIMPLE), executionContext);
        }
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
    public void addBatch() {
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
            initBatchPreparedStatementExecutor();
            return batchPreparedStatementExecutor.executeBatch(executionContext.getSqlStatementContext());
        } finally {
            clearBatch();
        }
    }
    
    private void initBatchPreparedStatementExecutor() throws SQLException {
        PreparedStatementExecuteGroupEngine executeGroupEngine = new PreparedStatementExecuteGroupEngine(
                schemaContexts.getProperties().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY), schemaContexts.getDefaultSchemaContext().getSchema().getRules());
        batchPreparedStatementExecutor.init(executeGroupEngine.generate(
                new ArrayList<>(batchPreparedStatementExecutor.getBatchExecutionUnits()).stream().map(BatchExecutionUnit::getExecutionUnit).collect(Collectors.toList()), connection, statementOption));
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
        return schemaContexts.getDefaultSchemaContext().getSchema().getRules().stream().anyMatch(
            each -> each instanceof DataNodeRoutedRule && ((DataNodeRoutedRule) each).isNeedAccumulate(executionContext.getSqlStatementContext().getTablesContext().getTableNames()));
    }
    
    @Override
    public Collection<PreparedStatement> getRoutedStatements() {
        return statements;
    }
    
    private void clearStatements() throws SQLException {
        statements.clear();
        for (Statement each : statements) {
            each.close();
        }
    }
}
