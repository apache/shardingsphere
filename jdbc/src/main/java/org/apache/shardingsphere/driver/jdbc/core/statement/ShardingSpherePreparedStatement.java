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
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.keygen.DialectGeneratedKeyOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.exception.core.SQLExceptionTransformEngine;
import org.apache.shardingsphere.driver.executor.callback.add.StatementAddCallback;
import org.apache.shardingsphere.driver.executor.engine.batch.preparedstatement.DriverExecuteBatchExecutor;
import org.apache.shardingsphere.driver.executor.engine.facade.DriverExecutorFacade;
import org.apache.shardingsphere.driver.jdbc.adapter.AbstractPreparedStatementAdapter;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.resultset.GeneratedKeysResultSet;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;
import org.apache.shardingsphere.driver.jdbc.core.statement.metadata.ShardingSphereParameterMetaData;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.syntax.EmptySQLException;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.resoure.StorageConnectorReusableRuleAttribute;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

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
import java.util.Map;
import java.util.Optional;

/**
 * ShardingSphere prepared statement.
 */
@HighFrequencyInvocation
public final class ShardingSpherePreparedStatement extends AbstractPreparedStatementAdapter {
    
    @Getter
    private final ShardingSphereConnection connection;
    
    private final ShardingSphereMetaData metaData;
    
    private final String sql;
    
    private final HintValueContext hintValueContext;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final ShardingSphereDatabase usedDatabase;
    
    private final StatementOption statementOption;
    
    @Getter(AccessLevel.PROTECTED)
    private final StatementManager statementManager;
    
    @Getter
    private final ParameterMetaData parameterMetaData;
    
    private final DriverExecutorFacade driverExecutorFacade;
    
    private final DriverExecuteBatchExecutor executeBatchExecutor;
    
    private final List<PreparedStatement> statements = new ArrayList<>();
    
    private final List<List<Object>> parameterSets = new ArrayList<>();
    
    private final Collection<Comparable<?>> generatedValues = new LinkedList<>();
    
    private final boolean statementsCacheable;
    
    private Map<String, Integer> columnLabelAndIndexMap;
    
    private ResultSet currentResultSet;
    
    private ResultSet currentBatchGeneratedKeysResultSet;
    
    private QueryContext queryContext;
    
    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql) throws SQLException {
        this(connection, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT, false, null);
    }
    
    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        this(connection, sql, resultSetType, resultSetConcurrency, ResultSet.HOLD_CURSORS_OVER_COMMIT, false, null);
    }
    
    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql, final int autoGeneratedKeys) throws SQLException {
        this(connection, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT, RETURN_GENERATED_KEYS == autoGeneratedKeys, null);
    }
    
    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql, final String[] columns) throws SQLException {
        this(connection, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT, true, columns);
    }
    
    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency,
                                           final int resultSetHoldability) throws SQLException {
        this(connection, sql, resultSetType, resultSetConcurrency, resultSetHoldability, false, null);
    }
    
    private ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String originSQL, final int resultSetType, final int resultSetConcurrency,
                                            final int resultSetHoldability, final boolean returnGeneratedKeys, final String[] columns) throws SQLException {
        ShardingSpherePreconditions.checkNotEmpty(originSQL, () -> new EmptySQLException().toSQLException());
        this.connection = connection;
        metaData = connection.getContextManager().getMetaDataContexts().getMetaData();
        sql = SQLHintUtils.removeHint(originSQL);
        hintValueContext = SQLHintUtils.extractHint(originSQL);
        DatabaseType databaseType = metaData.getDatabase(connection.getCurrentDatabaseName()).getProtocolType();
        SQLStatement sqlStatement = metaData.getGlobalRuleMetaData().getSingleRule(SQLParserRule.class).getSQLParserEngine(databaseType).parse(sql, true);
        sqlStatementContext = new SQLBindEngine(metaData, connection.getCurrentDatabaseName(), hintValueContext).bind(sqlStatement);
        String usedDatabaseName = sqlStatementContext.getTablesContext().getDatabaseName().orElse(connection.getCurrentDatabaseName());
        connection.getDatabaseConnectionManager().getConnectionContext().setCurrentDatabaseName(connection.getCurrentDatabaseName());
        usedDatabase = metaData.getDatabase(usedDatabaseName);
        statementOption = returnGeneratedKeys ? new StatementOption(true, columns) : new StatementOption(resultSetType, resultSetConcurrency, resultSetHoldability);
        statementManager = new StatementManager();
        connection.getStatementManagers().add(statementManager);
        parameterMetaData = new ShardingSphereParameterMetaData(sqlStatement);
        driverExecutorFacade = new DriverExecutorFacade(connection, statementOption, statementManager, JDBCDriverType.PREPARED_STATEMENT);
        executeBatchExecutor = new DriverExecuteBatchExecutor(connection, metaData, statementOption, statementManager, usedDatabase);
        statementsCacheable = isStatementsCacheable();
    }
    
    private boolean isStatementsCacheable() {
        return usedDatabase.getRuleMetaData().getAttributes(StorageConnectorReusableRuleAttribute.class).size() == usedDatabase.getRuleMetaData().getRules().size()
                && !HintManager.isInstantiated();
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        try {
            if (statementsCacheable && !statements.isEmpty()) {
                resetParameters();
                return statements.iterator().next().executeQuery();
            }
            clearPrevious();
            QueryContext queryContext = createQueryContext();
            this.queryContext = queryContext;
            handleAutoCommitBeforeExecution(queryContext.getSqlStatementContext().getSqlStatement(), connection);
            findGeneratedKey().ifPresent(optional -> generatedValues.addAll(optional.getGeneratedValues()));
            currentResultSet =
                    driverExecutorFacade.executeQuery(usedDatabase, metaData, queryContext, this, columnLabelAndIndexMap, (StatementAddCallback<PreparedStatement>) this::addStatements, this::replay);
            if (currentResultSet instanceof ShardingSphereResultSet) {
                columnLabelAndIndexMap = ((ShardingSphereResultSet) currentResultSet).getColumnLabelAndIndexMap();
            }
            return currentResultSet;
            // CHECKSTYLE:OFF
        } catch (final RuntimeException | SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, usedDatabase.getProtocolType());
        } finally {
            executeBatchExecutor.clear();
            clearParameters();
        }
    }
    
    private void addStatements(final Collection<PreparedStatement> statements, final Collection<List<Object>> parameterSets) {
        this.statements.addAll(statements);
        this.parameterSets.addAll(parameterSets);
    }
    
    private void resetParameters() throws SQLException {
        replaySetParameter(statements, Collections.singletonList(getParameters()));
    }
    
    @Override
    public int executeUpdate() throws SQLException {
        try {
            if (statementsCacheable && !statements.isEmpty()) {
                resetParameters();
                return statements.iterator().next().executeUpdate();
            }
            clearPrevious();
            QueryContext queryContext = createQueryContext();
            this.queryContext = queryContext;
            handleAutoCommitBeforeExecution(queryContext.getSqlStatementContext().getSqlStatement(), connection);
            int result = driverExecutorFacade.executeUpdate(usedDatabase, metaData, queryContext,
                    (sql, statement) -> ((PreparedStatement) statement).executeUpdate(), (StatementAddCallback<PreparedStatement>) this::addStatements, this::replay);
            findGeneratedKey().ifPresent(optional -> generatedValues.addAll(optional.getGeneratedValues()));
            return result;
            // CHECKSTYLE:OFF
        } catch (final RuntimeException | SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, usedDatabase.getProtocolType());
        } finally {
            clearBatch();
        }
    }
    
    @Override
    public boolean execute() throws SQLException {
        try {
            if (statementsCacheable && !statements.isEmpty()) {
                resetParameters();
                return statements.iterator().next().execute();
            }
            clearPrevious();
            QueryContext queryContext = createQueryContext();
            this.queryContext = queryContext;
            handleAutoCommitBeforeExecution(queryContext.getSqlStatementContext().getSqlStatement(), connection);
            boolean result = driverExecutorFacade.execute(usedDatabase, metaData, queryContext, (sql, statement) -> ((PreparedStatement) statement).execute(),
                    (StatementAddCallback<PreparedStatement>) this::addStatements, this::replay);
            findGeneratedKey().ifPresent(optional -> generatedValues.addAll(optional.getGeneratedValues()));
            return result;
            // CHECKSTYLE:OFF
        } catch (final RuntimeException | SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, usedDatabase.getProtocolType());
        } finally {
            clearBatch();
        }
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        if (null != currentResultSet) {
            return currentResultSet;
        }
        driverExecutorFacade.getResultSet(usedDatabase, queryContext, this, statements).ifPresent(optional -> currentResultSet = optional);
        if (null == columnLabelAndIndexMap && currentResultSet instanceof ShardingSphereResultSet) {
            columnLabelAndIndexMap = ((ShardingSphereResultSet) currentResultSet).getColumnLabelAndIndexMap();
        }
        return currentResultSet;
    }
    
    private QueryContext createQueryContext() {
        List<Object> params = new ArrayList<>(getParameters());
        if (sqlStatementContext instanceof ParameterAware) {
            ((ParameterAware) sqlStatementContext).bindParameters(params);
        }
        return new QueryContext(sqlStatementContext, sql, params, hintValueContext, connection.getDatabaseConnectionManager().getConnectionContext(), metaData, true);
    }
    
    private void replay() throws SQLException {
        replaySetParameter(statements, parameterSets);
        for (Statement each : statements) {
            getMethodInvocationRecorder().replay(each);
        }
    }
    
    private void replaySetParameter(final List<PreparedStatement> statements, final List<List<Object>> parameterSets) throws SQLException {
        for (int i = 0; i < statements.size(); i++) {
            replaySetParameter(statements.get(i), parameterSets.get(i));
        }
    }
    
    private void clearPrevious() {
        currentResultSet = null;
        statements.clear();
        parameterSets.clear();
        generatedValues.clear();
    }
    
    private Optional<GeneratedKeyContext> findGeneratedKey() {
        return sqlStatementContext instanceof InsertStatementContext ? ((InsertStatementContext) sqlStatementContext).getGeneratedKeyContext() : Optional.empty();
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        if (null != currentBatchGeneratedKeysResultSet) {
            return currentBatchGeneratedKeysResultSet;
        }
        Optional<GeneratedKeyContext> generatedKey = findGeneratedKey();
        if (generatedKey.isPresent() && statementOption.isReturnGeneratedKeys() && !generatedValues.isEmpty()) {
            return new GeneratedKeysResultSet(getGeneratedKeysColumnName(generatedKey.get().getColumnName()), generatedValues.iterator(), this);
        }
        for (PreparedStatement each : statements) {
            ResultSet resultSet = each.getGeneratedKeys();
            while (resultSet.next()) {
                generatedValues.add((Comparable<?>) resultSet.getObject(1));
            }
        }
        String columnName = generatedKey.map(GeneratedKeyContext::getColumnName).orElse(null);
        return new GeneratedKeysResultSet(getGeneratedKeysColumnName(columnName), generatedValues.iterator(), this);
    }
    
    private String getGeneratedKeysColumnName(final String columnName) {
        Optional<DialectGeneratedKeyOption> generatedKeyOption = new DatabaseTypeRegistry(usedDatabase.getProtocolType()).getDialectDatabaseMetaData().getGeneratedKeyOption();
        return generatedKeyOption.isPresent() ? generatedKeyOption.get().getColumnName() : columnName;
    }
    
    @Override
    public void addBatch() {
        currentResultSet = null;
        QueryContext queryContext = createQueryContext();
        this.queryContext = queryContext;
        executeBatchExecutor.addBatch(queryContext, usedDatabase);
        findGeneratedKey().ifPresent(optional -> generatedValues.addAll(optional.getGeneratedValues()));
        clearParameters();
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        try {
            return executeBatchExecutor.executeBatch(usedDatabase, sqlStatementContext, generatedValues, statementOption,
                    (StatementAddCallback<PreparedStatement>) (statements, parameterSets) -> this.statements.addAll(statements),
                    this::replaySetParameter,
                    () -> {
                        currentBatchGeneratedKeysResultSet = getGeneratedKeys();
                        statements.clear();
                    });
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, usedDatabase.getProtocolType());
        } finally {
            clearBatch();
        }
    }
    
    @Override
    public void clearBatch() {
        currentResultSet = null;
        closeCurrentBatchGeneratedKeysResultSet();
        executeBatchExecutor.clear();
        clearParameters();
    }
    
    private void closeCurrentBatchGeneratedKeysResultSet() {
        if (null != currentBatchGeneratedKeysResultSet) {
            try {
                currentBatchGeneratedKeysResultSet.close();
            } catch (final SQLException ignored) {
            } finally {
                currentBatchGeneratedKeysResultSet = null;
            }
        }
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
        for (DataNodeRuleAttribute each : usedDatabase.getRuleMetaData().getAttributes(DataNodeRuleAttribute.class)) {
            if (each.isNeedAccumulate(sqlStatementContext.getTablesContext().getTableNames())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Collection<PreparedStatement> getRoutedStatements() {
        return statements;
    }
    
    @Override
    protected void closeExecutor() throws SQLException {
        driverExecutorFacade.close();
    }
}
