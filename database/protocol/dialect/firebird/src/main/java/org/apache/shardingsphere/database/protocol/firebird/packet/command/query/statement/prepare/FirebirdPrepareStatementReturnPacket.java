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
import lombok.Setter;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnValue;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Firebird prepare statement return data packet.
 */
@Getter
@Setter
public final class FirebirdPrepareStatementReturnPacket extends FirebirdPacket {
    
    private FirebirdSQLInfoReturnValue type;
    
    private final List<FirebirdReturnColumnPacket> describeSelect = new ArrayList<>();
    
    private final List<FirebirdReturnColumnPacket> describeBind = new ArrayList<>();
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        writeInt(FirebirdSQLInfoPacketType.STMT_TYPE, type.getCode(), payload);
        writeDescribe(FirebirdSQLInfoPacketType.SELECT, describeSelect, payload);
        writeDescribe(FirebirdSQLInfoPacketType.BIND, describeBind, payload);
        writeCode(FirebirdCommonInfoPacketType.END, payload);
    }
    
    private static void writeDescribe(final FirebirdSQLInfoPacketType describeType, final Collection<FirebirdReturnColumnPacket> columns, final FirebirdPacketPayload payload) {
        writeCode(describeType, payload);
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
