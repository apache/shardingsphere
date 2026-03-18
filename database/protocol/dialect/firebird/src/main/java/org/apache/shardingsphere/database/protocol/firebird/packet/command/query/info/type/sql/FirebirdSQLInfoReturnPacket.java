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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.List;

/**
 * SQL info return data packet for Firebird.
 */
@RequiredArgsConstructor
@Getter
public final class FirebirdSQLInfoReturnPacket extends FirebirdPacket {
    
    private final List<FirebirdInfoPacketType> infoItems;
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        for (FirebirdInfoPacketType type : infoItems) {
            if (type.isCommon()) {
                FirebirdCommonInfoPacketType.parseCommonInfo(payload, (FirebirdCommonInfoPacketType) type);
            } else {
                parseSQLInfo(payload, (FirebirdSQLInfoPacketType) type);
            }
        }
    }
    
    private void parseSQLInfo(final FirebirdPacketPayload payload, final FirebirdSQLInfoPacketType type) {
        // TODO implement other request types handle
        switch (type) {
            case RECORDS:
                // TODO handle actual update count
                processRecords(payload);
                return;
            default:
                throw new FirebirdProtocolException("Unknown database information request type %d", type.getCode());
        }
    }
    
    private void processRecords(final FirebirdPacketPayload payload) {
        // TODO handle actual update count
        payload.writeInt1(FirebirdSQLInfoPacketType.RECORDS.getCode());
        payload.writeInt2LE(0);
        payload.writeInt1(FirebirdSQLInfoReturnValue.SELECT.getCode());
        payload.writeInt2LE(4);
        payload.writeInt4LE(0);
        payload.writeInt1(FirebirdSQLInfoReturnValue.INSERT.getCode());
        payload.writeInt2LE(4);
        payload.writeInt4LE(0);
        payload.writeInt1(FirebirdSQLInfoReturnValue.UPDATE.getCode());
        payload.writeInt2LE(4);
        payload.writeInt4LE(0);
        payload.writeInt1(FirebirdSQLInfoReturnValue.DELETE.getCode());
        payload.writeInt2LE(4);
        payload.writeInt4LE(0);
    }
}
