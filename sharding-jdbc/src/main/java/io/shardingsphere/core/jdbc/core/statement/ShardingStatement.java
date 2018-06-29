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
import io.shardingsphere.core.executor.type.statement.StatementExecutor;
import io.shardingsphere.core.executor.type.statement.StatementUnit;
import io.shardingsphere.core.jdbc.adapter.AbstractStatementAdapter;
import io.shardingsphere.core.jdbc.core.ShardingContext;
import io.shardingsphere.core.jdbc.core.connection.ShardingConnection;
import io.shardingsphere.core.jdbc.core.resultset.GeneratedKeysResultSet;
import io.shardingsphere.core.jdbc.core.resultset.ShardingResultSet;
import io.shardingsphere.core.jdbc.metadata.dialect.JDBCShardingRefreshHandler;
import io.shardingsphere.core.merger.JDBCQueryResult;
import io.shardingsphere.core.merger.MergeEngine;
import io.shardingsphere.core.merger.MergeEngineFactory;
import io.shardingsphere.core.merger.MergedResult;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.merger.event.EventMergeType;
import io.shardingsphere.core.merger.event.ResultSetMergeEvent;
import io.shardingsphere.core.parsing.parser.sql.dal.DALStatement;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.DQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.routing.SQLExecutionUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.StatementRoutingEngine;
import io.shardingsphere.core.routing.event.EventRoutingType;
import io.shardingsphere.core.routing.event.SqlRoutingEvent;
import io.shardingsphere.core.routing.router.sharding.GeneratedKey;
import io.shardingsphere.core.util.EventBusInstance;
import lombok.AccessLevel;
import lombok.Getter;

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
 */
@Getter
public class ShardingStatement extends AbstractStatementAdapter {
    
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
            List<ResultSet> resultSets = generateExecutor(sql).executeQuery();
            List<QueryResult> queryResults = new ArrayList<>(resultSets.size());
            for (ResultSet each : resultSets) {
                queryResults.add(new JDBCQueryResult(each));
            }
            MergeEngine mergeEngine = MergeEngineFactory.newInstance(connection.getShardingContext().getShardingRule(), queryResults,
                    routeResult.getSqlStatement(), connection.getShardingContext().getShardingMetaData());
            result = new ShardingResultSet(resultSets, merge(mergeEngine), this);
        } finally {
            currentResultSet = null;
        }
        currentResultSet = result;
        return result;
    }
    
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        try {
            return generateExecutor(sql).executeUpdate();
        } finally {
            if (routeResult != null && connection != null) {
                JDBCShardingRefreshHandler.build(routeResult, connection).execute();
            }
            currentResultSet = null;
        }
    }

    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        if (RETURN_GENERATED_KEYS == autoGeneratedKeys) {
            returnGeneratedKeys = true;
        }
        try {
            return generateExecutor(sql).executeUpdate(autoGeneratedKeys);
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        returnGeneratedKeys = true;
        try {
            return generateExecutor(sql).executeUpdate(columnIndexes);
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        returnGeneratedKeys = true;
        try {
            return generateExecutor(sql).executeUpdate(columnNames);
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public boolean execute(final String sql) throws SQLException {
        try {
            return generateExecutor(sql).execute();
        } finally {
            if (routeResult != null && connection != null) {
                JDBCShardingRefreshHandler.build(routeResult, connection).execute();
            }
            currentResultSet = null;
        }
    }

    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        if (RETURN_GENERATED_KEYS == autoGeneratedKeys) {
            returnGeneratedKeys = true;
        }
        try {
            return generateExecutor(sql).execute(autoGeneratedKeys);
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        returnGeneratedKeys = true;
        try {
            return generateExecutor(sql).execute(columnIndexes);
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        returnGeneratedKeys = true;
        try {
            return generateExecutor(sql).execute(columnNames);
        } finally {
            currentResultSet = null;
        }
    }
    
    private StatementExecutor generateExecutor(final String sql) throws SQLException {
        clearPrevious();
        sqlRoute(sql);
        Collection<StatementUnit> statementUnits = new LinkedList<>();
        for (SQLExecutionUnit each : routeResult.getExecutionUnits()) {
            Statement statement = connection.getConnection(each.getDataSource()).createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
            replayMethodsInvocation(statement);
            statementUnits.add(new StatementUnit(each, statement));
            routedStatements.add(statement);
        }
        return new StatementExecutor(connection.getShardingContext().getExecutorEngine(), routeResult.getSqlStatement().getType(), statementUnits);
    }
    
    private void clearPrevious() throws SQLException {
        for (Statement each : routedStatements) {
            each.close();
        }
        routedStatements.clear();
    }
    
    private void sqlRoute(final String sql) {
        ShardingContext shardingContext = connection.getShardingContext();
        SqlRoutingEvent event = new SqlRoutingEvent(sql);
        EventBusInstance.getInstance().post(event);
        try {
            routeResult = new StatementRoutingEngine(shardingContext.getShardingRule(),
                    shardingContext.getShardingMetaData(), shardingContext.getDatabaseType(), shardingContext.isShowSQL())
                    .route(sql);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            event.setException(ex);
            event.setEventRoutingType(EventRoutingType.ROUTE_FAILURE);
            EventBusInstance.getInstance().post(event);
            throw ex;
        }
        event.setEventRoutingType(EventRoutingType.ROUTE_SUCCESS);
        EventBusInstance.getInstance().post(event);
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
            queryResults.add(new JDBCQueryResult(resultSet));
        }
        if (routeResult.getSqlStatement() instanceof SelectStatement || routeResult.getSqlStatement() instanceof DALStatement) {
            MergeEngine mergeEngine = MergeEngineFactory.newInstance(connection.getShardingContext().getShardingRule(), queryResults,
                    routeResult.getSqlStatement(), connection.getShardingContext().getShardingMetaData());
            currentResultSet = new ShardingResultSet(resultSets, merge(mergeEngine), this);
        }
        return currentResultSet;
    }
    
    private MergedResult merge(final MergeEngine mergeEngine) throws SQLException {
        ResultSetMergeEvent event = new ResultSetMergeEvent();
        try {
            EventBusInstance.getInstance().post(event);
            MergedResult result = mergeEngine.merge();
            event.setEventMergeType(EventMergeType.MERGE_SUCCESS);
            EventBusInstance.getInstance().post(event);
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            event.setException(ex);
            event.setEventMergeType(EventMergeType.MERGE_FAILURE);
            EventBusInstance.getInstance().post(event);
            throw ex;
        }
    }
}
