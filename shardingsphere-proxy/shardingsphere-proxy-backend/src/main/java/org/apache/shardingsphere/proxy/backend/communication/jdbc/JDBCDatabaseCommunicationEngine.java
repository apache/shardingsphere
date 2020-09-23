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

package org.apache.shardingsphere.proxy.backend.communication.jdbc;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.governance.core.event.model.persist.MetaDataPersistEvent;
import org.apache.shardingsphere.governance.core.event.GovernanceEventBus;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.log.SQLLogger;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.query.QueryHeader;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.refresh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.refresh.MetaDataRefreshStrategyFactory;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaDataLoader;
import org.apache.shardingsphere.infra.rule.DataNodeRoutedRule;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.SQLExecuteEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.infra.context.sql.LogicSQLContext;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryData;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Database access engine for JDBC.
 */
@RequiredArgsConstructor
public final class JDBCDatabaseCommunicationEngine implements DatabaseCommunicationEngine {
    
    private final LogicSQLContext logicSQLContext;
    
    private final SQLExecuteEngine sqlExecuteEngine;
    
    private BackendResponse response;
    
    private MergedResult mergedResult;
    
    @Override
    public BackendResponse execute() throws SQLException {
        ExecutionContext executionContext = new KernelProcessor().generateExecutionContext(logicSQLContext, ProxyContext.getInstance().getSchemaContexts().getProps());
        logSQL(executionContext);
        return doExecute(executionContext);
    }
    
    private void logSQL(final ExecutionContext executionContext) {
        if (ProxyContext.getInstance().getSchemaContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)) {
            SQLLogger.logSQL(logicSQLContext.getSql(), ProxyContext.getInstance().getSchemaContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SIMPLE), executionContext);
        }
    }
    
    private BackendResponse doExecute(final ExecutionContext executionContext) throws SQLException {
        if (executionContext.getExecutionUnits().isEmpty()) {
            return new UpdateResponse();
        }
        sqlExecuteEngine.checkExecutePrerequisites(executionContext);
        response = sqlExecuteEngine.execute(executionContext);
        refreshTableMetaData(executionContext.getSqlStatementContext());
        return merge(executionContext.getSqlStatementContext());
    }
    
    @SuppressWarnings("unchecked")
    private void refreshTableMetaData(final SQLStatementContext<?> sqlStatementContext) throws SQLException {
        if (null == sqlStatementContext) {
            return;
        }
        Optional<MetaDataRefreshStrategy> refreshStrategy = MetaDataRefreshStrategyFactory.newInstance(sqlStatementContext);
        if (refreshStrategy.isPresent()) {
            refreshStrategy.get().refreshMetaData(logicSQLContext.getSchemaContext().getSchema().getMetaData(), ProxyContext.getInstance().getSchemaContexts().getDatabaseType(),
                    logicSQLContext.getSchemaContext().getSchema().getDataSources(), sqlStatementContext, this::loadTableMetaData);
            GovernanceEventBus.getInstance().post(
                    new MetaDataPersistEvent(logicSQLContext.getSchemaContext().getName(), logicSQLContext.getSchemaContext().getSchema().getMetaData().getRuleSchemaMetaData()));
        }
    }
    
    private Optional<TableMetaData> loadTableMetaData(final String tableName) throws SQLException {
        RuleSchemaMetaDataLoader loader = new RuleSchemaMetaDataLoader(logicSQLContext.getSchemaContext().getSchema().getRules());
        return loader.load(ProxyContext.getInstance().getSchemaContexts().getDatabaseType(),
                logicSQLContext.getSchemaContext().getSchema().getDataSources(), tableName, ProxyContext.getInstance().getSchemaContexts().getProps());
    }
    
    private BackendResponse merge(final SQLStatementContext<?> sqlStatementContext) throws SQLException {
        if (response instanceof UpdateResponse) {
            mergeUpdateCount(sqlStatementContext);
            return response;
        }
        mergedResult = mergeQuery(sqlStatementContext, ((QueryResponse) response).getQueryResults());
        return response;
    }
    
    private void mergeUpdateCount(final SQLStatementContext<?> sqlStatementContext) {
        if (isNeedAccumulate(sqlStatementContext)) {
            ((UpdateResponse) response).mergeUpdateCount();
        }
    }
    
    private boolean isNeedAccumulate(final SQLStatementContext<?> sqlStatementContext) {
        Optional<DataNodeRoutedRule> dataNodeRoutedRule =
                logicSQLContext.getSchemaContext().getSchema().getRules().stream().filter(each -> each instanceof DataNodeRoutedRule).findFirst().map(rule -> (DataNodeRoutedRule) rule);
        return dataNodeRoutedRule.isPresent() && dataNodeRoutedRule.get().isNeedAccumulate(sqlStatementContext.getTablesContext().getTableNames());
    }
    
    private MergedResult mergeQuery(final SQLStatementContext<?> sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        MergeEngine mergeEngine = new MergeEngine(ProxyContext.getInstance().getSchemaContexts().getDatabaseType(),
                logicSQLContext.getSchemaContext().getSchema().getMetaData().getRuleSchemaMetaData().getConfiguredSchemaMetaData(), 
                ProxyContext.getInstance().getSchemaContexts().getProps(), logicSQLContext.getSchemaContext().getSchema().getRules());
        return mergeEngine.merge(queryResults, sqlStatementContext);
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
