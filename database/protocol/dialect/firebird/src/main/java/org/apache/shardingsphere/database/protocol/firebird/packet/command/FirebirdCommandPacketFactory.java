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

package org.apache.shardingsphere.database.protocol.firebird.packet.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.UnsupportedCommandException;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.admin.FirebirdUnsupportedCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdSingleHandleCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchCreateCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchExecuteCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchMessageCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchSyncCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdSeekBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdSegmentCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFetchStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFreeStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdPrepareStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdStartTransactionPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

/**
 * Command packet factory for Firebird.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdCommandPacketFactory {
    
    /**
     * Create new instance of command packet.
     *
     * @param commandPacketType command packet type for Firebird
     * @param payload packet payload for Firebird
     * @param protocolVersion protocol version of Firebird
     * @param connectionId connection ID
     * @return created instance
     */
    public static FirebirdCommandPacket newInstance(final FirebirdCommandPacketType commandPacketType, final FirebirdPacketPayload payload, final FirebirdProtocolVersion protocolVersion,
                                                    final int connectionId) {
        switch (commandPacketType) {
            case COMMIT:
            case PREPARE:
            case ROLLBACK:
            case UNWIND:
            case RELEASE:
            case CLOSE_BLOB:
            case CANCEL_BLOB:
            case DETACH:
            case DROP_DATABASE:
            case SERVICE_DETACH:
            case COMMIT_RETAINING:
            case ROLLBACK_RETAINING:
            case ALLOCATE_STATEMENT:
            case BATCH_RLS:
            case BATCH_CANCEL:
                return new FirebirdSingleHandleCommandPacket(payload);
            case INFO_DATABASE:
            case INFO_BLOB:
            case INFO_SQL:
                return new FirebirdInfoCommandPacket(commandPacketType, payload);
            case TRANSACTION:
                return new FirebirdStartTransactionPacket(payload);
            case CREATE_BLOB:
            case OPEN_BLOB:
            case CREATE_BLOB2:
            case OPEN_BLOB2:
                return new FirebirdBlobCommandPacket(commandPacketType, payload);
            case GET_SEGMENT:
            case PUT_SEGMENT:
            case BATCH_SEGMENTS:
                return new FirebirdSegmentCommandPacket(commandPacketType, payload);
            case SEEK_BLOB:
                return new FirebirdSeekBlobCommandPacket(payload);
            case PREPARE_STATEMENT:
                return new FirebirdPrepareStatementPacket(payload);
            case EXECUTE:
            case EXECUTE2:
                return new FirebirdExecuteStatementPacket(payload, protocolVersion);
            case FETCH:
                return new FirebirdFetchStatementPacket(payload);
            case FREE_STATEMENT:
                return new FirebirdFreeStatementPacket(payload);
            case BATCH_CREATE:
                return new FirebirdBatchCreateCommandPacket(payload);
            case BATCH_MSG:
                return new FirebirdBatchMessageCommandPacket(payload);
            case BATCH_EXEC:
                return new FirebirdBatchExecuteCommandPacket(payload);
            case BATCH_SYNC:
                return new FirebirdBatchSyncCommandPacket(payload);
            default:
                return new FirebirdUnsupportedCommandPacket(commandPacketType);
        }
    }
    
    /**
     * Get expected length of command packet including message type.
     *
     * @param commandPacketType command packet type for Firebird
     * @param payload packet payload for Firebird
     * @param protocolVersion protocol version of Firebird
     * @param connectionId connection ID
     * @return expected length of packet, or -1 if the packet is incomplete
     * @throws UnsupportedCommandException if the command packet type cannot be framed
     */
    public static int getExpectedLength(final FirebirdCommandPacketType commandPacketType, final FirebirdPacketPayload payload, final FirebirdProtocolVersion protocolVersion, final int connectionId) {
        switch (commandPacketType) {
            case PING:
            case ABORT_AUX_CONNECTION:
                return 4;
            case CANCEL_EVENTS:
                return 12;
            case CONNECT_REQUEST:
            case AUX_CONNECT:
                return 16;
            case RECEIVE:
            case START:
            case START_AND_RECEIVE:
                return 24;
            case COMMIT:
            case PREPARE:
            case ROLLBACK:
            case UNWIND:
            case RELEASE:
            case CLOSE_BLOB:
            case CANCEL_BLOB:
            case DETACH:
            case DROP_DATABASE:
            case SERVICE_DETACH:
            case COMMIT_RETAINING:
            case ROLLBACK_RETAINING:
            case ALLOCATE_STATEMENT:
            case BATCH_RLS:
            case BATCH_CANCEL:
            case CANCEL:
                return 8;
            case INFO_BLOB:
            case INFO_DATABASE:
            case INFO_REQUEST:
            case INFO_TRANSACTION:
            case SERVICE_INFO:
            case INFO_SQL:
            case INFO_BATCH:
            case INFO_CURSOR:
                return FirebirdInfoCommandPacket.getLength(commandPacketType, payload);
            case TRANSACTION:
                return FirebirdStartTransactionPacket.getLength(payload);
            case CREATE_BLOB:
            case OPEN_BLOB:
            case CREATE_BLOB2:
            case OPEN_BLOB2:
                return FirebirdBlobCommandPacket.getLength(commandPacketType, payload);
            case GET_SEGMENT:
            case PUT_SEGMENT:
            case BATCH_SEGMENTS:
                return FirebirdSegmentCommandPacket.getLength(payload);
            case SEEK_BLOB:
                return FirebirdSeekBlobCommandPacket.getLength();
            case PREPARE_STATEMENT:
                return FirebirdPrepareStatementPacket.getLength(payload);
            case EXECUTE:
            case EXECUTE2:
                return FirebirdExecuteStatementPacket.getLength(payload, protocolVersion);
            case FETCH:
                return FirebirdFetchStatementPacket.getLength(payload);
            case FREE_STATEMENT:
                return FirebirdFreeStatementPacket.getLength();
            case BATCH_CREATE:
                return FirebirdBatchCreateCommandPacket.getLength(payload);
            case BATCH_MSG:
                return FirebirdBatchMessageCommandPacket.getLength(payload, connectionId);
            case BATCH_EXEC:
                return FirebirdBatchExecuteCommandPacket.getLength();
            case BATCH_SYNC:
                return FirebirdBatchSyncCommandPacket.getLength();
            default:
                throw UnsupportedCommandException.forCommandPacketLength(commandPacketType.getValue());
        }
    }
}
