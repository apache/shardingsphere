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
import org.apache.shardingsphere.driver.executor.callback.execute.StatementExecuteCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.StatementExecuteUpdateCallback;
import org.apache.shardingsphere.driver.executor.engine.batch.statement.BatchStatementExecutor;
import org.apache.shardingsphere.driver.executor.engine.facade.DriverExecutorFacade;
import org.apache.shardingsphere.driver.jdbc.adapter.AbstractStatementAdapter;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.resultset.GeneratedKeysResultSet;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.syntax.EmptySQLException;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * ShardingSphere statement.
 */
@HighFrequencyInvocation
public final class ShardingSphereStatement extends AbstractStatementAdapter {
    
    @Getter
    private final ShardingSphereConnection connection;
    
    private final ShardingSphereMetaData metaData;
    
    private final StatementOption statementOption;
    
    @Getter(AccessLevel.PROTECTED)
    private final StatementManager statementManager;
    
    private final DriverExecutorFacade driverExecutorFacade;
    
    private final BatchStatementExecutor batchStatementExecutor;
    
    private final List<Statement> statements;
    
    private String usedDatabaseName;
    
    private QueryContext queryContext;
    
    private boolean returnGeneratedKeys;
    
    private ResultSet currentResultSet;
    
    public ShardingSphereStatement(final ShardingSphereConnection connection) {
        this(connection, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }
    
    public ShardingSphereStatement(final ShardingSphereConnection connection, final int resultSetType, final int resultSetConcurrency) {
        this(connection, resultSetType, resultSetConcurrency, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }
    
    public ShardingSphereStatement(final ShardingSphereConnection connection, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) {
        this.connection = connection;
        metaData = connection.getContextManager().getMetaDataContexts().getMetaData();
        statementOption = new StatementOption(resultSetType, resultSetConcurrency, resultSetHoldability);
        statementManager = new StatementManager();
        connection.getStatementManagers().add(statementManager);
        driverExecutorFacade = new DriverExecutorFacade(connection, statementOption, statementManager, JDBCDriverType.STATEMENT);
        batchStatementExecutor = new BatchStatementExecutor(this);
        statements = new LinkedList<>();
        usedDatabaseName = connection.getCurrentDatabaseName();
    }
    
    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        QueryContext queryContext = createQueryContext(sql);
        try {
            prepareExecute(queryContext);
            ShardingSphereDatabase usedDatabase = metaData.getDatabase(usedDatabaseName);
            currentResultSet = driverExecutorFacade.executeQuery(usedDatabase, metaData, queryContext, this, null,
                    (StatementAddCallback<Statement>) (statements, parameterSets) -> this.statements.addAll(statements), this::replay);
            return currentResultSet;
            // CHECKSTYLE:OFF
        } catch (final RuntimeException | SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            currentResultSet = null;
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(usedDatabaseName).getProtocolType());
        }
    }
    
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        try {
            return executeUpdate(sql, (actualSQL, statement) -> statement.executeUpdate(actualSQL));
            // CHECKSTYLE:OFF
        } catch (final RuntimeException | SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(usedDatabaseName).getProtocolType());
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        if (RETURN_GENERATED_KEYS == autoGeneratedKeys) {
            currentResultSet = null;
            returnGeneratedKeys = true;
        }
        try {
            return executeUpdate(sql, (actualSQL, statement) -> statement.executeUpdate(actualSQL, autoGeneratedKeys));
            // CHECKSTYLE:OFF
        } catch (final RuntimeException | SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(usedDatabaseName).getProtocolType());
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        returnGeneratedKeys = true;
        try {
            return executeUpdate(sql, (actualSQL, statement) -> statement.executeUpdate(actualSQL, columnIndexes));
            // CHECKSTYLE:OFF
        } catch (final RuntimeException | SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(usedDatabaseName).getProtocolType());
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        returnGeneratedKeys = true;
        try {
            return executeUpdate(sql, (actualSQL, statement) -> statement.executeUpdate(actualSQL, columnNames));
            // CHECKSTYLE:OFF
        } catch (final RuntimeException | SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(usedDatabaseName).getProtocolType());
        }
    }
    
    private int executeUpdate(final String sql, final StatementExecuteUpdateCallback updateCallback) throws SQLException {
        currentResultSet = null;
        QueryContext queryContext = createQueryContext(sql);
        prepareExecute(queryContext);
        ShardingSphereDatabase usedDatabase = metaData.getDatabase(usedDatabaseName);
        return driverExecutorFacade.executeUpdate(usedDatabase, metaData, queryContext,
                updateCallback, (StatementAddCallback<Statement>) (statements, parameterSets) -> this.statements.addAll(statements), this::replay);
    }
    
    @Override
    public boolean execute(final String sql) throws SQLException {
        try {
            return execute(sql, (actualSQL, statement) -> statement.execute(actualSQL));
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(usedDatabaseName).getProtocolType());
        }
    }
    
    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        try {
            if (RETURN_GENERATED_KEYS == autoGeneratedKeys) {
                returnGeneratedKeys = true;
            }
            return execute(sql, (actualSQL, statement) -> statement.execute(actualSQL, autoGeneratedKeys));
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(usedDatabaseName).getProtocolType());
        }
    }
    
    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        try {
            returnGeneratedKeys = true;
            return execute(sql, (actualSQL, statement) -> statement.execute(actualSQL, columnIndexes));
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(usedDatabaseName).getProtocolType());
        }
    }
    
    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        try {
            returnGeneratedKeys = true;
            return execute(sql, (actualSQL, statement) -> statement.execute(actualSQL, columnNames));
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(usedDatabaseName).getProtocolType());
        }
    }
    
    private boolean execute(final String sql, final StatementExecuteCallback statementExecuteCallback) throws SQLException {
        currentResultSet = null;
        QueryContext queryContext = createQueryContext(sql);
        prepareExecute(queryContext);
        ShardingSphereDatabase usedDatabase = metaData.getDatabase(usedDatabaseName);
        return driverExecutorFacade.execute(usedDatabase, metaData, queryContext, statementExecuteCallback,
                (StatementAddCallback<Statement>) (statements, parameterSets) -> this.statements.addAll(statements), this::replay);
    }
    
    private QueryContext createQueryContext(final String originSQL) throws SQLException {
        ShardingSpherePreconditions.checkNotEmpty(originSQL, () -> new EmptySQLException().toSQLException());
        HintValueContext hintValueContext = SQLHintUtils.extractHint(originSQL);
        String sql = SQLHintUtils.removeHint(originSQL);
        DatabaseType databaseType = metaData.getDatabase(usedDatabaseName).getProtocolType();
        SQLStatement sqlStatement = metaData.getGlobalRuleMetaData().getSingleRule(SQLParserRule.class).getSQLParserEngine(databaseType).parse(sql, false);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(metaData, connection.getCurrentDatabaseName(), hintValueContext).bind(sqlStatement);
        return new QueryContext(sqlStatementContext, sql, Collections.emptyList(), hintValueContext, connection.getDatabaseConnectionManager().getConnectionContext(), metaData);
    }
    
    private void prepareExecute(final QueryContext queryContext) throws SQLException {
        handleAutoCommitBeforeExecution(queryContext.getSqlStatementContext().getSqlStatement(), connection);
        this.queryContext = queryContext;
        ShardingSpherePreconditions.checkNotNull(this.queryContext, () -> new IllegalStateException("Query context can not be null"));
        usedDatabaseName = queryContext.getSqlStatementContext().getTablesContext().getDatabaseName().orElse(connection.getCurrentDatabaseName());
        connection.getDatabaseConnectionManager().getConnectionContext().setCurrentDatabaseName(connection.getCurrentDatabaseName());
        clearStatements();
    }
    
    private void clearStatements() throws SQLException {
        for (Statement each : statements) {
            each.close();
        }
        statements.clear();
    }
    
    private void replay() throws SQLException {
        for (Statement each : statements) {
            getMethodInvocationRecorder().replay(each);
        }
    }
    
    @Override
    public void addBatch(final String sql) {
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
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        if (null != currentResultSet) {
            return currentResultSet;
        }
        driverExecutorFacade.getResultSet(metaData.getDatabase(usedDatabaseName), queryContext, this, statements).ifPresent(optional -> currentResultSet = optional);
        return currentResultSet;
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
        for (DataNodeRuleAttribute each : metaData.getDatabase(usedDatabaseName).getRuleMetaData().getAttributes(DataNodeRuleAttribute.class)) {
            if (each.isNeedAccumulate(queryContext.getSqlStatementContext().getTablesContext().getTableNames())) {
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
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        return sqlStatementContext instanceof InsertStatementContext ? ((InsertStatementContext) sqlStatementContext).getGeneratedKeyContext() : Optional.empty();
    }
    
    private String getGeneratedKeysColumnName(final String columnName) {
        Optional<DialectGeneratedKeyOption> generatedKeyOption =
                new DatabaseTypeRegistry(metaData.getDatabase(usedDatabaseName).getProtocolType()).getDialectDatabaseMetaData().getGeneratedKeyOption();
        return generatedKeyOption.isPresent() ? generatedKeyOption.get().getColumnName() : columnName;
    }
    
    @Override
    protected void closeExecutor() throws SQLException {
        driverExecutorFacade.close();
    }
}
