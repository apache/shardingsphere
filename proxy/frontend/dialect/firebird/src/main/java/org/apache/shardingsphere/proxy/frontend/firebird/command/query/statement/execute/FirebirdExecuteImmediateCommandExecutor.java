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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.execute;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteImmediatePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.BackendConnectionException;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionAccessType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * Firebird execute immediate command executor.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public final class FirebirdExecuteImmediateCommandExecutor implements CommandExecutor {
    
    private final FirebirdExecuteImmediatePacket packet;
    
    private final ConnectionSession connectionSession;
    
    private ConnectionSession transactionSession;
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData();
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
        SQLStatement sqlStatement = metaData.getGlobalRuleMetaData().getSingleRule(SQLParserRule.class).getSQLParserEngine(databaseType).parse(packet.getSQL(), true);
        if (sqlStatement instanceof SetTransactionStatement) {
            ShardingSpherePreconditions.checkState(0 == packet.getTransactionId(), () -> new DatabaseProtocolException("Transaction is already active"));
            SetTransactionStatement statement = (SetTransactionStatement) sqlStatement;
            int transactionId = FirebirdTransactionIdGenerator.getInstance().startTransaction(connectionSession, false, statement.isDesiredAccessMode(TransactionAccessType.READ_ONLY),
                    statement.getIsolationLevel().orElse(TransactionIsolationLevel.REPEATABLE_READ));
            return Collections.singleton(new FirebirdGenericResponsePacket().setHandle(transactionId));
        }
        ShardingSpherePreconditions.checkState(!isResultSetStatement(sqlStatement), () -> new UnsupportedSQLOperationException("Execute immediate does not support a result set"));
        FirebirdTransactionIdGenerator transactionIdGenerator = FirebirdTransactionIdGenerator.getInstance();
        transactionSession = sqlStatement instanceof CommitStatement || sqlStatement instanceof RollbackStatement
                ? transactionIdGenerator.getTransactionSession(connectionSession.getConnectionId(), packet.getTransactionId())
                : transactionIdGenerator.getTransactionSessionForExecution(connectionSession.getConnectionId(), packet.getTransactionId());
        SQLStatementContext sqlStatementContext = new SQLBindEngine(metaData, transactionSession.getCurrentDatabaseName(), packet.getHintValueContext()).bind(sqlStatement);
        QueryContext queryContext =
                new QueryContext(sqlStatementContext, packet.getSQL(), Collections.emptyList(), packet.getHintValueContext(), transactionSession.getConnectionContext(), metaData, true);
        ProxyBackendHandler handler = ProxyBackendHandlerFactory.newInstance(databaseType, queryContext, transactionSession, true);
        ShardingSpherePreconditions.checkState(!(handler.execute() instanceof QueryResponseHeader), () -> new UnsupportedSQLOperationException("Execute immediate does not support a result set"));
        int transactionId = packet.getTransactionId();
        if (sqlStatement instanceof CommitStatement || sqlStatement instanceof RollbackStatement) {
            FirebirdTransactionIdGenerator.getInstance().closeTransaction(connectionSession.getConnectionId(), transactionId);
            transactionSession = null;
            transactionId = 0;
        }
        return Collections.singleton(new FirebirdGenericResponsePacket().setHandle(transactionId));
    }
    
    private boolean isResultSetStatement(final SQLStatement statement) {
        return statement instanceof SelectStatement
                || statement instanceof InsertStatement && ((InsertStatement) statement).getReturning().isPresent()
                || statement instanceof UpdateStatement && ((UpdateStatement) statement).getReturning().isPresent()
                || statement instanceof DeleteStatement && ((DeleteStatement) statement).getReturning().isPresent();
    }
    
    @Override
    @SneakyThrows(BackendConnectionException.class)
    public void close() {
        if (null != transactionSession) {
            transactionSession.clearQueryContext();
            transactionSession.getDatabaseConnectionManager().closeExecutionResources();
        }
    }
}
