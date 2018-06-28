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

package io.shardingsphere.proxy.backend.common;

import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import io.netty.channel.pool.SimpleChannelPool;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.merger.MergeEngineFactory;
import io.shardingsphere.core.merger.MergedResult;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.parsing.SQLJudgeEngine;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.routing.SQLExecutionUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.StatementRoutingEngine;
import io.shardingsphere.core.routing.router.masterslave.MasterSlaveRouter;
import io.shardingsphere.proxy.backend.ShardingProxyClient;
import io.shardingsphere.proxy.backend.mysql.MySQLQueryResult;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.metadata.ProxyShardingRefreshHandler;
import io.shardingsphere.proxy.transport.common.packet.CommandPacketRebuilder;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.constant.StatusFlag;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.TextResultSetRowPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import io.shardingsphere.proxy.util.MySQLResultCache;
import io.shardingsphere.proxy.util.SynchronizedFuture;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * SQL packets backend handler.
 *
 * @author wangkai
 * @author linjiaqi
 * @author panjuan
 */
@Slf4j
@Getter
public final class SQLPacketsBackendHandler implements BackendHandler {
    private static final int CONNECT_TIMEOUT = 30;
    
    private SynchronizedFuture<List<QueryResult>> synchronizedFuture;
    
    private final CommandPacketRebuilder rebuilder;
    
    private final DatabaseType databaseType;
    
    private final boolean showSQL;
    
    private MergedResult mergedResult;
    
    private int currentSequenceId;
    
    private int columnCount;
    
    private boolean isMerged;
    
    private boolean hasMoreResultValueFlag;
    
    public SQLPacketsBackendHandler(final CommandPacketRebuilder rebuilder, final DatabaseType databaseType, final boolean showSQL) {
        this.rebuilder = rebuilder;
        this.databaseType = databaseType;
        this.showSQL = showSQL;
        isMerged = false;
        hasMoreResultValueFlag = true;
    }
    
    @Override
    public CommandResponsePackets execute() {
        if (RuleRegistry.getInstance().isOnlyMasterSlave()) {
            return executeForMasterSlave();
        } else {
            return executeForSharding();
        }
    }
    
    protected CommandResponsePackets executeForMasterSlave() {
        MasterSlaveRouter masterSlaveRouter = new MasterSlaveRouter(RuleRegistry.getInstance().getMasterSlaveRule());
        SQLStatement sqlStatement = new SQLJudgeEngine(rebuilder.sql()).judge();
        String dataSourceName = masterSlaveRouter.route(sqlStatement.getType()).iterator().next();
        
        synchronizedFuture = new SynchronizedFuture<>(1);
        MySQLResultCache.getInstance().putFuture(rebuilder.connectionId(), synchronizedFuture);
        CommandPacket commandPacket = rebuilder.rebuild(new Object[]{rebuilder.sequenceId(), rebuilder.connectionId(), rebuilder.sql()});
        executeCommand(dataSourceName, rebuilder.connectionId(), commandPacket);
        //TODO timeout should be set.
        List<QueryResult> queryResults = synchronizedFuture.get(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        MySQLResultCache.getInstance().deleteFuture(rebuilder.connectionId());
        
        List<CommandResponsePackets> packets = new LinkedList<>();
        for (QueryResult each : queryResults) {
            packets.add(((MySQLQueryResult) each).getCommandResponsePackets());
        }
        return merge(sqlStatement, packets, queryResults);
    }
    
    protected CommandResponsePackets executeForSharding() {
        StatementRoutingEngine routingEngine = new StatementRoutingEngine(RuleRegistry.getInstance().getShardingRule(), RuleRegistry.getInstance().getShardingMetaData(), databaseType, showSQL);
        SQLRouteResult routeResult = routingEngine.route(rebuilder.sql());
        if (routeResult.getExecutionUnits().isEmpty()) {
            return new CommandResponsePackets(new OKPacket(1, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
        }
        
        synchronizedFuture = new SynchronizedFuture<>(routeResult.getExecutionUnits().size());
        MySQLResultCache.getInstance().putFuture(rebuilder.connectionId(), synchronizedFuture);
        for (SQLExecutionUnit each : routeResult.getExecutionUnits()) {
            CommandPacket commandPacket = rebuilder.rebuild(new Object[]{rebuilder.sequenceId(), rebuilder.connectionId(), each.getSqlUnit().getSql()});
            executeCommand(each.getDataSource(), rebuilder.connectionId(), commandPacket);
        }
        //TODO timeout should be set.
        List<QueryResult> queryResults = synchronizedFuture.get(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        MySQLResultCache.getInstance().deleteFuture(rebuilder.connectionId());
        
        List<CommandResponsePackets> packets = Lists.newArrayListWithCapacity(queryResults.size());
        for (QueryResult each : queryResults) {
            MySQLQueryResult queryResult = (MySQLQueryResult) each;
            if (currentSequenceId == 0) {
                currentSequenceId = queryResult.getCurrentSequenceId();
            }
            if (columnCount == 0) {
                columnCount = queryResult.getColumnCount();
            }
            packets.add(queryResult.getCommandResponsePackets());
        }
        
        CommandResponsePackets result = merge(routeResult.getSqlStatement(), packets, queryResults);
        ProxyShardingRefreshHandler.build(routeResult).execute();
        return result;
    }
    
    protected CommandResponsePackets merge(final SQLStatement sqlStatement, final List<CommandResponsePackets> packets, final List<QueryResult> queryResults) {
        CommandResponsePackets headPackets = new CommandResponsePackets();
        for (CommandResponsePackets each : packets) {
            headPackets.addPacket(each.getHeadPacket());
        }
        for (DatabaseProtocolPacket each : headPackets.getDatabaseProtocolPackets()) {
            if (each instanceof ErrPacket) {
                return new CommandResponsePackets(each);
            }
        }
        if (SQLType.DML == sqlStatement.getType()) {
            return mergeDML(headPackets);
        }
        if (SQLType.DQL == sqlStatement.getType() || SQLType.DAL == sqlStatement.getType()) {
            return mergeDQLorDAL(sqlStatement, packets, queryResults);
        }
        return packets.get(0);
    }
    
    private void executeCommand(final String dataSourceName, final int connectionId, final CommandPacket commandPacket) {
        SimpleChannelPool pool = null;
        Channel channel = null;
        try {
            pool = ShardingProxyClient.getInstance().getPoolMap().get(dataSourceName);
            //TODO timeout should be set.
            channel = pool.acquire().get(CONNECT_TIMEOUT, TimeUnit.SECONDS);
            MySQLResultCache.getInstance().putConnection(channel.id().asShortText(), connectionId);
            channel.writeAndFlush(commandPacket);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (null != pool && null != channel) {
                pool.release(channel);
            }
        }
    }
    
    private CommandResponsePackets mergeDML(final CommandResponsePackets firstPackets) {
        int affectedRows = 0;
        long lastInsertId = 0;
        for (DatabaseProtocolPacket each : firstPackets.getDatabaseProtocolPackets()) {
            if (each instanceof OKPacket) {
                OKPacket okPacket = (OKPacket) each;
                affectedRows += okPacket.getAffectedRows();
                lastInsertId = okPacket.getLastInsertId();
            }
        }
        return new CommandResponsePackets(new OKPacket(1, affectedRows, lastInsertId, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
    }
    
    private CommandResponsePackets mergeDQLorDAL(final SQLStatement sqlStatement, final List<CommandResponsePackets> packets, final List<QueryResult> queryResults) {
        try {
            mergedResult = MergeEngineFactory.newInstance(RuleRegistry.getInstance().getShardingRule(), queryResults,
                    sqlStatement, RuleRegistry.getInstance().getShardingMetaData()).merge();
            isMerged = true;
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        }
        return packets.get(0);
    }
    
    /**
     * Has more Result value.
     *
     * @return has more result value
     * @throws SQLException sql exception
     */
    public boolean hasMoreResultValue() throws SQLException {
        if (!isMerged || !hasMoreResultValueFlag) {
            return false;
        }
        if (!mergedResult.next()) {
            hasMoreResultValueFlag = false;
        }
        return true;
    }
    
    /**
     * Get result value.
     *
     * @return database protocol packet
     */
    public DatabaseProtocolPacket getResultValue() {
        if (!hasMoreResultValueFlag) {
            return new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue());
        }
        try {
            List<Object> data = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                data.add(mergedResult.getValue(i, Object.class));
            }
            return new TextResultSetRowPacket(++currentSequenceId, data);
        } catch (final SQLException ex) {
            return new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage());
        }
    }
}
