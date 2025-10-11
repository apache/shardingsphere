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
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.admin.FirebirdUnsupportedCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdGetBlobSegmentCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdOpenBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdPutBlobSegmentCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdSeekBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCreateBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCloseBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCancelBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.database.FirebirdDatabaseInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdAllocateStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFetchStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFreeStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdPrepareStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdCommitTransactionPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdRollbackTransactionPacket;
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
     * @return created instance
     */
    public static FirebirdCommandPacket newInstance(final FirebirdCommandPacketType commandPacketType, final FirebirdPacketPayload payload, final FirebirdProtocolVersion protocolVersion) {
        switch (commandPacketType) {
            case INFO_DATABASE:
                return FirebirdDatabaseInfoPacketType.createPacket(payload);
            case TRANSACTION:
                return new FirebirdStartTransactionPacket(payload);
            case CREATE_BLOB:
            case CREATE_BLOB2:
                return new FirebirdCreateBlobCommandPacket(commandPacketType, payload);
            case OPEN_BLOB:
            case OPEN_BLOB2:
                return new FirebirdOpenBlobCommandPacket(commandPacketType, payload);
            case GET_SEGMENT:
                return new FirebirdGetBlobSegmentCommandPacket(payload);
            case PUT_SEGMENT:
                return new FirebirdPutBlobSegmentCommandPacket(payload);
            case CANCEL_BLOB:
                return new FirebirdCancelBlobCommandPacket(payload);
            case CLOSE_BLOB:
                return new FirebirdCloseBlobCommandPacket(payload);
            case SEEK_BLOB:
                return new FirebirdSeekBlobCommandPacket(payload);
            case ALLOCATE_STATEMENT:
                return new FirebirdAllocateStatementPacket(payload);
            case PREPARE_STATEMENT:
                return new FirebirdPrepareStatementPacket(payload);
            case EXECUTE:
            case EXECUTE2:
                return new FirebirdExecuteStatementPacket(payload, protocolVersion);
            case FETCH:
                return new FirebirdFetchStatementPacket(payload);
            case INFO_SQL:
                return FirebirdSQLInfoPacketType.createPacket(payload);
            case COMMIT:
                return new FirebirdCommitTransactionPacket(payload);
            case ROLLBACK:
                return new FirebirdRollbackTransactionPacket(payload);
            case FREE_STATEMENT:
                return new FirebirdFreeStatementPacket(payload);
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
     * @return expected length of packet, or 0 if length is variable
     */
    public static int getExpectedLength(final FirebirdCommandPacketType commandPacketType, final FirebirdPacketPayload payload, final FirebirdProtocolVersion protocolVersion) {
        return getLength(commandPacketType, payload, protocolVersion);
    }
    
    private static int getLength(final FirebirdCommandPacketType commandPacketType, final FirebirdPacketPayload payload,
                                 final FirebirdProtocolVersion protocolVersion) throws IndexOutOfBoundsException {
        switch (commandPacketType) {
            case INFO_DATABASE:
            case INFO_SQL:
                return FirebirdInfoPacket.getLength(payload);
            case TRANSACTION:
                return FirebirdStartTransactionPacket.getLength(payload);
            case CREATE_BLOB:
            case CREATE_BLOB2:
                return FirebirdCreateBlobCommandPacket.getLength(commandPacketType, payload);
            case OPEN_BLOB:
            case OPEN_BLOB2:
                return FirebirdOpenBlobCommandPacket.getLength(commandPacketType, payload);
            case GET_SEGMENT:
                return FirebirdGetBlobSegmentCommandPacket.getLength(payload);
            case PUT_SEGMENT:
                return FirebirdPutBlobSegmentCommandPacket.getLength(payload);
            case CANCEL_BLOB:
                return FirebirdCancelBlobCommandPacket.getLength();
            case CLOSE_BLOB:
                return FirebirdCloseBlobCommandPacket.getLength();
            case SEEK_BLOB:
                return FirebirdSeekBlobCommandPacket.getLength();
            case ALLOCATE_STATEMENT:
                return FirebirdAllocateStatementPacket.getLength();
            case PREPARE_STATEMENT:
                return FirebirdPrepareStatementPacket.getLength(payload);
            case EXECUTE:
            case EXECUTE2:
                return FirebirdExecuteStatementPacket.getLength(payload, protocolVersion);
            case FETCH:
                return FirebirdFetchStatementPacket.getLength(payload);
            case COMMIT:
                return FirebirdCommitTransactionPacket.getLength();
            case ROLLBACK:
                return FirebirdRollbackTransactionPacket.getLength();
            case FREE_STATEMENT:
                return FirebirdFreeStatementPacket.getLength();
            default:
                return 0;
        }
    }
}
