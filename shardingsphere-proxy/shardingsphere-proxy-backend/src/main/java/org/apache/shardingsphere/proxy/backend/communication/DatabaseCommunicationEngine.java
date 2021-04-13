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

package org.apache.shardingsphere.proxy.backend.communication;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.engine.MetadataRefreshEngine;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.data.impl.BinaryQueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.impl.TextQueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeaderBuilder;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Database communication engine.
 */
@RequiredArgsConstructor
public final class DatabaseCommunicationEngine {
    
    private final String driverType;
    
    private final ShardingSphereMetaData metaData;
    
    private final LogicSQL logicSQL;
    
    private final ProxySQLExecutor proxySQLExecutor;
    
    private final KernelProcessor kernelProcessor;
    
    private List<QueryHeader> queryHeaders;
    
    private MergedResult mergedResult;
    
    private ProxyLockEngine proxyLockEngine;
    
    public DatabaseCommunicationEngine(final String driverType, final ShardingSphereMetaData metaData, final LogicSQL logicSQL, final BackendConnection backendConnection) {
        this.driverType = driverType;
        this.metaData = metaData;
        this.logicSQL = logicSQL;
        proxySQLExecutor = new ProxySQLExecutor(driverType, backendConnection);
        kernelProcessor = new KernelProcessor();
        proxyLockEngine = new ProxyLockEngine(proxySQLExecutor, new MetadataRefreshEngine(metaData, 
                ProxyContext.getInstance().getMetaDataContexts().getProps(), ProxyContext.getInstance().getLock().orElse(null)), backendConnection.getSchemaName());
    }
    
    /**
     * Execute to database.
     *
     * @return backend response
     * @throws SQLException SQL exception
     */
    public ResponseHeader execute() throws SQLException {
        ExecutionContext executionContext = kernelProcessor.generateExecutionContext(logicSQL, metaData, ProxyContext.getInstance().getMetaDataContexts().getProps());
        if (executionContext.getExecutionUnits().isEmpty()) {
            return new UpdateResponseHeader(executionContext.getSqlStatementContext().getSqlStatement());
        }
        proxySQLExecutor.checkExecutePrerequisites(executionContext);
        Collection<ExecuteResult> executeResults = proxyLockEngine.execute(executionContext);
        ExecuteResult executeResultSample = executeResults.iterator().next();
        return executeResultSample instanceof QueryResult
                ? processExecuteQuery(executionContext, executeResults.stream().map(each -> (QueryResult) each).collect(Collectors.toList()), (QueryResult) executeResultSample)
                : processExecuteUpdate(executionContext, executeResults.stream().map(each -> (UpdateResult) each).collect(Collectors.toList()));
    }
    
    private QueryResponseHeader processExecuteQuery(final ExecutionContext executionContext, final List<QueryResult> queryResults, final QueryResult queryResultSample) throws SQLException {
        queryHeaders = createQueryHeaders(executionContext, queryResultSample);
        mergedResult = mergeQuery(executionContext.getSqlStatementContext(), queryResults);
        return new QueryResponseHeader(queryHeaders);
    }
    
    private List<QueryHeader> createQueryHeaders(final ExecutionContext executionContext, final QueryResult queryResultSample) throws SQLException {
        int columnCount = getColumnCount(executionContext, queryResultSample);
        List<QueryHeader> result = new ArrayList<>(columnCount);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            result.add(createQueryHeader(executionContext, queryResultSample, metaData, columnIndex));
        }
        return result;
    }
    
    private QueryHeader createQueryHeader(final ExecutionContext executionContext,
                                          final QueryResult queryResultSample, final ShardingSphereMetaData metaData, final int columnIndex) throws SQLException {
        return hasSelectExpandProjections(executionContext.getSqlStatementContext())
                ? QueryHeaderBuilder.build(((SelectStatementContext) executionContext.getSqlStatementContext()).getProjectionsContext(), queryResultSample.getMetaData(), metaData, columnIndex)
                : QueryHeaderBuilder.build(queryResultSample.getMetaData(), metaData, columnIndex);
    }
    
    private int getColumnCount(final ExecutionContext executionContext, final QueryResult queryResultSample) throws SQLException {
        return hasSelectExpandProjections(executionContext.getSqlStatementContext())
                ? ((SelectStatementContext) executionContext.getSqlStatementContext()).getProjectionsContext().getExpandProjections().size() : queryResultSample.getMetaData().getColumnCount();
    }
    
    private boolean hasSelectExpandProjections(final SQLStatementContext<?> sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && !((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections().isEmpty();
    }
    
    private MergedResult mergeQuery(final SQLStatementContext<?> sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        MergeEngine mergeEngine = new MergeEngine(ProxyContext.getInstance().getMetaDataContexts().getMetaData(metaData.getName()).getResource().getDatabaseType(),
                metaData.getSchema(), ProxyContext.getInstance().getMetaDataContexts().getProps(), metaData.getRuleMetaData().getRules());
        return mergeEngine.merge(queryResults, sqlStatementContext);
    }
    
    private UpdateResponseHeader processExecuteUpdate(final ExecutionContext executionContext, final Collection<UpdateResult> updateResults) {
        UpdateResponseHeader result = new UpdateResponseHeader(executionContext.getSqlStatementContext().getSqlStatement(), updateResults);
        mergeUpdateCount(executionContext.getSqlStatementContext(), result);
        return result;
    }
    
    private void mergeUpdateCount(final SQLStatementContext<?> sqlStatementContext, final UpdateResponseHeader response) {
        if (isNeedAccumulate(sqlStatementContext)) {
            response.mergeUpdateCount();
        }
    }
    
    private boolean isNeedAccumulate(final SQLStatementContext<?> sqlStatementContext) {
        Optional<DataNodeContainedRule> dataNodeContainedRule = 
                metaData.getRuleMetaData().getRules().stream().filter(each -> each instanceof DataNodeContainedRule).findFirst().map(rule -> (DataNodeContainedRule) rule);
        return dataNodeContainedRule.isPresent() && dataNodeContainedRule.get().isNeedAccumulate(sqlStatementContext.getTablesContext().getTableNames());
    }
    
    /**
     * Goto next result value.
     *
     * @return has more result value or not
     * @throws SQLException SQL exception
     */
    public boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    /**
     * Get query response row.
     *
     * @return query response row
     * @throws SQLException SQL exception
     */
    public QueryResponseRow getQueryResponseRow() throws SQLException {
        List<QueryResponseCell> cells = new ArrayList<>(queryHeaders.size());
        boolean isBinary = isBinary();
        for (int columnIndex = 1; columnIndex <= queryHeaders.size(); columnIndex++) {
            Object data = mergedResult.getValue(columnIndex, Object.class);
            if (isBinary) {
                cells.add(new BinaryQueryResponseCell(queryHeaders.get(columnIndex - 1).getColumnType(), data));
            } else {
                cells.add(new TextQueryResponseCell(data));
            }
        }
        return new QueryResponseRow(cells);
    }
    
    private boolean isBinary() {
        return JDBCDriverType.PREPARED_STATEMENT.equals(driverType);
    }
}
