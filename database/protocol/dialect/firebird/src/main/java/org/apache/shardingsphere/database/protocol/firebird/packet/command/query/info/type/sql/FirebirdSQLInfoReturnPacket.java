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
    
    private static final int REQ_SELECT_COUNT = 13;
    
    private static final int REQ_INSERT_COUNT = 14;
    
    private static final int REQ_UPDATE_COUNT = 15;
    
    private static final int REQ_DELETE_COUNT = 16;
    
    private static final int COUNT_VALUE_LENGTH = 4;
    
    private static final int RECORDS_CLUSTER_LENGTH = 4 * (1 + 2 + COUNT_VALUE_LENGTH);
    
    private final List<FirebirdInfoPacketType> infoItems;
    
    private final FirebirdSQLRecordsInfo recordsInfo;
    
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
                processRecords(payload);
                return;
            default:
                throw new FirebirdProtocolException("Unknown database information request type %d", type.getCode());
        }
    }
    
    private void processRecords(final FirebirdPacketPayload payload) {
        payload.writeInt1(FirebirdSQLInfoPacketType.RECORDS.getCode());
        payload.writeInt2LE(RECORDS_CLUSTER_LENGTH);
        writeCount(payload, REQ_SELECT_COUNT, 0L);
        writeCount(payload, REQ_INSERT_COUNT, recordsInfo.getInsertCount());
        writeCount(payload, REQ_UPDATE_COUNT, recordsInfo.getUpdateCount());
        writeCount(payload, REQ_DELETE_COUNT, recordsInfo.getDeleteCount());
    }
    
    private void writeCount(final FirebirdPacketPayload payload, final int countType, final long count) {
        payload.writeInt1(countType);
        payload.writeInt2LE(COUNT_VALUE_LENGTH);
        payload.writeInt4LE((int) count);
    }
}
