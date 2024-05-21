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

package org.apache.shardingsphere.proxy.backend.handler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.authority.checker.AuthorityChecker;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.distsql.statement.ral.queryable.QueryableRALStatement;
import org.apache.shardingsphere.distsql.statement.rql.RQLStatement;
import org.apache.shardingsphere.distsql.statement.rul.RULStatement;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.executor.audit.SQLAuditEngine;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.distsql.DistSQLStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.DatabaseAdminBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.data.DatabaseBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.database.DatabaseOperateBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.distsql.DistSQLBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.extra.ExtraProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.skip.SkipBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.transaction.TransactionBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.transaction.TransactionalErrorAllowedSQLStatementHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.state.ProxyClusterState;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.EmptyStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.FlushStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateUserStatement;
import org.apache.shardingsphere.transaction.util.AutoCommitUtils;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

/**
 * Proxy backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyBackendHandlerFactory {
    
    /**
     * Create new instance of backend handler.
     *
     * @param databaseType database type
     * @param sql SQL to be executed
     * @param sqlStatement SQL statement
     * @param connectionSession connection session
     * @param hintValueContext hint query context
     * @return created instance
     * @throws SQLException SQL exception
     */
    public static ProxyBackendHandler newInstance(final DatabaseType databaseType, final String sql, final SQLStatement sqlStatement,
                                                  final ConnectionSession connectionSession, final HintValueContext hintValueContext) throws SQLException {
        if (sqlStatement instanceof EmptyStatement) {
            return new SkipBackendHandler(sqlStatement);
        }
        SQLStatementContext sqlStatementContext = sqlStatement instanceof DistSQLStatement ? new DistSQLStatementContext((DistSQLStatement) sqlStatement)
                : new SQLBindEngine(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(), connectionSession.getDefaultDatabaseName(), hintValueContext).bind(sqlStatement,
                        Collections.emptyList());
        QueryContext queryContext = new QueryContext(sqlStatementContext, sql, Collections.emptyList(), hintValueContext);
        connectionSession.setQueryContext(queryContext);
        return newInstance(databaseType, queryContext, connectionSession, false);
    }
    
    /**
     * Create new instance of backend handler.
     *
     * @param databaseType database type
     * @param queryContext query context
     * @param connectionSession connection session
     * @param preferPreparedStatement use prepared statement as possible
     * @return created instance
     * @throws SQLException SQL exception
     */
    public static ProxyBackendHandler newInstance(final DatabaseType databaseType, final QueryContext queryContext,
                                                  final ConnectionSession connectionSession, final boolean preferPreparedStatement) throws SQLException {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        allowExecutingWhenTransactionalError(databaseType, connectionSession, sqlStatement);
        checkUnsupportedSQLStatement(sqlStatement);
        checkClusterState(sqlStatement);
        if (sqlStatement instanceof EmptyStatement) {
            return new SkipBackendHandler(sqlStatement);
        }
        if (sqlStatement instanceof DistSQLStatement) {
            checkUnsupportedDistSQLStatementInTransaction(sqlStatement, connectionSession);
            return DistSQLBackendHandlerFactory.newInstance((DistSQLStatement) sqlStatement, connectionSession);
        }
        String sql = queryContext.getSql();
        handleAutoCommit(sqlStatement, connectionSession);
        if (sqlStatement instanceof TCLStatement) {
            return TransactionBackendHandlerFactory.newInstance(sqlStatementContext, sql, connectionSession);
        }
        Optional<ProxyBackendHandler> backendHandler = DatabaseAdminBackendHandlerFactory.newInstance(databaseType, sqlStatementContext, connectionSession, sql, queryContext.getParameters());
        if (backendHandler.isPresent()) {
            return backendHandler.get();
        }
        Optional<ExtraProxyBackendHandler> extraHandler = findExtraProxyBackendHandler(sqlStatement);
        if (extraHandler.isPresent()) {
            return extraHandler.get();
        }
        Optional<ProxyBackendHandler> databaseOperateHandler = findDatabaseOperateBackendHandler(sqlStatement, connectionSession);
        if (databaseOperateHandler.isPresent()) {
            return databaseOperateHandler.get();
        }
        String databaseName = sqlStatementContext.getTablesContext().getDatabaseName().isPresent()
                ? sqlStatementContext.getTablesContext().getDatabaseName().get()
                : connectionSession.getDatabaseName();
        if (null == databaseName) {
            return DatabaseBackendHandlerFactory.newInstance(queryContext, connectionSession, preferPreparedStatement);
        }
        AuthorityRule authorityRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName);
        ShardingSpherePreconditions.checkState(new AuthorityChecker(authorityRule, connectionSession.getGrantee()).isAuthorized(databaseName),
                () -> new UnknownDatabaseException(databaseName));
        SQLAuditEngine.audit(sqlStatementContext, queryContext.getParameters(), ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData(),
                database, connectionSession.getGrantee(), queryContext.getHintValueContext());
        backendHandler = DatabaseAdminBackendHandlerFactory.newInstance(databaseType, sqlStatementContext, connectionSession);
        return backendHandler.orElseGet(() -> DatabaseBackendHandlerFactory.newInstance(queryContext, connectionSession, preferPreparedStatement));
    }
    
    private static void allowExecutingWhenTransactionalError(final DatabaseType databaseType, final ConnectionSession connectionSession, final SQLStatement sqlStatement) throws SQLException {
        if (!connectionSession.getConnectionContext().getTransactionContext().isExceptionOccur()) {
            return;
        }
        Optional<TransactionalErrorAllowedSQLStatementHandler> allowedSQLStatementHandler = DatabaseTypedSPILoader.findService(TransactionalErrorAllowedSQLStatementHandler.class, databaseType);
        if (allowedSQLStatementHandler.isPresent()) {
            allowedSQLStatementHandler.get().judgeContinueToExecute(sqlStatement);
        }
    }
    
    private static void checkUnsupportedDistSQLStatementInTransaction(final SQLStatement sqlStatement, final ConnectionSession connectionSession) {
        ShardingSpherePreconditions.checkState(!connectionSession.getTransactionStatus().isInTransaction() || isSupportedDistSQLStatementInTransaction(sqlStatement),
                () -> new UnsupportedSQLOperationException("Non-query DistSQL is not supported within a transaction"));
    }
    
    private static boolean isSupportedDistSQLStatementInTransaction(final SQLStatement sqlStatement) {
        return sqlStatement instanceof RQLStatement || sqlStatement instanceof QueryableRALStatement || sqlStatement instanceof RULStatement;
    }
    
    private static void handleAutoCommit(final SQLStatement sqlStatement, final ConnectionSession connectionSession) {
        if (AutoCommitUtils.needOpenTransaction(sqlStatement)) {
            connectionSession.getDatabaseConnectionManager().handleAutoCommit();
        }
    }
    
    private static Optional<ExtraProxyBackendHandler> findExtraProxyBackendHandler(final SQLStatement sqlStatement) {
        for (ExtraProxyBackendHandler each : ShardingSphereServiceLoader.getServiceInstances(ExtraProxyBackendHandler.class)) {
            if (each.accept(sqlStatement)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private static Optional<ProxyBackendHandler> findDatabaseOperateBackendHandler(final SQLStatement sqlStatement, final ConnectionSession connectionSession) {
        if (sqlStatement instanceof CreateDatabaseStatement || sqlStatement instanceof DropDatabaseStatement) {
            return Optional.of(DatabaseOperateBackendHandlerFactory.newInstance(sqlStatement, connectionSession));
        }
        return Optional.empty();
    }
    
    private static void checkUnsupportedSQLStatement(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof DCLStatement || sqlStatement instanceof FlushStatement || sqlStatement instanceof MySQLShowCreateUserStatement || sqlStatement instanceof RenameTableStatement) {
            throw new UnsupportedSQLOperationException(String.format("unsupported SQL statement `%s`", sqlStatement.getClass().getSimpleName()));
        }
    }
    
    private static void checkClusterState(final SQLStatement sqlStatement) {
        ClusterState clusterCurrentState = ProxyContext.getInstance().getContextManager().getStateContext().getCurrentClusterState();
        if (ClusterState.OK != clusterCurrentState) {
            TypedSPILoader.getService(ProxyClusterState.class, clusterCurrentState.name()).check(sqlStatement);
        }
    }
}
