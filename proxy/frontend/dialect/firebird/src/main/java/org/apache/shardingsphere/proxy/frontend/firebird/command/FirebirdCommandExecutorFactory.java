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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCancelBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCloseBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCreateBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdGetBlobSegmentCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdOpenBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdPutBlobSegmentCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdSeekBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdAllocateStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFetchStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFreeStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdPrepareStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdCommitTransactionPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdRollbackTransactionPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdStartTransactionPacket;
import org.apache.shardingsphere.database.protocol.packet.command.CommandPacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.admin.FirebirdUnsupportedCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors.FirebirdCancelBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors.FirebirdCloseBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors.FirebirdCreateBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors.FirebirdGetBlobSegmentCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors.FirebirdOpenBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors.FirebirdPutBlobSegmentCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors.FirebirdSeekBlobCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.info.FirebirdBlobInfoExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.info.FirebirdDatabaseInfoExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.info.FirebirdSQLInfoExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.allocate.FirebirdAllocateStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.execute.FirebirdExecuteStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.free.FirebirdFreeStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.prepare.FirebirdPrepareStatementCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdCommitTransactionCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdRollbackTransactionCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdStartTransactionCommandExecutor;

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
            case INFO_DATABASE:
                return new FirebirdDatabaseInfoExecutor((FirebirdInfoPacket) commandPacket, connectionSession);
            case INFO_BLOB:
                return new FirebirdBlobInfoExecutor((FirebirdInfoPacket) commandPacket);
            case TRANSACTION:
                return new FirebirdStartTransactionCommandExecutor((FirebirdStartTransactionPacket) commandPacket, connectionSession);
            case CREATE_BLOB:
            case CREATE_BLOB2:
                return new FirebirdCreateBlobCommandExecutor((FirebirdCreateBlobCommandPacket) commandPacket, connectionSession);
            case OPEN_BLOB:
            case OPEN_BLOB2:
                return new FirebirdOpenBlobCommandExecutor((FirebirdOpenBlobCommandPacket) commandPacket, connectionSession);
            case GET_SEGMENT:
                return new FirebirdGetBlobSegmentCommandExecutor((FirebirdGetBlobSegmentCommandPacket) commandPacket);
            case PUT_SEGMENT:
                return new FirebirdPutBlobSegmentCommandExecutor((FirebirdPutBlobSegmentCommandPacket) commandPacket, connectionSession);
            case CANCEL_BLOB:
                return new FirebirdCancelBlobCommandExecutor((FirebirdCancelBlobCommandPacket) commandPacket, connectionSession);
            case CLOSE_BLOB:
                return new FirebirdCloseBlobCommandExecutor((FirebirdCloseBlobCommandPacket) commandPacket, connectionSession);
            case SEEK_BLOB:
                return new FirebirdSeekBlobCommandExecutor((FirebirdSeekBlobCommandPacket) commandPacket, connectionSession);
            case ALLOCATE_STATEMENT:
                return new FirebirdAllocateStatementCommandExecutor((FirebirdAllocateStatementPacket) commandPacket, connectionSession);
            case PREPARE_STATEMENT:
                return new FirebirdPrepareStatementCommandExecutor((FirebirdPrepareStatementPacket) commandPacket, connectionSession);
            case EXECUTE:
            case EXECUTE2:
                return new FirebirdExecuteStatementCommandExecutor((FirebirdExecuteStatementPacket) commandPacket, connectionSession);
            case FETCH:
                return new FirebirdFetchStatementCommandExecutor((FirebirdFetchStatementPacket) commandPacket, connectionSession);
            case INFO_SQL:
                return new FirebirdSQLInfoExecutor((FirebirdInfoPacket) commandPacket, connectionSession);
            case COMMIT:
                return new FirebirdCommitTransactionCommandExecutor((FirebirdCommitTransactionPacket) commandPacket, connectionSession);
            case ROLLBACK:
                return new FirebirdRollbackTransactionCommandExecutor((FirebirdRollbackTransactionPacket) commandPacket, connectionSession);
            case FREE_STATEMENT:
                return new FirebirdFreeStatementCommandExecutor((FirebirdFreeStatementPacket) commandPacket, connectionSession);
            default:
                return new FirebirdUnsupportedCommandExecutor();
        }
    }
}
