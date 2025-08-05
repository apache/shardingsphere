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

package org.apache.shardingsphere.db.protocol.firebird.packet.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.db.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.admin.FirebirdUnsupportedCommandPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.database.FirebirdDatabaseInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.FirebirdAllocateStatementPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.FirebirdFetchStatementPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.FirebirdFreeStatementPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteStatementPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.prepare.FirebirdPrepareStatementPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.transaction.FirebirdCommitTransactionPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.transaction.FirebirdRollbackTransactionPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.transaction.FirebirdStartTransactionPacket;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;

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
     * Validate length of command packet.
     *
     * @param commandPacketType command packet type for Firebird
     * @param payload packet payload for Firebird
     * @param capacity maximum allowed capacity
     * @param protocolVersion protocol version of Firebird
     * @return true if length is valid, false otherwise
     */
    public static boolean isValidLength(final FirebirdCommandPacketType commandPacketType, final FirebirdPacketPayload payload, final int capacity, final FirebirdProtocolVersion protocolVersion) {
        try {
            return getLength(commandPacketType, payload, protocolVersion) <= capacity;
        } catch (final IndexOutOfBoundsException ignored) {
            payload.getByteBuf().resetReaderIndex();
            return false;
        }
    }
    
    private static int getLength(final FirebirdCommandPacketType commandPacketType, final FirebirdPacketPayload payload,
                                 final FirebirdProtocolVersion protocolVersion) throws IndexOutOfBoundsException {
        switch (commandPacketType) {
            case INFO_DATABASE:
            case INFO_SQL:
                return FirebirdInfoPacket.getLength(payload);
            case TRANSACTION:
                return FirebirdStartTransactionPacket.getLength(payload);
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
