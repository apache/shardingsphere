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

package io.shardingsphere.proxy.backend.netty;

import io.netty.channel.Channel;
import io.netty.channel.pool.SimpleChannelPool;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.merger.MergeEngineFactory;
import io.shardingsphere.core.merger.MergedResult;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.merger.dal.show.ShowDatabasesMergedResult;
import io.shardingsphere.core.metadata.table.executor.TableMetaDataLoader;
import io.shardingsphere.core.parsing.SQLJudgeEngine;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.UseStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.StatementRoutingEngine;
import io.shardingsphere.core.routing.router.masterslave.MasterSlaveRouter;
import io.shardingsphere.proxy.backend.AbstractBackendHandler;
import io.shardingsphere.proxy.backend.BackendExecutorContext;
import io.shardingsphere.proxy.backend.ResultPacket;
import io.shardingsphere.proxy.backend.netty.client.BackendNettyClientManager;
import io.shardingsphere.proxy.backend.netty.client.response.mysql.MySQLQueryResult;
import io.shardingsphere.proxy.backend.netty.future.FutureRegistry;
import io.shardingsphere.proxy.backend.netty.future.SynchronizedFuture;
import io.shardingsphere.proxy.config.ProxyContext;
import io.shardingsphere.proxy.config.ProxyTableMetaDataConnectionManager;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.frontend.common.FrontendHandler;
import io.shardingsphere.proxy.runtime.ChannelRegistry;
import io.shardingsphere.proxy.transport.common.packet.DatabasePacket;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.text.query.ComQueryPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Netty backend handler.
 *
 * @author wangkai
 * @author linjiaqi
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class NettyBackendHandler extends AbstractBackendHandler {
    
    private static final ProxyContext PROXY_CONTEXT = ProxyContext.getInstance();
    
    private static final BackendNettyClientManager CLIENT_MANAGER = BackendNettyClientManager.getInstance();
    
    private final FrontendHandler frontendHandler;
    
    private final RuleRegistry ruleRegistry;
    
    private final int connectionId;
    
    private final int sequenceId;
    
    private final String sql;
    
    private final DatabaseType databaseType;
    
    private final Map<String, List<Channel>> channelMap = new HashMap<>();
    
    private SynchronizedFuture synchronizedFuture;
    
    private int currentSequenceId;
    
    private int columnCount;
    
    private MergedResult mergedResult;
    
    @Override
    protected CommandResponsePackets execute0() throws InterruptedException, ExecutionException, TimeoutException, SQLException {
        return ruleRegistry.isMasterSlaveOnly() ? executeForMasterSlave() : executeForSharding();
    }
    
    private CommandResponsePackets executeForMasterSlave() throws InterruptedException, ExecutionException, TimeoutException {
        SQLStatement sqlStatement = new SQLJudgeEngine(sql).judge();
        if (sqlStatement instanceof UseStatement) {
            return handleUseStatement((UseStatement) sqlStatement, frontendHandler);
        }
        String dataSourceName = new MasterSlaveRouter(ruleRegistry.getMasterSlaveRule(), PROXY_CONTEXT.isShowSQL()).route(sql).iterator().next();
        synchronizedFuture = new SynchronizedFuture(1);
        FutureRegistry.getInstance().put(connectionId, synchronizedFuture);
        executeSQL(dataSourceName, sql);
        List<QueryResult> queryResults = synchronizedFuture.get(PROXY_CONTEXT.getBackendNIOConfig().getConnectionTimeoutSeconds(), TimeUnit.SECONDS);
        FutureRegistry.getInstance().delete(connectionId);
        List<CommandResponsePackets> packets = new LinkedList<>();
        for (QueryResult each : queryResults) {
            packets.add(((MySQLQueryResult) each).getCommandResponsePackets());
        }
        return merge(sqlStatement, packets, queryResults);
    }
    
    private CommandResponsePackets executeForSharding() throws InterruptedException, ExecutionException, TimeoutException, SQLException {
        StatementRoutingEngine routingEngine = new StatementRoutingEngine(
                ruleRegistry.getShardingRule(), ruleRegistry.getMetaData().getTable(), databaseType, PROXY_CONTEXT.isShowSQL(), ruleRegistry.getMetaData().getDataSource());
        SQLRouteResult routeResult = routingEngine.route(sql);
        if (routeResult.getSqlStatement() != null && routeResult.getSqlStatement() instanceof UseStatement) {
            return handleUseStatement((UseStatement) routeResult.getSqlStatement(), frontendHandler);
        }
        if (routeResult.getRouteUnits().isEmpty()) {
            return new CommandResponsePackets(new OKPacket(1));
        }
        synchronizedFuture = new SynchronizedFuture(routeResult.getRouteUnits().size());
        FutureRegistry.getInstance().put(connectionId, synchronizedFuture);
        for (RouteUnit each : routeResult.getRouteUnits()) {
            executeSQL(each.getDataSourceName(), each.getSqlUnit().getSql());
        }
        List<QueryResult> queryResults = synchronizedFuture.get(PROXY_CONTEXT.getBackendNIOConfig().getConnectionTimeoutSeconds(), TimeUnit.SECONDS);
        FutureRegistry.getInstance().delete(connectionId);
        List<CommandResponsePackets> packets = new ArrayList<>(queryResults.size());
        for (QueryResult each : queryResults) {
            MySQLQueryResult queryResult = (MySQLQueryResult) each;
            if (0 == currentSequenceId) {
                currentSequenceId = queryResult.getCurrentSequenceId();
            }
            if (0 == columnCount) {
                columnCount = queryResult.getColumnCount();
            }
            packets.add(queryResult.getCommandResponsePackets());
        }
        SQLStatement sqlStatement = routeResult.getSqlStatement();
        CommandResponsePackets result = merge(sqlStatement, packets, queryResults);
        if (SQLType.DDL == sqlStatement.getType() && !sqlStatement.getTables().isEmpty()) {
            refreshTableMetaData(sqlStatement.getTables().getSingleTableName());
        }
        return result;
    }
    
    private void executeSQL(final String dataSourceName, final String sql) throws InterruptedException, ExecutionException, TimeoutException {
        if (!channelMap.containsKey(dataSourceName)) {
            channelMap.put(dataSourceName, new ArrayList<Channel>());
        }
        SimpleChannelPool pool = CLIENT_MANAGER.getBackendNettyClient(ruleRegistry.getSchemaName()).getPoolMap().get(dataSourceName);
        Channel channel = pool.acquire().get(PROXY_CONTEXT.getBackendNIOConfig().getConnectionTimeoutSeconds(), TimeUnit.SECONDS);
        channelMap.get(dataSourceName).add(channel);
        ChannelRegistry.getInstance().putConnectionId(channel.id().asShortText(), connectionId);
        channel.writeAndFlush(new ComQueryPacket(sequenceId, sql));
    }
    
    private CommandResponsePackets merge(final SQLStatement sqlStatement, final List<CommandResponsePackets> packets, final List<QueryResult> queryResults) {
        CommandResponsePackets headPackets = new CommandResponsePackets();
        for (CommandResponsePackets each : packets) {
            headPackets.getPackets().add(each.getHeadPacket());
        }
        for (DatabasePacket each : headPackets.getPackets()) {
            if (each instanceof ErrPacket) {
                return new CommandResponsePackets(each);
            }
        }
        if (SQLType.TCL == sqlStatement.getType()) {
            channelRelease();
        }
        if (SQLType.DML == sqlStatement.getType()) {
            return mergeDML(headPackets);
        }
        if (SQLType.DQL == sqlStatement.getType() || SQLType.DAL == sqlStatement.getType()) {
            return mergeDQLorDAL(sqlStatement, packets, queryResults);
        }
        return packets.get(0);
    }
    
    private CommandResponsePackets mergeDML(final CommandResponsePackets firstPackets) {
        int affectedRows = 0;
        long lastInsertId = 0;
        for (DatabasePacket each : firstPackets.getPackets()) {
            if (each instanceof OKPacket) {
                OKPacket okPacket = (OKPacket) each;
                affectedRows += okPacket.getAffectedRows();
                lastInsertId = okPacket.getLastInsertId();
            }
        }
        return new CommandResponsePackets(new OKPacket(1, affectedRows, lastInsertId));
    }
    
    private CommandResponsePackets mergeDQLorDAL(final SQLStatement sqlStatement, final List<CommandResponsePackets> packets, final List<QueryResult> queryResults) {
        try {
            mergedResult = MergeEngineFactory.newInstance(ruleRegistry.getShardingRule(), queryResults, sqlStatement, ruleRegistry.getMetaData().getTable()).merge();
            if (mergedResult instanceof ShowDatabasesMergedResult) {
                mergedResult = new ShowDatabasesMergedResult(PROXY_CONTEXT.getSchemaNames());
            }
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex));
        }
        return packets.get(0);
    }
    
    // TODO :jiaqi use sql packet to refresh meta data
    // TODO refresh table meta data by SQL parse result
    private void refreshTableMetaData(final String logicTableName) throws SQLException {
        TableMetaDataLoader tableMetaDataLoader = new TableMetaDataLoader(ruleRegistry.getMetaData().getDataSource(), 
                BackendExecutorContext.getInstance().getExecuteEngine(), new ProxyTableMetaDataConnectionManager(ruleRegistry.getBackendDataSource()), PROXY_CONTEXT.getMaxConnectionsSizePerQuery());
        ruleRegistry.getMetaData().getTable().put(logicTableName, tableMetaDataLoader.load(logicTableName, ruleRegistry.getShardingRule()));
    }
    
    @Override
    public boolean next() throws SQLException {
        if (null == mergedResult || !mergedResult.next()) {
            channelRelease();
            return false;
        }
        return true;
    }
    
    @Override
    public ResultPacket getResultValue() throws SQLException {
        List<Object> data = new ArrayList<>(columnCount);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            data.add(mergedResult.getValue(columnIndex, Object.class));
        }
        return new ResultPacket(++currentSequenceId, data, columnCount, Collections.<ColumnType>emptyList());
    }
    
    private void channelRelease() {
        for (Entry<String, List<Channel>> entry : channelMap.entrySet()) {
            for (Channel each : entry.getValue()) {
                CLIENT_MANAGER.getBackendNettyClient(ruleRegistry.getSchemaName()).getPoolMap().get(entry.getKey()).release(each);
            }
        }
    }
}
