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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidSegstrIdException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidStatementHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidTransactionHandleException;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdErrorPacketFactory;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdStatusVector;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBlobBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdSQLResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.firebird.command.admin.FirebirdCancellationRegistry;
import org.apache.shardingsphere.proxy.frontend.firebird.command.admin.FirebirdCancellationRegistry.Scope;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch.FirebirdBatchStatementManager;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobWriteCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdBinaryRowBuilder;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementResourceCleaner;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionSessionRegistry;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.firebirdsql.gds.ISCConstants;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Firebird execute statement command executor.
 */
@RequiredArgsConstructor
public final class FirebirdExecuteStatementCommandExecutor implements CommandExecutor {
    
    private final FirebirdExecuteStatementPacket packet;
    
    private final ConnectionSession connectionSession;
    
    private ProxyBackendHandler proxyBackendHandler;
    
    @Getter
    private ResponseType responseType;
    
    @Override
    @HighFrequencyInvocation
    public Collection<DatabasePacket> execute() throws SQLException {
        int statementId = packet.getStatementId() & 0xFFFF;
        FirebirdServerPreparedStatement preparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(statementId);
        ShardingSpherePreconditions.checkNotNull(preparedStatement, () -> new InvalidStatementHandleException(packet.getStatementId()));
        validateTransactionHandle();
        ConnectionSession transactionSession = FirebirdTransactionSessionRegistry.getInstance().get(connectionSession, packet.getTransactionId());
        ShardingSpherePreconditions.checkNotNull(transactionSession, () -> new InvalidTransactionHandleException(packet.getTransactionId()));
        if (packet.isStoredProcedure() && -1 == (short) packet.getOutputMessageNumber()) {
            return Collections.singleton(new FirebirdGenericResponsePacket().setHandle(packet.getTransactionId()));
        }
        if (null != FirebirdBatchStatementManager.getInstance().getBatchStatement(connectionSession.getConnectionId(), statementId)) {
            FirebirdStatusVector statusVector = FirebirdStatusVector.fromSegments(Arrays.asList(
                    FirebirdStatusVector.Segment.withArguments(ISCConstants.isc_sqlerr, Collections.singletonList(FirebirdStatusVector.Argument.ofNumber(-502)), null),
                    FirebirdStatusVector.Segment.withArguments(ISCConstants.isc_dsql_cursor_open_err, Collections.emptyList(), null)));
            return Collections.singleton(new FirebirdGenericResponsePacket().setStatusVector(statusVector));
        }
        transactionSession.beginPreparedStatementCache(FirebirdStatementResourceCleaner.createPreparedStatementCacheKey(statementId));
        try {
            ResponseHeader responseHeader = executePreparedStatement(preparedStatement, packet.getParameterValues(), transactionSession);
            if (responseHeader instanceof QueryResponseHeader) {
                responseType = ResponseType.QUERY;
                FirebirdFetchStatementCache.getInstance().registerStatement(connectionSession.getConnectionId(), statementId, proxyBackendHandler);
                FirebirdFetchStatementCache.getInstance().registerOwner(connectionSession.getConnectionId(), statementId, transactionSession);
                transactionSession.getDatabaseConnectionManager().markResourceInUse(proxyBackendHandler);
            } else {
                responseType = ResponseType.UPDATE;
                preparedStatement.setAffectedRows(((UpdateResponseHeader) responseHeader).getUpdateCount());
            }
            Collection<DatabasePacket> result = new LinkedList<>();
            if (packet.isStoredProcedure() && proxyBackendHandler.next()) {
                result.add(getSQLResponse());
            }
            result.add(new FirebirdGenericResponsePacket().setHandle(getTransactionHandleAfterExecution(preparedStatement.getSqlStatementContext().getSqlStatement())));
            return result;
        } catch (final SQLException ex) {
            return Collections.singleton(FirebirdErrorPacketFactory.newInstance(ex));
        } catch (final ShardingSphereSQLException | SQLDialectException ex) {
            if (transactionSession == connectionSession && !(ex instanceof InvalidSegstrIdException)) {
                throw ex;
            }
            return Collections.singleton(FirebirdErrorPacketFactory.newInstance(ex));
        } finally {
            transactionSession.finishPreparedStatementCache();
            if (transactionSession != connectionSession) {
                transactionSession.clearQueryContext();
                FirebirdTransactionSessionRegistry.getInstance().closeExecutionResources(transactionSession);
            }
        }
    }
    
    private void validateTransactionHandle() {
        ShardingSpherePreconditions.checkState(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(connectionSession.getConnectionId(), packet.getTransactionId()),
                () -> new InvalidTransactionHandleException(packet.getTransactionId()));
    }
    
    private ResponseHeader executePreparedStatement(final FirebirdServerPreparedStatement preparedStatement, final List<Object> params,
                                                    final ConnectionSession transactionSession) throws SQLException {
        Collection<Long> blobIdsToRemove = bindBlobParameters(params);
        try {
            SQLStatementContext sqlStatementContext = preparedStatement.getSqlStatementContext();
            if (sqlStatementContext instanceof ParameterAware) {
                ((ParameterAware) sqlStatementContext).bindParameters(params);
            }
            QueryContext queryContext = new QueryContext(sqlStatementContext, preparedStatement.getSql(), params, preparedStatement.getHintValueContext(), transactionSession.getConnectionContext(),
                    ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(), true);
            proxyBackendHandler = ProxyBackendHandlerFactory.newInstance(TypedSPILoader.getService(DatabaseType.class, "Firebird"), queryContext, transactionSession, true);
            try (Scope ignored = FirebirdCancellationRegistry.getInstance().enter(connectionSession.getConnectionId(), transactionSession.getDatabaseConnectionManager())) {
                return proxyBackendHandler.execute();
            }
        } finally {
            clearBlobUploads(blobIdsToRemove);
        }
    }
    
    private Collection<Long> bindBlobParameters(final List<Object> params) {
        List<FirebirdBinaryColumnType> parameterTypes = packet.getParameterTypes();
        Collection<Long> result = new LinkedList<>();
        int paramCount = Math.min(parameterTypes.size(), params.size());
        for (int i = 0; i < paramCount; i++) {
            if (parameterTypes.get(i) != FirebirdBinaryColumnType.BLOB) {
                continue;
            }
            Object paramValue = params.get(i);
            if (!(paramValue instanceof Long)) {
                params.set(i, null);
                continue;
            }
            long blobId = (Long) paramValue;
            if (0L == blobId) {
                params.set(i, new byte[0]);
                continue;
            }
            if (blobId < 0L) {
                byte[] resultBlobContent = FirebirdBlobBinaryProtocolValue.getBlobContent(connectionSession.getConnectionId(), blobId);
                ShardingSpherePreconditions.checkNotNull(resultBlobContent, () -> new InvalidSegstrIdException(blobId));
                params.set(i, resultBlobContent);
                continue;
            }
            ShardingSpherePreconditions.checkState(FirebirdBlobWriteCache.getInstance().isClosed(connectionSession.getConnectionId(), packet.getTransactionId(), blobId),
                    () -> new InvalidSegstrIdException(blobId));
            params.set(i, FirebirdBlobWriteCache.getInstance().getBlobData(connectionSession.getConnectionId(), packet.getTransactionId(), blobId).get());
            result.add(blobId);
        }
        return result;
    }
    
    private void clearBlobUploads(final Collection<Long> blobIds) {
        for (Long each : blobIds) {
            FirebirdBlobWriteCache.getInstance().removeWrite(connectionSession.getConnectionId(), packet.getTransactionId(), each);
        }
    }
    
    private FirebirdSQLResponsePacket getSQLResponse() throws SQLException {
        return new FirebirdSQLResponsePacket(FirebirdBinaryRowBuilder.build(proxyBackendHandler.getRowData()));
    }
    
    @HighFrequencyInvocation
    private int getTransactionHandleAfterExecution(final SQLStatement sqlStatement) throws SQLException {
        int transactionId = packet.getTransactionId();
        if (sqlStatement instanceof CommitStatement || sqlStatement instanceof RollbackStatement && !((RollbackStatement) sqlStatement).getSavepointName().isPresent()) {
            FirebirdTransactionIdGenerator.getInstance().closeTransaction(connectionSession.getConnectionId(), transactionId);
            FirebirdTransactionSessionRegistry.getInstance().unregister(connectionSession, transactionId);
            return 0;
        }
        return transactionId;
    }
}
