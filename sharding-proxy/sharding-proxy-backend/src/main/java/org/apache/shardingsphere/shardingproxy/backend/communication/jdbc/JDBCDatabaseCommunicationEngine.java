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
import org.apache.shardingsphere.core.merge.MergeEngineFactory;
import org.apache.shardingsphere.core.merge.MergedResult;
import org.apache.shardingsphere.core.merge.dal.show.ShowTablesMergedResult;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.DerivedColumn;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DDLStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.ConnectionStatus;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.JDBCExecuteEngine;
import org.apache.shardingsphere.shardingproxy.backend.exception.TableModifyInTransactionException;
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchemas;
import org.apache.shardingsphere.shardingproxy.backend.schema.ShardingSchema;
import org.apache.shardingsphere.spi.database.DatabaseType;
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
    
    private final DatabaseType databaseType = LogicSchemas.getInstance().getDatabaseType();
    
    private final LogicSchema logicSchema;
    
    private final String sql;
    
    private final JDBCExecuteEngine executeEngine;
    
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
        OptimizedStatement optimizedStatement = routeResult.getOptimizedStatement();
        if (isExecuteDDLInXATransaction(optimizedStatement.getSQLStatement())) {
            return new ErrorResponse(new TableModifyInTransactionException(optimizedStatement.getTables().isSingleTable() ? optimizedStatement.getTables().getSingleTableName() : "unknown_table"));
        }
        response = executeEngine.execute(routeResult);
        if (logicSchema instanceof ShardingSchema) {
            logicSchema.refreshTableMetaData(routeResult.getOptimizedStatement());
        }
        return merge(routeResult);
    }
    
    private boolean isExecuteDDLInXATransaction(final SQLStatement sqlStatement) {
        BackendConnection connection = executeEngine.getBackendConnection();
        return TransactionType.XA == connection.getTransactionType() && sqlStatement instanceof DDLStatement && ConnectionStatus.TRANSACTION == connection.getStateHandler().getStatus();
    }
    
    private BackendResponse merge(final SQLRouteResult routeResult) throws SQLException {
        if (response instanceof UpdateResponse) {
            if (!isAllBroadcastTables(routeResult.getOptimizedStatement())) {
                ((UpdateResponse) response).mergeUpdateCount();
            }
            return response;
        }
        mergedResult = MergeEngineFactory.newInstance(databaseType, 
                logicSchema.getShardingRule(), routeResult, logicSchema.getMetaData().getTable(), ((QueryResponse) response).getQueryResults()).merge();
        if (mergedResult instanceof ShowTablesMergedResult) {
            ((ShowTablesMergedResult) mergedResult).resetColumnLabel(logicSchema.getName());
        }
        return getQueryHeaderResponseWithoutDerivedColumns(((QueryResponse) response).getQueryHeaders());
    }
    
    private boolean isAllBroadcastTables(final OptimizedStatement optimizedStatement) {
        return logicSchema instanceof ShardingSchema && logicSchema.getShardingRule().isAllBroadcastTables(optimizedStatement.getTables().getTableNames());
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
