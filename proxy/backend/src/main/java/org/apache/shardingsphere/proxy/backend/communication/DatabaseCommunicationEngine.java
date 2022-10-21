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

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.aware.CursorDefinitionAware;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.type.CursorAvailable;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefreshEngine;
import org.apache.shardingsphere.infra.distsql.exception.resource.EmptyResourceException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.event.MetaDataRefreshedEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtil;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.handler.data.DatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilderEngine;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Database communication engine.
 */
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
public abstract class DatabaseCommunicationEngine implements DatabaseBackendHandler {
    
    private final String driverType;
    
    private final ShardingSphereDatabase database;
    
    private final QueryContext queryContext;
    
    private final KernelProcessor kernelProcessor = new KernelProcessor();
    
    private final MetaDataRefreshEngine metadataRefreshEngine;
    
    private List<QueryHeader> queryHeaders;
    
    private MergedResult mergedResult;
    
    private final BackendConnection<?> backendConnection;
    
    public DatabaseCommunicationEngine(final String driverType, final ShardingSphereDatabase database, final QueryContext queryContext, final BackendConnection<?> backendConnection) {
        SQLStatementContext<?> sqlStatementContext = queryContext.getSqlStatementContext();
        failedIfBackendNotReady(backendConnection.getConnectionSession(), sqlStatementContext);
        this.driverType = driverType;
        this.database = database;
        this.queryContext = queryContext;
        this.backendConnection = backendConnection;
        metadataRefreshEngine = new MetaDataRefreshEngine(database, ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps());
        if (sqlStatementContext instanceof CursorAvailable) {
            prepareCursorStatementContext((CursorAvailable) sqlStatementContext, backendConnection.getConnectionSession());
        }
    }
    
    private void failedIfBackendNotReady(final ConnectionSession connectionSession, final SQLStatementContext<?> sqlStatementContext) {
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(connectionSession.getDatabaseName());
        boolean isSystemSchema = SystemSchemaUtil.containsSystemSchema(sqlStatementContext.getDatabaseType(), sqlStatementContext.getTablesContext().getSchemaNames(), database);
        ShardingSpherePreconditions.checkState(isSystemSchema || database.containsDataSource(), () -> new EmptyResourceException(connectionSession.getDatabaseName()));
        if (!isSystemSchema && !database.isComplete()) {
            throw new RuleNotExistedException();
        }
    }
    
    private void prepareCursorStatementContext(final CursorAvailable statementContext, final ConnectionSession connectionSession) {
        if (statementContext.getCursorName().isPresent()) {
            String cursorName = statementContext.getCursorName().get().getIdentifier().getValue().toLowerCase();
            prepareCursorStatementContext(statementContext, connectionSession, cursorName);
        }
        if (statementContext instanceof CloseStatementContext && ((CloseStatementContext) statementContext).getSqlStatement().isCloseAll()) {
            connectionSession.getConnectionContext().clearCursorConnectionContext();
        }
    }
    
    private void prepareCursorStatementContext(final CursorAvailable statementContext, final ConnectionSession connectionSession, final String cursorName) {
        if (statementContext instanceof CursorStatementContext) {
            connectionSession.getConnectionContext().getCursorConnectionContext().getCursorDefinitions().put(cursorName, (CursorStatementContext) statementContext);
        }
        if (statementContext instanceof CursorDefinitionAware) {
            CursorStatementContext cursorStatementContext = (CursorStatementContext) connectionSession.getConnectionContext().getCursorConnectionContext().getCursorDefinitions().get(cursorName);
            Preconditions.checkArgument(null != cursorStatementContext, "Cursor %s does not exist.", cursorName);
            ((CursorDefinitionAware) statementContext).setUpCursorDefinition(cursorStatementContext);
        }
        if (statementContext instanceof CloseStatementContext) {
            connectionSession.getConnectionContext().getCursorConnectionContext().removeCursorName(cursorName);
        }
    }
    
    protected void refreshMetaData(final ExecutionContext executionContext) throws SQLException {
        Optional<MetaDataRefreshedEvent> event = metadataRefreshEngine.refresh(executionContext.getSqlStatementContext(), executionContext.getRouteContext().getRouteUnits());
        if (ProxyContext.getInstance().getContextManager().getInstanceContext().isCluster() && event.isPresent()) {
            ProxyContext.getInstance().getContextManager().getInstanceContext().getEventBusContext().post(event.get());
        }
    }
    
    protected QueryResponseHeader processExecuteQuery(final ExecutionContext executionContext, final List<QueryResult> queryResults, final QueryResult queryResultSample) throws SQLException {
        queryHeaders = createQueryHeaders(executionContext, queryResultSample);
        mergedResult = mergeQuery(executionContext.getSqlStatementContext(), queryResults);
        return new QueryResponseHeader(queryHeaders);
    }
    
    protected List<QueryHeader> createQueryHeaders(final ExecutionContext executionContext, final QueryResult queryResultSample) throws SQLException {
        int columnCount = getColumnCount(executionContext, queryResultSample);
        List<QueryHeader> result = new ArrayList<>(columnCount);
        QueryHeaderBuilderEngine queryHeaderBuilderEngine = new QueryHeaderBuilderEngine(database.getProtocolType());
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            result.add(createQueryHeader(queryHeaderBuilderEngine, executionContext, queryResultSample, database, columnIndex));
        }
        return result;
    }
    
    protected QueryHeader createQueryHeader(final QueryHeaderBuilderEngine queryHeaderBuilderEngine, final ExecutionContext executionContext,
                                            final QueryResult queryResultSample, final ShardingSphereDatabase database, final int columnIndex) throws SQLException {
        return hasSelectExpandProjections(executionContext.getSqlStatementContext()) ? queryHeaderBuilderEngine.build(
                ((SelectStatementContext) executionContext.getSqlStatementContext()).getProjectionsContext(), queryResultSample.getMetaData(), database, columnIndex)
                : queryHeaderBuilderEngine.build(queryResultSample.getMetaData(), database, columnIndex);
    }
    
    protected int getColumnCount(final ExecutionContext executionContext, final QueryResult queryResultSample) throws SQLException {
        return hasSelectExpandProjections(executionContext.getSqlStatementContext())
                ? ((SelectStatementContext) executionContext.getSqlStatementContext()).getProjectionsContext().getExpandProjections().size()
                : queryResultSample.getMetaData().getColumnCount();
    }
    
    protected boolean hasSelectExpandProjections(final SQLStatementContext<?> sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && !((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections().isEmpty();
    }
    
    protected MergedResult mergeQuery(final SQLStatementContext<?> sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        MergeEngine mergeEngine = new MergeEngine(database, ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps(),
                getBackendConnection().getConnectionSession().getConnectionContext());
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
        Optional<DataNodeContainedRule> dataNodeContainedRule = findDataNodeContainedRule();
        return dataNodeContainedRule.isPresent() && dataNodeContainedRule.get().isNeedAccumulate(sqlStatementContext.getTablesContext().getTableNames());
    }
    
    private Optional<DataNodeContainedRule> findDataNodeContainedRule() {
        for (ShardingSphereRule each : database.getRuleMetaData().getRules()) {
            if (each instanceof DataNodeContainedRule) {
                return Optional.of((DataNodeContainedRule) each);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Goto next result value.
     *
     * @return has more result value or not
     * @throws SQLException SQL exception
     */
    @Override
    public boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    /**
     * Get query response row.
     *
     * @return query response row
     * @throws SQLException SQL exception
     */
    @Override
    public QueryResponseRow getRowData() throws SQLException {
        List<QueryResponseCell> cells = new ArrayList<>(queryHeaders.size());
        for (int columnIndex = 1; columnIndex <= queryHeaders.size(); columnIndex++) {
            Object data = mergedResult.getValue(columnIndex, Object.class);
            cells.add(new QueryResponseCell(queryHeaders.get(columnIndex - 1).getColumnType(), data));
        }
        return new QueryResponseRow(cells);
    }
}
