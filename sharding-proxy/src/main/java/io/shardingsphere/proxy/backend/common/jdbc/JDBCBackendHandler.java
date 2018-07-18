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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
    
    private MergedResult mergedResult;
    
    private int currentSequenceId;
    
    private int columnCount;
    
    private List<ColumnType> columnTypes;
    
    private boolean isMerged;
    
    private boolean hasMoreResultValueFlag;
    
    private final List<QueryResult> queryResults = new CopyOnWriteArrayList<>();
    
    private final RuleRegistry ruleRegistry;
    
    private final EventLoopGroup userGroup;
    
    private final ConnectionManager connectionManager;
    
    public JDBCBackendHandler(final String sql) {
        this.sql = sql;
        isMerged = false;
        hasMoreResultValueFlag = true;
        ruleRegistry = RuleRegistry.getInstance();
        userGroup = ExecutorContext.getInstance().getUserGroup();
        connectionManager = new ConnectionManager();
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
        Iterator<SQLExecutionUnit> sqlExecutionUnits = routeResult.getExecutionUnits().iterator();
        SQLExecutionUnit firstSQLExecutionUnit = sqlExecutionUnits.next();
        List<Future<JDBCExecuteResponse>> futureList = asyncExecute(isReturnGeneratedKeys, Lists.newArrayList(sqlExecutionUnits));
        CommandResponsePackets firstCommandResponsePackets = syncExecute(isReturnGeneratedKeys, firstSQLExecutionUnit);
        List<CommandResponsePackets> packets = buildCommandResponsePackets(firstCommandResponsePackets, futureList);
        CommandResponsePackets result = merge(sqlStatement, packets);
        if (!ruleRegistry.isMasterSlaveOnly()) {
            ProxyShardingRefreshHandler.build(routeResult).execute();
        }
        return result;
    }
    
    private List<Future<JDBCExecuteResponse>> asyncExecute(final boolean isReturnGeneratedKeys, final Collection<SQLExecutionUnit> sqlExecutionUnits) throws SQLException {
        List<Future<JDBCExecuteResponse>> result = new LinkedList<>();
        for (SQLExecutionUnit each : sqlExecutionUnits) {
            String actualSQL = each.getSqlUnit().getSql();
            Statement statement = createStatement(connectionManager.getConnection(each.getDataSource()), actualSQL, isReturnGeneratedKeys);
            result.add(userGroup.submit(new JDBCExecuteCallable(createExecuteWorker(statement, isReturnGeneratedKeys, actualSQL))));
        }
        return result;
    }
    
    private CommandResponsePackets syncExecute(final boolean isReturnGeneratedKeys, final SQLExecutionUnit firstSQLExecutionUnit) throws SQLException {
        String actualSQL = firstSQLExecutionUnit.getSqlUnit().getSql();
        Statement statement = createStatement(connectionManager.getConnection(firstSQLExecutionUnit.getDataSource()), actualSQL, isReturnGeneratedKeys);
        JDBCExecuteWorker executeWorker = createExecuteWorker(statement, isReturnGeneratedKeys, actualSQL);
        JDBCExecuteResponse result = executeWorker.execute();
        columnCount = result.getColumnCount();
        columnTypes = result.getColumnTypes();
        queryResults.add(result.getQueryResult());
        return result.getCommandResponsePackets();
    }
    
    // TODO should isolate Atomikos API to SPI
    private boolean isUnsupportedXA(final SQLType sqlType) throws SystemException {
        return TransactionType.XA == ruleRegistry.getTransactionType() && SQLType.DDL == sqlType && Status.STATUS_NO_TRANSACTION != AtomikosUserTransaction.getInstance().getStatus();
    }
    
    protected abstract Statement createStatement(Connection connection, String actualSQL, boolean isReturnGeneratedKeys) throws SQLException;
    
    protected abstract JDBCExecuteWorker createExecuteWorker(Statement statement, boolean isReturnGeneratedKeys, String actualSQL);
    
    private List<CommandResponsePackets> buildCommandResponsePackets(final CommandResponsePackets firstCommandResponsePackets, final List<Future<JDBCExecuteResponse>> futureList) {
        List<CommandResponsePackets> result = new ArrayList<>();
        for (Future<JDBCExecuteResponse> each : futureList) {
            try {
                JDBCExecuteResponse executeResponse = each.get();
                result.add(executeResponse.getCommandResponsePackets());
                queryResults.add(executeResponse.getQueryResult());
            } catch (final InterruptedException | ExecutionException ex) {
                throw new ShardingException(ex.getMessage(), ex);
            }
        }
        result.add(firstCommandResponsePackets);
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
