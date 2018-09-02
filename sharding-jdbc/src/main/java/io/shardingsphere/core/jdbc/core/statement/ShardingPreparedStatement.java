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
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.event.merger.MergeEvent;
import io.shardingsphere.core.event.routing.RoutingEvent;
import io.shardingsphere.core.executor.ShardingExecuteGroup;
import io.shardingsphere.core.executor.batch.BatchPreparedStatementExecuteUnit;
import io.shardingsphere.core.executor.batch.ConnectionStrictlyBatchPreparedStatementExecutor;
import io.shardingsphere.core.executor.batch.MemoryStrictlyBatchPreparedStatementExecutor;
import io.shardingsphere.core.executor.prepared.ConnectionStrictlyPreparedStatementExecutor;
import io.shardingsphere.core.executor.prepared.MemoryStrictlyPreparedStatementExecutor;
import io.shardingsphere.core.executor.prepared.PreparedStatementExecutor;
import io.shardingsphere.core.executor.prepared.PreparedStatementExecuteUnit;
import io.shardingsphere.core.executor.sql.SQLExecuteUnit;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteTemplate;
import io.shardingsphere.core.executor.sql.execute.result.MemoryQueryResult;
import io.shardingsphere.core.executor.sql.execute.result.StreamQueryResult;
import io.shardingsphere.core.executor.sql.prepare.SQLExecutePrepareCallback;
import io.shardingsphere.core.executor.sql.prepare.SQLExecutePrepareTemplate;
import io.shardingsphere.core.jdbc.adapter.AbstractShardingPreparedStatementAdapter;
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
import io.shardingsphere.core.routing.PreparedStatementRoutingEngine;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.router.sharding.GeneratedKey;
import lombok.AccessLevel;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * PreparedStatement that support sharding.
 * 
 * @author zhangliang
 * @author caohao
 * @author maxiaoguang
 * @author panjuan
 */
@Getter
public final class ShardingPreparedStatement extends AbstractShardingPreparedStatementAdapter {
    
    private final ShardingConnection connection;
    
    private final int resultSetType;
    
    private final int resultSetConcurrency;
    
    private final int resultSetHoldability;
    
    private final PreparedStatementRoutingEngine routingEngine;
    
    private final List<BatchPreparedStatementExecuteUnit> batchStatementUnits = new LinkedList<>();
    
    private final Collection<PreparedStatement> routedStatements = new LinkedList<>();

    private final String sql;

    private int batchCount;
    
    @Getter(AccessLevel.NONE)
    private boolean returnGeneratedKeys;
    
    @Getter(AccessLevel.NONE)
    private SQLRouteResult routeResult;
    
    @Getter(AccessLevel.NONE)
    private ResultSet currentResultSet;
    
    public ShardingPreparedStatement(final ShardingConnection connection, final String sql) {
        this(connection, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }
    
    public ShardingPreparedStatement(final ShardingConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency) {
        this(connection, sql, resultSetType, resultSetConcurrency, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }
    
    public ShardingPreparedStatement(final ShardingConnection connection, final String sql, final int autoGeneratedKeys) {
        this(connection, sql);
        if (Statement.RETURN_GENERATED_KEYS == autoGeneratedKeys) {
            returnGeneratedKeys = true;
        }
    }
    
    public ShardingPreparedStatement(final ShardingConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) {
        this.connection = connection;
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.resultSetHoldability = resultSetHoldability;
        this.sql = sql;
        ShardingContext shardingContext = connection.getShardingDataSource().getShardingContext();
        routingEngine = new PreparedStatementRoutingEngine(sql, shardingContext.getShardingRule(), 
                shardingContext.getMetaData().getTable(), shardingContext.getDatabaseType(), shardingContext.isShowSQL(), shardingContext.getMetaData().getDataSource());
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        routedStatements.clear();
        ResultSet result;
        try {
            sqlRoute();
            List<ResultSet> resultSets = getPreparedStatementExecutor().executeQuery();
            MergeEngine mergeEngine = MergeEngineFactory.newInstance(
                    connection.getShardingDataSource().getShardingContext().getShardingRule(), getQueryResults(resultSets), routeResult.getSqlStatement(), 
                    connection.getShardingDataSource().getShardingContext().getMetaData().getTable());
            result = new ShardingResultSet(resultSets, merge(mergeEngine), this);
        } finally {
            clearBatch();
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
    public int executeUpdate() throws SQLException {
        routedStatements.clear();
        try {
            sqlRoute();
            return getPreparedStatementExecutor().executeUpdate();
        } finally {
            refreshTableMetaData();
            clearBatch();
        }
    }
    
    @Override
    public boolean execute() throws SQLException {
        routedStatements.clear();
        try {
            sqlRoute();
            return getPreparedStatementExecutor().execute();
        } finally {
            refreshTableMetaData();
            clearBatch();
        }
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
    public void clearBatch() {
        currentResultSet = null;
        clearParameters();
        batchStatementUnits.clear();
        batchCount = 0;
    }
    
    @Override
    public void addBatch() throws SQLException {
        try {
            for (BatchPreparedStatementExecuteUnit each : routeBatch()) {
                each.getStatement().addBatch();
                each.mapAddBatchCount(batchCount);
            }
            batchCount++;
        } finally {
            currentResultSet = null;
            clearParameters();
        }
    }
    
    private List<BatchPreparedStatementExecuteUnit> routeBatch() throws SQLException {
        List<BatchPreparedStatementExecuteUnit> result = new ArrayList<>();
        sqlRoute();
        for (RouteUnit each : routeResult.getRouteUnits()) {
            BatchPreparedStatementExecuteUnit batchStatementUnit = getBatchPreparedStatementExecuteUnit(each);
            replaySetParameter(batchStatementUnit.getStatement(), each.getSqlUnit().getParameterSets().get(0));
            result.add(batchStatementUnit);
        }
        return result;
    }
    
    private void sqlRoute() {
        RoutingEvent event = new RoutingEvent(sql);
        ShardingEventBusInstance.getInstance().post(event);
        try {
            routeResult = routingEngine.route(getParameters());
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
    
    private PreparedStatementExecutor getPreparedStatementExecutor() throws SQLException {
        SQLExecuteTemplate sqlExecuteTemplate = new SQLExecuteTemplate(connection.getShardingDataSource().getShardingContext().getExecuteEngine());
        if (ConnectionMode.MEMORY_STRICTLY == connection.getShardingDataSource().getShardingContext().getConnectionMode()) {
            return new MemoryStrictlyPreparedStatementExecutor(routeResult.getSqlStatement().getType(), sqlExecuteTemplate, getExecuteUnitsForMemoryStrictly());
        }
        return new ConnectionStrictlyPreparedStatementExecutor(routeResult.getSqlStatement().getType(), sqlExecuteTemplate, getExecuteUnitsForConnectionStrictly());
    }
    
    private Collection<PreparedStatementExecuteUnit> getExecuteUnitsForMemoryStrictly() throws SQLException {
        Collection<PreparedStatementExecuteUnit> result = new LinkedList<>();
        for (RouteUnit each : routeResult.getRouteUnits()) {
            result.add(getPreparedStatementExecuteUnit(connection.getConnection(each.getDataSourceName()), each));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Collection<ShardingExecuteGroup<PreparedStatementExecuteUnit>> getExecuteUnitsForConnectionStrictly() throws SQLException {
        SQLExecutePrepareTemplate sqlExecutePrepareTemplate = new SQLExecutePrepareTemplate(connection.getShardingDataSource().getShardingContext().getMaxConnectionsSizePerQuery());
        return (Collection) sqlExecutePrepareTemplate.getExecuteUnitGroups(routeResult.getRouteUnits(), new SQLExecutePrepareCallback() {
            
            @Override
            public Connection getConnection(final String dataSourceName) throws SQLException {
                return ShardingPreparedStatement.this.connection.getConnection(dataSourceName);
            }
            
            @Override
            public SQLExecuteUnit createSQLExecuteUnit(final Connection connection, final RouteUnit routeUnit) throws SQLException {
                return getPreparedStatementExecuteUnit(connection, routeUnit);
            }
        });
    }
    
    private PreparedStatementExecuteUnit getPreparedStatementExecuteUnit(final Connection connection, final RouteUnit routeUnit) throws SQLException {
        PreparedStatement preparedStatement = createPreparedStatement(connection, routeUnit.getSqlUnit().getSql());
        routedStatements.add(preparedStatement);
        replaySetParameter(preparedStatement, routeUnit.getSqlUnit().getParameterSets().get(0));
        return new PreparedStatementExecuteUnit(routeUnit, preparedStatement);
    }
    
    private BatchPreparedStatementExecuteUnit getBatchPreparedStatementExecuteUnit(final RouteUnit routeUnit) throws SQLException {
        Optional<BatchPreparedStatementExecuteUnit> preparedBatchStatement = Iterators.tryFind(batchStatementUnits.iterator(), new Predicate<BatchPreparedStatementExecuteUnit>() {
            
            @Override
            public boolean apply(final BatchPreparedStatementExecuteUnit input) {
                return Objects.equals(input.getRouteUnit(), routeUnit);
            }
        });
        if (preparedBatchStatement.isPresent()) {
            preparedBatchStatement.get().getRouteUnit().getSqlUnit().getParameterSets().add(routeUnit.getSqlUnit().getParameterSets().get(0));
            return preparedBatchStatement.get();
        }
        BatchPreparedStatementExecuteUnit result = new BatchPreparedStatementExecuteUnit(
                routeUnit, createPreparedStatement(connection.getConnection(routeUnit.getDataSourceName()), routeUnit.getSqlUnit().getSql()));
        batchStatementUnits.add(result);
        return result;
    }
    
    private PreparedStatement createPreparedStatement(final Connection connection, final String sql) throws SQLException {
        return returnGeneratedKeys ? connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        try {
            SQLExecuteTemplate sqlExecuteTemplate = new SQLExecuteTemplate(connection.getShardingDataSource().getShardingContext().getExecuteEngine());
            if (ConnectionMode.MEMORY_STRICTLY == connection.getShardingDataSource().getShardingContext().getConnectionMode()) {
                return new MemoryStrictlyBatchPreparedStatementExecutor(connection.getShardingDataSource().getShardingContext().getDatabaseType(), 
                        routeResult.getSqlStatement().getType(), batchCount, sqlExecuteTemplate, batchStatementUnits).executeBatch();
            }
            return new ConnectionStrictlyBatchPreparedStatementExecutor(connection.getShardingDataSource().getShardingContext().getDatabaseType(), 
                    routeResult.getSqlStatement().getType(), batchCount, sqlExecuteTemplate, partitionBatchPreparedStatementUnitGroups()).executeBatch();
        } finally {
            clearBatch();
        }
    }
    
    private List<List<BatchPreparedStatementExecuteUnit>> partitionBatchPreparedStatementUnitGroups() {
        List<List<BatchPreparedStatementExecuteUnit>> result = new LinkedList<>();
        for (List<BatchPreparedStatementExecuteUnit> each : getBatchPreparedStatementUnitGroups().values()) {
            int desiredPartitionSize = Math.max(each.size() / connection.getShardingDataSource().getShardingContext().getMaxConnectionsSizePerQuery(), 1);
            result.addAll(Lists.partition(each, desiredPartitionSize));
        }
        return result;
    }
    
    private Map<String, List<BatchPreparedStatementExecuteUnit>> getBatchPreparedStatementUnitGroups() {
        Map<String, List<BatchPreparedStatementExecuteUnit>> result = new HashMap<>(batchStatementUnits.size(), 1);
        for (BatchPreparedStatementExecuteUnit each : batchStatementUnits) {
            String dataSourceName = each.getRouteUnit().getDataSourceName();
            if (!result.containsKey(dataSourceName)) {
                result.put(dataSourceName, new LinkedList<BatchPreparedStatementExecuteUnit>());
            }
            result.get(dataSourceName).add(each);
        } 
        return result;
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        Optional<GeneratedKey> generatedKey = getGeneratedKey();
        if (returnGeneratedKeys && generatedKey.isPresent()) {
            return new GeneratedKeysResultSet(routeResult.getGeneratedKey().getGeneratedKeys().iterator(), generatedKey.get().getColumn().getName(), this);
        }
        if (1 == routedStatements.size()) {
            return routedStatements.iterator().next().getGeneratedKeys();
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
        for (PreparedStatement each : routedStatements) {
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
