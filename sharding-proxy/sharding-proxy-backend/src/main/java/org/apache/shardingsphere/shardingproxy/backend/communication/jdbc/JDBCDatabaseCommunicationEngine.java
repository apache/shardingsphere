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
import org.apache.shardingsphere.core.optimize.sharding.statement.ShardingOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DDLStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.rule.EncryptRule;
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
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.EncryptSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.impl.ShardingSchema;
import org.apache.shardingsphere.spi.database.DatabaseType;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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
        ShardingOptimizedStatement shardingStatement = routeResult.getShardingStatement();
        if (isExecuteDDLInXATransaction(shardingStatement.getSQLStatement())) {
            return new ErrorResponse(new TableModifyInTransactionException(shardingStatement.getTables().isSingleTable() ? shardingStatement.getTables().getSingleTableName() : "unknown_table"));
        }
        response = executeEngine.execute(routeResult);
        if (logicSchema instanceof ShardingSchema) {
            logicSchema.refreshTableMetaData(routeResult.getShardingStatement());
        }
        return merge(routeResult);
    }
    
    private boolean isExecuteDDLInXATransaction(final SQLStatement sqlStatement) {
        BackendConnection connection = executeEngine.getBackendConnection();
        return TransactionType.XA == connection.getTransactionType() && sqlStatement instanceof DDLStatement && ConnectionStatus.TRANSACTION == connection.getStateHandler().getStatus();
    }
    
    private BackendResponse merge(final SQLRouteResult routeResult) throws SQLException {
        if (response instanceof UpdateResponse) {
            mergeUpdateCount(routeResult);
            return response;
        }
        setMergedResult(routeResult);
        resetColumnLabelForShowTablesMergedResult();
        handleColumnsForQueryHeader(routeResult);
        return response;
    }
    
    private void mergeUpdateCount(final SQLRouteResult routeResult) {
        if (!isAllBroadcastTables(routeResult.getShardingStatement())) {
            ((UpdateResponse) response).mergeUpdateCount();
        }
    }
    
    private boolean isAllBroadcastTables(final OptimizedStatement optimizedStatement) {
        return logicSchema instanceof ShardingSchema && logicSchema.getShardingRule().isAllBroadcastTables(optimizedStatement.getTables().getTableNames());
    }
    
    private void setMergedResult(final SQLRouteResult routeResult) throws SQLException {
        mergedResult = MergeEngineFactory.newInstance(databaseType,
                logicSchema.getShardingRule(), routeResult, logicSchema.getMetaData().getTables(), ((QueryResponse) response).getQueryResults()).merge();
    }
    
    private void resetColumnLabelForShowTablesMergedResult() {
        if (mergedResult instanceof ShowTablesMergedResult) {
            ((ShowTablesMergedResult) mergedResult).resetColumnLabel(logicSchema.getName());
        }
    }
    
    private void handleColumnsForQueryHeader(final SQLRouteResult routeResult) {
        removeDerivedColumns();
        removeAssistedQueryColumns(routeResult);
        setLogicColumns();
    } 
    
    private void removeDerivedColumns() {
        List<QueryHeader> toRemove = new LinkedList<>();
        List<QueryHeader> queryHeaders = ((QueryResponse) response).getQueryHeaders();
        for (QueryHeader each : queryHeaders) {
            if (DerivedColumn.isDerivedColumn(each.getColumnLabel())) {
                toRemove.add(each);
            }
        }
        queryHeaders.removeAll(toRemove);
    }
    
    private void removeAssistedQueryColumns(final SQLRouteResult routeResult) {
        List<QueryHeader> toRemove = new LinkedList<>();
        List<QueryHeader> queryHeaders = ((QueryResponse) response).getQueryHeaders();
        Collection<String> assistedQueryColumns = getAssistedQueryColumns(routeResult);
        for (QueryHeader each : queryHeaders) {
            if (assistedQueryColumns.contains(each.getColumnName())) {
                toRemove.add(each);
            }
        }
        queryHeaders.removeAll(toRemove);
    }
    
    private Collection<String> getAssistedQueryColumns(final SQLRouteResult routeResult) {
        Collection<String> result = new LinkedList<>();
        EncryptRule encryptRule = getEncryptRule();
        for (String each : routeResult.getShardingStatement().getTables().getTableNames()) {
            result.addAll(encryptRule.getAssistedQueryColumns(each));
        }
        return result;
    }
    
    private EncryptRule getEncryptRule() {
        return logicSchema instanceof EncryptSchema ? ((EncryptSchema) logicSchema).getEncryptRule() : logicSchema.getShardingRule().getEncryptRule();
    }
    
    private void setLogicColumns() {
        List<QueryHeader> queryHeaders = ((QueryResponse) response).getQueryHeaders();
        EncryptRule encryptRule = getEncryptRule();
        for (QueryHeader each : queryHeaders) {
            if (encryptRule.isCipherColumn(each.getTable(), each.getColumnName())) {
                each.setColumnLabelAndName(encryptRule.getLogicColumn(each.getTable(), each.getColumnName()));
            }
        }
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
