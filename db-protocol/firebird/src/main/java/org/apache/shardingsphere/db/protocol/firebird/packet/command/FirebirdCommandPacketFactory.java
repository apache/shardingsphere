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
import org.apache.shardingsphere.db.protocol.firebird.packet.command.admin.FirebirdUnsupportedCommandPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.database.FirebirdDatabaseInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.FirebirdAllocateStatementPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.FirebirdExecuteStatementPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.FirebirdPrepareStatementPacket;
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
     * @return created instance
     */
    public static FirebirdCommandPacket newInstance(final FirebirdCommandPacketType commandPacketType, final FirebirdPacketPayload payload) {
        switch (commandPacketType) {
            case OP_INFO_DATABASE:
                return FirebirdDatabaseInfoPacketType.createPacket(payload);
            case OP_TRANSACTION:
                return new FirebirdStartTransactionPacket(payload);
            case OP_ALLOCATE_STATEMENT:
                return new FirebirdAllocateStatementPacket(payload);
            case OP_PREPARE_STATEMENT:
                return new FirebirdPrepareStatementPacket(payload);
            case OP_EXECUTE:
                return new FirebirdExecuteStatementPacket(payload);
            case OP_INFO_SQL:
                return FirebirdSQLInfoPacketType.createPacket(payload);
            default:
                return new FirebirdUnsupportedCommandPacket(commandPacketType);
        }
    }
}
