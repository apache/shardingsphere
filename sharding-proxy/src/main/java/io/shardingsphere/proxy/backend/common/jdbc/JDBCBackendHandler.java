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

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.constant.TransactionType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.merger.MergeEngineFactory;
import io.shardingsphere.core.merger.MergedResult;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.proxy.backend.common.BackendHandler;
import io.shardingsphere.proxy.backend.common.jdbc.execute.JDBCExecuteEngine;
import io.shardingsphere.proxy.backend.common.jdbc.execute.response.ExecuteQueryResponse;
import io.shardingsphere.proxy.backend.common.jdbc.execute.response.ExecuteResponse;
import io.shardingsphere.proxy.backend.common.jdbc.execute.response.ExecuteUpdateResponse;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.metadata.ProxyShardingRefreshHandler;
import io.shardingsphere.proxy.transport.common.packet.DatabasePacket;
import io.shardingsphere.proxy.transport.mysql.constant.ServerErrorCode;
import io.shardingsphere.proxy.transport.mysql.packet.command.reponse.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.reponse.QueryResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import io.shardingsphere.transaction.xa.AtomikosUserTransaction;

import javax.transaction.Status;
import javax.transaction.SystemException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Backend handler via JDBC to connect databases.
 *
 * @author zhaojun
 * @author zhangliang
 */
public final class JDBCBackendHandler implements BackendHandler {
    
    private final String sql;
    
    private final RuleRegistry ruleRegistry;
    
    private final BackendConnection backendConnection;
    
    private final JDBCExecuteEngine executeEngine;
    
    private ExecuteResponse executeResponse;
    
    private MergedResult mergedResult;
    
    private int currentSequenceId;
    
    public JDBCBackendHandler(final String sql, final JDBCExecuteEngine executeEngine) {
        this.sql = sql;
        this.executeEngine = executeEngine;
        ruleRegistry = RuleRegistry.getInstance();
        backendConnection = executeEngine.getBackendConnection();
    }
    
    @Override
    public CommandResponsePackets execute() {
        try {
            return execute(executeEngine.getJdbcExecutorWrapper().route(sql, DatabaseType.MySQL));
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex));
        } catch (final SystemException | ShardingException ex) {
            Optional<SQLException> sqlException = findSQLException(ex);
            return sqlException.isPresent()
                    ? new CommandResponsePackets(new ErrPacket(1, sqlException.get())) : new CommandResponsePackets(new ErrPacket(1, ServerErrorCode.ER_STD_UNKNOWN_EXCEPTION, ex.getMessage()));
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
        executeResponse = executeEngine.execute(routeResult, isReturnGeneratedKeys);
        if (!ruleRegistry.isMasterSlaveOnly()) {
            ProxyShardingRefreshHandler.build(sqlStatement).execute();
        }
        return merge(sqlStatement);
    }
    
    // TODO should isolate Atomikos API to SPI
    private boolean isUnsupportedXA(final SQLType sqlType) throws SystemException {
        return TransactionType.XA == ruleRegistry.getTransactionType() && SQLType.DDL == sqlType && Status.STATUS_NO_TRANSACTION != AtomikosUserTransaction.getInstance().getStatus();
    }
    
    private CommandResponsePackets merge(final SQLStatement sqlStatement) throws SQLException {
        if (executeResponse instanceof ExecuteUpdateResponse) {
            return ((ExecuteUpdateResponse) executeResponse).merge();
        }
        mergedResult = MergeEngineFactory.newInstance(
                ruleRegistry.getShardingRule(), ((ExecuteQueryResponse) executeResponse).getQueryResults(), sqlStatement, ruleRegistry.getShardingMetaData()).merge();
        QueryResponsePackets result = ((ExecuteQueryResponse) executeResponse).getQueryResponsePackets();
        currentSequenceId = result.getPackets().size();
        return result;
    }
    
    private Optional<SQLException> findSQLException(final Exception exception) {
        if (null == exception.getCause()) {
            return Optional.absent();
        }
        if (exception.getCause() instanceof SQLException) {
            return Optional.of((SQLException) exception.getCause());
        }
        if (null == exception.getCause().getCause()) {
            return Optional.absent();
        }
        if (exception.getCause().getCause() instanceof SQLException) {
            return Optional.of((SQLException) exception.getCause());
        }
        return Optional.absent();
    }
    
    @Override
    public boolean next() throws SQLException {
        if (null == mergedResult || !mergedResult.next()) {
            backendConnection.close();
            return false;
        }
        return true;
    }
    
    @Override
    public DatabasePacket getResultValue() {
        QueryResponsePackets queryResponsePackets = ((ExecuteQueryResponse) executeResponse).getQueryResponsePackets();
        try {
            List<Object> data = new ArrayList<>(queryResponsePackets.getColumnCount());
            for (int i = 1; i <= queryResponsePackets.getColumnCount(); i++) {
                data.add(mergedResult.getValue(i, Object.class));
            }
            return executeEngine.getJdbcExecutorWrapper().createResultSetPacket(
                    ++currentSequenceId, data, queryResponsePackets.getColumnCount(), queryResponsePackets.getColumnTypes(), DatabaseType.MySQL);
        } catch (final SQLException ex) {
            return new ErrPacket(++currentSequenceId, ex);
        }
    }
}
