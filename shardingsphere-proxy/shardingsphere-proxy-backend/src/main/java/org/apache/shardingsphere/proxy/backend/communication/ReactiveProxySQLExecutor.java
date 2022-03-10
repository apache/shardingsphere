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

import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.sane.JDBCSaneQueryResultEngineFactory;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.vertx.VertxExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.vertx.VertxExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.vertx.VertxExecutionContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.communication.vertx.VertxBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.vertx.VertxBackendStatement;
import org.apache.shardingsphere.proxy.backend.communication.vertx.executor.ProxyReactiveExecutor;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.TableModifyInTransactionException;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.OpenGaussStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Reactive proxy SQL executor.
 */
public final class ReactiveProxySQLExecutor {
    
    private static final String TYPE = "Vert.x";
    
    private final VertxBackendConnection backendConnection;
    
    private final ProxyReactiveExecutor reactiveExecutor;
    
    public ReactiveProxySQLExecutor(final VertxBackendConnection backendConnection) {
        this.backendConnection = backendConnection;
        reactiveExecutor = new ProxyReactiveExecutor(new VertxExecutor(BackendExecutorContext.getInstance().getExecutorEngine()));
    }
    
    /**
     * Check execute prerequisites.
     *
     * @param executionContext execution context
     */
    public void checkExecutePrerequisites(final ExecutionContext executionContext) {
        if (isExecuteDDLInXATransaction(executionContext.getSqlStatementContext().getSqlStatement())
                || isExecuteDDLInPostgreSQLOpenGaussTransaction(executionContext.getSqlStatementContext().getSqlStatement())) {
            throw new TableModifyInTransactionException(executionContext.getSqlStatementContext());
        }
    }
    
    private boolean isExecuteDDLInXATransaction(final SQLStatement sqlStatement) {
        TransactionStatus transactionStatus = backendConnection.getConnectionSession().getTransactionStatus();
        return TransactionType.XA == transactionStatus.getTransactionType() && sqlStatement instanceof DDLStatement && transactionStatus.isInTransaction();
    }
    
    private boolean isExecuteDDLInPostgreSQLOpenGaussTransaction(final SQLStatement sqlStatement) {
        // TODO implement DDL statement commit/rollback in PostgreSQL/openGauss transaction
        boolean isPostgreSQLOpenGaussStatement = sqlStatement instanceof PostgreSQLStatement || sqlStatement instanceof OpenGaussStatement;
        return sqlStatement instanceof DDLStatement && isPostgreSQLOpenGaussStatement && backendConnection.getConnectionSession().getTransactionStatus().isInTransaction();
    }
    
    /**
     * Execute SQL.
     *
     * @param executionContext execution context
     * @return execute results
     * @throws SQLException SQL exception
     */
    public Future<List<ExecuteResult>> execute(final ExecutionContext executionContext) throws SQLException {
        String schemaName = backendConnection.getConnectionSession().getSchemaName();
        Collection<ShardingSphereRule> rules = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(schemaName).getRuleMetaData().getRules();
        int maxConnectionsSizePerQuery = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return useDriverToExecute(executionContext, rules, maxConnectionsSizePerQuery);
    }
    
    private Future<List<ExecuteResult>> useDriverToExecute(final ExecutionContext executionContext, final Collection<ShardingSphereRule> rules,
                                                                 final int maxConnectionsSizePerQuery) throws SQLException {
        VertxBackendStatement statementManager = (VertxBackendStatement) backendConnection.getConnectionSession().getStatementManager();
        DriverExecutionPrepareEngine<VertxExecutionUnit, Future<? extends SqlClient>> prepareEngine = new DriverExecutionPrepareEngine<>(
                TYPE, maxConnectionsSizePerQuery, backendConnection, statementManager, new VertxExecutionContext(), rules);
        ExecutionGroupContext<VertxExecutionUnit> executionGroupContext;
        try {
            executionGroupContext = prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
        } catch (final SQLException ex) {
            return Future.succeededFuture(getSaneExecuteResults(executionContext, ex));
        }
        executionGroupContext.setSchemaName(backendConnection.getConnectionSession().getSchemaName());
        executionGroupContext.setGrantee(backendConnection.getConnectionSession().getGrantee());
        return reactiveExecutor.execute(executionContext.getLogicSQL(), executionGroupContext);
    }
    
    private List<ExecuteResult> getSaneExecuteResults(final ExecutionContext executionContext, final SQLException originalException) throws SQLException {
        DatabaseType databaseType = ProxyContext.getInstance().getMetaData(backendConnection.getConnectionSession().getSchemaName()).getResource().getDatabaseType();
        Optional<ExecuteResult> executeResult = JDBCSaneQueryResultEngineFactory.newInstance(databaseType).getSaneQueryResult(executionContext.getSqlStatementContext().getSqlStatement());
        if (executeResult.isPresent()) {
            return Collections.singletonList(executeResult.get());
        }
        throw originalException;
    }
}
