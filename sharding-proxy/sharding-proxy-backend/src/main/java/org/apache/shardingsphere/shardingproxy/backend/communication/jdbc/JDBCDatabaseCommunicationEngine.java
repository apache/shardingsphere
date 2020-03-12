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
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.strategy.spi.Encryptor;
import org.apache.shardingsphere.sharding.merge.ShardingResultMergerEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.ConnectionStatus;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.JDBCExecuteEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.merge.ProxyResultDecoratorEngine;
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
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DDLStatement;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.underlying.common.constant.properties.PropertiesConstant;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.executor.QueryResult;
import org.apache.shardingsphere.underlying.executor.context.ExecutionContext;
import org.apache.shardingsphere.underlying.merge.MergeEntry;
import org.apache.shardingsphere.underlying.merge.engine.ResultProcessEngine;
import org.apache.shardingsphere.underlying.merge.result.MergedResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Database access engine for JDBC.
 */
@RequiredArgsConstructor
public final class JDBCDatabaseCommunicationEngine implements DatabaseCommunicationEngine {
    
    private final LogicSchema logicSchema;
    
    private final String sql;
    
    private final JDBCExecuteEngine executeEngine;
    
    private BackendResponse response;
    
    private MergedResult mergedResult;
    
    @Override
    public BackendResponse execute() {
        try {
            ExecutionContext executionContext = executeEngine.getJdbcExecutorWrapper().route(sql);
            return execute(executionContext);
        } catch (final SQLException ex) {
            return new ErrorResponse(ex);
        }
    }
    
    private BackendResponse execute(final ExecutionContext executionContext) throws SQLException {
        if (executionContext.getExecutionUnits().isEmpty()) {
            return new UpdateResponse();
        }
        SQLStatementContext sqlStatementContext = executionContext.getSqlStatementContext();
        if (isExecuteDDLInXATransaction(sqlStatementContext.getSqlStatement())) {
            return new ErrorResponse(new TableModifyInTransactionException(getTableName(sqlStatementContext)));
        }
        response = executeEngine.execute(executionContext);
        if (logicSchema instanceof ShardingSchema) {
            logicSchema.refreshTableMetaData(executionContext.getSqlStatementContext());
        }
        return merge(executionContext.getSqlStatementContext());
    }
    
    private boolean isExecuteDDLInXATransaction(final SQLStatement sqlStatement) {
        BackendConnection connection = executeEngine.getBackendConnection();
        return TransactionType.XA == connection.getTransactionType() && sqlStatement instanceof DDLStatement && ConnectionStatus.TRANSACTION == connection.getStateHandler().getStatus();
    }
    
    private String getTableName(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof TableAvailable) {
            if (((TableAvailable) sqlStatementContext).getAllTables().isEmpty()) {
                return "unknown_table";
            }
            return ((TableAvailable) sqlStatementContext).getAllTables().iterator().next().getTableName().getIdentifier().getValue();
        }
        return "unknown_table";
    }
    
    private BackendResponse merge(final SQLStatementContext sqlStatementContext) throws SQLException {
        if (response instanceof UpdateResponse) {
            mergeUpdateCount(sqlStatementContext);
            return response;
        }
        this.mergedResult = createMergedResult(sqlStatementContext, ((QueryResponse) response).getQueryResults());
        return response;
    }
    
    private void mergeUpdateCount(final SQLStatementContext sqlStatementContext) {
        if (!isAllBroadcastTables(sqlStatementContext)) {
            ((UpdateResponse) response).mergeUpdateCount();
        }
    }
    
    private boolean isAllBroadcastTables(final SQLStatementContext sqlStatementContext) {
        return logicSchema instanceof ShardingSchema && logicSchema.getShardingRule().isAllBroadcastTables(sqlStatementContext.getTablesContext().getTableNames());
    }
    
    private MergedResult createMergedResult(final SQLStatementContext sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        Map<BaseRule, ResultProcessEngine> engines = new HashMap<>(2, 1);
        engines.put(logicSchema.getShardingRule(), new ShardingResultMergerEngine());
        EncryptRule encryptRule = getEncryptRule();
        if (!encryptRule.getEncryptTableNames().isEmpty()) {
            engines.put(encryptRule, new ProxyResultDecoratorEngine(((QueryResponse) response).getQueryHeaders()));
        }
        MergeEntry mergeEntry = new MergeEntry(
                LogicSchemas.getInstance().getDatabaseType(), logicSchema.getMetaData().getTables(), ShardingProxyContext.getInstance().getProperties(), engines);
        return mergeEntry.process(queryResults, sqlStatementContext);
    }
    
    private EncryptRule getEncryptRule() {
        return logicSchema instanceof EncryptSchema ? ((EncryptSchema) logicSchema).getEncryptRule() : logicSchema.getShardingRule().getEncryptRule();
    }
    
    @Override
    public boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    @Override
    public QueryData getQueryData() throws SQLException {
        Optional<EncryptRule> encryptRule = findEncryptRule();
        boolean isQueryWithCipherColumn = ShardingProxyContext.getInstance().getProperties().getValue(PropertiesConstant.QUERY_WITH_CIPHER_COLUMN);
        List<QueryHeader> queryHeaders = ((QueryResponse) response).getQueryHeaders();
        List<Object> row = new ArrayList<>(queryHeaders.size());
        for (int columnIndex = 1; columnIndex <= queryHeaders.size(); columnIndex++) {
            Object value = mergedResult.getValue(columnIndex, Object.class);
            if (isQueryWithCipherColumn && encryptRule.isPresent()) {
                QueryHeader queryHeader = ((QueryResponse) response).getQueryHeaders().get(columnIndex - 1);
                Optional<Encryptor> encryptor = encryptRule.get().findEncryptor(queryHeader.getTable(), queryHeader.getColumnName());
                if (encryptor.isPresent()) {
                    value = encryptor.get().decrypt(getCiphertext(value));
                }
            }
            row.add(value);
        }
        return new QueryData(getColumnTypes(queryHeaders), row);
    }
    
    private Optional<EncryptRule> findEncryptRule() {
        if (logicSchema instanceof ShardingSchema) {
            return Optional.of(logicSchema.getShardingRule().getEncryptRule());
        }
        if (logicSchema instanceof EncryptSchema) {
            return Optional.of(((EncryptSchema) logicSchema).getEncryptRule());
        }
        return Optional.empty();
    }
    
    private String getCiphertext(final Object value) {
        return null == value ? null : value.toString();
    }
    
    private List<Integer> getColumnTypes(final List<QueryHeader> queryHeaders) {
        List<Integer> result = new ArrayList<>(queryHeaders.size());
        for (QueryHeader each : queryHeaders) {
            result.add(each.getColumnType());
        }
        return result;
    }
}
