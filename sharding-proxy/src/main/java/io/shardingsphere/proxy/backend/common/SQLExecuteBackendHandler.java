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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.merger.MergeEngineFactory;
import io.shardingsphere.core.merger.MergedResult;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.parsing.SQLJudgeEngine;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.routing.SQLExecutionUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.StatementRoutingEngine;
import io.shardingsphere.core.routing.router.masterslave.MasterSlaveRouter;
import io.shardingsphere.core.routing.router.masterslave.MasterVisitedManager;
import io.shardingsphere.proxy.backend.mysql.MySQLPacketQueryResult;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.metadata.ProxyShardingRefreshHandler;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.constant.StatusFlag;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.FieldCountPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.TextResultSetRowPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import lombok.Getter;
import lombok.Setter;

/**
 * SQL execute backend handler.
 *
 * @author zhangliang
 * @author panjuan
 * @author wangkai
 * @author linjiaqi
 */
@Getter
@Setter
public final class SQLExecuteBackendHandler implements BackendHandler {
    
    private final String sql;
    
    private List<Connection> connections;
    
    private List<ResultSet> resultSets;
    
    private MergedResult mergedResult;
    
    private int currentSequenceId;
    
    private int columnCount;
    
    private boolean isMerged;
    
    private boolean hasMoreResultValueFlag;
    
    private final DatabaseType databaseType;
    
    private final boolean showSQL;
    
    public SQLExecuteBackendHandler(final String sql, final DatabaseType databaseType, final boolean showSQL) {
        this.sql = sql;
        connections = new CopyOnWriteArrayList<>();
        resultSets = new CopyOnWriteArrayList<>();
        isMerged = false;
        hasMoreResultValueFlag = true;
        this.databaseType = databaseType;
        this.showSQL = showSQL;
    }
    
    @Override
    public CommandResponsePackets execute() {
        try {
            if (RuleRegistry.getInstance().isOnlyMasterSlave()) {
                return executeForMasterSlave();
            } else {
                return executeForSharding();
            }
        } catch (final Exception ex) {
            return new CommandResponsePackets(new ErrPacket(1, 0, "", "", "" + ex.getMessage()));
        }
    }
    
    private CommandResponsePackets executeForMasterSlave() {
        MasterSlaveRouter masterSlaveRouter = new MasterSlaveRouter(RuleRegistry.getInstance().getMasterSlaveRule());
        SQLStatement sqlStatement = new SQLJudgeEngine(sql).judge();
        String dataSourceName = masterSlaveRouter.route(sqlStatement.getType()).iterator().next();
        List<CommandResponsePackets> packets = new CopyOnWriteArrayList<>();
        ExecutorService executorService = RuleRegistry.getInstance().getExecutorService();
        List<Future<CommandResponsePackets>> resultList = new ArrayList<>(1024);
        resultList.add(executorService.submit(new SQLExecuteWorker(this, sqlStatement, dataSourceName, sql)));
        getCommandResponsePackets(resultList, packets);
        return merge(sqlStatement, packets);
    }
    
    private CommandResponsePackets executeForSharding() {
        StatementRoutingEngine routingEngine = new StatementRoutingEngine(RuleRegistry.getInstance().getShardingRule(), RuleRegistry.getInstance().getShardingMetaData(), databaseType, showSQL);
        SQLRouteResult routeResult = routingEngine.route(sql);
        if (routeResult.getExecutionUnits().isEmpty()) {
            return new CommandResponsePackets(new OKPacket(1, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
        }
        List<CommandResponsePackets> packets = new CopyOnWriteArrayList<>();
        ExecutorService executorService = RuleRegistry.getInstance().getExecutorService();
        List<Future<CommandResponsePackets>> resultList = new ArrayList<>(1024);
        for (SQLExecutionUnit each : routeResult.getExecutionUnits()) {
            resultList.add(executorService.submit(new SQLExecuteWorker(this, routeResult.getSqlStatement(), each.getDataSource(), each.getSqlUnit().getSql())));
        }
        getCommandResponsePackets(resultList, packets);
        CommandResponsePackets result = merge(routeResult.getSqlStatement(), packets);
        ProxyShardingRefreshHandler.build(routeResult).execute();
        return result;
    }
    
    private void getCommandResponsePackets(final List<Future<CommandResponsePackets>> resultList, final List<CommandResponsePackets> packets) {
        for (Future<CommandResponsePackets> each : resultList) {
            try {
                while (!each.isDone()) {
                    continue;
                }
                packets.add(each.get());
            } catch (final InterruptedException | ExecutionException ex) {
                throw new ShardingException(ex.getMessage(), ex);
            }
        }
    }
    
    private CommandResponsePackets merge(final SQLStatement sqlStatement, final List<CommandResponsePackets> packets) {
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
            return mergeDQLorDAL(sqlStatement, packets);
        }
        return packets.get(0);
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
    
    private CommandResponsePackets mergeDQLorDAL(final SQLStatement sqlStatement, final List<CommandResponsePackets> packets) {
        List<QueryResult> queryResults = new ArrayList<>(packets.size());
        for (int i = 0; i < packets.size(); i++) {
            // TODO replace to a common PacketQueryResult
            queryResults.add(new MySQLPacketQueryResult(packets.get(i), resultSets.get(i)));
        }
        try {
            mergedResult = MergeEngineFactory.newInstance(RuleRegistry.getInstance().getShardingRule(), queryResults, sqlStatement).merge();
            isMerged = true;
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        }
        return buildPackets(packets);
    }
    
    private CommandResponsePackets buildPackets(final List<CommandResponsePackets> packets) {
        CommandResponsePackets result = new CommandResponsePackets();
        Iterator<DatabaseProtocolPacket> databaseProtocolPacketsSampling = packets.iterator().next().getDatabaseProtocolPackets().iterator();
        FieldCountPacket fieldCountPacketSampling = (FieldCountPacket) databaseProtocolPacketsSampling.next();
        result.addPacket(fieldCountPacketSampling);
        ++currentSequenceId;
        for (int i = 0; i < columnCount; i++) {
            result.addPacket(databaseProtocolPacketsSampling.next());
            ++currentSequenceId;
        }
        result.addPacket(databaseProtocolPacketsSampling.next());
        ++currentSequenceId;
        return result;
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
            cleanJDBCResources();
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
    
    private void cleanJDBCResources() {
        for (ResultSet each : resultSets) {
            if (null != each) {
                try {
                    each.close();
                } catch (final SQLException ignore) {
                }
            }
        }
        for (Connection each : connections) {
            if (null != each) {
                try {
                    each.close();
                    MasterVisitedManager.clear();
                } catch (final SQLException ignore) {
                }
            }
        }
    }
}
