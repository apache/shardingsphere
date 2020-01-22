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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.statement;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.sharding.route.engine.keygen.GeneratedKey;
import org.apache.shardingsphere.core.shard.BaseShardingEngine;
import org.apache.shardingsphere.core.shard.SimpleQueryShardingEngine;
import org.apache.shardingsphere.sharding.execute.context.ShardingExecutionContext;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.sharding.execute.sql.execute.result.StreamQueryResult;
import org.apache.shardingsphere.sharding.merge.ShardingResultMergerEngine;
import org.apache.shardingsphere.shardingjdbc.executor.StatementExecutor;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractStatementAdapter;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.constant.SQLExceptionConstant;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShardingRuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.GeneratedKeysResultSet;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.resultset.ShardingResultSet;
import org.apache.shardingsphere.shardingjdbc.merge.JDBCEncryptResultDecoratorEngine;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.executor.QueryResult;
import org.apache.shardingsphere.underlying.merge.MergeEntry;
import org.apache.shardingsphere.underlying.merge.engine.ResultProcessEngine;
import org.apache.shardingsphere.underlying.merge.result.MergedResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Statement that support sharding.
 *
 * @author gaohongtao
 * @author caohao
 * @author zhangliang
 * @author zhaojun
 * @author panjuan
 */
public final class ShardingStatement extends AbstractStatementAdapter {
    
    @Getter
    private final ShardingConnection connection;
    
    private final StatementExecutor statementExecutor;
    
    private boolean returnGeneratedKeys;
    
    private ShardingExecutionContext shardingExecutionContext;
    
    private ResultSet currentResultSet;
    
    public ShardingStatement(final ShardingConnection connection) {
        this(connection, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }
    
    public ShardingStatement(final ShardingConnection connection, final int resultSetType, final int resultSetConcurrency) {
        this(connection, resultSetType, resultSetConcurrency, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }
    
    public ShardingStatement(final ShardingConnection connection, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) {
        super(Statement.class);
        this.connection = connection;
        statementExecutor = new StatementExecutor(resultSetType, resultSetConcurrency, resultSetHoldability, connection);
    }
    
    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        if (Strings.isNullOrEmpty(sql)) {
            throw new SQLException(SQLExceptionConstant.SQL_STRING_NULL_OR_EMPTY);
        }
        ResultSet result;
        try {
            clearPrevious();
            shard(sql);
            initStatementExecutor();
            result = getResultSet(statementExecutor.executeQuery());
        } finally {
            currentResultSet = null;
        }
        currentResultSet = result;
        return result;
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        if (null != currentResultSet) {
            return currentResultSet;
        }
        List<ResultSet> resultSets = new ArrayList<>(statementExecutor.getStatements().size());
        List<QueryResult> queryResults = new ArrayList<>(statementExecutor.getStatements().size());
        for (Statement each : statementExecutor.getStatements()) {
            ResultSet resultSet = each.getResultSet();
            resultSets.add(resultSet);
            if (resultSet != null) {
                queryResults.add(new StreamQueryResult(resultSet));
            }
        }
        if (shardingExecutionContext.getSqlStatementContext() instanceof SelectSQLStatementContext || shardingExecutionContext.getSqlStatementContext().getSqlStatement() instanceof DALStatement) {
            currentResultSet = new ShardingResultSet(resultSets, createMergedResult(resultSets, queryResults), this, shardingExecutionContext);
        }
        return currentResultSet;
    }
    
    private ShardingResultSet getResultSet(final List<QueryResult> queryResults) throws SQLException {
        List<ResultSet> resultSets = statementExecutor.getResultSets();
        return new ShardingResultSet(resultSets, createMergedResult(resultSets, queryResults), this, shardingExecutionContext);
    }
    
    private MergedResult createMergedResult(final List<ResultSet> resultSets, final List<QueryResult> queryResults) throws SQLException {
        Map<BaseRule, ResultProcessEngine> engines = new HashMap<>(2, 1);
        engines.put(connection.getRuntimeContext().getRule(), new ShardingResultMergerEngine());
        EncryptRule encryptRule = connection.getRuntimeContext().getRule().getEncryptRule();
        if (!encryptRule.getEncryptTableNames().isEmpty()) {
            engines.put(encryptRule, new JDBCEncryptResultDecoratorEngine(resultSets.get(0).getMetaData()));
        }
        MergeEntry mergeEntry = new MergeEntry(connection.getRuntimeContext().getDatabaseType(), 
                connection.getRuntimeContext().getMetaData().getRelationMetas(), connection.getRuntimeContext().getProperties(), engines);
        return mergeEntry.process(queryResults, shardingExecutionContext.getSqlStatementContext());
    }
    
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        try {
            clearPrevious();
            shard(sql);
            initStatementExecutor();
            return statementExecutor.executeUpdate();
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
            clearPrevious();
            shard(sql);
            initStatementExecutor();
            return statementExecutor.executeUpdate(autoGeneratedKeys);
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        returnGeneratedKeys = true;
        try {
            clearPrevious();
            shard(sql);
            initStatementExecutor();
            return statementExecutor.executeUpdate(columnIndexes);
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        returnGeneratedKeys = true;
        try {
            clearPrevious();
            shard(sql);
            initStatementExecutor();
            return statementExecutor.executeUpdate(columnNames);
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public boolean execute(final String sql) throws SQLException {
        try {
            clearPrevious();
            shard(sql);
            initStatementExecutor();
            return statementExecutor.execute();
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
            clearPrevious();
            shard(sql);
            initStatementExecutor();
            return statementExecutor.execute(autoGeneratedKeys);
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        returnGeneratedKeys = true;
        try {
            clearPrevious();
            shard(sql);
            initStatementExecutor();
            return statementExecutor.execute(columnIndexes);
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        returnGeneratedKeys = true;
        try {
            clearPrevious();
            shard(sql);
            initStatementExecutor();
            return statementExecutor.execute(columnNames);
        } finally {
            currentResultSet = null;
        }
    }
    
    private void initStatementExecutor() throws SQLException {
        statementExecutor.init(shardingExecutionContext);
        replayMethodForStatements();
    }
    
    private void replayMethodForStatements() {
        for (Statement each : statementExecutor.getStatements()) {
            replayMethodsInvocation(each);
        }
    }
    
    private void shard(final String sql) {
        ShardingRuntimeContext runtimeContext = connection.getRuntimeContext();
        BaseShardingEngine shardingEngine = new SimpleQueryShardingEngine(runtimeContext.getRule(), runtimeContext.getProperties(), runtimeContext.getMetaData(), runtimeContext.getParseEngine());
        shardingExecutionContext = (ShardingExecutionContext) shardingEngine.shard(sql, Collections.emptyList());
    }
    
    private void clearPrevious() throws SQLException {
        statementExecutor.clear();
    }
    
    @SuppressWarnings("MagicConstant")
    @Override
    public int getResultSetType() {
        return statementExecutor.getResultSetType();
    }
    
    @SuppressWarnings("MagicConstant")
    @Override
    public int getResultSetConcurrency() {
        return statementExecutor.getResultSetConcurrency();
    }
    
    @Override
    public int getResultSetHoldability() {
        return statementExecutor.getResultSetHoldability();
    }
    
    @Override
    public boolean isAccumulate() {
        return !connection.getRuntimeContext().getRule().isAllBroadcastTables(shardingExecutionContext.getSqlStatementContext().getTablesContext().getTableNames());
    }
    
    @Override
    public Collection<Statement> getRoutedStatements() {
        return statementExecutor.getStatements();
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        Optional<GeneratedKey> generatedKey = getGeneratedKey();
        if (returnGeneratedKeys && generatedKey.isPresent()) {
            Preconditions.checkState(shardingExecutionContext.getGeneratedKey().isPresent());
            return new GeneratedKeysResultSet(shardingExecutionContext.getGeneratedKey().get().getGeneratedValues().iterator(), generatedKey.get().getColumnName(), this);
        }
        if (1 == getRoutedStatements().size()) {
            return getRoutedStatements().iterator().next().getGeneratedKeys();
        }
        return new GeneratedKeysResultSet();
    }
    
    private Optional<GeneratedKey> getGeneratedKey() {
        return null != shardingExecutionContext && shardingExecutionContext.getSqlStatementContext().getSqlStatement() instanceof InsertStatement
                ? shardingExecutionContext.getGeneratedKey() : Optional.<GeneratedKey>absent();
    }
}
