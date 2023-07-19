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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rul.sql;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.distsql.parser.statement.rul.sql.PreviewStatement;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.aware.CursorDefinitionAware;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.type.CursorAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
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
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rul.executor.ConnectionSessionRequiredRULExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
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
public final class PreviewExecutor implements ConnectionSessionRequiredRULExecutor<PreviewStatement> {
    
    private final KernelProcessor kernelProcessor = new KernelProcessor();
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("data_source_name", "actual_sql");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereMetaData metaData, final ConnectionSession connectionSession, final PreviewStatement sqlStatement) throws SQLException {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        String databaseName = getDatabaseName(connectionSession);
        String databaseType = DatabaseTypeEngine.getTrunkDatabaseTypeName(metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType());
        ShardingSphereRuleMetaData globalRuleMetaData = metaDataContexts.getMetaData().getGlobalRuleMetaData();
        SQLParserRule sqlParserRule = globalRuleMetaData.getSingleRule(SQLParserRule.class);
        String sql = sqlParserRule.isSqlCommentParseEnabled() ? sqlStatement.getSql() : SQLHintUtils.removeHint(sqlStatement.getSql());
        SQLStatement previewedStatement = sqlParserRule.getSQLParserEngine(databaseType).parse(sql, false);
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(metaDataContexts.getMetaData(), previewedStatement, databaseName);
        HintValueContext hintValueContext = sqlParserRule.isSqlCommentParseEnabled() ? new HintValueContext() : SQLHintUtils.extractHint(sqlStatement.getSql()).orElseGet(HintValueContext::new);
        QueryContext queryContext = new QueryContext(sqlStatementContext, sql, Collections.emptyList(), hintValueContext);
        connectionSession.setQueryContext(queryContext);
        if (sqlStatementContext instanceof CursorAvailable && sqlStatementContext instanceof CursorDefinitionAware) {
            setUpCursorDefinition(sqlStatementContext, connectionSession);
        }
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(connectionSession.getDatabaseName());
        ShardingSpherePreconditions.checkState(database.isComplete(), () -> new RuleNotExistedException(connectionSession.getDatabaseName()));
        ConfigurationProperties props = metaDataContexts.getMetaData().getProps();
        String schemaName = queryContext.getSqlStatementContext().getTablesContext().getSchemaName().orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(database.getProtocolType(), databaseName));
        SQLFederationEngine sqlFederationEngine = new SQLFederationEngine(databaseName, schemaName, metaDataContexts.getMetaData(), metaDataContexts.getStatistics(),
                new JDBCExecutor(BackendExecutorContext.getInstance().getExecutorEngine(), connectionSession.getConnectionContext()));
        Collection<ExecutionUnit> executionUnits = isUseFederation(queryContext, metaDataContexts, connectionSession, sqlFederationEngine)
                ? getFederationExecutionUnits(queryContext, metaDataContexts, connectionSession, sqlFederationEngine)
                : kernelProcessor.generateExecutionContext(queryContext, database, globalRuleMetaData, props, connectionSession.getConnectionContext()).getExecutionUnits();
        return executionUnits.stream().map(this::buildRow).collect(Collectors.toList());
    }
    
    private void setUpCursorDefinition(final SQLStatementContext sqlStatementContext, final ConnectionSession connectionSession) {
        if (!((CursorAvailable) sqlStatementContext).getCursorName().isPresent()) {
            return;
        }
        String cursorName = ((CursorAvailable) sqlStatementContext).getCursorName().get().getIdentifier().getValue().toLowerCase();
        CursorStatementContext cursorStatementContext = (CursorStatementContext) connectionSession.getConnectionContext().getCursorContext().getCursorDefinitions().get(cursorName);
        Preconditions.checkArgument(null != cursorStatementContext, "Cursor %s does not exist.", cursorName);
        ((CursorDefinitionAware) sqlStatementContext).setUpCursorDefinition(cursorStatementContext);
    }
    
    private boolean isUseFederation(final QueryContext queryContext, final MetaDataContexts metaDataContexts, final ConnectionSession connectionSession,
                                    final SQLFederationEngine sqlFederationEngine) {
        return sqlFederationEngine.decide(queryContext.getSqlStatementContext(), queryContext.getParameters(),
                metaDataContexts.getMetaData().getDatabase(connectionSession.getDatabaseName()), metaDataContexts.getMetaData().getGlobalRuleMetaData());
    }
    
    private LocalDataQueryResultRow buildRow(final ExecutionUnit unit) {
        return new LocalDataQueryResultRow(unit.getDataSourceName(), unit.getSqlUnit().getSql());
    }
    
    private Collection<ExecutionUnit> getFederationExecutionUnits(final QueryContext queryContext, final MetaDataContexts metaDataContexts,
                                                                  final ConnectionSession connectionSession, final SQLFederationEngine sqlFederationEngine) {
        SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
        boolean isReturnGeneratedKeys = sqlStatement instanceof MySQLInsertStatement;
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine(isReturnGeneratedKeys, metaDataContexts, connectionSession);
        SQLFederationExecutorContext context = new SQLFederationExecutorContext(true, queryContext, metaDataContexts.getMetaData());
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(getDatabaseName(connectionSession));
        sqlFederationEngine.executeQuery(prepareEngine, createPreviewFederationCallback(database.getProtocolType(), database.getResourceMetaData(), sqlStatement), context);
        return context.getExecutionUnits();
    }
    
    private JDBCExecutorCallback<ExecuteResult> createPreviewFederationCallback(final DatabaseType protocolType, final ShardingSphereResourceMetaData resourceMetaData,
                                                                                final SQLStatement sqlStatement) {
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
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine(final boolean isReturnGeneratedKeys, final MetaDataContexts metaDataContexts,
                                                                                                           final ConnectionSession connectionSession) {
        int maxConnectionsSizePerQuery = metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new DriverExecutionPrepareEngine<>(JDBCDriverType.STATEMENT, maxConnectionsSizePerQuery, connectionSession.getDatabaseConnectionManager(),
                (JDBCBackendStatement) connectionSession.getStatementManager(), new StatementOption(isReturnGeneratedKeys),
                metaDataContexts.getMetaData().getDatabase(getDatabaseName(connectionSession)).getRuleMetaData().getRules(),
                metaDataContexts.getMetaData().getDatabase(getDatabaseName(connectionSession)).getResourceMetaData().getStorageTypes());
    }
    
    private String getDatabaseName(final ConnectionSession connectionSession) {
        String result = Strings.isNullOrEmpty(connectionSession.getDatabaseName()) ? connectionSession.getDefaultDatabaseName() : connectionSession.getDatabaseName();
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(result), NoDatabaseSelectedException::new);
        ShardingSpherePreconditions.checkState(ProxyContext.getInstance().databaseExists(result), () -> new UnknownDatabaseException(result));
        return result;
    }
    
    @Override
    public String getType() {
        return PreviewStatement.class.getName();
    }
}
