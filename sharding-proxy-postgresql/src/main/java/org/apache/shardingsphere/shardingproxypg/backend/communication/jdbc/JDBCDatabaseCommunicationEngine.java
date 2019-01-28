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

package org.apache.shardingsphere.shardingproxypg.backend.communication.jdbc;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.SQLType;
import org.apache.shardingsphere.core.merger.MergeEngineFactory;
import org.apache.shardingsphere.core.merger.MergedResult;
import org.apache.shardingsphere.core.merger.dal.show.ShowTablesMergedResult;
import org.apache.shardingsphere.core.parsing.parser.constant.DerivedColumn;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.routing.SQLRouteResult;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingproxypg.backend.ResultPacket;
import org.apache.shardingsphere.shardingproxypg.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxypg.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxypg.backend.communication.jdbc.connection.ConnectionStatus;
import org.apache.shardingsphere.shardingproxypg.backend.communication.jdbc.execute.JDBCExecuteEngine;
import org.apache.shardingsphere.shardingproxypg.backend.communication.jdbc.execute.response.ExecuteQueryResponse;
import org.apache.shardingsphere.shardingproxypg.backend.communication.jdbc.execute.response.ExecuteResponse;
import org.apache.shardingsphere.shardingproxypg.backend.communication.jdbc.execute.response.ExecuteUpdateResponse;
import org.apache.shardingsphere.shardingproxypg.runtime.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxypg.runtime.schema.ShardingSchema;
import org.apache.shardingsphere.shardingproxypg.transport.mysql.packet.command.query.ColumnDefinition41Packet;
import org.apache.shardingsphere.shardingproxypg.transport.mysql.packet.command.query.FieldCountPacket;
import org.apache.shardingsphere.shardingproxypg.transport.mysql.packet.generic.EofPacket;
import org.apache.shardingsphere.shardingproxypg.transport.mysql.packet.generic.OKPacket;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.PostgreSQLCommandResponsePackets;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.query.PostgreSQLQueryResponsePackets;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.generic.ErrorResponse;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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
    
    private ExecuteResponse executeResponse;
    
    private MergedResult mergedResult;
    
    private int currentSequenceId;
    
    @Override
    public PostgreSQLCommandResponsePackets execute() {
        try {
            return execute(executeEngine.getJdbcExecutorWrapper().route(sql, DatabaseType.PostgreSQL));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            return new PostgreSQLCommandResponsePackets(ex);
        }
    }
    
    private PostgreSQLCommandResponsePackets execute(final SQLRouteResult routeResult) throws SQLException {
        if (routeResult.getRouteUnits().isEmpty()) {
            return new PostgreSQLCommandResponsePackets(new OKPacket(1));
        }
        SQLStatement sqlStatement = routeResult.getSqlStatement();
        if (isUnsupportedXA(sqlStatement.getType())) {
            return new PostgreSQLCommandResponsePackets(new ErrorResponse());
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
    
    private PostgreSQLCommandResponsePackets merge(final SQLStatement sqlStatement) throws SQLException {
        if (executeResponse instanceof ExecuteUpdateResponse) {
            if (logicSchema instanceof ShardingSchema && ((ShardingSchema) logicSchema).getShardingRule().isAllBroadcastTables(sqlStatement.getTables().getTableNames())) {
                return new PostgreSQLCommandResponsePackets(((ExecuteUpdateResponse) executeResponse).getPackets().get(0));
            }
            return ((ExecuteUpdateResponse) executeResponse).merge();
        }
        mergedResult = MergeEngineFactory.newInstance(
                DatabaseType.PostgreSQL, getShardingRule(), sqlStatement, logicSchema.getMetaData().getTable(), ((ExecuteQueryResponse) executeResponse).getQueryResults()).merge();
        if (mergedResult instanceof ShowTablesMergedResult) {
            ((ShowTablesMergedResult) mergedResult).resetColumnLabel(logicSchema.getName());
            setResponseColumnLabelForShowTablesMergedResult(((ExecuteQueryResponse) executeResponse).getPostgreSQLQueryResponsePackets());
        }
        PostgreSQLQueryResponsePackets result = getQueryResponsePacketsWithoutDerivedColumns(((ExecuteQueryResponse) executeResponse).getPostgreSQLQueryResponsePackets());
        currentSequenceId = result.getPackets().size();
        return result;
    }
    
    private ShardingRule getShardingRule() {
        return logicSchema instanceof ShardingSchema ? ((ShardingSchema) logicSchema).getShardingRule() : new ShardingRule(new ShardingRuleConfiguration(), logicSchema.getDataSources().keySet());
    }
    
    private PostgreSQLQueryResponsePackets getQueryResponsePacketsWithoutDerivedColumns(final PostgreSQLQueryResponsePackets postgreSQLQueryResponsePackets) {
        Collection<ColumnDefinition41Packet> columnDefinition41Packets = new ArrayList<>(postgreSQLQueryResponsePackets.getColumnCount());
        int columnCount = 0;
        for (ColumnDefinition41Packet each : postgreSQLQueryResponsePackets.getColumnDefinition41Packets()) {
            if (!DerivedColumn.isDerivedColumn(each.getName())) {
                columnDefinition41Packets.add(each);
                columnCount++;
            }
        }
        FieldCountPacket fieldCountPacket = new FieldCountPacket(1, columnCount);
        return new PostgreSQLQueryResponsePackets(fieldCountPacket, columnDefinition41Packets, new EofPacket(columnCount + 2));
    }
    
    private void setResponseColumnLabelForShowTablesMergedResult(final PostgreSQLQueryResponsePackets postgreSQLQueryResponsePackets) {
        for (ColumnDefinition41Packet each : postgreSQLQueryResponsePackets.getColumnDefinition41Packets()) {
            if (each.getName().startsWith("Tables_in_")) {
                each.setName("Tables_in_" + logicSchema.getName());
                break;
            }
        }
    }
    
    @Override
    public boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    @Override
    public ResultPacket getResultValue() throws SQLException {
        PostgreSQLQueryResponsePackets postgreSQLQueryResponsePackets = ((ExecuteQueryResponse) executeResponse).getPostgreSQLQueryResponsePackets();
        int columnCount = postgreSQLQueryResponsePackets.getColumnCount();
        List<Object> data = new ArrayList<>(columnCount);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            data.add(mergedResult.getValue(columnIndex, Object.class));
        }
        return new ResultPacket(++currentSequenceId, data, columnCount, postgreSQLQueryResponsePackets.getColumnTypes());
    }
}
