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

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.DistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.QueryableRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.RQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rul.RULStatement;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.check.SQLCheckEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.DatabaseAdminBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.data.DatabaseBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.database.DatabaseOperateBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.distsql.DistSQLBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.extra.ExtraProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.skip.SkipBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.transaction.TransactionBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.FlushStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.EmptyStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowCreateUserStatement;
import org.apache.shardingsphere.transaction.utils.AutoCommitUtils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Proxy backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyBackendHandlerFactory {
    
    static {
        ShardingSphereServiceLoader.register(ExtraProxyBackendHandler.class);
    }
    
    /**
     * Create new instance of backend handler.
     *
     * @param databaseType database type
     * @param sql SQL to be executed
     * @param connectionSession connection session
     * @return created instance
     * @throws SQLException SQL exception
     */
    public static ProxyBackendHandler newInstance(final DatabaseType databaseType, final String sql, final ConnectionSession connectionSession) throws SQLException {
        if (Strings.isNullOrEmpty(SQLUtil.trimComment(sql))) {
            return new SkipBackendHandler(new EmptyStatement());
        }
        SQLParserRule sqlParserRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(getProtocolType(databaseType, connectionSession).getType()).parse(sql, false);
        return newInstance(databaseType, sql, sqlStatement, connectionSession);
    }
    
    /**
     * Create new instance of backend handler.
     *
     * @param databaseType database type
     * @param sql SQL to be executed
     * @param sqlStatement SQL statement
     * @param connectionSession connection session
     * @return created instance
     * @throws SQLException SQL exception
     */
    public static ProxyBackendHandler newInstance(final DatabaseType databaseType, final String sql, final SQLStatement sqlStatement,
                                                  final ConnectionSession connectionSession) throws SQLException {
        if (sqlStatement instanceof EmptyStatement) {
            return new SkipBackendHandler(sqlStatement);
        }
        if (sqlStatement instanceof DistSQLStatement) {
            checkUnsupportedDistSQLStatementInTransaction(sqlStatement, connectionSession);
            return DistSQLBackendHandlerFactory.newInstance((DistSQLStatement) sqlStatement, connectionSession);
        }
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabases(),
                sqlStatement, connectionSession.getDefaultDatabaseName());
        QueryContext queryContext = new QueryContext(sqlStatementContext, sql, Collections.emptyList());
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
    @SuppressWarnings("unchecked")
    public static ProxyBackendHandler newInstance(final DatabaseType databaseType, final QueryContext queryContext, final ConnectionSession connectionSession,
                                                  final boolean preferPreparedStatement) throws SQLException {
        SQLStatementContext<?> sqlStatementContext = queryContext.getSqlStatementContext();
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        databaseType.handleRollbackOnly(connectionSession.getTransactionStatus().isRollbackOnly(), sqlStatement);
        checkUnsupportedSQLStatement(sqlStatement);
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
            return TransactionBackendHandlerFactory.newInstance((SQLStatementContext<TCLStatement>) sqlStatementContext, sql, connectionSession);
        }
        Optional<ProxyBackendHandler> backendHandler = DatabaseAdminBackendHandlerFactory.newInstance(databaseType, sqlStatementContext, connectionSession, sql);
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
        SQLCheckEngine.check(sqlStatementContext, Collections.emptyList(),
                getRules(databaseName), databaseName, ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabases(), connectionSession.getGrantee());
        backendHandler = DatabaseAdminBackendHandlerFactory.newInstance(databaseType, sqlStatementContext, connectionSession);
        return backendHandler.orElseGet(() -> DatabaseBackendHandlerFactory.newInstance(queryContext, connectionSession, preferPreparedStatement));
    }
    
    private static void checkUnsupportedDistSQLStatementInTransaction(final SQLStatement sqlStatement, final ConnectionSession connectionSession) {
        ShardingSpherePreconditions.checkState(!connectionSession.getTransactionStatus().isInTransaction() || isSupportedDistSQLStatementInTransaction(sqlStatement),
                () -> new UnsupportedSQLOperationException("Non-query dist sql is not supported within a transaction"));
    }
    
    private static boolean isSupportedDistSQLStatementInTransaction(final SQLStatement sqlStatement) {
        return sqlStatement instanceof RQLStatement || sqlStatement instanceof QueryableRALStatement || sqlStatement instanceof RULStatement;
    }
    
    private static DatabaseType getProtocolType(final DatabaseType defaultDatabaseType, final ConnectionSession connectionSession) {
        String databaseName = connectionSession.getDatabaseName();
        return Strings.isNullOrEmpty(databaseName) || !ProxyContext.getInstance().databaseExists(databaseName)
                ? defaultDatabaseType
                : ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getProtocolType();
    }
    
    private static void handleAutoCommit(final SQLStatement sqlStatement, final ConnectionSession connectionSession) {
        if (AutoCommitUtils.needOpenTransaction(sqlStatement)) {
            connectionSession.getBackendConnection().handleAutoCommit();
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
    
    private static Collection<ShardingSphereRule> getRules(final String databaseName) {
        MetaDataContexts contexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        if (Strings.isNullOrEmpty(databaseName) || !ProxyContext.getInstance().databaseExists(databaseName)) {
            return contexts.getMetaData().getGlobalRuleMetaData().getRules();
        }
        Collection<ShardingSphereRule> result;
        result = new LinkedList<>(contexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getRules());
        result.addAll(contexts.getMetaData().getGlobalRuleMetaData().getRules());
        return result;
    }
    
    private static void checkUnsupportedSQLStatement(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof DCLStatement || sqlStatement instanceof FlushStatement || sqlStatement instanceof MySQLShowCreateUserStatement) {
            throw new UnsupportedSQLOperationException("Unsupported operation");
        }
    }
}
