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

import com.google.common.collect.Lists;
import io.netty.channel.EventLoopGroup;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.constant.TransactionType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.merger.MergeEngineFactory;
import io.shardingsphere.core.merger.MergedResult;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.parsing.SQLJudgeEngine;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.routing.SQLExecutionUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.core.routing.router.masterslave.MasterSlaveRouter;
import io.shardingsphere.proxy.backend.common.BackendHandler;
import io.shardingsphere.proxy.backend.common.ProxyMode;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.metadata.ProxyShardingRefreshHandler;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.constant.ServerErrorCode;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.FieldCountPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import io.shardingsphere.proxy.util.ExecutorContext;
import io.shardingsphere.transaction.xa.AtomikosUserTransaction;
import lombok.Getter;

import javax.transaction.Status;
import javax.transaction.SystemException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Backend handler via JDBC to connect databases.
 *
 * @author zhaojun
 */
@Getter
public abstract class JDBCBackendHandler implements BackendHandler {
    
    private final String sql;
    
    private final RuleRegistry ruleRegistry;
    
    private final EventLoopGroup userGroup;
    
    private final ConnectionManager connectionManager;
    
    private final List<QueryResult> queryResults;
    
    private MergedResult mergedResult;
    
    private int currentSequenceId;
    
    private int columnCount;
    
    private List<ColumnType> columnTypes;
    
    private boolean isMerged;
    
    private boolean hasMoreResultValueFlag;
    
    public JDBCBackendHandler(final String sql) {
        this.sql = sql;
        ruleRegistry = RuleRegistry.getInstance();
        userGroup = ExecutorContext.getInstance().getUserGroup();
        connectionManager = new ConnectionManager();
        queryResults = new LinkedList<>();
        isMerged = false;
        hasMoreResultValueFlag = true;
    }
    
    @Override
    public final CommandResponsePackets execute() {
        try {
            return execute(ruleRegistry.isMasterSlaveOnly() ? doMasterSlaveRoute() : doShardingRoute());
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex));
        } catch (final SystemException | ShardingException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ServerErrorCode.ER_STD_UNKNOWN_EXCEPTION, ex.getMessage()));
        }
    }
    
    private CommandResponsePackets execute(final SQLRouteResult routeResult) throws SQLException, SystemException {
        if (routeResult.getExecutionUnits().isEmpty()) {
            return new CommandResponsePackets(new OKPacket(1));
        }
        SQLStatement sqlStatement = routeResult.getSqlStatement();
        boolean isReturnGeneratedKeys = sqlStatement instanceof InsertStatement;
        if (isUnsupportedXA(sqlStatement.getType())) {
            return new CommandResponsePackets(new ErrPacket(1, 
                    ServerErrorCode.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE, sqlStatement.getTables().isSingleTable() ? sqlStatement.getTables().getSingleTableName() : "unknown_table"));
        }
        List<CommandResponsePackets> packets = ProxyMode.MEMORY_STRICTLY == ruleRegistry.getProxyMode()
                ? executeWithMemoryStrictlyMode(routeResult, isReturnGeneratedKeys) : executeWithConnectionStrictlyMode(routeResult, isReturnGeneratedKeys);
        CommandResponsePackets result = merge(sqlStatement, packets);
        if (!ruleRegistry.isMasterSlaveOnly()) {
            ProxyShardingRefreshHandler.build(routeResult).execute();
        }
        return result;
    }
    
    // TODO should isolate Atomikos API to SPI
    private boolean isUnsupportedXA(final SQLType sqlType) throws SystemException {
        return TransactionType.XA == ruleRegistry.getTransactionType() && SQLType.DDL == sqlType && Status.STATUS_NO_TRANSACTION != AtomikosUserTransaction.getInstance().getStatus();
    }
    
    private List<CommandResponsePackets> executeWithMemoryStrictlyMode(final SQLRouteResult routeResult, final boolean isReturnGeneratedKeys) throws SQLException {
        Iterator<SQLExecutionUnit> sqlExecutionUnits = routeResult.getExecutionUnits().iterator();
        SQLExecutionUnit firstSQLExecutionUnit = sqlExecutionUnits.next();
        List<Future<JDBCExecuteResponse>> futureList = asyncExecuteWithMemoryStrictlyMode(isReturnGeneratedKeys, Lists.newArrayList(sqlExecutionUnits));
        JDBCExecuteResponse firstJDBCExecuteResponse = syncExecuteWithMemoryStrictlyMode(isReturnGeneratedKeys, firstSQLExecutionUnit);
        return buildCommandResponsePacketsWithMemoryStrictlyMode(firstJDBCExecuteResponse, futureList);
    }
    
    private List<Future<JDBCExecuteResponse>> asyncExecuteWithMemoryStrictlyMode(final boolean isReturnGeneratedKeys, final Collection<SQLExecutionUnit> sqlExecutionUnits) {
        List<Future<JDBCExecuteResponse>> result = new LinkedList<>();
        for (SQLExecutionUnit each : sqlExecutionUnits) {
            final String dataSourceName = each.getDataSource();
            final String actualSQL = each.getSqlUnit().getSql();
            result.add(userGroup.submit(new Callable<JDBCExecuteResponse>() {
                
                @Override
                public JDBCExecuteResponse call() throws SQLException {
                    return createExecuteWorker(connectionManager.getConnection(dataSourceName), actualSQL, isReturnGeneratedKeys).execute();
                }
            }));
        }
        return result;
    }
    
    private JDBCExecuteResponse syncExecuteWithMemoryStrictlyMode(final boolean isReturnGeneratedKeys, final SQLExecutionUnit sqlExecutionUnit) throws SQLException {
        return createExecuteWorker(connectionManager.getConnection(sqlExecutionUnit.getDataSource()), sqlExecutionUnit.getSqlUnit().getSql(), isReturnGeneratedKeys).execute();
    }
    
    private List<CommandResponsePackets> buildCommandResponsePacketsWithMemoryStrictlyMode(final JDBCExecuteResponse firstJDBCExecuteResponse, final List<Future<JDBCExecuteResponse>> futureList) {
        List<CommandResponsePackets> result = new ArrayList<>(futureList.size() + 1);
        result.add(firstJDBCExecuteResponse.getCommandResponsePackets());
        columnCount = firstJDBCExecuteResponse.getColumnCount();
        columnTypes = firstJDBCExecuteResponse.getColumnTypes();
        queryResults.add(firstJDBCExecuteResponse.getQueryResult());
        for (Future<JDBCExecuteResponse> each : futureList) {
            try {
                JDBCExecuteResponse executeResponse = each.get();
                result.add(executeResponse.getCommandResponsePackets());
                queryResults.add(executeResponse.getQueryResult());
            } catch (final InterruptedException | ExecutionException ex) {
                throw new ShardingException(ex.getMessage(), ex);
            }
        }
        return result;
    }
    
    private List<CommandResponsePackets> executeWithConnectionStrictlyMode(final SQLRouteResult routeResult, final boolean isReturnGeneratedKeys) throws SQLException {
        Map<String, Collection<SQLUnit>> sqlExecutionUnits = routeResult.getSQLUnitGroups();
        Entry<String, Collection<SQLUnit>> firstEntry = sqlExecutionUnits.entrySet().iterator().next();
        sqlExecutionUnits.remove(firstEntry.getKey());
        List<Future<Collection<JDBCExecuteResponse>>> futureList = asyncExecuteWithConnectionStrictlyMode(isReturnGeneratedKeys, sqlExecutionUnits);
        Collection<JDBCExecuteResponse> firstJDBCExecuteResponses = syncExecuteWithConnectionStrictlyMode(isReturnGeneratedKeys, firstEntry.getKey(), firstEntry.getValue());
        return buildCommandResponsePacketsWithConnectionStrictlyMode(firstJDBCExecuteResponses, futureList);
    }
    
    private List<Future<Collection<JDBCExecuteResponse>>> asyncExecuteWithConnectionStrictlyMode(
            final boolean isReturnGeneratedKeys, final Map<String, Collection<SQLUnit>> sqlUnitGroups) throws SQLException {
        List<Future<Collection<JDBCExecuteResponse>>> result = new LinkedList<>();
        for (Entry<String, Collection<SQLUnit>> entry : sqlUnitGroups.entrySet()) {
            final Connection connection = connectionManager.getConnection(entry.getKey());
            final Collection<SQLUnit> sqlUnits = entry.getValue();
            result.add(userGroup.submit(new Callable<Collection<JDBCExecuteResponse>>() {
                
                @Override
                public Collection<JDBCExecuteResponse> call() throws SQLException {
                    Collection<JDBCExecuteResponse> result = new LinkedList<>();
                    for (SQLUnit each : sqlUnits) {
                        result.add(createExecuteWorker(connection, each.getSql(), isReturnGeneratedKeys).execute());
                    }
                    return result;
                }
            }));
        }
        return result;
    }
    
    private Collection<JDBCExecuteResponse> syncExecuteWithConnectionStrictlyMode(
            final boolean isReturnGeneratedKeys, final String dataSourceName, final Collection<SQLUnit> sqlUnits) throws SQLException {
        Collection<JDBCExecuteResponse> result = new LinkedList<>();
        for (SQLUnit each : sqlUnits) {
            String actualSQL = each.getSql();
            result.add(createExecuteWorker(connectionManager.getConnection(dataSourceName), actualSQL, isReturnGeneratedKeys).execute());
        }
        return result;
    }
    
    private List<CommandResponsePackets> buildCommandResponsePacketsWithConnectionStrictlyMode(
            final Collection<JDBCExecuteResponse> firstJDBCExecuteResponses, final List<Future<Collection<JDBCExecuteResponse>>> futureList) {
        List<CommandResponsePackets> result = new LinkedList<>();
        for (JDBCExecuteResponse each : firstJDBCExecuteResponses) {
            result.add(each.getCommandResponsePackets());
            if (0 != columnCount) {
                columnCount = each.getColumnCount();
            }
            if (null != columnTypes) {
                columnTypes = each.getColumnTypes();
            }
            queryResults.add(each.getQueryResult());
        }
        for (Future<Collection<JDBCExecuteResponse>> each : futureList) {
            try {
                Collection<JDBCExecuteResponse> executeResponses = each.get();
                for (JDBCExecuteResponse jdbcExecuteResponse : executeResponses) {
                    result.add(jdbcExecuteResponse.getCommandResponsePackets());
                    queryResults.add(jdbcExecuteResponse.getQueryResult());
                }
            } catch (final InterruptedException | ExecutionException ex) {
                throw new ShardingException(ex.getMessage(), ex);
            }
        }
        return result;
    }
    
    protected abstract JDBCExecuteWorker createExecuteWorker(Connection connection, String actualSQL, boolean isReturnGeneratedKeys) throws SQLException;
    
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
                // TODO consider about insert multiple values
                lastInsertId = okPacket.getLastInsertId();
            }
        }
        return new CommandResponsePackets(new OKPacket(1, affectedRows, lastInsertId));
    }
    
    private CommandResponsePackets mergeDQLorDAL(final SQLStatement sqlStatement, final List<CommandResponsePackets> packets) {
        try {
            mergedResult = MergeEngineFactory.newInstance(ruleRegistry.getShardingRule(), queryResults, sqlStatement, ruleRegistry.getShardingMetaData()).merge();
            isMerged = true;
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex));
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
    
    private SQLRouteResult doMasterSlaveRoute() {
        SQLStatement sqlStatement = new SQLJudgeEngine(sql).judge();
        SQLRouteResult result = new SQLRouteResult(sqlStatement);
        for (String each : new MasterSlaveRouter(ruleRegistry.getMasterSlaveRule()).route(sqlStatement.getType())) {
            result.getExecutionUnits().add(new SQLExecutionUnit(each, new SQLUnit(sql, Collections.<List<Object>>emptyList())));
        }
        return result;
    }
    
    protected abstract SQLRouteResult doShardingRoute();
    
    @Override
    public final boolean hasMoreResultValue() throws SQLException {
        if (!isMerged || !hasMoreResultValueFlag) {
            connectionManager.close();
            return false;
        }
        if (!mergedResult.next()) {
            hasMoreResultValueFlag = false;
        }
        return true;
    }
    
    @Override
    public final DatabaseProtocolPacket getResultValue() {
        if (!hasMoreResultValueFlag) {
            return new EofPacket(++currentSequenceId);
        }
        try {
            List<Object> data = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                data.add(mergedResult.getValue(i, Object.class));
            }
            return newDatabaseProtocolPacket(++currentSequenceId, data, columnTypes);
        } catch (final SQLException ex) {
            return new ErrPacket(1, ex);
        }
    }
    
    protected abstract DatabaseProtocolPacket newDatabaseProtocolPacket(int sequenceId, List<Object> data, List<ColumnType> columnTypes);
}
