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
import org.apache.shardingsphere.driver.executor.DriverJDBCExecutor;
import org.apache.shardingsphere.driver.executor.callback.ExecuteCallback;
import org.apache.shardingsphere.driver.executor.callback.ExecuteUpdateCallback;
import org.apache.shardingsphere.driver.executor.callback.impl.StatementExecuteQueryCallback;
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
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.log.SQLLogger;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.executor.sql.prepare.raw.RawExecutionPrepareEngine;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.type.RawExecutionRule;
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
        metaDataContexts = connection.getMetaDataContexts();
        statements = new LinkedList<>();
        statementOption = new StatementOption(resultSetType, resultSetConcurrency, resultSetHoldability);
        JDBCExecutor jdbcExecutor = new JDBCExecutor(metaDataContexts.getExecutorEngine(), connection.isHoldTransaction());
        driverJDBCExecutor = new DriverJDBCExecutor(connection.getDataSourceMap(), metaDataContexts, jdbcExecutor);
        rawExecutor = new RawExecutor(metaDataContexts.getExecutorEngine(), connection.isHoldTransaction());
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
            if (metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                queryResults = rawExecutor.executeQuery(createRawExecutionGroups(), new RawSQLExecutorCallback());
            } else {
                Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups = createExecutionGroups();
                cacheStatements(executionGroups);
                queryResults = driverJDBCExecutor.executeQuery(
                        executionGroups, new StatementExecuteQueryCallback(metaDataContexts.getDatabaseType(), SQLExecutorExceptionHandler.isExceptionThrown()));
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
            if (metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                return rawExecutor.executeUpdate(createRawExecutionGroups(), new RawSQLExecutorCallback());
            }
            Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups = createExecutionGroups();
            cacheStatements(executionGroups);
            return executeUpdate(executionGroups, 
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
            if (metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                return rawExecutor.executeUpdate(createRawExecutionGroups(), new RawSQLExecutorCallback());
            }
            Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups = createExecutionGroups();
            cacheStatements(executionGroups);
            return executeUpdate(executionGroups,
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
            if (metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                return rawExecutor.executeUpdate(createRawExecutionGroups(), new RawSQLExecutorCallback());
            }
            Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups = createExecutionGroups();
            cacheStatements(executionGroups);
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
            if (metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                return rawExecutor.executeUpdate(createRawExecutionGroups(), new RawSQLExecutorCallback());
            }
            Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups = createExecutionGroups();
            cacheStatements(executionGroups);
            return executeUpdate(executionGroups,
                (actualSQL, statement) -> statement.executeUpdate(actualSQL, columnNames), executionContext.getSqlStatementContext(), executionContext.getRouteContext().getRouteUnits());
        } finally {
            currentResultSet = null;
        }
    }
    
    private int executeUpdate(final Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups, final ExecuteUpdateCallback updater,
                              final SQLStatementContext<?> sqlStatementContext, final Collection<RouteUnit> routeUnits) throws SQLException {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        JDBCExecutorCallback<Integer> callback = new JDBCExecutorCallback<Integer>(metaDataContexts.getDatabaseType(), isExceptionThrown) {
            
            @Override
            protected Integer executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return updater.executeUpdate(sql, statement);
            }
        };
        return driverJDBCExecutor.executeUpdate(executionGroups, sqlStatementContext, routeUnits, callback);
    }
    
    @Override
    public boolean execute(final String sql) throws SQLException {
        try {
            executionContext = createExecutionContext(sql);
            if (metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                // TODO process getStatement
                return rawExecutor.execute(createRawExecutionGroups(), new RawSQLExecutorCallback());
            }
            Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups = createExecutionGroups();
            cacheStatements(executionGroups);
            return execute(executionGroups, 
                (actualSQL, statement) -> statement.execute(actualSQL), executionContext.getSqlStatementContext().getSqlStatement(), executionContext.getRouteContext().getRouteUnits());
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
            if (metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                // TODO process getStatement
                return rawExecutor.execute(createRawExecutionGroups(), new RawSQLExecutorCallback());
            }
            Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups = createExecutionGroups();
            cacheStatements(executionGroups);
            return execute(executionGroups, (actualSQL, statement) -> statement.execute(actualSQL, autoGeneratedKeys), 
                    executionContext.getSqlStatementContext().getSqlStatement(), executionContext.getRouteContext().getRouteUnits());
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        returnGeneratedKeys = true;
        try {
            executionContext = createExecutionContext(sql);
            if (metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                // TODO process getStatement
                return rawExecutor.execute(createRawExecutionGroups(), new RawSQLExecutorCallback());
            }
            Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups = createExecutionGroups();
            cacheStatements(executionGroups);
            return execute(executionGroups, (actualSQL, statement) -> statement.execute(actualSQL, columnIndexes),
                    executionContext.getSqlStatementContext().getSqlStatement(), executionContext.getRouteContext().getRouteUnits());
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        returnGeneratedKeys = true;
        try {
            executionContext = createExecutionContext(sql);
            if (metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                // TODO process getStatement
                return rawExecutor.execute(createRawExecutionGroups(), new RawSQLExecutorCallback());
            }
            Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups = createExecutionGroups();
            cacheStatements(executionGroups);
            return execute(executionGroups, (actualSQL, statement) -> statement.execute(actualSQL, columnNames),
                    executionContext.getSqlStatementContext().getSqlStatement(), executionContext.getRouteContext().getRouteUnits());
        } finally {
            currentResultSet = null;
        }
    }
    
    private boolean execute(final Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups, final ExecuteCallback executor,
                            final SQLStatement sqlStatement, final Collection<RouteUnit> routeUnits) throws SQLException {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        JDBCExecutorCallback<Boolean> jdbcExecutorCallback = new JDBCExecutorCallback<Boolean>(metaDataContexts.getDatabaseType(), isExceptionThrown) {
            
            @Override
            protected Boolean executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return executor.execute(sql, statement);
            }
        };
        return driverJDBCExecutor.execute(executionGroups, sqlStatement, routeUnits, jdbcExecutorCallback);
    }
    
    private ExecutionContext createExecutionContext(final String sql) throws SQLException {
        clearStatements();
        LogicSQL logicSQL = createLogicSQL(sql);
        ExecutionContext result = kernelProcessor.generateExecutionContext(logicSQL, metaDataContexts.getDefaultMetaData(), metaDataContexts.getProps());
        logSQL(logicSQL, metaDataContexts.getProps(), result);
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
        ShardingSphereSchema schema = metaDataContexts.getDefaultMetaData().getSchema();
        ShardingSphereSQLParserEngine sqlStatementParserEngine = new ShardingSphereSQLParserEngine(DatabaseTypeRegistry.getTrunkDatabaseTypeName(metaDataContexts.getDatabaseType()));
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(schema, Collections.emptyList(), sqlStatement);
        return new LogicSQL(sqlStatementContext, sql, Collections.emptyList());
    }
    
    private Collection<ExecutionGroup<JDBCExecutionUnit>> createExecutionGroups() throws SQLException {
        int maxConnectionsSizePerQuery = metaDataContexts.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = new DriverExecutionPrepareEngine<>(
                JDBCDriverType.STATEMENT, maxConnectionsSizePerQuery, connection, statementOption, metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules());
        return prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
    }
    
    private Collection<ExecutionGroup<RawSQLExecutionUnit>> createRawExecutionGroups() throws SQLException {
        int maxConnectionsSizePerQuery = metaDataContexts.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new RawExecutionPrepareEngine(maxConnectionsSizePerQuery, metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules())
                .prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
    }
    
    private void cacheStatements(final Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups) {
        for (ExecutionGroup<JDBCExecutionUnit> each : executionGroups) {
            statements.addAll(each.getInputs().stream().map(JDBCExecutionUnit::getStorageResource).collect(Collectors.toList()));
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
                result.add(new JDBCStreamQueryResult(each));
            }
        }
        return result;
    }
    
    private MergedResult mergeQuery(final List<QueryResult> queryResults) throws SQLException {
        ShardingSphereMetaData metaData = metaDataContexts.getDefaultMetaData();
        MergeEngine mergeEngine = new MergeEngine(metaDataContexts.getDatabaseType(), metaData.getSchema(), metaDataContexts.getProps(), metaData.getRuleMetaData().getRules());
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
        return metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().stream().anyMatch(
            each -> each instanceof DataNodeContainedRule && ((DataNodeContainedRule) each).isNeedAccumulate(executionContext.getSqlStatementContext().getTablesContext().getTableNames()));
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
