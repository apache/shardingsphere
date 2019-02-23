/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.SQLType;
import org.apache.shardingsphere.core.merger.MergeEngineFactory;
import org.apache.shardingsphere.core.merger.MergedResult;
import org.apache.shardingsphere.core.merger.dal.show.ShowTablesMergedResult;
import org.apache.shardingsphere.core.parsing.parser.constant.DerivedColumn;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.routing.SQLRouteResult;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.ConnectionStatus;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.JDBCExecuteEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.ExecuteQueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.ExecuteResponse;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.ExecuteUpdateResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.common.FailureResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.common.SuccessResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryHeaderResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.query.ResultPacket;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.runtime.schema.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.DataHeaderPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.QueryResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database access engine for JDBC.
 *
 * @author zhaojun
 * @author zhangliang
 * @author panjuan
 * @author maxiaoguang
 */
@RequiredArgsConstructor
public final class JDBCDatabaseCommunicationEngine implements DatabaseCommunicationEngine {
    
    private final LogicSchema logicSchema;
    
    private final String sql;
    
    private final JDBCExecuteEngine executeEngine;
    
    private final DatabaseType databaseType = GlobalRegistry.getInstance().getDatabaseType();
    
    private ExecuteResponse executeResponse;
    
    private MergedResult mergedResult;
    
    private int currentSequenceId;
    
    @Override
    public BackendResponse execute() {
        try {
            SQLRouteResult routeResult = executeEngine.getJdbcExecutorWrapper().route(sql, databaseType);
            return execute(routeResult);
        } catch (final SQLException ex) {
            return new FailureResponse(ex);
        }
    }
    
    private BackendResponse execute(final SQLRouteResult routeResult) throws SQLException {
        if (routeResult.getRouteUnits().isEmpty()) {
            return new SuccessResponse();
        }
        SQLStatement sqlStatement = routeResult.getSqlStatement();
        if (isUnsupportedXA(sqlStatement.getType()) || isUnsupportedBASE(sqlStatement.getType())) {
            return new FailureResponse(
                    MySQLServerErrorCode.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE, sqlStatement.getTables().isSingleTable() ? sqlStatement.getTables().getSingleTableName() : "unknown_table");
        }
        executeResponse = executeEngine.execute(routeResult);
        if (logicSchema instanceof ShardingSchema) {
            logicSchema.refreshTableMetaData(routeResult.getSqlStatement());
        }
        return merge(sqlStatement);
    }
    
    private boolean isUnsupportedXA(final SQLType sqlType) {
        BackendConnection connection = executeEngine.getBackendConnection();
        return TransactionType.XA == connection.getTransactionType() && SQLType.DDL == sqlType && ConnectionStatus.TRANSACTION == connection.getStateHandler().getStatus();
    }
    
    private boolean isUnsupportedBASE(final SQLType sqlType) {
        BackendConnection connection = executeEngine.getBackendConnection();
        return TransactionType.BASE == connection.getTransactionType() && SQLType.DML != sqlType && ConnectionStatus.TRANSACTION == connection.getStateHandler().getStatus();
    }
    
    private BackendResponse merge(final SQLStatement sqlStatement) throws SQLException {
        if (executeResponse instanceof ExecuteUpdateResponse) {
            return ((ExecuteUpdateResponse) executeResponse).getBackendResponse(!isAllBroadcastTables(sqlStatement));
        }
        mergedResult = MergeEngineFactory.newInstance(
            databaseType, getShardingRule(), sqlStatement, logicSchema.getMetaData().getTable(), ((ExecuteQueryResponse) executeResponse).getQueryResults()).merge();
        if (mergedResult instanceof ShowTablesMergedResult) {
            ((ShowTablesMergedResult) mergedResult).resetColumnLabel(logicSchema.getName());
        }
        QueryHeaderResponse result = getQueryHeaderResponseWithoutDerivedColumns(((ExecuteQueryResponse) executeResponse).getQueryResponsePackets());
        currentSequenceId = result.getSequenceId();
        return result;
    }
    
    private boolean isAllBroadcastTables(final SQLStatement sqlStatement) {
        return logicSchema instanceof ShardingSchema && ((ShardingSchema) logicSchema).getShardingRule().isAllBroadcastTables(sqlStatement.getTables().getTableNames());
    }
    
    private ShardingRule getShardingRule() {
        return logicSchema instanceof ShardingSchema ? ((ShardingSchema) logicSchema).getShardingRule() : new ShardingRule(new ShardingRuleConfiguration(), logicSchema.getDataSources().keySet());
    }
    
    private QueryHeaderResponse getQueryHeaderResponseWithoutDerivedColumns(final QueryResponsePackets queryResponsePackets) {
        List<QueryHeader> dataHeaderPackets = new ArrayList<>(queryResponsePackets.getFieldCount());
        int columnCount = 0;
        for (DataHeaderPacket each : queryResponsePackets.getDataHeaderPackets()) {
            if (!DerivedColumn.isDerivedColumn(each.getName())) {
                QueryHeader queryHeader = new QueryHeader(each.getSequenceId(), each.getSchema(), each.getTable(), each.getOrgTable(), each.getName(), each.getOrgName(),
                        each.getColumnLength(), each.getColumnType(), each.getDecimals());
                dataHeaderPackets.add(queryHeader);
                columnCount++;
            }
        }
        return new QueryHeaderResponse(queryResponsePackets.getColumnTypes(), columnCount, dataHeaderPackets, columnCount + 2);
    }
    
    @Override
    public boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    @Override
    public ResultPacket getResultValue() throws SQLException {
        QueryResponsePackets queryResponsePackets = ((ExecuteQueryResponse) executeResponse).getQueryResponsePackets();
        int columnCount = queryResponsePackets.getFieldCount();
        List<Object> row = new ArrayList<>(columnCount);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            row.add(mergedResult.getValue(columnIndex, Object.class));
        }
        return new ResultPacket(++currentSequenceId, row, columnCount, queryResponsePackets.getColumnTypes());
    }
}
