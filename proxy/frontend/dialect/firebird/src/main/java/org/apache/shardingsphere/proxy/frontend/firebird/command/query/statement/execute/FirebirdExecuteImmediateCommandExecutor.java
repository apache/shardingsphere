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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidTransactionHandleException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteImmediatePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbExceptionBuilder;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * Executes Firebird SQL without a prepared statement handle.
 */
@RequiredArgsConstructor
public final class FirebirdExecuteImmediateCommandExecutor implements CommandExecutor {
    
    private final FirebirdExecuteImmediatePacket packet;
    
    private final ConnectionSession connectionSession;
    
    @HighFrequencyInvocation
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        int connectionId = connectionSession.getConnectionId();
        int transactionId = packet.getTransactionId();
        FirebirdTransactionIdGenerator transactionIds = FirebirdTransactionIdGenerator.getInstance();
        ShardingSpherePreconditions.checkState(0 == transactionId || transactionIds.isTransactionActive(connectionId, transactionId),
                () -> new InvalidTransactionHandleException(transactionId));
        DatabaseType databaseType = connectionSession.getProtocolType();
        SQLParserRule sqlParserRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(databaseType).parse(packet.getSQL(), true);
        if (sqlStatement instanceof SetTransactionStatement) {
            return Collections.singleton(new FirebirdGenericResponsePacket().setHandle(startTransaction((SetTransactionStatement) sqlStatement)));
        }
        ShardingSpherePreconditions.checkState(0 != transactionId, () -> new InvalidTransactionHandleException(transactionId));
        validateMessages(sqlStatement);
        ProxyBackendHandlerFactory.newInstance(databaseType, packet.getSQL(), sqlStatement, connectionSession, packet.getHintValueContext()).execute();
        if (sqlStatement instanceof CommitStatement || sqlStatement instanceof RollbackStatement) {
            transactionIds.closeTransaction(connectionId, transactionId);
            return Collections.singleton(new FirebirdGenericResponsePacket());
        }
        return Collections.singleton(new FirebirdGenericResponsePacket().setHandle(transactionId));
    }
    
    private int startTransaction(final SetTransactionStatement sqlStatement) {
        int transactionId = packet.getTransactionId();
        ShardingSpherePreconditions.checkState(0 == transactionId, () -> new InvalidTransactionHandleException(transactionId));
        int connectionId = connectionSession.getConnectionId();
        connectionSession.setAutoCommit(false);
        connectionSession.setReadOnly(false);
        connectionSession.setIsolationLevel(sqlStatement.getIsolationLevel().orElse(TransactionIsolationLevel.REPEATABLE_READ));
        return FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionId);
    }
    
    private void validateMessages(final SQLStatement sqlStatement) throws SQLException {
        ShardingSpherePreconditions.checkState(!requiresOutputMessage(sqlStatement), () -> FbExceptionBuilder.forException(ISCConstants.isc_dsql_no_output_sqlda).toSQLException());
        ShardingSpherePreconditions.checkState(0 == sqlStatement.getParameterCount(), () -> FbExceptionBuilder.forException(ISCConstants.isc_dsql_no_input_sqlda).toSQLException());
    }
    
    private boolean requiresOutputMessage(final SQLStatement sqlStatement) {
        return sqlStatement instanceof SelectStatement
                || sqlStatement instanceof InsertStatement && ((InsertStatement) sqlStatement).getInsertSelect().isPresent() && ((InsertStatement) sqlStatement).getReturning().isPresent()
                || sqlStatement instanceof UpdateStatement && ((UpdateStatement) sqlStatement).getReturning().isPresent()
                || sqlStatement instanceof DeleteStatement && ((DeleteStatement) sqlStatement).getReturning().isPresent();
    }
}
