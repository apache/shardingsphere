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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.query;

import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.executor.audit.SQLAuditEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Handler for MySQL multi statements.
 */
public final class MySQLMultiStatementsHandler implements ProxyBackendHandler {
    
    private static final Pattern MULTI_UPDATE_STATEMENTS = Pattern.compile(";(?=\\s*update)", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern MULTI_DELETE_STATEMENTS = Pattern.compile(";(?=\\s*delete)", Pattern.CASE_INSENSITIVE);
    
    private final KernelProcessor kernelProcessor = new KernelProcessor();
    
    private final JDBCExecutor jdbcExecutor;
    
    private final ConnectionSession connectionSession;
    
    private final SQLStatement sqlStatementSample;
    
    private final MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
    
    private final Map<String, List<ExecutionUnit>> dataSourcesToExecutionUnits = new HashMap<>();
    
    private ExecutionContext anyExecutionContext;
    
    public MySQLMultiStatementsHandler(final ConnectionSession connectionSession, final SQLStatement sqlStatementSample, final String sql) {
        jdbcExecutor = new JDBCExecutor(BackendExecutorContext.getInstance().getExecutorEngine(), connectionSession.getConnectionContext());
        connectionSession.getDatabaseConnectionManager().handleAutoCommit();
        this.connectionSession = connectionSession;
        this.sqlStatementSample = sqlStatementSample;
        Pattern pattern = sqlStatementSample instanceof UpdateStatement ? MULTI_UPDATE_STATEMENTS : MULTI_DELETE_STATEMENTS;
        SQLParserEngine sqlParserEngine = getSQLParserEngine();
        for (String each : extractMultiStatements(pattern, sql)) {
            SQLStatement eachSQLStatement = sqlParserEngine.parse(each, false);
            ExecutionContext executionContext = createExecutionContext(createQueryContext(each, eachSQLStatement));
            if (null == anyExecutionContext) {
                anyExecutionContext = executionContext;
            }
            for (ExecutionUnit eachExecutionUnit : executionContext.getExecutionUnits()) {
                dataSourcesToExecutionUnits.computeIfAbsent(eachExecutionUnit.getDataSourceName(), unused -> new LinkedList<>()).add(eachExecutionUnit);
            }
        }
    }
    
    private SQLParserEngine getSQLParserEngine() {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        return sqlParserRule.getSQLParserEngine(TypedSPILoader.getService(DatabaseType.class, "MySQL").getType());
    }
    
    private List<String> extractMultiStatements(final Pattern pattern, final String sql) {
        // TODO Multi statements should be split by SQL Parser instead of simple regexp.
        return Arrays.asList(pattern.split(sql));
    }
    
    private QueryContext createQueryContext(final String sql, final SQLStatement sqlStatement) {
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(
                metaDataContexts.getMetaData(), Collections.emptyList(), sqlStatement, connectionSession.getDatabaseName());
        return new QueryContext(sqlStatementContext, sql, Collections.emptyList());
    }
    
    private ExecutionContext createExecutionContext(final QueryContext queryContext) {
        ShardingSphereRuleMetaData globalRuleMetaData = metaDataContexts.getMetaData().getGlobalRuleMetaData();
        ShardingSphereDatabase currentDatabase = metaDataContexts.getMetaData().getDatabase(connectionSession.getDatabaseName());
        SQLAuditEngine.audit(queryContext.getSqlStatementContext(), queryContext.getParameters(), globalRuleMetaData, currentDatabase, null, queryContext.getHintValueContext());
        return kernelProcessor.generateExecutionContext(queryContext, currentDatabase, globalRuleMetaData, metaDataContexts.getMetaData().getProps(), connectionSession.getConnectionContext());
    }
    
    @Override
    public ResponseHeader execute() throws SQLException {
        Collection<ShardingSphereRule> rules = metaDataContexts.getMetaData().getDatabase(connectionSession.getDatabaseName()).getRuleMetaData().getRules();
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = new DriverExecutionPrepareEngine<>(JDBCDriverType.STATEMENT, metaDataContexts.getMetaData().getProps()
                .<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY), connectionSession.getDatabaseConnectionManager(),
                (JDBCBackendStatement) connectionSession.getStatementManager(), new StatementOption(false), rules,
                metaDataContexts.getMetaData().getDatabase(connectionSession.getDatabaseName()).getResourceMetaData().getStorageTypes());
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = prepareEngine.prepare(anyExecutionContext.getRouteContext(), samplingExecutionUnit(),
                new ExecutionGroupReportContext(connectionSession.getProcessId(), connectionSession.getDatabaseName(), connectionSession.getGrantee()));
        for (ExecutionGroup<JDBCExecutionUnit> eachGroup : executionGroupContext.getInputGroups()) {
            for (JDBCExecutionUnit each : eachGroup.getInputs()) {
                prepareBatchedStatement(each);
            }
        }
        return executeBatchedStatements(executionGroupContext);
    }
    
    private Collection<ExecutionUnit> samplingExecutionUnit() {
        Collection<ExecutionUnit> result = new LinkedList<>();
        for (List<ExecutionUnit> each : dataSourcesToExecutionUnits.values()) {
            result.add(each.get(0));
        }
        return result;
    }
    
    private void prepareBatchedStatement(final JDBCExecutionUnit each) throws SQLException {
        Statement statement = each.getStorageResource();
        for (ExecutionUnit eachExecutionUnit : dataSourcesToExecutionUnits.get(each.getExecutionUnit().getDataSourceName())) {
            statement.addBatch(eachExecutionUnit.getSqlUnit().getSql());
        }
    }
    
    private UpdateResponseHeader executeBatchedStatements(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext) throws SQLException {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        ShardingSphereResourceMetaData resourceMetaData = metaDataContexts.getMetaData().getDatabase(connectionSession.getDatabaseName()).getResourceMetaData();
        JDBCExecutorCallback<int[]> callback = new BatchedJDBCExecutorCallback(resourceMetaData, sqlStatementSample, isExceptionThrown);
        List<int[]> executeResults = jdbcExecutor.execute(executionGroupContext, callback);
        int updated = 0;
        for (int[] eachResult : executeResults) {
            for (int each : eachResult) {
                updated += each;
            }
        }
        // TODO Each logic SQL should correspond to an OK Packet.
        return new UpdateResponseHeader(sqlStatementSample, Collections.singletonList(new UpdateResult(updated, 0L)));
    }
    
    private static final class BatchedJDBCExecutorCallback extends JDBCExecutorCallback<int[]> {
        
        private BatchedJDBCExecutorCallback(final ShardingSphereResourceMetaData resourceMetaData, final SQLStatement sqlStatement, final boolean isExceptionThrown) {
            super(TypedSPILoader.getService(DatabaseType.class, "MySQL"), resourceMetaData, sqlStatement, isExceptionThrown);
        }
        
        @Override
        protected int[] executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
            try {
                return statement.executeBatch();
            } finally {
                statement.close();
            }
        }
        
        @SuppressWarnings("OptionalContainsCollection")
        @Override
        protected Optional<int[]> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
            return Optional.empty();
        }
    }
}
