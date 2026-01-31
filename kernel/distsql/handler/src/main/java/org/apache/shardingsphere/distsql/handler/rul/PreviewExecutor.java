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

package org.apache.shardingsphere.distsql.handler.rul;

import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorConnectionContextAware;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.rul.sql.PreviewStatement;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorHeldSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.EmptyRuleException;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.CursorSQLStatementAttribute;
import org.apache.shardingsphere.sqlfederation.context.SQLFederationContext;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Preview executor.
 */
@Setter
public final class PreviewExecutor implements DistSQLQueryExecutor<PreviewStatement>, DistSQLExecutorDatabaseAware, DistSQLExecutorConnectionContextAware {
    
    private ShardingSphereDatabase database;
    
    private DistSQLConnectionContext connectionContext;
    
    @Override
    public Collection<String> getColumnNames(final PreviewStatement sqlStatement) {
        return Arrays.asList("data_source_name", "actual_sql");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final PreviewStatement sqlStatement, final ContextManager contextManager) {
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
        String toBePreviewedSQL = sqlStatement.getSql();
        SQLStatement toBePreviewedStatement = metaData.getGlobalRuleMetaData().getSingleRule(SQLParserRule.class).getSQLParserEngine(database.getProtocolType()).parse(toBePreviewedSQL, false);
        HintValueContext hintValueContext = copyHintValueContext(connectionContext.getQueryContext().getHintValueContext());
        hintValueContext.setSkipMetadataValidate(true);
        String currentDatabaseName = connectionContext.getQueryContext().getConnectionContext().getCurrentDatabaseName().orElse(null);
        SQLStatementContext toBePreviewedStatementContext = new SQLBindEngine(metaData, currentDatabaseName, hintValueContext).bind(toBePreviewedStatement);
        QueryContext queryContext = new QueryContext(
                toBePreviewedStatementContext, toBePreviewedSQL, Collections.emptyList(), hintValueContext, connectionContext.getQueryContext().getConnectionContext(), metaData);
        if (toBePreviewedStatementContext.getSqlStatement().getAttributes().findAttribute(CursorSQLStatementAttribute.class).isPresent()
                && toBePreviewedStatementContext instanceof CursorHeldSQLStatementContext) {
            setUpCursorDefinition((CursorHeldSQLStatementContext) toBePreviewedStatementContext);
        }
        ShardingSpherePreconditions.checkState(database.isComplete(), () -> new EmptyRuleException(database.getName()));
        String schemaName = getSchemaName(queryContext.getSqlStatementContext(), database);
        Collection<ExecutionUnit> executionUnits = getExecutionUnits(contextManager, schemaName, metaData, queryContext);
        return executionUnits.stream().map(each -> new LocalDataQueryResultRow(each.getDataSourceName(), each.getSqlUnit().getSql())).collect(Collectors.toList());
    }
    
    private String getSchemaName(final SQLStatementContext sqlStatementContext, final ShardingSphereDatabase database) {
        String defaultSchemaName = new DatabaseTypeRegistry(sqlStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(database.getName());
        return sqlStatementContext.getTablesContext().getSchemaName().orElse(defaultSchemaName);
    }
    
    private HintValueContext copyHintValueContext(final HintValueContext hintValueContext) {
        HintValueContext result = new HintValueContext();
        result.getShardingDatabaseValues().putAll(hintValueContext.getShardingDatabaseValues());
        result.getShardingTableValues().putAll(hintValueContext.getShardingTableValues());
        result.getDisableAuditNames().addAll(hintValueContext.getDisableAuditNames());
        result.setDataSourceName(hintValueContext.getDataSourceName());
        result.setDatabaseShardingOnly(hintValueContext.isDatabaseShardingOnly());
        result.setWriteRouteOnly(hintValueContext.isWriteRouteOnly());
        result.setSkipSQLRewrite(hintValueContext.isSkipSQLRewrite());
        result.setShadow(hintValueContext.isShadow());
        return result;
    }
    
    private Collection<ExecutionUnit> getExecutionUnits(final ContextManager contextManager, final String schemaName, final ShardingSphereMetaData metaData, final QueryContext queryContext) {
        ExecutorEngine executorEngine = contextManager.getExecutorEngine();
        JDBCExecutor jdbcExecutor = new JDBCExecutor(executorEngine, connectionContext.getQueryContext().getConnectionContext());
        SQLFederationEngine federationEngine = new SQLFederationEngine(database.getName(), schemaName, metaData, contextManager.getMetaDataContexts().getStatistics(), jdbcExecutor);
        if (federationEngine.decide(queryContext, metaData.getGlobalRuleMetaData())) {
            return getFederationExecutionUnits(queryContext, metaData, federationEngine);
        }
        return new KernelProcessor().generateExecutionContext(queryContext, metaData.getGlobalRuleMetaData(), metaData.getProps()).getExecutionUnits();
    }
    
    private void setUpCursorDefinition(final CursorHeldSQLStatementContext toBePreviewedStatementContext) {
        Optional<CursorNameSegment> cursorNameSegment = toBePreviewedStatementContext.getSqlStatement().getAttributes().getAttribute(CursorSQLStatementAttribute.class).getCursorName();
        if (!cursorNameSegment.isPresent()) {
            return;
        }
        String cursorName = cursorNameSegment.get().getIdentifier().getValue().toLowerCase();
        CursorStatementContext cursorStatementContext = connectionContext.getQueryContext().getConnectionContext().getCursorContext().getCursorStatementContexts().get(cursorName);
        Preconditions.checkNotNull(cursorStatementContext, "Cursor %s does not exist.", cursorName);
        toBePreviewedStatementContext.setCursorStatementContext(cursorStatementContext);
    }
    
    private Collection<ExecutionUnit> getFederationExecutionUnits(final QueryContext queryContext, final ShardingSphereMetaData metaData, final SQLFederationEngine federationEngine) {
        SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine(metaData);
        String processId = newProcessId(queryContext);
        SQLFederationContext context = new SQLFederationContext(true, queryContext, metaData, processId);
        federationEngine.executeQuery(prepareEngine, createPreviewCallback(sqlStatement), context);
        return context.getPreviewExecutionUnits();
    }
    
    private String newProcessId(final QueryContext queryContext) {
        return new ProcessEngine().connect(queryContext.getUsedDatabase().getName(), queryContext.getConnectionContext().getGrantee());
    }
    
    private JDBCExecutorCallback<ExecuteResult> createPreviewCallback(final SQLStatement sqlStatement) {
        return new JDBCExecutorCallback<ExecuteResult>(database.getProtocolType(), database.getResourceMetaData(), sqlStatement, SQLExecutorExceptionHandler.isExceptionThrown()) {
            
            @Override
            protected ExecuteResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
                return new JDBCStreamQueryResult(statement.executeQuery(sql));
            }
            
            @Override
            protected Optional<ExecuteResult> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
                return Optional.empty();
            }
        };
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine(final ShardingSphereMetaData metaData) {
        int maxConnectionsSizePerQuery = metaData.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new DriverExecutionPrepareEngine<>(JDBCDriverType.STATEMENT, maxConnectionsSizePerQuery, connectionContext.getDatabaseConnectionManager(),
                connectionContext.getExecutorStatementManager(), new StatementOption(false), database.getRuleMetaData().getRules(), metaData);
    }
    
    @Override
    public Class<PreviewStatement> getType() {
        return PreviewStatement.class;
    }
}
