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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rul.type;

import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.distsql.statement.rul.sql.PreviewStatement;
import org.apache.shardingsphere.infra.binder.context.aware.CursorDefinitionAware;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.CursorAvailable;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
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
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rul.aware.ConnectionSessionAwareRULExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.DatabaseNameUtils;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;
import org.apache.shardingsphere.sqlfederation.executor.SQLFederationExecutorContext;

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
public final class PreviewExecutor implements ConnectionSessionAwareRULExecutor<PreviewStatement> {
    
    private ConnectionSession connectionSession;
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("data_source_name", "actual_sql");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereMetaData metaData, final PreviewStatement sqlStatement) throws SQLException {
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(DatabaseNameUtils.getDatabaseName(sqlStatement, connectionSession));
        String toBePreviewedSQL = SQLHintUtils.removeHint(sqlStatement.getSql());
        HintValueContext hintValueContext = SQLHintUtils.extractHint(sqlStatement.getSql()).orElseGet(HintValueContext::new);
        SQLStatement toBePreviewedStatement = metaData.getGlobalRuleMetaData().getSingleRule(SQLParserRule.class).getSQLParserEngine(database.getProtocolType()).parse(toBePreviewedSQL, false);
        SQLStatementContext toBePreviewedStatementContext = new SQLBindEngine(metaData, database.getName(), hintValueContext).bind(toBePreviewedStatement, Collections.emptyList());
        QueryContext queryContext = new QueryContext(toBePreviewedStatementContext, toBePreviewedSQL, Collections.emptyList(), hintValueContext);
        connectionSession.setQueryContext(queryContext);
        if (toBePreviewedStatementContext instanceof CursorAvailable && toBePreviewedStatementContext instanceof CursorDefinitionAware) {
            setUpCursorDefinition(toBePreviewedStatementContext);
        }
        ShardingSpherePreconditions.checkState(database.isComplete(), () -> new RuleNotExistedException(database.getName()));
        String schemaName = queryContext.getSqlStatementContext().getTablesContext().getSchemaName()
                .orElseGet(() -> new DatabaseTypeRegistry(database.getProtocolType()).getDefaultSchemaName(database.getName()));
        SQLFederationEngine federationEngine = new SQLFederationEngine(database.getName(), schemaName, metaData, ProxyContext.getInstance().getContextManager().getMetaDataContexts().getStatistics(),
                new JDBCExecutor(BackendExecutorContext.getInstance().getExecutorEngine(), connectionSession.getConnectionContext()));
        Collection<ExecutionUnit> executionUnits = federationEngine.decide(queryContext.getSqlStatementContext(), queryContext.getParameters(), database, metaData.getGlobalRuleMetaData())
                ? getFederationExecutionUnits(queryContext, metaData, database, federationEngine)
                : new KernelProcessor().generateExecutionContext(
                        queryContext, database, metaData.getGlobalRuleMetaData(), metaData.getProps(), connectionSession.getConnectionContext()).getExecutionUnits();
        return executionUnits.stream().map(each -> new LocalDataQueryResultRow(each.getDataSourceName(), each.getSqlUnit().getSql())).collect(Collectors.toList());
    }
    
    private void setUpCursorDefinition(final SQLStatementContext toBePreviewedStatementContext) {
        if (!((CursorAvailable) toBePreviewedStatementContext).getCursorName().isPresent()) {
            return;
        }
        String cursorName = ((CursorAvailable) toBePreviewedStatementContext).getCursorName().get().getIdentifier().getValue().toLowerCase();
        CursorStatementContext cursorStatementContext = (CursorStatementContext) connectionSession.getConnectionContext().getCursorContext().getCursorDefinitions().get(cursorName);
        Preconditions.checkNotNull(cursorStatementContext, "Cursor %s does not exist.", cursorName);
        ((CursorDefinitionAware) toBePreviewedStatementContext).setUpCursorDefinition(cursorStatementContext);
    }
    
    private Collection<ExecutionUnit> getFederationExecutionUnits(final QueryContext queryContext, final ShardingSphereMetaData metaData,
                                                                  final ShardingSphereDatabase database, final SQLFederationEngine federationEngine) {
        SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
        // TODO move dialect MySQLInsertStatement into database type module @zhangliang
        boolean isReturnGeneratedKeys = sqlStatement instanceof MySQLInsertStatement;
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine(isReturnGeneratedKeys, metaData.getProps(), database);
        SQLFederationExecutorContext context = new SQLFederationExecutorContext(true, queryContext, metaData);
        federationEngine.executeQuery(prepareEngine, createPreviewFederationCallback(database.getProtocolType(), database.getResourceMetaData(), sqlStatement), context);
        return context.getExecutionUnits();
    }
    
    private JDBCExecutorCallback<ExecuteResult> createPreviewFederationCallback(final DatabaseType protocolType, final ResourceMetaData resourceMetaData, final SQLStatement sqlStatement) {
        return new JDBCExecutorCallback<ExecuteResult>(protocolType, resourceMetaData, sqlStatement, SQLExecutorExceptionHandler.isExceptionThrown()) {
            
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
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine(final boolean isReturnGeneratedKeys,
                                                                                                           final ConfigurationProperties props, final ShardingSphereDatabase database) {
        int maxConnectionsSizePerQuery = props.<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new DriverExecutionPrepareEngine<>(JDBCDriverType.STATEMENT, maxConnectionsSizePerQuery,
                connectionSession.getDatabaseConnectionManager(), (JDBCBackendStatement) connectionSession.getStatementManager(),
                new StatementOption(isReturnGeneratedKeys), database.getRuleMetaData().getRules(), database.getResourceMetaData().getStorageUnits());
    }
    
    @Override
    public Class<PreviewStatement> getType() {
        return PreviewStatement.class;
    }
}
