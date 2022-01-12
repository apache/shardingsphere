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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefreshEngine;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.data.impl.BinaryQueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.impl.TextQueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeaderBuilder;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Database communication engine.
 *
 * @param <T> type of execute result
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
public abstract class DatabaseCommunicationEngine<T> {
    
    private final String driverType;
    
    private final ShardingSphereMetaData metaData;
    
    private final LogicSQL logicSQL;
    
    private final KernelProcessor kernelProcessor = new KernelProcessor();
    
    private final MetaDataRefreshEngine metadataRefreshEngine;
    
    private List<QueryHeader> queryHeaders;
    
    private MergedResult mergedResult;
    
    private final BackendConnection<?> backendConnection;
    
    public DatabaseCommunicationEngine(final String driverType, final ShardingSphereMetaData metaData, final LogicSQL logicSQL, final BackendConnection<?> backendConnection) {
        this.driverType = driverType;
        this.metaData = metaData;
        this.logicSQL = logicSQL;
        this.backendConnection = backendConnection;
        String schemaName = backendConnection.getConnectionSession().getSchemaName();
        metadataRefreshEngine = new MetaDataRefreshEngine(metaData,
                ProxyContext.getInstance().getContextManager().getMetaDataContexts().getOptimizerContext().getFederationMetaData().getSchemas().get(schemaName),
                ProxyContext.getInstance().getContextManager().getMetaDataContexts().getOptimizerContext().getPlannerContexts(),
                ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps());
    }
    
    /**
     * Execute.
     *
     * @return execute result
     */
    public abstract T execute();
    
    protected void refreshMetaData(final ExecutionContext executionContext) throws SQLException {
        SQLStatement sqlStatement = executionContext.getSqlStatementContext().getSqlStatement();
        metadataRefreshEngine.refresh(sqlStatement, executionContext.getRouteContext().getRouteUnits().stream().map(each -> each.getDataSourceMapper().getLogicName()).collect(Collectors.toList()));
    }
    
    protected QueryResponseHeader processExecuteQuery(final ExecutionContext executionContext, final List<QueryResult> queryResults, final QueryResult queryResultSample) throws SQLException {
        queryHeaders = createQueryHeaders(executionContext, queryResultSample);
        mergedResult = mergeQuery(executionContext.getSqlStatementContext(), queryResults);
        return new QueryResponseHeader(queryHeaders);
    }
    
    protected List<QueryHeader> createQueryHeaders(final ExecutionContext executionContext, final QueryResult queryResultSample) throws SQLException {
        int columnCount = getColumnCount(executionContext, queryResultSample);
        List<QueryHeader> result = new ArrayList<>(columnCount);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            result.add(createQueryHeader(executionContext, queryResultSample, metaData, columnIndex));
        }
        return result;
    }
    
    protected QueryHeader createQueryHeader(final ExecutionContext executionContext,
                                            final QueryResult queryResultSample, final ShardingSphereMetaData metaData, final int columnIndex) throws SQLException {
        return hasSelectExpandProjections(executionContext.getSqlStatementContext())
                ? QueryHeaderBuilder.build(((SelectStatementContext) executionContext.getSqlStatementContext()).getProjectionsContext(), queryResultSample.getMetaData(), metaData, columnIndex)
                : QueryHeaderBuilder.build(queryResultSample.getMetaData(), metaData, columnIndex);
    }
    
    protected int getColumnCount(final ExecutionContext executionContext, final QueryResult queryResultSample) throws SQLException {
        return hasSelectExpandProjections(executionContext.getSqlStatementContext())
                ? ((SelectStatementContext) executionContext.getSqlStatementContext()).getProjectionsContext().getExpandProjections().size() : queryResultSample.getMetaData().getColumnCount();
    }
    
    protected boolean hasSelectExpandProjections(final SQLStatementContext<?> sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && !((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections().isEmpty();
    }
    
    protected MergedResult mergeQuery(final SQLStatementContext<?> sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        MergeEngine mergeEngine = new MergeEngine(DefaultSchema.LOGIC_NAME,
                ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(metaData.getName()).getResource().getDatabaseType(),
                metaData.getSchema(), ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps(), metaData.getRuleMetaData().getRules());
        return mergeEngine.merge(queryResults, sqlStatementContext);
    }
    
    protected UpdateResponseHeader processExecuteUpdate(final ExecutionContext executionContext, final Collection<UpdateResult> updateResults) {
        UpdateResponseHeader result = new UpdateResponseHeader(executionContext.getSqlStatementContext().getSqlStatement(), updateResults);
        mergeUpdateCount(executionContext.getSqlStatementContext(), result);
        return result;
    }
    
    protected void mergeUpdateCount(final SQLStatementContext<?> sqlStatementContext, final UpdateResponseHeader response) {
        if (isNeedAccumulate(sqlStatementContext)) {
            response.mergeUpdateCount();
        }
    }
    
    protected boolean isNeedAccumulate(final SQLStatementContext<?> sqlStatementContext) {
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
    
    protected boolean isBinary() {
        return !JDBCDriverType.STATEMENT.equals(driverType);
    }
}
