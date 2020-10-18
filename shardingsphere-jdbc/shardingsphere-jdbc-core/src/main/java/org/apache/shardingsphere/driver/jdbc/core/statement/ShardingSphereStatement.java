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
import org.apache.shardingsphere.driver.executor.StatementExecutor;
import org.apache.shardingsphere.driver.jdbc.adapter.AbstractStatementAdapter;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.constant.SQLExceptionConstant;
import org.apache.shardingsphere.driver.jdbc.core.resultset.GeneratedKeysResultSet;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.context.schema.SchemaContexts;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.kernel.InputGroup;
import org.apache.shardingsphere.infra.executor.sql.ExecutorConstant;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.log.SQLLogger;
import org.apache.shardingsphere.infra.executor.sql.raw.RawSQLExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.RawJDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.raw.group.RawExecuteGroupEngine;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.StatementExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.executor.SQLExecutor;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.group.StatementExecuteGroupEngine;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.group.StatementOption;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.queryresult.StreamQueryResult;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.model.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.rule.DataNodeRoutedRule;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.rdl.parser.engine.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;

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
    
    private final SchemaContexts schemaContexts;
    
    private final List<Statement> statements;
    
    private final StatementOption statementOption;
    
    private final StatementExecutor statementExecutor;
    
    private final RawJDBCExecutor rawExecutor;
    
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
        schemaContexts = connection.getSchemaContexts();
        statements = new LinkedList<>();
        statementOption = new StatementOption(resultSetType, resultSetConcurrency, resultSetHoldability);
        statementExecutor = new StatementExecutor(connection.getDataSourceMap(), schemaContexts, new SQLExecutor(schemaContexts.getExecutorKernel(), connection.isHoldTransaction()));
        rawExecutor = new RawJDBCExecutor(schemaContexts.getExecutorKernel(), connection.isHoldTransaction());
        kernelProcessor = new KernelProcessor();
    }
    
    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        if (Strings.isNullOrEmpty(sql)) {
            throw new SQLException(SQLExceptionConstant.SQL_STRING_NULL_OR_EMPTY);
        }
        ResultSet result;
        try {
            executionContext = createExecutionContext(sql);
            List<QueryResult> queryResults;
            if (ExecutorConstant.MANAGED_RESOURCE) {
                Collection<InputGroup<StatementExecuteUnit>> inputGroups = getInputGroups();
                cacheStatements(inputGroups);
                queryResults = statementExecutor.executeQuery(inputGroups);
            } else {
                queryResults = rawExecutor.executeQuery(getRawInputGroups(), new RawSQLExecutorCallback());
            }
            MergedResult mergedResult = mergeQuery(queryResults);
            result = new ShardingSphereResultSet(statements.stream().map(this::getResultSet).collect(Collectors.toList()), mergedResult, this, executionContext);
        } finally {
            currentResultSet = null;
        }
        currentResultSet = result;
        return result;
    }
    
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        try {
            executionContext = createExecutionContext(sql);
            if (ExecutorConstant.MANAGED_RESOURCE) {
                Collection<InputGroup<StatementExecuteUnit>> inputGroups = getInputGroups();
                cacheStatements(inputGroups);
                return statementExecutor.executeUpdate(inputGroups, executionContext.getSqlStatementContext(), executionContext.getRouteContext().getRouteUnits());
            } else {
                return rawExecutor.executeUpdate(getRawInputGroups(), new RawSQLExecutorCallback());
            }
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
            if (ExecutorConstant.MANAGED_RESOURCE) {
                Collection<InputGroup<StatementExecuteUnit>> inputGroups = getInputGroups();
                cacheStatements(inputGroups);
                return statementExecutor.executeUpdate(inputGroups, executionContext.getSqlStatementContext(), executionContext.getRouteContext().getRouteUnits(), autoGeneratedKeys);
            } else {
                return rawExecutor.executeUpdate(getRawInputGroups(), new RawSQLExecutorCallback());
            }
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        returnGeneratedKeys = true;
        try {
            executionContext = createExecutionContext(sql);
            if (ExecutorConstant.MANAGED_RESOURCE) {
                Collection<InputGroup<StatementExecuteUnit>> inputGroups = getInputGroups();
                cacheStatements(inputGroups);
                return statementExecutor.executeUpdate(inputGroups, executionContext.getSqlStatementContext(), executionContext.getRouteContext().getRouteUnits(), columnIndexes);
            } else {
                return rawExecutor.executeUpdate(getRawInputGroups(), new RawSQLExecutorCallback());
            }
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        returnGeneratedKeys = true;
        try {
            executionContext = createExecutionContext(sql);
            if (ExecutorConstant.MANAGED_RESOURCE) {
                Collection<InputGroup<StatementExecuteUnit>> inputGroups = getInputGroups();
                cacheStatements(inputGroups);
                return statementExecutor.executeUpdate(inputGroups, executionContext.getSqlStatementContext(), executionContext.getRouteContext().getRouteUnits(), columnNames);
            } else {
                return rawExecutor.executeUpdate(getRawInputGroups(), new RawSQLExecutorCallback());
            }
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public boolean execute(final String sql) throws SQLException {
        try {
            executionContext = createExecutionContext(sql);
            if (ExecutorConstant.MANAGED_RESOURCE) {
                Collection<InputGroup<StatementExecuteUnit>> inputGroups = getInputGroups();
                cacheStatements(inputGroups);
                return statementExecutor.execute(inputGroups, executionContext.getSqlStatementContext().getSqlStatement(), executionContext.getRouteContext().getRouteUnits());
            } else {
                // TODO process getStatement
                return rawExecutor.execute(getRawInputGroups(), new RawSQLExecutorCallback());
            }
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        if (RETURN_GENERATED_KEYS == autoGeneratedKeys) {
            returnGeneratedKeys = true;
        }
        try {
            executionContext = createExecutionContext(sql);
            if (ExecutorConstant.MANAGED_RESOURCE) {
                Collection<InputGroup<StatementExecuteUnit>> inputGroups = getInputGroups();
                cacheStatements(inputGroups);
                return statementExecutor.execute(inputGroups, executionContext.getSqlStatementContext().getSqlStatement(), executionContext.getRouteContext().getRouteUnits(), autoGeneratedKeys);
            } else {
                // TODO process getStatement
                return rawExecutor.execute(getRawInputGroups(), new RawSQLExecutorCallback());
            }
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        returnGeneratedKeys = true;
        try {
            executionContext = createExecutionContext(sql);
            if (ExecutorConstant.MANAGED_RESOURCE) {
                Collection<InputGroup<StatementExecuteUnit>> inputGroups = getInputGroups();
                cacheStatements(inputGroups);
                return statementExecutor.execute(inputGroups, executionContext.getSqlStatementContext().getSqlStatement(), executionContext.getRouteContext().getRouteUnits(), columnIndexes);
            } else {
                // TODO process getStatement
                return rawExecutor.execute(getRawInputGroups(), new RawSQLExecutorCallback());
            }
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        returnGeneratedKeys = true;
        try {
            executionContext = createExecutionContext(sql);
            if (ExecutorConstant.MANAGED_RESOURCE) {
                Collection<InputGroup<StatementExecuteUnit>> inputGroups = getInputGroups();
                cacheStatements(inputGroups);
                return statementExecutor.execute(inputGroups, executionContext.getSqlStatementContext().getSqlStatement(), executionContext.getRouteContext().getRouteUnits(), columnNames);
            } else {
                // TODO process getStatement
                return rawExecutor.execute(getRawInputGroups(), new RawSQLExecutorCallback());
            }
        } finally {
            currentResultSet = null;
        }
    }
    
    private ExecutionContext createExecutionContext(final String sql) throws SQLException {
        clearStatements();
        LogicSQL logicSQL = createLogicSQL(sql);
        ExecutionContext result = kernelProcessor.generateExecutionContext(logicSQL, schemaContexts.getDefaultSchema(), schemaContexts.getProps());
        logSQL(logicSQL, schemaContexts.getProps(), result);
        return result;
    }
    
    private void clearStatements() throws SQLException {
        for (Statement each : statements) {
            each.close();
        }
        statements.clear();
    }
    
    private void logSQL(final LogicSQL logicSQL, final ConfigurationProperties props, final ExecutionContext executionContext) {
        if (props.<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)) {
            SQLLogger.logSQL(logicSQL, props.<Boolean>getValue(ConfigurationPropertyKey.SQL_SIMPLE), executionContext);
        }
    }
    
    private LogicSQL createLogicSQL(final String sql) {
        PhysicalSchemaMetaData schemaMetaData = schemaContexts.getDefaultSchema().getMetaData().getSchemaMetaData().getSchemaMetaData();
        ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(DatabaseTypeRegistry.getTrunkDatabaseTypeName(schemaContexts.getDatabaseType()));
        SQLStatement sqlStatement = sqlParserEngine.parse(sql, false);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(schemaMetaData, Collections.emptyList(), sqlStatement);
        return new LogicSQL(sqlStatementContext, sql, Collections.emptyList());
    }
    
    private Collection<InputGroup<StatementExecuteUnit>> getInputGroups() throws SQLException {
        int maxConnectionsSizePerQuery = schemaContexts.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new StatementExecuteGroupEngine(maxConnectionsSizePerQuery, connection, statementOption,
                schemaContexts.getDefaultSchema().getRules()).generate(executionContext.getRouteContext(), executionContext.getExecutionUnits());
    }
    
    private Collection<InputGroup<RawSQLExecuteUnit>> getRawInputGroups() throws SQLException {
        int maxConnectionsSizePerQuery = schemaContexts.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new RawExecuteGroupEngine(maxConnectionsSizePerQuery, schemaContexts.getDefaultSchema().getRules())
                .generate(executionContext.getRouteContext(), executionContext.getExecutionUnits());
    }
    
    private void cacheStatements(final Collection<InputGroup<StatementExecuteUnit>> inputGroups) {
        for (InputGroup<StatementExecuteUnit> each : inputGroups) {
            statements.addAll(each.getInputs().stream().map(StatementExecuteUnit::getStorageResource).collect(Collectors.toList()));
        }
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
    
    private MergedResult mergeQuery(final List<QueryResult> queryResults) throws SQLException {
        ShardingSphereSchema schema = schemaContexts.getDefaultSchema();
        MergeEngine mergeEngine = new MergeEngine(schemaContexts.getDatabaseType(),
                schema.getMetaData().getSchemaMetaData().getConfiguredSchemaMetaData(), schemaContexts.getProps(), schema.getRules());
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
        return schemaContexts.getDefaultSchema().getRules().stream().anyMatch(
            each -> each instanceof DataNodeRoutedRule && ((DataNodeRoutedRule) each).isNeedAccumulate(executionContext.getSqlStatementContext().getTablesContext().getTableNames()));
    }
    
    @Override
    public Collection<Statement> getRoutedStatements() {
        return statements;
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        Optional<GeneratedKeyContext> generatedKey = findGeneratedKey();
        if (returnGeneratedKeys && generatedKey.isPresent()) {
            return new GeneratedKeysResultSet(generatedKey.get().getColumnName(), generatedKey.get().getGeneratedValues().iterator(), this);
        }
        if (1 == getRoutedStatements().size()) {
            return getRoutedStatements().iterator().next().getGeneratedKeys();
        }
        return new GeneratedKeysResultSet();
    }
    
    private Optional<GeneratedKeyContext> findGeneratedKey() {
        return executionContext.getSqlStatementContext() instanceof InsertStatementContext
                ? ((InsertStatementContext) executionContext.getSqlStatementContext()).getGeneratedKeyContext() : Optional.empty();
    }
}
