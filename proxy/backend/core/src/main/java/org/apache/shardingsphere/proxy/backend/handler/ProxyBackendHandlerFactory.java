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
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.QueryableRALStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.RQLStatement;
import org.apache.shardingsphere.distsql.statement.type.rul.RULStatement;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.executor.checker.SQLExecutionChecker;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.state.ShardingSphereState;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.distsql.DistSQLStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.DatabaseAdminProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.data.DatabaseProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.database.DatabaseOperateProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.distsql.DistSQLProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.skip.SkipProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.TCLProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.state.DialectProxyStateSupportedSQLProvider;
import org.apache.shardingsphere.proxy.backend.state.ProxyClusterStateChecker;
import org.apache.shardingsphere.proxy.backend.state.ProxySQLSupportedJudgeEngine;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.EmptyStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;
import org.apache.shardingsphere.transaction.util.AutoCommitUtils;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Proxy backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyBackendHandlerFactory {
    
    private static final Collection<Class<? extends SQLStatement>> UNSUPPORTED_STANDARD_SQL_STATEMENT_TYPES = Arrays.asList(DCLStatement.class, RenameTableStatement.class);
    
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
            return new SkipProxyBackendHandler(sqlStatement);
        }
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData();
        SQLStatementContext sqlStatementContext = sqlStatement instanceof DistSQLStatement
                ? new DistSQLStatementContext((DistSQLStatement) sqlStatement)
                : new SQLBindEngine(metaData, connectionSession.getCurrentDatabaseName(), hintValueContext).bind(sqlStatement);
        QueryContext queryContext = new QueryContext(sqlStatementContext, sql, Collections.emptyList(), hintValueContext, connectionSession.getConnectionContext(), metaData);
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
        connectionSession.setQueryContext(queryContext);
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        checkAllowedSQLStatementWhenTransactionFailed(databaseType, sqlStatement, connectionSession.getConnectionContext().getTransactionContext());
        checkSupportedSQLStatement(databaseType, sqlStatement);
        checkClusterState(databaseType, sqlStatement);
        if (sqlStatement instanceof EmptyStatement) {
            return new SkipProxyBackendHandler(sqlStatement);
        }
        if (sqlStatement instanceof DistSQLStatement) {
            checkSupportedDistSQLStatementInTransaction(sqlStatement, connectionSession);
            return DistSQLProxyBackendHandlerFactory.newInstance((DistSQLStatement) sqlStatement, queryContext, connectionSession);
        }
        handleAutoCommit(sqlStatement, connectionSession);
        if (sqlStatement instanceof TCLStatement) {
            return TCLProxyBackendHandlerFactory.newInstance(queryContext, connectionSession);
        }
        Optional<ProxyBackendHandler> databaseAdminHandler = DatabaseAdminProxyBackendHandlerFactory.newInstance(
                databaseType, sqlStatementContext, connectionSession, queryContext.getSql(), queryContext.getParameters());
        if (databaseAdminHandler.isPresent()) {
            return databaseAdminHandler.get();
        }
        Optional<ProxyBackendHandler> databaseOperateHandler = findDatabaseOperateProxyBackendHandler(sqlStatement, connectionSession);
        if (databaseOperateHandler.isPresent()) {
            return databaseOperateHandler.get();
        }
        String databaseName = sqlStatementContext.getTablesContext().getDatabaseName().isPresent()
                ? sqlStatementContext.getTablesContext().getDatabaseName().get()
                : connectionSession.getUsedDatabaseName();
        if (null != databaseName) {
            checkSQLExecution(queryContext, connectionSession.getConnectionContext().getGrantee(), databaseName);
        }
        return DatabaseProxyBackendHandlerFactory.newInstance(queryContext, connectionSession, preferPreparedStatement);
    }
    
    private static void checkAllowedSQLStatementWhenTransactionFailed(final DatabaseType databaseType, final SQLStatement sqlStatement,
                                                                      final TransactionConnectionContext transactionContext) throws SQLException {
        if (transactionContext.isExceptionOccur()
                && DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType).getTransactionOption().isAllowCommitAndRollbackOnlyWhenTransactionFailed()) {
            ShardingSpherePreconditions.checkState(sqlStatement instanceof CommitStatement || sqlStatement instanceof RollbackStatement,
                    () -> new SQLFeatureNotSupportedException("Current transaction is aborted, commands ignored until end of transaction block."));
        }
    }
    
    private static void checkSupportedSQLStatement(final DatabaseType databaseType, final SQLStatement sqlStatement) {
        ShardingSpherePreconditions.checkState(isSupportedSQLStatement(databaseType, sqlStatement),
                () -> new UnsupportedSQLOperationException(String.format("unsupported SQL statement `%s`", sqlStatement.getClass().getSimpleName())));
    }
    
    private static boolean isSupportedSQLStatement(final DatabaseType databaseType, final SQLStatement sqlStatement) {
        Collection<Class<? extends SQLStatement>> unsupportedDialectSQLStatementTypes = DatabaseTypedSPILoader.findService(DialectProxyStateSupportedSQLProvider.class, databaseType)
                .map(DialectProxyStateSupportedSQLProvider::getUnsupportedSQLStatementTypesOnReadyState).orElse(Collections.emptyList());
        return new ProxySQLSupportedJudgeEngine(
                Collections.emptyList(), Collections.emptyList(), UNSUPPORTED_STANDARD_SQL_STATEMENT_TYPES, unsupportedDialectSQLStatementTypes).isSupported(sqlStatement);
    }
    
    private static void checkClusterState(final DatabaseType databaseType, final SQLStatement sqlStatement) {
        ShardingSphereState currentState = ProxyContext.getInstance().getContextManager().getStateContext().getState();
        TypedSPILoader.findService(ProxyClusterStateChecker.class, currentState).ifPresent(optional -> optional.check(sqlStatement, databaseType));
    }
    
    private static void checkSupportedDistSQLStatementInTransaction(final SQLStatement sqlStatement, final ConnectionSession connectionSession) {
        ShardingSpherePreconditions.checkState(!connectionSession.getTransactionStatus().isInTransaction() || isSupportedDistSQLStatementInTransaction(sqlStatement),
                () -> new UnsupportedSQLOperationException("Non-query DistSQL is not supported within a transaction"));
    }
    
    private static boolean isSupportedDistSQLStatementInTransaction(final SQLStatement sqlStatement) {
        return sqlStatement instanceof RQLStatement || sqlStatement instanceof QueryableRALStatement || sqlStatement instanceof RULStatement;
    }
    
    private static void handleAutoCommit(final SQLStatement sqlStatement, final ConnectionSession connectionSession) {
        if (AutoCommitUtils.isNeedStartTransaction(sqlStatement)) {
            connectionSession.getDatabaseConnectionManager().handleAutoCommit();
        }
    }
    
    private static Optional<ProxyBackendHandler> findDatabaseOperateProxyBackendHandler(final SQLStatement sqlStatement, final ConnectionSession connectionSession) {
        return sqlStatement instanceof CreateDatabaseStatement || sqlStatement instanceof DropDatabaseStatement
                ? Optional.of(DatabaseOperateProxyBackendHandlerFactory.newInstance(sqlStatement, connectionSession))
                : Optional.empty();
    }
    
    private static void checkSQLExecution(final QueryContext queryContext, final Grantee grantee, final String databaseName) {
        ShardingSphereDatabase database = queryContext.getMetaData().getDatabase(databaseName);
        ShardingSphereServiceLoader.getServiceInstances(SQLExecutionChecker.class).forEach(each -> each.check(grantee, queryContext, database));
    }
}
