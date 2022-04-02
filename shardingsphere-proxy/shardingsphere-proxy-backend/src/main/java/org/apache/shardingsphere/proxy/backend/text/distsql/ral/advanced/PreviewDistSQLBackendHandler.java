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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.advanced;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.distsql.parser.statement.ral.advanced.PreviewStatement;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.SchemaNotExistedException;
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
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.communication.SQLStatementSchemaHolder;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.exception.RuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Preview dist sql backend handler.
 */
public final class PreviewDistSQLBackendHandler extends QueryableRALBackendHandler<PreviewStatement, PreviewDistSQLBackendHandler> {
    
    private static final String DATA_SOURCE_NAME = "data_source_name";
    
    private static final String ACTUAL_SQL = "actual_sql";
    
    private ConnectionSession connectionSession;
    
    private final KernelProcessor kernelProcessor = new KernelProcessor();
    
    @Override
    public PreviewDistSQLBackendHandler init(final HandlerParameter<PreviewStatement> parameter) {
        connectionSession = parameter.getConnectionSession();
        return super.init(parameter);
    }
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(DATA_SOURCE_NAME, ACTUAL_SQL);
    }
    
    @Override
    protected Collection<List<Object>> getRows(final ContextManager contextManager) throws SQLException {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        String schemaName = getSchemaName();
        String databaseType = DatabaseTypeRegistry.getTrunkDatabaseTypeName(metaDataContexts.getMetaData(schemaName).getResource().getDatabaseType());
        Optional<SQLParserRule> sqlParserRule = metaDataContexts.getGlobalRuleMetaData().findSingleRule(SQLParserRule.class);
        Preconditions.checkState(sqlParserRule.isPresent());
        SQLStatement previewedStatement = new ShardingSphereSQLParserEngine(databaseType, sqlParserRule.get().toParserConfiguration()).parse(sqlStatement.getSql(), false);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(metaDataContexts.getMetaDataMap(), previewedStatement, schemaName);
        // TODO optimize SQLStatementSchemaHolder
        if (sqlStatementContext instanceof TableAvailable) {
            ((TableAvailable) sqlStatementContext).getTablesContext().getDatabaseName().ifPresent(SQLStatementSchemaHolder::set);
        }
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getMetaData(connectionSession.getSchemaName());
        if (!metaData.isComplete()) {
            throw new RuleNotExistedException();
        }
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, sqlStatement.getSql(), Collections.emptyList());
        ExecutionContext executionContext = kernelProcessor.generateExecutionContext(logicSQL, metaData, metaDataContexts.getProps());
        Collection<ExecutionUnit> executionUnits = executionContext.getRouteContext().isFederated()
                ? getFederationExecutionUnits(logicSQL, schemaName, metaDataContexts) : executionContext.getExecutionUnits();
        return executionUnits.stream().map(this::buildRow).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private List<Object> buildRow(final ExecutionUnit unit) {
        return Arrays.asList(unit.getDataSourceName(), unit.getSqlUnit().getSql());
    }
    
    private Collection<ExecutionUnit> getFederationExecutionUnits(final LogicSQL logicSQL, final String schemaName, final MetaDataContexts metaDataContexts) throws SQLException {
        SQLStatement sqlStatement = logicSQL.getSqlStatementContext().getSqlStatement();
        boolean isReturnGeneratedKeys = sqlStatement instanceof MySQLInsertStatement;
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine(isReturnGeneratedKeys, metaDataContexts);
        FederationContext context = new FederationContext(true, logicSQL, metaDataContexts.getMetaDataMap());
        DatabaseType databaseType = metaDataContexts.getMetaData(getSchemaName()).getResource().getDatabaseType();
        FederationExecutor executor = FederationExecutorFactory.newInstance(schemaName, schemaName, metaDataContexts.getOptimizerContext(),
                metaDataContexts.getProps(), new JDBCExecutor(BackendExecutorContext.getInstance().getExecutorEngine(), false));
        executor.executeQuery(prepareEngine, createPreviewFederationCallback(sqlStatement, databaseType), context);
        return context.getExecutionUnits();
    }
    
    private JDBCExecutorCallback<ExecuteResult> createPreviewFederationCallback(final SQLStatement sqlStatement, final DatabaseType databaseType) {
        return new JDBCExecutorCallback<ExecuteResult>(databaseType, sqlStatement, SQLExecutorExceptionHandler.isExceptionThrown()) {
            
            @Override
            protected ExecuteResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return new JDBCStreamQueryResult(statement.executeQuery(sql));
            }
            
            @Override
            protected Optional<ExecuteResult> getSaneResult(final SQLStatement sqlStatement) {
                return Optional.empty();
            }
        };
    }
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine(final boolean isReturnGeneratedKeys, final MetaDataContexts metaData) {
        int maxConnectionsSizePerQuery = metaData.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new DriverExecutionPrepareEngine<>(JDBCDriverType.STATEMENT, maxConnectionsSizePerQuery, (JDBCBackendConnection) connectionSession.getBackendConnection(),
                (JDBCBackendStatement) connectionSession.getStatementManager(), new StatementOption(isReturnGeneratedKeys), metaData.getMetaData(getSchemaName()).getRuleMetaData().getRules());
    }
    
    private String getSchemaName() {
        String result = !Strings.isNullOrEmpty(connectionSession.getSchemaName()) ? connectionSession.getSchemaName() : connectionSession.getDefaultSchemaName();
        if (Strings.isNullOrEmpty(result)) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().getAllSchemaNames().contains(result)) {
            throw new SchemaNotExistedException(result);
        }
        return result;
    }
}
