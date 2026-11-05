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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnValue;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Firebird prepare statement return data packet.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class FirebirdPrepareStatementReturnPacket extends FirebirdPacket {
    
    private final Collection<FirebirdSQLInfoPacketType> requestedItems;
    
    private FirebirdSQLInfoReturnValue type;
    
    private int statementFlags;
    
    private final List<FirebirdReturnColumnPacket> describeSelect = new ArrayList<>();
    
    private final List<FirebirdReturnColumnPacket> describeBind = new ArrayList<>();
    
    @Override
    @HighFrequencyInvocation
    protected void write(final FirebirdPacketPayload payload) {
        for (FirebirdSQLInfoPacketType each : requestedItems) {
            switch (each) {
                case STMT_TYPE:
                    writeInt(FirebirdSQLInfoPacketType.STMT_TYPE, type.getCode(), payload);
                    break;
                case STMT_FLAGS:
                    writeInt(FirebirdSQLInfoPacketType.STMT_FLAGS, statementFlags, payload);
                    break;
                case SELECT:
                    writeDescription(FirebirdSQLInfoPacketType.SELECT, describeSelect, payload);
                    break;
                case BIND:
                    writeDescription(FirebirdSQLInfoPacketType.BIND, describeBind, payload);
                    break;
                default:
                    break;
            }
        }
        writeCode(FirebirdCommonInfoPacketType.END, payload);
    }
    
    private static void writeDescription(final FirebirdSQLInfoPacketType type, final Collection<FirebirdReturnColumnPacket> columns, final FirebirdPacketPayload payload) {
        writeCode(type, payload);
        writeInt(FirebirdSQLInfoPacketType.DESCRIBE_VARS, columns.size(), payload);
        for (FirebirdReturnColumnPacket each : columns) {
            each.write(payload);
        }
    }
    
    static void writeCode(final FirebirdInfoPacketType code, final FirebirdPacketPayload payload) {
        payload.writeInt1(code.getCode());
    }
    
    static void writeInt(final FirebirdInfoPacketType code, final int value, final FirebirdPacketPayload payload) {
        payload.writeInt1(code.getCode());
        payload.writeInt2LE(4);
        payload.writeInt4LE(value);
    }
    
    static void writeString(final FirebirdInfoPacketType code, final String value, final FirebirdPacketPayload payload) {
        payload.writeInt1(code.getCode());
        byte[] valueBytes = null == value ? new byte[0] : value.getBytes(payload.getCharset());
        payload.writeInt2LE(valueBytes.length);
        payload.writeBytes(valueBytes);
    }
}
