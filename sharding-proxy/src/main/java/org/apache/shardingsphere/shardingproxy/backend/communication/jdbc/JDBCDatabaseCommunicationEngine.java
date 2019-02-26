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
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.runtime.schema.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
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
    
    private BackendResponse response;
    
    private MergedResult mergedResult;
    
    @Override
    public BackendResponse execute() {
        try {
            SQLRouteResult routeResult = executeEngine.getJdbcExecutorWrapper().route(sql, databaseType);
            return execute(routeResult);
        } catch (final SQLException ex) {
            return new ErrorResponse(ex);
        }
    }
    
    private BackendResponse execute(final SQLRouteResult routeResult) throws SQLException {
        if (routeResult.getRouteUnits().isEmpty()) {
            return new UpdateResponse();
        }
        SQLStatement sqlStatement = routeResult.getSqlStatement();
        if (isUnsupportedXA(sqlStatement.getType()) || isUnsupportedBASE(sqlStatement.getType())) {
            return new ErrorResponse(
                    MySQLServerErrorCode.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE, sqlStatement.getTables().isSingleTable() ? sqlStatement.getTables().getSingleTableName() : "unknown_table");
        }
        response = executeEngine.execute(routeResult);
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
        if (response instanceof UpdateResponse) {
            if (!isAllBroadcastTables(sqlStatement)) {
                ((UpdateResponse) response).mergeUpdateCount();
            }
            return response;
        }
        mergedResult = MergeEngineFactory.newInstance(
            databaseType, getShardingRule(), sqlStatement, logicSchema.getMetaData().getTable(), ((QueryResponse) response).getQueryResults()).merge();
        if (mergedResult instanceof ShowTablesMergedResult) {
            ((ShowTablesMergedResult) mergedResult).resetColumnLabel(logicSchema.getName());
        }
        return getQueryHeaderResponseWithoutDerivedColumns(((QueryResponse) response).getQueryHeaders());
    }
    
    private boolean isAllBroadcastTables(final SQLStatement sqlStatement) {
        return logicSchema instanceof ShardingSchema && ((ShardingSchema) logicSchema).getShardingRule().isAllBroadcastTables(sqlStatement.getTables().getTableNames());
    }
    
    private ShardingRule getShardingRule() {
        return logicSchema instanceof ShardingSchema ? ((ShardingSchema) logicSchema).getShardingRule() : new ShardingRule(new ShardingRuleConfiguration(), logicSchema.getDataSources().keySet());
    }
    
    private QueryResponse getQueryHeaderResponseWithoutDerivedColumns(final List<QueryHeader> queryHeaders) {
        List<QueryHeader> derivedColumnQueryHeaders = new LinkedList<>();
        for (QueryHeader each : queryHeaders) {
            if (DerivedColumn.isDerivedColumn(each.getColumnLabel())) {
                derivedColumnQueryHeaders.add(each);
            }
        }
        queryHeaders.removeAll(derivedColumnQueryHeaders);
        return new QueryResponse(queryHeaders);
    }
    
    @Override
    public boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    @Override
    public QueryData getQueryData() throws SQLException {
        List<QueryHeader> queryHeaders = ((QueryResponse) response).getQueryHeaders();
        List<Object> row = new ArrayList<>(queryHeaders.size());
        for (int columnIndex = 1; columnIndex <= queryHeaders.size(); columnIndex++) {
            row.add(mergedResult.getValue(columnIndex, Object.class));
        }
        return new QueryData(getColumnTypes(queryHeaders), row);
    }
    
    private List<Integer> getColumnTypes(final List<QueryHeader> queryHeaders) {
        List<Integer> result = new ArrayList<>(queryHeaders.size());
        for (QueryHeader each : queryHeaders) {
            result.add(each.getColumnType());
        }
        return result;
    }
}
