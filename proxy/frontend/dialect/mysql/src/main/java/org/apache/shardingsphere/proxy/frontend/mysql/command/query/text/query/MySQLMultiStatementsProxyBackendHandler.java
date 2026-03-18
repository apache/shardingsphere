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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.driver.executor.engine.batch.preparedstatement.BatchExecutionUnit;
import org.apache.shardingsphere.driver.executor.engine.batch.preparedstatement.BatchPreparedStatementExecutor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.executor.audit.SQLAuditEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.MultiStatementsUpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.util.MultiSQLSplitter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * MySQL multi-statements proxy backend handler.
 */
public final class MySQLMultiStatementsProxyBackendHandler implements ProxyBackendHandler {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
    
    private final Collection<QueryContext> multiSQLQueryContexts = new LinkedList<>();
    
    private final ConnectionSession connectionSession;
    
    private final SQLStatement sqlStatementSample;
    
    private final BatchPreparedStatementExecutor batchExecutor;
    
    public MySQLMultiStatementsProxyBackendHandler(final ConnectionSession connectionSession, final SQLStatement sqlStatementSample, final String sql) {
        connectionSession.getDatabaseConnectionManager().handleAutoCommit();
        this.connectionSession = connectionSession;
        this.sqlStatementSample = sqlStatementSample;
        JDBCExecutor jdbcExecutor = new JDBCExecutor(BackendExecutorContext.getInstance().getExecutorEngine(), connectionSession.getConnectionContext());
        batchExecutor = new BatchPreparedStatementExecutor(metaDataContexts.getMetaData().getDatabase(connectionSession.getUsedDatabaseName()), jdbcExecutor, connectionSession.getProcessId());
        SQLParserEngine sqlParserEngine = getSQLParserEngine();
        for (String each : MultiSQLSplitter.split(sql)) {
            SQLStatement eachSQLStatement = sqlParserEngine.parse(each, false);
            multiSQLQueryContexts.add(createQueryContext(each, eachSQLStatement));
        }
    }
    
    private SQLParserEngine getSQLParserEngine() {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        return sqlParserRule.getSQLParserEngine(databaseType);
    }
    
    private QueryContext createQueryContext(final String sql, final SQLStatement sqlStatement) {
        HintValueContext hintValueContext = SQLHintUtils.extractHint(sql);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(
                metaDataContexts.getMetaData(), connectionSession.getCurrentDatabaseName(), hintValueContext).bind(sqlStatement);
        return new QueryContext(sqlStatementContext, sql, Collections.emptyList(), hintValueContext, connectionSession.getConnectionContext(), metaDataContexts.getMetaData());
    }
    
    @Override
    public ResponseHeader execute() throws SQLException {
        Collection<ShardingSphereRule> rules = metaDataContexts.getMetaData().getDatabase(connectionSession.getUsedDatabaseName()).getRuleMetaData().getRules();
        int maxConnectionsSizePerQuery = metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine =
                new DriverExecutionPrepareEngine<>(JDBCDriverType.STATEMENT, maxConnectionsSizePerQuery, connectionSession.getDatabaseConnectionManager(),
                        (JDBCBackendStatement) connectionSession.getStatementManager(), new StatementOption(false), rules, metaDataContexts.getMetaData());
        return executeMultiStatements(prepareEngine);
    }
    
    private ResponseHeader executeMultiStatements(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine) throws SQLException {
        Collection<ExecutionContext> executionContexts = createExecutionContexts();
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext =
                prepareEngine.prepare(connectionSession.getUsedDatabaseName(), executionContexts.iterator().next(), createExecutionUnits(),
                        new ExecutionGroupReportContext(connectionSession.getProcessId(), connectionSession.getUsedDatabaseName(), connectionSession.getConnectionContext().getGrantee()));
        batchExecutor.init(executionGroupContext);
        executeAddBatch(executionGroupContext);
        return new MultiStatementsUpdateResponseHeader(buildUpdateResponseHeaders(batchExecutor.executeBatch(multiSQLQueryContexts.iterator().next().getSqlStatementContext())));
    }
    
    private List<ExecutionUnit> createExecutionUnits() {
        List<ExecutionUnit> result = new ArrayList<>(batchExecutor.getBatchExecutionUnits().size());
        for (BatchExecutionUnit each : batchExecutor.getBatchExecutionUnits()) {
            ExecutionUnit executionUnit = each.getExecutionUnit();
            result.add(executionUnit);
        }
        return result;
    }
    
    private Collection<ExecutionContext> createExecutionContexts() {
        Collection<ExecutionContext> result = new LinkedList<>();
        for (QueryContext each : multiSQLQueryContexts) {
            ExecutionContext executionContext = createExecutionContext(each);
            batchExecutor.addBatchForExecutionUnits(executionContext.getExecutionUnits());
            result.add(executionContext);
        }
        return result;
    }
    
    private ExecutionContext createExecutionContext(final QueryContext queryContext) {
        ShardingSphereDatabase currentDatabase = metaDataContexts.getMetaData().getDatabase(connectionSession.getCurrentDatabaseName());
        SQLAuditEngine.audit(queryContext, currentDatabase);
        return new KernelProcessor().generateExecutionContext(queryContext, metaDataContexts.getMetaData().getGlobalRuleMetaData(), metaDataContexts.getMetaData().getProps());
    }
    
    private void executeAddBatch(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext) throws SQLException {
        for (ExecutionGroup<JDBCExecutionUnit> each : executionGroupContext.getInputGroups()) {
            for (JDBCExecutionUnit unit : each.getInputs()) {
                unit.getStorageResource().addBatch(unit.getExecutionUnit().getSqlUnit().getSql());
            }
        }
    }
    
    private Collection<UpdateResponseHeader> buildUpdateResponseHeaders(final int[] updateCounts) {
        Collection<UpdateResponseHeader> result = new LinkedList<>();
        for (int each : updateCounts) {
            result.add(new UpdateResponseHeader(sqlStatementSample, Collections.singletonList(new UpdateResult(each, 0L))));
        }
        return result;
    }
}
