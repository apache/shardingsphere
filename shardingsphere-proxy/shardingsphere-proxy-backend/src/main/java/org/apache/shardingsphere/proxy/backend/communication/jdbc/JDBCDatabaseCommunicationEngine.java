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
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.executor.sql.result.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.log.SQLLogger;
import org.apache.shardingsphere.infra.executor.sql.execute.raw.execute.result.query.QueryHeader;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresherFactory;
import org.apache.shardingsphere.infra.metadata.schema.refresher.spi.SchemaChangedNotifier;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.SQLExecuteEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryData;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Database access engine for JDBC.
 */
@RequiredArgsConstructor
public final class JDBCDatabaseCommunicationEngine implements DatabaseCommunicationEngine {
    
    private final LogicSQL logicSQL;
    
    private final ShardingSphereMetaData metaData;
    
    private final SQLExecuteEngine sqlExecuteEngine;
    
    private final KernelProcessor kernelProcessor = new KernelProcessor();
    
    private BackendResponse response;
    
    private MergedResult mergedResult;
    
    @Override
    public BackendResponse execute() throws SQLException {
        ExecutionContext executionContext = kernelProcessor.generateExecutionContext(logicSQL, metaData, ProxyContext.getInstance().getMetaDataContexts().getProps());
        logSQL(executionContext);
        return doExecute(executionContext);
    }
    
    private void logSQL(final ExecutionContext executionContext) {
        if (ProxyContext.getInstance().getMetaDataContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)) {
            SQLLogger.logSQL(logicSQL, ProxyContext.getInstance().getMetaDataContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SIMPLE), executionContext);
        }
    }
    
    private BackendResponse doExecute(final ExecutionContext executionContext) throws SQLException {
        if (executionContext.getExecutionUnits().isEmpty()) {
            return new UpdateResponse();
        }
        sqlExecuteEngine.checkExecutePrerequisites(executionContext);
        response = sqlExecuteEngine.execute(executionContext);
        refreshSchema(executionContext);
        return merge(executionContext.getSqlStatementContext());
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void refreshSchema(final ExecutionContext executionContext) throws SQLException {
        SQLStatement sqlStatement = executionContext.getSqlStatementContext().getSqlStatement();
        if (null == sqlStatement) {
            return;
        }
        Optional<SchemaRefresher> schemaRefresher = SchemaRefresherFactory.newInstance(sqlStatement);
        if (schemaRefresher.isPresent()) {
            Collection<String> routeDataSourceNames = executionContext.getRouteContext().getRouteUnits().stream().map(each -> each.getDataSourceMapper().getLogicName()).collect(Collectors.toList());
            SchemaBuilderMaterials materials = new SchemaBuilderMaterials(ProxyContext.getInstance().getMetaDataContexts().getDatabaseType(), 
                    metaData.getResource().getDataSources(), metaData.getRuleMetaData().getRules(), ProxyContext.getInstance().getMetaDataContexts().getProps());
            schemaRefresher.get().refresh(metaData.getSchema(), routeDataSourceNames, sqlStatement, materials);
            notifySchemaChanged(metaData.getName(), metaData.getSchema());
        }
    }
    
    private void notifySchemaChanged(final String schemaName, final ShardingSphereSchema schema) {
        OrderedSPIRegistry.getRegisteredServices(Collections.singletonList(schema), SchemaChangedNotifier.class).values().forEach(each -> each.notify(schemaName, schema));
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
        Optional<DataNodeContainedRule> dataNodeContainedRule = 
                metaData.getRuleMetaData().getRules().stream().filter(each -> each instanceof DataNodeContainedRule).findFirst().map(rule -> (DataNodeContainedRule) rule);
        return dataNodeContainedRule.isPresent() && dataNodeContainedRule.get().isNeedAccumulate(sqlStatementContext.getTablesContext().getTableNames());
    }
    
    private MergedResult mergeQuery(final SQLStatementContext<?> sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        MergeEngine mergeEngine = new MergeEngine(ProxyContext.getInstance().getMetaDataContexts().getDatabaseType(), 
                metaData.getSchema(), ProxyContext.getInstance().getMetaDataContexts().getProps(), metaData.getRuleMetaData().getRules());
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
        return new QueryData(queryHeaders.stream().map(QueryHeader::getColumnType).collect(Collectors.toList()), row);
    }
}
