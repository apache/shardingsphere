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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.binary.BinaryCell;
import org.apache.shardingsphere.database.protocol.binary.BinaryRow;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdSQLResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.upload.FirebirdBlobUploadCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Firebird execute statement command executor.
 */
@RequiredArgsConstructor
@Slf4j
public final class FirebirdExecuteStatementCommandExecutor implements CommandExecutor {
    
    private final FirebirdExecuteStatementPacket packet;
    
    private final ConnectionSession connectionSession;
    
    private ProxyBackendHandler proxyBackendHandler;
    
    @Getter
    private ResponseType responseType;
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        FirebirdServerPreparedStatement preparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(packet.getStatementId());
        List<Object> params = packet.getParameterValues();
        List<Long> blobIdsToRemove = bindBlobParameters(params);
        SQLStatementContext sqlStatementContext = preparedStatement.getSqlStatementContext();
        if (sqlStatementContext instanceof ParameterAware) {
            ((ParameterAware) sqlStatementContext).bindParameters(params);
        }
        QueryContext queryContext = new QueryContext(sqlStatementContext, preparedStatement.getSql(), params, preparedStatement.getHintValueContext(), connectionSession.getConnectionContext(),
                ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(), true);
        proxyBackendHandler = ProxyBackendHandlerFactory.newInstance(TypedSPILoader.getService(DatabaseType.class, "Firebird"), queryContext, connectionSession, true);
        ResponseHeader responseHeader = proxyBackendHandler.execute();
        if (responseHeader instanceof QueryResponseHeader) {
            responseType = ResponseType.QUERY;
            FirebirdFetchStatementCache.getInstance().registerStatement(connectionSession.getConnectionId(), packet.getStatementId(), proxyBackendHandler);
            connectionSession.getDatabaseConnectionManager().markResourceInUse(proxyBackendHandler);
        } else {
            responseType = ResponseType.UPDATE;
        }
        if (responseHeader instanceof UpdateResponseHeader) {
            UpdateResponseHeader updateResponseHeader = (UpdateResponseHeader) responseHeader;
            log.info("Firebird update count: {}", updateResponseHeader.getUpdateCount());
            clearBlobUploads(blobIdsToRemove);
        }
        Collection<DatabasePacket> result = new LinkedList<>();
        if (packet.isStoredProcedure() && proxyBackendHandler.next()) {
            result.add(getSQLResponse());
        }
        result.add(new FirebirdGenericResponsePacket());
        return result;
    }
    
    private List<Long> bindBlobParameters(final List<Object> params) {
        List<FirebirdBinaryColumnType> parameterTypes = packet.getParameterTypes();
        List<Long> blobIds = new LinkedList<>();
        int paramCount = Math.min(parameterTypes.size(), params.size());
        for (int i = 0; i < paramCount; i++) {
            if (parameterTypes.get(i) != FirebirdBinaryColumnType.BLOB) {
                continue;
            }
            Object paramValue = params.get(i);
            if (!(paramValue instanceof Long)) {
                log.info("Firebird BLOB parameter index={} bound as null (unexpected type)", i + 1);
                params.set(i, null);
                continue;
            }
            long blobId = (Long) paramValue;
            if (blobId <= 0L) {
                log.info("Firebird BLOB parameter index={} bound as null (blobId=0)", i + 1);
                params.set(i, null);
                continue;
            }
            Optional<byte[]> blobData = FirebirdBlobUploadCache.getInstance().getBlobData(connectionSession.getConnectionId(), blobId);
            if (!blobData.isPresent()) {
                log.info("Firebird BLOB parameter index={} bound as null (missing blobId={})", i + 1, blobId);
                params.set(i, null);
                continue;
            }
            byte[] bytes = blobData.get();
            boolean closed = FirebirdBlobUploadCache.getInstance().isClosed(connectionSession.getConnectionId(), blobId);
            if (!closed) {
                log.info("Firebird BLOB parameter index={} binding while upload still open (blobId={})", i + 1, blobId);
            }
            params.set(i, bytes);
            log.info("Firebird BLOB parameter index={} bound as byte[] length={}", i + 1, bytes.length);
            blobIds.add(blobId);
        }
        return blobIds;
    }
    
    private void clearBlobUploads(final List<Long> blobIds) {
        for (Long each : blobIds) {
            FirebirdBlobUploadCache.getInstance().removeUpload(connectionSession.getConnectionId(), each);
        }
    }
    
    private FirebirdSQLResponsePacket getSQLResponse() throws SQLException {
        QueryResponseRow queryResponseRow = proxyBackendHandler.getRowData();
        BinaryRow row = createBinaryRow(queryResponseRow);
        return new FirebirdSQLResponsePacket(row);
    }
    
    private BinaryRow createBinaryRow(final QueryResponseRow queryResponseRow) {
        List<BinaryCell> result = new ArrayList<>(queryResponseRow.getCells().size());
        for (QueryResponseCell each : queryResponseRow.getCells()) {
            result.add(new BinaryCell(FirebirdBinaryColumnType.valueOfJDBCType(each.getJdbcType()), each.getData()));
        }
        return new BinaryRow(result);
    }
}
