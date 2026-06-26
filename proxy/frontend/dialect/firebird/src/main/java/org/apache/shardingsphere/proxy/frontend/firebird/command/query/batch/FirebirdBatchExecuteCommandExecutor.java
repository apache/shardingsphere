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
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdStatusVector;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchExecuteCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdBatchCompletionStateResponse;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public final class FirebirdBatchExecuteCommandExecutor implements CommandExecutor {
    
    private final FirebirdBatchExecuteCommandPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        FirebirdBatchStatement batchStatement = FirebirdBatchRegistry.getInstance().getBatchStatement(connectionSession.getConnectionId(), packet.getStatementHandle());
        if (null == batchStatement) {
            throw new InvalidBatchHandleException(packet.getStatementHandle());
        }
        FirebirdServerPreparedStatement preparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(batchStatement.getStatementHandle());
        int messageCount = batchStatement.getParameterValues().size();
        FirebirdBatchedStatementsExecutor executor = new FirebirdBatchedStatementsExecutor(connectionSession, preparedStatement, batchStatement.getParameterValues());
        try {
            int[] updateCounts = executor.executeBatch();
            batchStatement.reset();
            return Collections.singleton(new FirebirdBatchCompletionStateResponse()
                    .setHandle(packet.getStatementHandle())
                    .setRecordsCount(messageCount)
                    .setUpdateCounts(batchStatement.isRecordCounts() ? updateCounts : new int[0]));
        } catch (final BatchUpdateException ex) {
            batchStatement.reset();
            return Collections.singleton(createErrorResponse(ex, messageCount, batchStatement.isRecordCounts()));
        }
    }
    
    private FirebirdBatchCompletionStateResponse createErrorResponse(final BatchUpdateException ex, final int messageCount, final boolean recordCounts) {
        int[] updateCounts = null == ex.getUpdateCounts() ? new int[0] : ex.getUpdateCounts();
        return new FirebirdBatchCompletionStateResponse()
                .setHandle(packet.getStatementHandle())
                .setRecordsCount(messageCount)
                .setUpdateCounts(recordCounts ? updateCounts : new int[0])
                .addDetailedError(getFailedElement(updateCounts, messageCount), new FirebirdStatusVector(ex));
    }
    
    /**
     * Get the index of the failed batch element. The backend batch runs in halt-at-first-error mode
     * ({@code FbBatchConfig.HALT_AT_FIRST_ERROR}), so a failure yields exactly one failed record: the first
     * {@link Statement#EXECUTE_FAILED} entry, or the count of successful records when no marker is present.
     *
     * @param updateCounts update counts carried by the batch failure
     * @param messageCount total number of batch messages
     * @return zero-based index of the failed element
     */
    private int getFailedElement(final int[] updateCounts, final int messageCount) {
        for (int i = 0; i < updateCounts.length; i++) {
            if (Statement.EXECUTE_FAILED == updateCounts[i]) {
                return i;
            }
        }
        return Math.max(0, Math.min(updateCounts.length, messageCount - 1));
    }
}
