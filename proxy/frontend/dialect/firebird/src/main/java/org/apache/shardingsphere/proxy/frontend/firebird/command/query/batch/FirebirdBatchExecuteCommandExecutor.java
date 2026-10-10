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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.SQLExceptionTransformEngine;
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidSegstrIdException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidTransactionHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ParameterConversionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.UnknownBatchBlobIdException;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdConstant;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdStatusVector;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchColumnDescriptor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchExecuteCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBlobBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdReturnColumnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdBatchCompletionStateResponse;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.FirebirdBlobParameterConverter;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobWriteCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public final class FirebirdBatchExecuteCommandExecutor implements CommandExecutor {
    
    private final FirebirdBatchExecuteCommandPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        FirebirdBatchStatementManager batchStatementManager = FirebirdBatchStatementManager.getInstance();
        FirebirdBatchStatement batchStatement = batchStatementManager.getBatchStatement(connectionSession.getConnectionId(), packet.getStatementHandle());
        if (null == batchStatement) {
            throw new InvalidBatchHandleException(packet.getStatementHandle());
        }
        validateTransactionHandle();
        registerStreamBlobs(batchStatementManager, batchStatement);
        int messageCount = batchStatement.getParameterValues().size();
        if (batchStatement.getParameterValues().isEmpty()) {
            batchStatementManager.resetBatchStatement(batchStatement);
            return Collections.singleton(new FirebirdBatchCompletionStateResponse()
                    .setHandle(packet.getStatementHandle())
                    .setRecordsCount(messageCount)
                    .setUpdateCounts(new int[0]));
        }
        FirebirdServerPreparedStatement preparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(batchStatement.getStatementHandle());
        FirebirdBatchCompletion completion = executeBatch(batchStatement, preparedStatement);
        batchStatementManager.resetBatchStatement(batchStatement);
        return Collections.singleton(createResponse(completion, batchStatement.isRecordCounts()));
    }
    
    private void validateTransactionHandle() {
        ShardingSpherePreconditions.checkState(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(connectionSession.getConnectionId(), packet.getTransactionHandle()),
                () -> new InvalidTransactionHandleException(packet.getTransactionHandle()));
    }
    
    private void registerStreamBlobs(final FirebirdBatchStatementManager batchStatementManager, final FirebirdBatchStatement batchStatement) {
        if (batchStatement.getBlobBuffer().isEmpty()) {
            return;
        }
        try {
            batchStatementManager.registerStreamBlobs(batchStatement, connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get());
        } catch (final SQLDialectException ex) {
            batchStatementManager.resetBatchStatement(batchStatement);
            throw ex;
        }
    }
    
    private FirebirdBatchCompletion executeBatch(final FirebirdBatchStatement batchStatement, final FirebirdServerPreparedStatement preparedStatement) throws SQLException {
        List<FirebirdBatchColumnDescriptor> columnDescriptors = batchStatement.getColumnDescriptors();
        boolean containsBlob = false;
        for (FirebirdBatchColumnDescriptor each : columnDescriptors) {
            containsBlob |= FirebirdBinaryColumnType.BLOB == each.getType();
        }
        if (!containsBlob) {
            return executeParameters(preparedStatement, batchStatement.getParameterValues(), batchStatement.isMultiError());
        }
        return executeBlobBatch(batchStatement, preparedStatement);
    }
    
    private FirebirdBatchCompletion executeParameters(final FirebirdServerPreparedStatement preparedStatement, final List<List<Object>> params, final boolean multiError) throws SQLException {
        if (params.isEmpty()) {
            return new FirebirdBatchCompletion(0, new int[0]);
        }
        return new FirebirdBatchedStatementsExecutor(connectionSession, preparedStatement, params, multiError).executeBatch();
    }
    
    private FirebirdBatchCompletion executeBlobBatch(final FirebirdBatchStatement batchStatement, final FirebirdServerPreparedStatement preparedStatement) throws SQLException {
        Map<Long, Long> blobIds = new HashMap<>(batchStatement.getBlobIds());
        Map<Long, byte[]> streamBlobContents = new HashMap<>(batchStatement.getStreamBlobContents());
        List<List<Object>> params = new ArrayList<>(batchStatement.getParameterValues().size());
        Collection<FirebirdBatchCompletion> completions = new ArrayList<>();
        for (List<Object> each : batchStatement.getParameterValues()) {
            SQLDialectException failure;
            try {
                params.add(bindRowBlobParameters(each, batchStatement.getColumnDescriptors(), preparedStatement.getParameterColumns(), blobIds, streamBlobContents));
                continue;
            } catch (final InvalidSegstrIdException | UnknownBatchBlobIdException | ParameterConversionException ex) {
                failure = ex;
            }
            FirebirdBatchCompletion completion = executeParameters(preparedStatement, params, batchStatement.isMultiError());
            completions.add(completion);
            if (completion.getRecordsCount() < params.size() || !batchStatement.isMultiError() && !completion.getFailures().isEmpty()) {
                return mergeCompletions(completions);
            }
            params.clear();
            if (!(failure instanceof InvalidSegstrIdException)) {
                batchStatement.getBlobIds().keySet().retainAll(blobIds.keySet());
                batchStatement.getStreamBlobContents().keySet().retainAll(streamBlobContents.keySet());
                throw failure;
            }
            SQLException cause = SQLExceptionTransformEngine.toSQLException(failure, TypedSPILoader.getService(DatabaseType.class, "Firebird"));
            completions.add(new FirebirdBatchCompletion(1, new int[]{FirebirdBatchCompletion.EXECUTE_FAILED}, Collections.singleton(new FirebirdBatchCompletion.Failure(0, cause))));
            if (!batchStatement.isMultiError()) {
                return mergeCompletions(completions);
            }
        }
        FirebirdBatchCompletion completion = executeParameters(preparedStatement, params, batchStatement.isMultiError());
        if (completions.isEmpty()) {
            return completion;
        }
        completions.add(completion);
        return mergeCompletions(completions);
    }
    
    private List<Object> bindRowBlobParameters(final List<Object> params, final List<FirebirdBatchColumnDescriptor> columnDescriptors, final List<FirebirdReturnColumnPacket> parameterColumns,
                                               final Map<Long, Long> blobIds, final Map<Long, byte[]> streamBlobContents) {
        List<Object> result = new ArrayList<>(params);
        resolveBatchBlobIds(result, columnDescriptors, blobIds, streamBlobContents);
        boolean[] moved = moveReadBlobParameters(result, columnDescriptors, parameterColumns);
        for (int i = 0; i < columnDescriptors.size(); i++) {
            if (!moved[i] && isBlobValue(columnDescriptors.get(i), result.get(i))) {
                byte[] content = getBlobContent(result.get(i));
                result.set(i, i < parameterColumns.size() ? createBlobParameterConverter().convert(content, columnDescriptors.get(i), parameterColumns.get(i)) : content);
            }
        }
        return result;
    }
    
    private void resolveBatchBlobIds(final List<Object> params, final List<FirebirdBatchColumnDescriptor> columnDescriptors, final Map<Long, Long> blobIds,
                                     final Map<Long, byte[]> streamBlobContents) {
        for (int i = 0; i < columnDescriptors.size(); i++) {
            if (!columnDescriptors.get(i).isBatchBlobId() || null == params.get(i) || 0L == (Long) params.get(i)) {
                continue;
            }
            long batchBlobId = (Long) params.get(i);
            Object blob = blobIds.containsKey(batchBlobId) ? blobIds.remove(batchBlobId) : streamBlobContents.remove(batchBlobId);
            ShardingSpherePreconditions.checkNotNull(blob, () -> new UnknownBatchBlobIdException(batchBlobId));
            params.set(i, blob);
        }
    }
    
    private boolean[] moveReadBlobParameters(final List<Object> params, final List<FirebirdBatchColumnDescriptor> columnDescriptors, final List<FirebirdReturnColumnPacket> parameterColumns) {
        boolean[] result = new boolean[columnDescriptors.size()];
        for (int i = Math.min(columnDescriptors.size(), parameterColumns.size()) - 1; i >= 0; i--) {
            if (!isBlobValue(columnDescriptors.get(i), params.get(i))) {
                continue;
            }
            FirebirdBlobParameterConverter converter = createBlobParameterConverter();
            if (converter.isReadOnMove(columnDescriptors.get(i), parameterColumns.get(i))) {
                params.set(i, converter.convert(getMovedBlobContent(params.get(i)), columnDescriptors.get(i), parameterColumns.get(i)));
                result[i] = true;
            }
        }
        return result;
    }
    
    private boolean isBlobValue(final FirebirdBatchColumnDescriptor columnDescriptor, final Object value) {
        return FirebirdBinaryColumnType.BLOB == columnDescriptor.getType() && (value instanceof Long || value instanceof byte[]);
    }
    
    private FirebirdBlobParameterConverter createBlobParameterConverter() {
        return new FirebirdBlobParameterConverter(connectionSession.getAttributeMap().attr(FirebirdConstant.CONNECTION_CHARSET_ID).get());
    }
    
    private byte[] getMovedBlobContent(final Object value) {
        try {
            return getBlobContent(value);
        } catch (final InvalidSegstrIdException ex) {
            throw new ParameterConversionException(ex);
        }
    }
    
    private byte[] getBlobContent(final Object value) {
        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        long blobId = (Long) value;
        if (0L == blobId) {
            return new byte[0];
        }
        if (blobId < 0L) {
            byte[] result = FirebirdBlobBinaryProtocolValue.getBlobContent(connectionSession.getConnectionId(), blobId);
            ShardingSpherePreconditions.checkNotNull(result, () -> new InvalidSegstrIdException(blobId));
            return result;
        }
        return FirebirdBlobWriteCache.getInstance().useBlobData(connectionSession.getConnectionId(), blobId, packet.getTransactionHandle()).orElseThrow(() -> new InvalidSegstrIdException(blobId));
    }
    
    private FirebirdBatchCompletion mergeCompletions(final Collection<FirebirdBatchCompletion> completions) {
        int recordsCount = 0;
        for (FirebirdBatchCompletion each : completions) {
            recordsCount += each.getRecordsCount();
        }
        int[] updateCounts = new int[recordsCount];
        Collection<FirebirdBatchCompletion.Failure> failures = new ArrayList<>();
        int offset = 0;
        for (FirebirdBatchCompletion each : completions) {
            System.arraycopy(each.getUpdateCounts(), 0, updateCounts, offset, each.getRecordsCount());
            appendFailures(failures, each, offset);
            offset += each.getRecordsCount();
        }
        return new FirebirdBatchCompletion(recordsCount, updateCounts, failures);
    }
    
    private void appendFailures(final Collection<FirebirdBatchCompletion.Failure> failures, final FirebirdBatchCompletion completion, final int offset) {
        for (FirebirdBatchCompletion.Failure each : completion.getFailures()) {
            failures.add(new FirebirdBatchCompletion.Failure(offset + each.getMessageIndex(), each.getCause()));
        }
    }
    
    private FirebirdBatchCompletionStateResponse createResponse(final FirebirdBatchCompletion completion, final boolean recordCounts) {
        FirebirdBatchCompletionStateResponse result = new FirebirdBatchCompletionStateResponse()
                .setHandle(packet.getStatementHandle())
                .setRecordsCount(completion.getRecordsCount())
                .setUpdateCounts(recordCounts ? completion.getUpdateCounts() : new int[0]);
        for (FirebirdBatchCompletion.Failure each : completion.getFailures()) {
            result.addDetailedError(each.getMessageIndex(), new FirebirdStatusVector(each.getCause()));
        }
        return result;
    }
}
