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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchExecuteCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchSendMessageCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdBatchCompletionStateResponse;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public final class FirebirdBatchExecuteCommandExecutor implements CommandExecutor {
    
    private final FirebirdBatchExecuteCommandPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        FirebirdBatchStatement batchStatement = FirebirdBatchRegistry.getInstance().getBatchStatement(connectionSession.getConnectionId(), packet.getStatementHandle());
        assert batchStatement != null;
        FirebirdServerPreparedStatement preparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(batchStatement.getStatementHandle());
        int[] updateCounts = new FirebirdBatchedStatementsExecutor(connectionSession, preparedStatement, batchStatement.getParameterValues()).executeBatch();
        batchStatement.clearParameterValues();
        FirebirdBatchSendMessageCommandPacket.resetBatchMessageHeader(connectionSession.getConnectionId());
        long updatedRecordsCount = 0L;
        for (int each : updateCounts) {
            if (each > 0) {
                updatedRecordsCount += each;
            }
        }
        return Collections.singleton(new FirebirdBatchCompletionStateResponse()
                .setHandle(packet.getStatementHandle())
                .setRecordsCount(updatedRecordsCount)
                .setUpdateCounts(updateCounts));
    }
}
