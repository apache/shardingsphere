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
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.core.routing.router.masterslave.MasterSlaveRouter;
import io.shardingsphere.proxy.backend.resource.BaseJDBCResource;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.metadata.ProxyShardingRefreshHandler;
import io.shardingsphere.proxy.transaction.AtomikosUserTransaction;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.constant.StatusFlag;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.FieldCountPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.Status;
import javax.transaction.SystemException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Abstract ExecuteBackendHandler for SQL or PrepareStatement.
 *
 * @author zhaojun
 */
@Getter
@Slf4j
public abstract class ExecuteBackendHandler implements BackendHandler {
    
    private final String sql;
    
    private MergedResult mergedResult;
    
    private int currentSequenceId;
    
    @Setter
    private int columnCount;
    
    private boolean isMerged;
    
    private boolean hasMoreResultValueFlag;
    
    private final DatabaseType databaseType;
    
    private final boolean showSQL;
    
    @Setter
    private BaseJDBCResource jdbcResource;
    
    private final List<ResultList> resultLists = new CopyOnWriteArrayList<>();
    
    public ExecuteBackendHandler(final String sql, final DatabaseType databaseType, final boolean showSQL) {
        this.sql = sql;
        isMerged = false;
        hasMoreResultValueFlag = true;
        this.databaseType = databaseType;
        this.showSQL = showSQL;
    }
    
    @Override
    public CommandResponsePackets execute() {
        try {
            SQLRouteResult sqlRouteResult = RuleRegistry.getInstance().isOnlyMasterSlave() ? doMasterSlaveRoute() : doSqlShardingRoute();
            return doExecuteInternal(sqlRouteResult);
        } catch (final Exception ex) {
            log.error("ExecuteBackendHandler", ex);
            return new CommandResponsePackets(new ErrPacket(1, 0, "", "", "" + ex.getMessage()));
        }
    }
    
    private CommandResponsePackets doExecuteInternal(final SQLRouteResult routeResult) throws SQLException, SystemException {
        if (routeResult.getExecutionUnits().isEmpty()) {
            return new CommandResponsePackets(new OKPacket(1, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
        }
        if (isXaDDL(routeResult)) {
            throw new SQLException("DDL command can't not execute in xa transaction mode.");
        }
        ExecutorService executorService = RuleRegistry.getInstance().getExecutorService();
        List<Future<CommandResponsePackets>> futureList = new ArrayList<>(1024);
        for (SQLExecutionUnit each : routeResult.getExecutionUnits()) {
            Statement statement = prepareResource(each.getDataSource(), each.getSqlUnit().getSql(), routeResult.getSqlStatement());
            futureList.add(executorService.submit(newSubmitTask(statement, routeResult.getSqlStatement(), each.getSqlUnit().getSql())));
        }
        List<CommandResponsePackets> packets = buildCommandResponsePackets(futureList);
        CommandResponsePackets result = merge(routeResult.getSqlStatement(), packets);
        if (!RuleRegistry.getInstance().isOnlyMasterSlave()) {
            ProxyShardingRefreshHandler.build(routeResult).execute();
        }
        return result;
    }
    
    private boolean isXaDDL(final SQLRouteResult routeResult) throws SystemException {
        return RuleRegistry.isXaTransaction()
                && SQLType.DDL.equals(routeResult.getSqlStatement().getType())
                && Status.STATUS_NO_TRANSACTION != AtomikosUserTransaction.getInstance().getStatus();
    }
    
    private SQLRouteResult doMasterSlaveRoute() {
        MasterSlaveRouter masterSlaveRouter = new MasterSlaveRouter(RuleRegistry.getInstance().getMasterSlaveRule());
        SQLStatement sqlStatement = new SQLJudgeEngine(sql).judge();
        SQLRouteResult result = new SQLRouteResult(sqlStatement, null);
        String dataSourceName = masterSlaveRouter.route(sqlStatement.getType()).iterator().next();
        SQLUnit sqlUnit = new SQLUnit(sql, Collections.<List<Object>>emptyList());
        result.getExecutionUnits().add(new SQLExecutionUnit(dataSourceName, sqlUnit));
        return result;
    }
    
    protected abstract SQLRouteResult doSqlShardingRoute();
    
    protected abstract Statement prepareResource(String dataSourceName, String unitSql, SQLStatement sqlStatement) throws SQLException;
    
    protected abstract Callable<CommandResponsePackets> newSubmitTask(Statement statement, SQLStatement sqlStatement, String unitSql);
    
    private List<CommandResponsePackets> buildCommandResponsePackets(final List<Future<CommandResponsePackets>> futureList) {
        List<CommandResponsePackets> result = new ArrayList<>();
        for (Future<CommandResponsePackets> each : futureList) {
            try {
                result.add(each.get());
            } catch (final InterruptedException | ExecutionException ex) {
                throw new ShardingException(ex.getMessage(), ex);
            }
        }
        return result;
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
            queryResults.add(newQueryResult(packets.get(i), i));
        }
        try {
            mergedResult = MergeEngineFactory.newInstance(RuleRegistry.getInstance().getShardingRule(),
                    queryResults, sqlStatement, RuleRegistry.getInstance().getShardingMetaData()).merge();
            isMerged = true;
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        }
        return buildPackets(packets);
    }
    
    protected abstract QueryResult newQueryResult(CommandResponsePackets packet, int index);
    
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
            jdbcResource.clear();
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
            return newDatabaseProtocolPacket(++currentSequenceId, data);
        } catch (final SQLException ex) {
            return new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage());
        }
    }
    
    protected abstract DatabaseProtocolPacket newDatabaseProtocolPacket(int sequenceId, List<Object> data);
}
