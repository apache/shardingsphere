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

package org.apache.shardingsphere.proxy.backend.text.distsql.rul.sql;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.distsql.parser.statement.rul.sql.PreviewStatement;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.aware.CursorDefinitionAware;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.type.CursorAvailable;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.exception.DatabaseNotExistedException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
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
import org.apache.shardingsphere.infra.federation.executor.FederationContext;
import org.apache.shardingsphere.infra.federation.executor.FederationExecutor;
import org.apache.shardingsphere.infra.federation.executor.FederationExecutorFactory;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.communication.SQLStatementDatabaseHolder;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.text.distsql.rul.SQLRULBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Preview handler.
 */
public final class PreviewHandler extends SQLRULBackendHandler<PreviewStatement> {
    
    private static final String DATA_SOURCE_NAME = "data_source_name";
    
    private static final String ACTUAL_SQL = "actual_sql";
    
    private final KernelProcessor kernelProcessor = new KernelProcessor();
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(DATA_SOURCE_NAME, ACTUAL_SQL);
    }
    
    @Override
    protected Collection<LocalDataQueryResultRow> getRows(final ContextManager contextManager) throws SQLException {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        String databaseName = getDatabaseName();
        String databaseType = DatabaseTypeEngine.getTrunkDatabaseTypeName(metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType());
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        SQLStatement previewedStatement = sqlParserRule.getSQLParserEngine(databaseType).parse(getSqlStatement().getSql(), false);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(metaDataContexts.getMetaData().getDatabases(), previewedStatement, databaseName);
        // TODO optimize SQLStatementDatabaseHolder
        if (sqlStatementContext instanceof TableAvailable) {
            ((TableAvailable) sqlStatementContext).getTablesContext().getDatabaseName().ifPresent(SQLStatementDatabaseHolder::set);
        }
        if (sqlStatementContext instanceof CursorAvailable && sqlStatementContext instanceof CursorDefinitionAware) {
            setUpCursorDefinition(sqlStatementContext);
        }
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(getConnectionSession().getDatabaseName());
        if (!database.isComplete()) {
            throw new RuleNotExistedException();
        }
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, getSqlStatement().getSql(), Collections.emptyList());
        ExecutionContext executionContext = kernelProcessor.generateExecutionContext(
                logicSQL, database, metaDataContexts.getMetaData().getGlobalRuleMetaData(), metaDataContexts.getMetaData().getProps());
        Collection<ExecutionUnit> executionUnits = executionContext.getRouteContext().isFederated()
                ? getFederationExecutionUnits(logicSQL, databaseName, metaDataContexts)
                : executionContext.getExecutionUnits();
        return executionUnits.stream().map(this::buildRow).collect(Collectors.toList());
    }
    
    private void setUpCursorDefinition(final SQLStatementContext<?> sqlStatementContext) {
        if (!((CursorAvailable) sqlStatementContext).getCursorName().isPresent()) {
            return;
        }
        String cursorName = ((CursorAvailable) sqlStatementContext).getCursorName().get().getIdentifier().getValue().toLowerCase();
        CursorStatementContext cursorStatementContext = getConnectionSession().getCursorDefinitions().get(cursorName);
        Preconditions.checkArgument(null != cursorStatementContext, "Cursor %s does not exist.", cursorName);
        ((CursorDefinitionAware) sqlStatementContext).setUpCursorDefinition(cursorStatementContext);
    }
    
    private LocalDataQueryResultRow buildRow(final ExecutionUnit unit) {
        return new LocalDataQueryResultRow(unit.getDataSourceName(), unit.getSqlUnit().getSql());
    }
    
    private Collection<ExecutionUnit> getFederationExecutionUnits(final LogicSQL logicSQL, final String databaseName, final MetaDataContexts metaDataContexts) throws SQLException {
        SQLStatement sqlStatement = logicSQL.getSqlStatementContext().getSqlStatement();
        boolean isReturnGeneratedKeys = sqlStatement instanceof MySQLInsertStatement;
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine(isReturnGeneratedKeys, metaDataContexts);
        FederationContext context = new FederationContext(true, logicSQL, metaDataContexts.getMetaData().getDatabases());
        DatabaseType databaseType = metaDataContexts.getMetaData().getDatabase(getDatabaseName()).getResource().getDatabaseType();
        String schemaName = logicSQL.getSqlStatementContext().getTablesContext().getSchemaName().orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(databaseType, databaseName));
        EventBusContext eventBusContext = ProxyContext.getInstance().getContextManager().getInstanceContext().getEventBusContext();
        FederationExecutor executor = FederationExecutorFactory.newInstance(databaseName, schemaName, metaDataContexts.getOptimizerContext(), metaDataContexts.getMetaData().getGlobalRuleMetaData(),
                metaDataContexts.getMetaData().getProps(), new JDBCExecutor(BackendExecutorContext.getInstance().getExecutorEngine(), false), eventBusContext);
        executor.executeQuery(prepareEngine, createPreviewFederationCallback(sqlStatement, databaseType, eventBusContext), context);
        return context.getExecutionUnits();
    }
    
    private JDBCExecutorCallback<ExecuteResult> createPreviewFederationCallback(final SQLStatement sqlStatement, final DatabaseType databaseType, final EventBusContext eventBusContext) {
        return new JDBCExecutorCallback<ExecuteResult>(databaseType, sqlStatement, SQLExecutorExceptionHandler.isExceptionThrown(), eventBusContext) {
            
            @Override
            protected ExecuteResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return new JDBCStreamQueryResult(statement.executeQuery(sql));
            }
            
            @Override
            protected Optional<ExecuteResult> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
                return Optional.empty();
            }
        };
    }
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine(final boolean isReturnGeneratedKeys, final MetaDataContexts metaDataContexts) {
        int maxConnectionsSizePerQuery = metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new DriverExecutionPrepareEngine<>(JDBCDriverType.STATEMENT, maxConnectionsSizePerQuery, (JDBCBackendConnection) getConnectionSession().getBackendConnection(),
                (JDBCBackendStatement) getConnectionSession().getStatementManager(), new StatementOption(isReturnGeneratedKeys),
                metaDataContexts.getMetaData().getDatabase(getDatabaseName()).getRuleMetaData().getRules());
    }
    
    private String getDatabaseName() {
        String result = !Strings.isNullOrEmpty(getConnectionSession().getDatabaseName()) ? getConnectionSession().getDatabaseName() : getConnectionSession().getDefaultDatabaseName();
        if (Strings.isNullOrEmpty(result)) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().databaseExists(result)) {
            throw new DatabaseNotExistedException(result);
        }
        return result;
    }
}
