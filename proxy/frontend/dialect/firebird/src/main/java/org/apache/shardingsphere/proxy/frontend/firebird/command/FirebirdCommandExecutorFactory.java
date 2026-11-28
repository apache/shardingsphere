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

package org.apache.shardingsphere.proxy.frontend.firebird.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.batches.create.FirebirdBatchCreateCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.batches.execute.FirebirdBatchExecuteCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.batches.messages.FirebirdBatchMessageCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.blobs.open.FirebirdBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.blobs.seek.FirebirdSeekBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.blobs.segments.FirebirdSegmentCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdSingleHandleCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.commonrequests.FirebirdInfoCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.statements.execute.FirebirdExecuteStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.statements.fetch.FirebirdFetchStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.statements.free.FirebirdFreeStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.statements.prepare.FirebirdPrepareStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.transactions.start.FirebirdStartTransactionPacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.batches.cancel.FirebirdBatchCancelCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.batches.create.FirebirdBatchCreateCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.batches.execute.FirebirdBatchExecuteCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.batches.messages.FirebirdBatchMessageCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.batches.release.FirebirdBatchReleaseCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.batches.sync.FirebirdBatchSyncCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.blobs.end.FirebirdEndBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.blobs.open.FirebirdOpenBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.blobs.seek.FirebirdSeekBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.blobs.segments.FirebirdGetSegmentCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.blobs.segments.FirebirdPutSegmentCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.databases.end.FirebirdEndDatabaseCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.info.FirebirdInfoCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.ping.FirebirdPingConnectionCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.statements.allocate.FirebirdAllocateStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.statements.execute.FirebirdExecuteStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.statements.fetch.FirebirdFetchCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.statements.free.FirebirdEndStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.statements.prepare.FirebirdPrepareStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.transactions.end.FirebirdEndTransactionCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.transactions.start.FirebirdStartTransactionCommandExecutor;

/**
 * Command executor factory for Firebird.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdCommandExecutorFactory {
    
    /**
     * Create new instance of command executor.
     *
     * @param commandPacketType command packet type for Firebird
     * @param commandPacket command packet for Firebird
     * @param connectionSession connection session
     * @return created instance
     */
    public static CommandExecutor newInstance(final FirebirdCommandPacketType commandPacketType, final CommandPacket commandPacket, final ConnectionSession connectionSession) {
        switch (commandPacketType) {
            case COMMIT:
            case ROLLBACK:
                return new FirebirdEndTransactionCommandExecutor((FirebirdSingleHandleCommandPacket) commandPacket, connectionSession, commandPacketType);
            case DETACH:
                return new FirebirdEndDatabaseCommandExecutor();
            case CANCEL_BLOB:
            case CLOSE_BLOB:
                return new FirebirdEndBlobCommandExecutor((FirebirdSingleHandleCommandPacket) commandPacket, connectionSession, commandPacketType);
            case ALLOCATE_STATEMENT:
                return new FirebirdAllocateStatementCommandExecutor(connectionSession);
            case INFO_BLOB:
            case INFO_DATABASE:
            case INFO_SQL:
                return new FirebirdInfoCommandExecutor((FirebirdInfoCommandPacket) commandPacket, connectionSession, commandPacketType);
            case TRANSACTION:
                return new FirebirdStartTransactionCommandExecutor((FirebirdStartTransactionPacket) commandPacket, connectionSession);
            case CREATE_BLOB:
            case OPEN_BLOB:
            case CREATE_BLOB2:
            case OPEN_BLOB2:
                return new FirebirdOpenBlobCommandExecutor((FirebirdBlobCommandPacket) commandPacket, connectionSession, commandPacketType);
            case GET_SEGMENT:
                return new FirebirdGetSegmentCommandExecutor((FirebirdSegmentCommandPacket) commandPacket, connectionSession);
            case PUT_SEGMENT:
            case BATCH_SEGMENTS:
                return new FirebirdPutSegmentCommandExecutor((FirebirdSegmentCommandPacket) commandPacket, connectionSession);
            case SEEK_BLOB:
                return new FirebirdSeekBlobCommandExecutor((FirebirdSeekBlobCommandPacket) commandPacket, connectionSession);
            case PREPARE_STATEMENT:
                return new FirebirdPrepareStatementCommandExecutor((FirebirdPrepareStatementPacket) commandPacket, connectionSession);
            case EXECUTE:
            case EXECUTE2:
                return new FirebirdExecuteStatementCommandExecutor((FirebirdExecuteStatementPacket) commandPacket, connectionSession);
            case FETCH:
                return new FirebirdFetchCommandExecutor((FirebirdFetchStatementPacket) commandPacket, connectionSession);
            case FREE_STATEMENT:
                return new FirebirdEndStatementCommandExecutor((FirebirdFreeStatementPacket) commandPacket, connectionSession);
            case PING:
                return new FirebirdPingConnectionCommandExecutor();
            case BATCH_CREATE:
                return new FirebirdBatchCreateCommandExecutor((FirebirdBatchCreateCommandPacket) commandPacket, connectionSession);
            case BATCH_MSG:
                return new FirebirdBatchMessageCommandExecutor((FirebirdBatchMessageCommandPacket) commandPacket, connectionSession);
            case BATCH_EXEC:
                return new FirebirdBatchExecuteCommandExecutor((FirebirdBatchExecuteCommandPacket) commandPacket, connectionSession);
            case BATCH_RLS:
                return new FirebirdBatchReleaseCommandExecutor((FirebirdSingleHandleCommandPacket) commandPacket, connectionSession);
            case BATCH_CANCEL:
                return new FirebirdBatchCancelCommandExecutor((FirebirdSingleHandleCommandPacket) commandPacket, connectionSession);
            case BATCH_SYNC:
                return new FirebirdBatchSyncCommandExecutor();
            default:
                return new FirebirdUnsupportedCommandExecutor(commandPacket, commandPacketType);
        }
    }
}
