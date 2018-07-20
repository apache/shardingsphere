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
import io.shardingsphere.core.parsing.SQLJudgeEngine;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.routing.SQLExecutionUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.core.routing.router.masterslave.MasterSlaveRouter;
import io.shardingsphere.proxy.backend.common.BackendHandler;
import io.shardingsphere.proxy.backend.common.jdbc.execute.JDBCExecuteEngine;
import io.shardingsphere.proxy.backend.common.jdbc.execute.SQLExecuteResponses;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.metadata.ProxyShardingRefreshHandler;
import io.shardingsphere.proxy.transport.common.packet.DatabasePacket;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.constant.ServerErrorCode;
import io.shardingsphere.proxy.transport.mysql.packet.command.reponse.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.reponse.QueryResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import io.shardingsphere.transaction.xa.AtomikosUserTransaction;
import lombok.Getter;

import javax.transaction.Status;
import javax.transaction.SystemException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Backend handler via JDBC to connect databases.
 *
 * @author zhaojun
 * @author zhangliang
 */
@Getter
public abstract class JDBCBackendHandler implements BackendHandler {
    
    private final String sql;
    
    private final RuleRegistry ruleRegistry;
    
    private final BackendConnection backendConnection;
    
    private final JDBCExecuteEngine executeEngine;
    
    private SQLExecuteResponses responses;
    
    private MergedResult mergedResult;
    
    private int currentSequenceId;
    
    private boolean isMerged;
    
    private boolean hasMoreResultValueFlag;
    
    public JDBCBackendHandler(final String sql, final JDBCExecuteEngine executeEngine) {
        this.sql = sql;
        this.executeEngine = executeEngine;
        ruleRegistry = RuleRegistry.getInstance();
        backendConnection = executeEngine.getBackendConnection();
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
        responses = executeEngine.execute(routeResult, isReturnGeneratedKeys);
        CommandResponsePackets result = merge(sqlStatement, responses.getCommandResponsePacketsList());
        if (!ruleRegistry.isMasterSlaveOnly()) {
            ProxyShardingRefreshHandler.build(routeResult).execute();
        }
        return result;
    }
    
    // TODO should isolate Atomikos API to SPI
    private boolean isUnsupportedXA(final SQLType sqlType) throws SystemException {
        return TransactionType.XA == ruleRegistry.getTransactionType() && SQLType.DDL == sqlType && Status.STATUS_NO_TRANSACTION != AtomikosUserTransaction.getInstance().getStatus();
    }
    
    private CommandResponsePackets merge(final SQLStatement sqlStatement, final Collection<CommandResponsePackets> packets) {
        Collection<DatabasePacket> headPackets = new LinkedList<>();
        for (CommandResponsePackets each : packets) {
            if (null != each) {
                if (each.getHeadPacket() instanceof ErrPacket) {
                    return new CommandResponsePackets(each.getHeadPacket());
                }
                headPackets.add(each.getHeadPacket());
            }
        }
        CommandResponsePackets firstCommandResponsePackets = packets.iterator().next();
        if (firstCommandResponsePackets instanceof QueryResponsePackets) {
            currentSequenceId += firstCommandResponsePackets.getPackets().size();
            try {
                mergedResult = mergeQuery(sqlStatement);
                return firstCommandResponsePackets;
            } catch (final SQLException ex) {
                return new CommandResponsePackets(new ErrPacket(1, ex));
            }
        }
        return mergeUpdate(headPackets);
    }
    
    private MergedResult mergeQuery(final SQLStatement sqlStatement) throws SQLException {
        isMerged = true;
        return MergeEngineFactory.newInstance(ruleRegistry.getShardingRule(), responses.getQueryResults(), sqlStatement, ruleRegistry.getShardingMetaData()).merge();
    }
    
    private CommandResponsePackets mergeUpdate(final Collection<DatabasePacket> packets) {
        int affectedRows = 0;
        long lastInsertId = 0;
        for (DatabasePacket each : packets) {
            if (each instanceof OKPacket) {
                OKPacket okPacket = (OKPacket) each;
                affectedRows += okPacket.getAffectedRows();
                // TODO consider about insert multiple values
                lastInsertId = okPacket.getLastInsertId();
            }
        }
        return new CommandResponsePackets(new OKPacket(1, affectedRows, lastInsertId));
    }
    
    private SQLRouteResult doMasterSlaveRoute() {
        SQLStatement sqlStatement = new SQLJudgeEngine(sql).judge();
        SQLRouteResult result = new SQLRouteResult(sqlStatement);
        for (String each : new MasterSlaveRouter(ruleRegistry.getMasterSlaveRule(), ruleRegistry.isShowSQL()).route(sql)) {
            result.getExecutionUnits().add(new SQLExecutionUnit(each, new SQLUnit(sql, Collections.<List<Object>>emptyList())));
        }
        return result;
    }
    
    protected abstract SQLRouteResult doShardingRoute();
    
    @Override
    public final boolean hasMoreResultValue() throws SQLException {
        if (!isMerged || !hasMoreResultValueFlag) {
            backendConnection.close();
            return false;
        }
        if (!mergedResult.next()) {
            hasMoreResultValueFlag = false;
        }
        return true;
    }
    
    @Override
    public final DatabasePacket getResultValue() {
        if (!hasMoreResultValueFlag) {
            return new EofPacket(++currentSequenceId);
        }
        try {
            List<Object> data = new ArrayList<>(responses.getColumnCount());
            for (int i = 1; i <= responses.getColumnCount(); i++) {
                data.add(mergedResult.getValue(i, Object.class));
            }
            return newDatabasePacket(++currentSequenceId, data, responses.getColumnCount(), responses.getColumnTypes());
        } catch (final SQLException ex) {
            return new ErrPacket(1, ex);
        }
    }
    
    protected abstract DatabasePacket newDatabasePacket(int sequenceId, List<Object> data, int columnCount, List<ColumnType> columnTypes);
}
