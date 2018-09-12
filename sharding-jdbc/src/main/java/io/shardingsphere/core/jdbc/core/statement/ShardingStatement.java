/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.jdbc.core.statement;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.event.merger.MergeEvent;
import io.shardingsphere.core.event.routing.RoutingEvent;
import io.shardingsphere.core.executor.sql.execute.result.StreamQueryResult;
import io.shardingsphere.core.executor.statement.StatementExecutor;
import io.shardingsphere.core.jdbc.adapter.AbstractStatementAdapter;
import io.shardingsphere.core.jdbc.core.ShardingContext;
import io.shardingsphere.core.jdbc.core.connection.ShardingConnection;
import io.shardingsphere.core.jdbc.core.resultset.GeneratedKeysResultSet;
import io.shardingsphere.core.jdbc.core.resultset.ShardingResultSet;
import io.shardingsphere.core.jdbc.metadata.JDBCTableMetaDataConnectionManager;
import io.shardingsphere.core.merger.MergeEngine;
import io.shardingsphere.core.merger.MergeEngineFactory;
import io.shardingsphere.core.merger.MergedResult;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.metadata.table.executor.TableMetaDataLoader;
import io.shardingsphere.core.parsing.parser.sql.dal.DALStatement;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.DQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.StatementRoutingEngine;
import io.shardingsphere.core.routing.router.sharding.GeneratedKey;
import lombok.AccessLevel;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Statement that support sharding.
 * 
 * @author gaohongtao
 * @author caohao
 * @author zhangliang
 * @author zhaojun
 * @author panjuan
 */
@Getter
public final class ShardingStatement extends AbstractStatementAdapter {
    
    private final ShardingConnection connection;
    
    private StatementExecutor statementExecutor;
    
    @Getter(AccessLevel.NONE)
    private boolean returnGeneratedKeys;
    
    @Getter(AccessLevel.NONE)
    private SQLRouteResult routeResult;
    
    @Getter(AccessLevel.NONE)
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
        this.statementExecutor = new StatementExecutor(resultSetType, resultSetConcurrency, resultSetHoldability, connection);
    }
    
    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        ResultSet result;
        try {
            clearPrevious();
            sqlRoute(sql);
            initStatementExecutor();
            MergeEngine mergeEngine = MergeEngineFactory.newInstance(
                    connection.getShardingDataSource().getShardingContext().getShardingRule(), statementExecutor.executeQuery(),
                    routeResult.getSqlStatement(), connection.getShardingDataSource().getShardingContext().getMetaData().getTable());
            result = new ShardingResultSet(statementExecutor.getResultSets(), merge(mergeEngine), this);
        } finally {
            currentResultSet = null;
        }
        currentResultSet = result;
        return result;
    }
    
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        try {
            clearPrevious();
            sqlRoute(sql);
            initStatementExecutor();
            return statementExecutor.executeUpdate();
        } finally {
            refreshTableMetaData();
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
            sqlRoute(sql);
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
            sqlRoute(sql);
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
            sqlRoute(sql);
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
            sqlRoute(sql);
            initStatementExecutor();
            return statementExecutor.execute();
        } finally {
            refreshTableMetaData();
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
            sqlRoute(sql);
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
            sqlRoute(sql);
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
            sqlRoute(sql);
            initStatementExecutor();
            return statementExecutor.execute(columnNames);
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        Optional<GeneratedKey> generatedKey = getGeneratedKey();
        if (returnGeneratedKeys && generatedKey.isPresent()) {
            return new GeneratedKeysResultSet(routeResult.getGeneratedKey().getGeneratedKeys().iterator(), generatedKey.get().getColumn().getName(), this);
        }
        if (1 == getRoutedStatements().size()) {
            return getRoutedStatements().iterator().next().getGeneratedKeys();
        }
        return new GeneratedKeysResultSet();
    }
    
    private Optional<GeneratedKey> getGeneratedKey() {
        if (null != routeResult && routeResult.getSqlStatement() instanceof InsertStatement) {
            return Optional.fromNullable(routeResult.getGeneratedKey());
        }
        return Optional.absent();
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        if (null != currentResultSet) {
            return currentResultSet;
        }
        if (1 == statementExecutor.getStatements().size() && routeResult.getSqlStatement() instanceof DQLStatement) {
            currentResultSet = statementExecutor.getStatements().iterator().next().getResultSet();
            return currentResultSet;
        }
        List<ResultSet> resultSets = new ArrayList<>(statementExecutor.getStatements().size());
        List<QueryResult> queryResults = new ArrayList<>(statementExecutor.getStatements().size());
        for (Statement each : statementExecutor.getStatements()) {
            ResultSet resultSet = each.getResultSet();
            resultSets.add(resultSet);
            queryResults.add(new StreamQueryResult(resultSet));
        }
        if (routeResult.getSqlStatement() instanceof SelectStatement || routeResult.getSqlStatement() instanceof DALStatement) {
            MergeEngine mergeEngine = MergeEngineFactory.newInstance(
                    connection.getShardingDataSource().getShardingContext().getShardingRule(), queryResults, routeResult.getSqlStatement(), 
                    connection.getShardingDataSource().getShardingContext().getMetaData().getTable());
            currentResultSet = new ShardingResultSet(resultSets, merge(mergeEngine), this);
        }
        return currentResultSet;
    }
    
    private void initStatementExecutor() throws SQLException {
        statementExecutor.init(routeResult);
        replayMethodForStatements();
    }
    
    private void replayMethodForStatements() {
        for (Statement each : statementExecutor.getStatements()) {
            replayMethodsInvocation(each);
        }
    }
    
    private void sqlRoute(final String sql) {
        ShardingContext shardingContext = connection.getShardingDataSource().getShardingContext();
        RoutingEvent event = new RoutingEvent(sql);
        ShardingEventBusInstance.getInstance().post(event);
        try {
            routeResult = new StatementRoutingEngine(shardingContext.getShardingRule(),
                    shardingContext.getMetaData().getTable(), shardingContext.getDatabaseType(), shardingContext.isShowSQL(), shardingContext.getMetaData().getDataSource()).route(sql);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            event.setExecuteFailure(ex);
            ShardingEventBusInstance.getInstance().post(event);
            throw ex;
        }
        event.setExecuteSuccess();
        ShardingEventBusInstance.getInstance().post(event);
    }
    
    // TODO refresh table meta data by SQL parse result
    private void refreshTableMetaData() throws SQLException {
        if (null != routeResult && null != connection && SQLType.DDL == routeResult.getSqlStatement().getType() && !routeResult.getSqlStatement().getTables().isEmpty()) {
            String logicTableName = routeResult.getSqlStatement().getTables().getSingleTableName();
            TableMetaDataLoader tableMetaDataLoader = new TableMetaDataLoader(connection.getShardingDataSource().getShardingContext().getMetaData().getDataSource(),
                    connection.getShardingDataSource().getShardingContext().getExecuteEngine(), new JDBCTableMetaDataConnectionManager(connection.getShardingDataSource().getDataSourceMap()),
                    connection.getShardingDataSource().getShardingContext().getMaxConnectionsSizePerQuery());
            connection.getShardingDataSource().getShardingContext().getMetaData().getTable().put(
                    logicTableName, tableMetaDataLoader.load(logicTableName, connection.getShardingDataSource().getShardingContext().getShardingRule()));
        }
    }
    
    private MergedResult merge(final MergeEngine mergeEngine) throws SQLException {
        MergeEvent event = new MergeEvent();
        try {
            ShardingEventBusInstance.getInstance().post(event);
            MergedResult result = mergeEngine.merge();
            event.setExecuteSuccess();
            ShardingEventBusInstance.getInstance().post(event);
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            event.setExecuteFailure(ex);
            ShardingEventBusInstance.getInstance().post(event);
            throw ex;
        }
    }
    
    private void clearPrevious() throws SQLException {
        statementExecutor.clear();
    }
    
    @Override
    public int getResultSetType() {
        return statementExecutor.getResultSetType();
    }
    
    @Override
    public int getResultSetConcurrency() {
        return statementExecutor.getResultSetConcurrency();
    }
    
    @Override
    public int getResultSetHoldability() {
        return statementExecutor.getResultSetHoldability();
    }
    
    @Override
    public Collection<Statement> getRoutedStatements() {
        return statementExecutor.getStatements();
    }
}

