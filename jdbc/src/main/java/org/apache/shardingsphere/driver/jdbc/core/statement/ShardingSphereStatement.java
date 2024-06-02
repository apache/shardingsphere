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
import org.apache.shardingsphere.infra.database.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.dialect.SQLExceptionTransformEngine;
import org.apache.shardingsphere.infra.exception.kernel.syntax.EmptySQLException;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
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

/**
 * ShardingSphere statement.
 */
@HighFrequencyInvocation
public final class ShardingSphereStatement extends AbstractStatementAdapter {
    
    @Getter
    private final ShardingSphereConnection connection;
    
    private final ShardingSphereMetaData metaData;
    
    private final List<Statement> statements;
    
    private final StatementOption statementOption;
    
    @Getter(AccessLevel.PROTECTED)
    private final DriverExecutor executor;
    
    @Getter(AccessLevel.PROTECTED)
    private final StatementManager statementManager;
    
    private final BatchStatementExecutor batchStatementExecutor;
    
    private String databaseName;
    
    private SQLStatementContext sqlStatementContext;
    
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
        statements = new LinkedList<>();
        statementOption = new StatementOption(resultSetType, resultSetConcurrency, resultSetHoldability);
        executor = new DriverExecutor(connection);
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
            ShardingSphereDatabase database = metaData.getDatabase(databaseName);
            sqlStatementContext = queryContext.getSqlStatementContext();
            clearStatements();
            currentResultSet = executor.executeQuery(database, queryContext, createDriverExecutionPrepareEngine(database), this, null,
                    (StatementReplayCallback<Statement>) (statements, parameterSets) -> replay(statements));
            statements.addAll(executor.getStatements());
            return currentResultSet;
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            currentResultSet = null;
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(databaseName).getProtocolType());
        }
    }
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine(final ShardingSphereDatabase database) {
        int maxConnectionsSizePerQuery = metaData.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new DriverExecutionPrepareEngine<>(JDBCDriverType.STATEMENT, maxConnectionsSizePerQuery, connection.getDatabaseConnectionManager(), statementManager, statementOption,
                database.getRuleMetaData().getRules(), database.getResourceMetaData().getStorageUnits());
    }
    
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        try {
            return executeUpdate(sql, (actualSQL, statement) -> statement.executeUpdate(actualSQL));
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(databaseName).getProtocolType());
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
            return executeUpdate(sql, (actualSQL, statement) -> statement.executeUpdate(actualSQL, autoGeneratedKeys));
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(databaseName).getProtocolType());
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        returnGeneratedKeys = true;
        try {
            return executeUpdate(sql, (actualSQL, statement) -> statement.executeUpdate(actualSQL, columnIndexes));
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(databaseName).getProtocolType());
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        returnGeneratedKeys = true;
        try {
            return executeUpdate(sql, (actualSQL, statement) -> statement.executeUpdate(actualSQL, columnNames));
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(databaseName).getProtocolType());
        } finally {
            currentResultSet = null;
        }
    }
    
    private int executeUpdate(final String sql, final ExecuteUpdateCallback updateCallback) throws SQLException {
        QueryContext queryContext = createQueryContext(sql);
        handleAutoCommit(queryContext.getSqlStatementContext().getSqlStatement());
        databaseName = queryContext.getDatabaseNameFromSQLStatement().orElse(connection.getDatabaseName());
        connection.getDatabaseConnectionManager().getConnectionContext().setCurrentDatabase(databaseName);
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        sqlStatementContext = queryContext.getSqlStatementContext();
        clearStatements();
        int result = executor.executeUpdate(database, queryContext, createDriverExecutionPrepareEngine(database), updateCallback,
                (StatementReplayCallback<Statement>) (statements, parameterSets) -> replay(statements));
        statements.addAll(executor.getStatements());
        return result;
    }
    
    @Override
    public boolean execute(final String sql) throws SQLException {
        try {
            return execute0(sql, (actualSQL, statement) -> statement.execute(actualSQL));
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(databaseName).getProtocolType());
        }
    }
    
    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        try {
            if (RETURN_GENERATED_KEYS == autoGeneratedKeys) {
                returnGeneratedKeys = true;
            }
            return execute0(sql, (actualSQL, statement) -> statement.execute(actualSQL, autoGeneratedKeys));
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(databaseName).getProtocolType());
        }
    }
    
    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        try {
            returnGeneratedKeys = true;
            return execute0(sql, (actualSQL, statement) -> statement.execute(actualSQL, columnIndexes));
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(databaseName).getProtocolType());
        }
    }
    
    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        try {
            returnGeneratedKeys = true;
            return execute0(sql, (actualSQL, statement) -> statement.execute(actualSQL, columnNames));
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaData);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaData.getDatabase(databaseName).getProtocolType());
        }
    }
    
    private boolean execute0(final String sql, final ExecuteCallback executeCallback) throws SQLException {
        currentResultSet = null;
        QueryContext queryContext = createQueryContext(sql);
        handleAutoCommit(queryContext.getSqlStatementContext().getSqlStatement());
        databaseName = queryContext.getDatabaseNameFromSQLStatement().orElse(connection.getDatabaseName());
        connection.getDatabaseConnectionManager().getConnectionContext().setCurrentDatabase(databaseName);
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        sqlStatementContext = queryContext.getSqlStatementContext();
        clearStatements();
        boolean result = executor.executeAdvance(database, queryContext, createDriverExecutionPrepareEngine(database),
                executeCallback, (StatementReplayCallback<Statement>) (statements, parameterSets) -> replay(statements));
        statements.addAll(executor.getStatements());
        return result;
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
        executor.clear();
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
        SQLParserRule sqlParserRule = metaData.getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        String sql = SQLHintUtils.removeHint(originSQL);
        HintValueContext hintValueContext = SQLHintUtils.extractHint(originSQL);
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(metaData.getDatabase(databaseName).getProtocolType()).parse(sql, false);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(metaData, databaseName, hintValueContext).bind(sqlStatement, Collections.emptyList());
        return new QueryContext(sqlStatementContext, sql, Collections.emptyList(), hintValueContext);
    }
    
    private void replay(final List<Statement> statements) throws SQLException {
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
        if (sqlStatementContext instanceof SelectStatementContext || sqlStatementContext.getSqlStatement() instanceof DALStatement) {
            List<ResultSet> resultSets = getResultSets();
            if (resultSets.isEmpty()) {
                return currentResultSet;
            }
            MergedResult mergedResult = mergeQuery(getQueryResults(resultSets), sqlStatementContext);
            boolean selectContainsEnhancedTable = sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isContainsEnhancedTable();
            currentResultSet = new ShardingSphereResultSet(resultSets, mergedResult, this, selectContainsEnhancedTable, sqlStatementContext);
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
        MergeEngine mergeEngine = new MergeEngine(metaData.getGlobalRuleMetaData(), metaData.getDatabase(databaseName),
                metaData.getProps(), connection.getDatabaseConnectionManager().getConnectionContext());
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
        for (DataNodeRuleAttribute each : metaData.getDatabase(databaseName).getRuleMetaData().getAttributes(DataNodeRuleAttribute.class)) {
            if (each.isNeedAccumulate(sqlStatementContext.getTablesContext().getTableNames())) {
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
        return sqlStatementContext instanceof InsertStatementContext ? ((InsertStatementContext) sqlStatementContext).getGeneratedKeyContext() : Optional.empty();
    }
    
    private String getGeneratedKeysColumnName(final String columnName) {
        return metaData.getDatabase(databaseName).getProtocolType() instanceof MySQLDatabaseType ? "GENERATED_KEY" : columnName;
    }
}
