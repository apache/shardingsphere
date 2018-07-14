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

package io.shardingsphere.proxy.backend.common.jdbc;

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.constant.TransactionType;
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
import io.shardingsphere.proxy.backend.common.BackendHandler;
import io.shardingsphere.proxy.backend.common.ProxyConnectionHolder;
import io.shardingsphere.proxy.backend.common.ProxyMode;
import io.shardingsphere.proxy.backend.common.ResultList;
import io.shardingsphere.proxy.backend.resource.BaseJDBCResource;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.metadata.ProxyShardingRefreshHandler;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.constant.StatusFlag;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.FieldCountPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import io.shardingsphere.transaction.xa.AtomikosUserTransaction;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.SystemException;
import java.sql.Connection;
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
 * Backend handler via JDBC to connect databases.
 *
 * @author zhaojun
 */
@Getter
@Slf4j
public abstract class JDBCBackendHandler implements BackendHandler {
    
    private final String sql;
    
    private MergedResult mergedResult;
    
    private int currentSequenceId;
    
    @Setter
    private int columnCount;
    
    private boolean isMerged;
    
    private boolean hasMoreResultValueFlag;
    
    private final BaseJDBCResource jdbcResource;
    
    private final RuleRegistry ruleRegistry;
    
    private final List<ResultList> resultLists;
    
    public JDBCBackendHandler(final String sql, final BaseJDBCResource jdbcResource) {
        this.sql = sql;
        isMerged = false;
        hasMoreResultValueFlag = true;
        this.jdbcResource = jdbcResource;
        ruleRegistry = RuleRegistry.getInstance();
        resultLists = new CopyOnWriteArrayList<>();
    }
    
    @Override
    public CommandResponsePackets execute() {
        try {
            return doExecuteInternal(ruleRegistry.isMasterSlaveOnly() ? doMasterSlaveRoute() : doSqlShardingRoute());
        } catch (final Exception ex) {
            log.error("ExecuteBackendHandler", ex);
            return new CommandResponsePackets(new ErrPacket(1, new SQLException(ex)));
        }
    }
    
    private CommandResponsePackets doExecuteInternal(final SQLRouteResult routeResult) throws SQLException, SystemException {
        if (routeResult.getExecutionUnits().isEmpty()) {
            return new CommandResponsePackets(new OKPacket(1));
        }
        if (isXaDDL(routeResult)) {
            throw new SQLException("DDL command can't not execute in xa transaction mode.");
        }
        ExecutorService executorService = ruleRegistry.getExecutorService();
        List<Future<CommandResponsePackets>> futureList = new ArrayList<>(1024);
        for (SQLExecutionUnit each : routeResult.getExecutionUnits()) {
            Statement statement = prepareResource(each.getDataSource(), each.getSqlUnit().getSql(), routeResult.getSqlStatement());
            futureList.add(executorService.submit(newSubmitTask(statement, routeResult.getSqlStatement(), each.getSqlUnit().getSql())));
        }
        List<CommandResponsePackets> packets = buildCommandResponsePackets(futureList);
        CommandResponsePackets result = merge(routeResult.getSqlStatement(), packets);
        if (!ruleRegistry.isMasterSlaveOnly()) {
            ProxyShardingRefreshHandler.build(routeResult).execute();
        }
        return result;
    }
    
    private boolean isXaDDL(final SQLRouteResult routeResult) throws SystemException {
        return TransactionType.XA.equals(ruleRegistry.getTransactionType())
                && SQLType.DDL.equals(routeResult.getSqlStatement().getType()) && Status.STATUS_NO_TRANSACTION != AtomikosUserTransaction.getInstance().getStatus();
    }
    
    private SQLRouteResult doMasterSlaveRoute() {
        SQLStatement sqlStatement = new SQLJudgeEngine(sql).judge();
        SQLRouteResult result = new SQLRouteResult(sqlStatement, null);
        String dataSourceName = new MasterSlaveRouter(ruleRegistry.getMasterSlaveRule()).route(sqlStatement.getType()).iterator().next();
        SQLUnit sqlUnit = new SQLUnit(sql, Collections.<List<Object>>emptyList());
        result.getExecutionUnits().add(new SQLExecutionUnit(dataSourceName, sqlUnit));
        return result;
    }
    
    protected abstract SQLRouteResult doSqlShardingRoute();
    
    protected abstract Statement prepareResource(String dataSourceName, String unitSQL, SQLStatement sqlStatement) throws SQLException;
    
    protected abstract Callable<CommandResponsePackets> newSubmitTask(Statement statement, SQLStatement sqlStatement, String unitSQL);
    
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
        return new CommandResponsePackets(new OKPacket(1, affectedRows, lastInsertId));
    }
    
    private CommandResponsePackets mergeDQLorDAL(final SQLStatement sqlStatement, final List<CommandResponsePackets> packets) {
        List<QueryResult> queryResults = new ArrayList<>(packets.size());
        for (int i = 0; i < packets.size(); i++) {
            queryResults.add(newQueryResult(packets.get(i), i));
        }
        try {
            mergedResult = MergeEngineFactory.newInstance(ruleRegistry.getShardingRule(), queryResults, sqlStatement, ruleRegistry.getShardingMetaData()).merge();
            isMerged = true;
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex));
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
    
    @Override
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
    
    @Override
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
            return new ErrPacket(1, ex);
        }
    }
    
    protected abstract DatabaseProtocolPacket newDatabaseProtocolPacket(int sequenceId, List<Object> data);
    
    protected Connection getConnection(final DataSource dataSource) throws SQLException {
        Connection result;
        if (ProxyMode.CONNECTION_STRICTLY == ruleRegistry.getProxyMode()) {
            result = ProxyConnectionHolder.getConnection(dataSource);
            if (null == result) {
                result = dataSource.getConnection();
                ProxyConnectionHolder.setConnection(dataSource, result);
            }
        } else {
            result = dataSource.getConnection();
        }
        return result;
    }
}
