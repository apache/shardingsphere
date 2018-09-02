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
import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.event.merger.MergeEvent;
import io.shardingsphere.core.event.routing.RoutingEvent;
import io.shardingsphere.core.executor.ShardingExecuteGroup;
import io.shardingsphere.core.executor.sql.SQLExecuteUnit;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteTemplate;
import io.shardingsphere.core.executor.sql.execute.result.MemoryQueryResult;
import io.shardingsphere.core.executor.sql.execute.result.StreamQueryResult;
import io.shardingsphere.core.executor.sql.prepare.SQLExecutePrepareCallback;
import io.shardingsphere.core.executor.sql.prepare.SQLExecutePrepareTemplate;
import io.shardingsphere.core.executor.statement.ConnectionStrictlyStatementExecutor;
import io.shardingsphere.core.executor.statement.MemoryStrictlyStatementExecutor;
import io.shardingsphere.core.executor.statement.StatementExecutor;
import io.shardingsphere.core.executor.statement.StatementExecuteUnit;
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
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.StatementRoutingEngine;
import io.shardingsphere.core.routing.router.sharding.GeneratedKey;
import lombok.AccessLevel;
import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
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
    
    private final int resultSetType;
    
    private final int resultSetConcurrency;
    
    private final int resultSetHoldability;
    
    private final Collection<Statement> routedStatements = new LinkedList<>();
    
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
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.resultSetHoldability = resultSetHoldability;
    }
    
    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        ResultSet result;
        try {
            clearPrevious();
            sqlRoute(sql);
            List<ResultSet> resultSets = getStatementExecutor().executeQuery();
            MergeEngine mergeEngine = MergeEngineFactory.newInstance(
                    connection.getShardingDataSource().getShardingContext().getShardingRule(), getQueryResults(resultSets), 
                    routeResult.getSqlStatement(), connection.getShardingDataSource().getShardingContext().getMetaData().getTable());
            result = new ShardingResultSet(resultSets, merge(mergeEngine), this);
        } finally {
            currentResultSet = null;
        }
        currentResultSet = result;
        return result;
    }
    
    private List<QueryResult> getQueryResults(final List<ResultSet> resultSets) throws SQLException {
        List<QueryResult> result = new ArrayList<>(resultSets.size());
        for (ResultSet each : resultSets) {
            if (ConnectionMode.MEMORY_STRICTLY == connection.getShardingDataSource().getShardingContext().getConnectionMode()) {
                result.add(new StreamQueryResult(each));
            } else {
                result.add(new MemoryQueryResult(each));
            }
        }
        return result;
    }
    
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        try {
            clearPrevious();
            sqlRoute(sql);
            return getStatementExecutor().executeUpdate();
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
            return getStatementExecutor().executeUpdate(autoGeneratedKeys);
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
            return getStatementExecutor().executeUpdate(columnIndexes);
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
            return getStatementExecutor().executeUpdate(columnNames);
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public boolean execute(final String sql) throws SQLException {
        try {
            clearPrevious();
            sqlRoute(sql);
            return getStatementExecutor().execute();
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
            return getStatementExecutor().execute(autoGeneratedKeys);
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
            return getStatementExecutor().execute(columnIndexes);
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
            return getStatementExecutor().execute(columnNames);
        } finally {
            currentResultSet = null;
        }
    }
    
    private StatementExecutor getStatementExecutor() throws SQLException {
        SQLExecuteTemplate sqlExecuteTemplate = new SQLExecuteTemplate(connection.getShardingDataSource().getShardingContext().getExecuteEngine());
        if (ConnectionMode.MEMORY_STRICTLY == connection.getShardingDataSource().getShardingContext().getConnectionMode()) {
            return new MemoryStrictlyStatementExecutor(routeResult.getSqlStatement().getType(), sqlExecuteTemplate, getExecuteUnitsForMemoryStrictly());
        }
        return new ConnectionStrictlyStatementExecutor(routeResult.getSqlStatement().getType(), sqlExecuteTemplate, getExecuteUnitsForConnectionStrictly());
    }
    
    private Collection<StatementExecuteUnit> getExecuteUnitsForMemoryStrictly() throws SQLException {
        Collection<StatementExecuteUnit> result = new LinkedList<>();
        for (RouteUnit each : routeResult.getRouteUnits()) {
            result.add(getStatementExecuteUnit(connection.getConnection(each.getDataSourceName()), each));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Collection<ShardingExecuteGroup<StatementExecuteUnit>> getExecuteUnitsForConnectionStrictly() throws SQLException {
        SQLExecutePrepareTemplate sqlExecutePrepareTemplate = new SQLExecutePrepareTemplate(connection.getShardingDataSource().getShardingContext().getMaxConnectionsSizePerQuery());
        return (Collection) sqlExecutePrepareTemplate.getExecuteUnitGroups(routeResult.getRouteUnits(), new SQLExecutePrepareCallback() {
            
            @Override
            public Connection getConnection(final String dataSourceName) throws SQLException {
                return ShardingStatement.this.connection.getConnection(dataSourceName);
            }
            
            @Override
            public SQLExecuteUnit createSQLExecuteUnit(final Connection connection, final RouteUnit routeUnit) throws SQLException {
                return getStatementExecuteUnit(connection, routeUnit);
            }
        });
    }
    
    private StatementExecuteUnit getStatementExecuteUnit(final Connection connection, final RouteUnit routeUnit) throws SQLException {
        Statement statement = connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        routedStatements.add(statement);
        replayMethodsInvocation(statement);
        return new StatementExecuteUnit(routeUnit, statement);
    }
    
    private void clearPrevious() throws SQLException {
        for (Statement each : routedStatements) {
            each.close();
        }
        routedStatements.clear();
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
        if (1 == routedStatements.size() && routeResult.getSqlStatement() instanceof DQLStatement) {
            currentResultSet = routedStatements.iterator().next().getResultSet();
            return currentResultSet;
        }
        List<ResultSet> resultSets = new ArrayList<>(routedStatements.size());
        List<QueryResult> queryResults = new ArrayList<>(routedStatements.size());
        for (Statement each : routedStatements) {
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
}
