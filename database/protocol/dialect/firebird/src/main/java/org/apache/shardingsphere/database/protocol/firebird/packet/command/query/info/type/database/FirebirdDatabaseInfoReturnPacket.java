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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdArchType;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Database info return data packet for Firebird.
 */
@RequiredArgsConstructor
@Getter
public final class FirebirdDatabaseInfoReturnPacket extends FirebirdPacket {
    
    private static final int SQL_DIALECT = 3;
    
    private static final String BUILD_TYPE = "V";
    
    private static final int MAJOR_VERSION = 5;
    
    private static final int MINOR_VERSION = 0;
    
    private static final int REV_NO = 0;
    
    private static final int BUILD_NO = 0;
    
    private static final String SERVER_NAME = String.format("Firebird %d.%d (ShardingSphere-Proxy)", MAJOR_VERSION, MINOR_VERSION);
    
    private static final String FB_VERSION = String.format("%s-%s%d.%d.%d.%d %s",
            FirebirdArchType.ARCHITECTURE.getIdentifier(),
            BUILD_TYPE,
            MAJOR_VERSION,
            MINOR_VERSION,
            REV_NO,
            BUILD_NO,
            SERVER_NAME);
    
    private final List<FirebirdInfoPacketType> infoItems;
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        for (FirebirdInfoPacketType type : infoItems) {
            if (type.isCommon()) {
                FirebirdCommonInfoPacketType.parseCommonInfo(payload, (FirebirdCommonInfoPacketType) type);
            } else {
                parseDatabaseInfo(payload, (FirebirdDatabaseInfoPacketType) type);
            }
        }
    }
    
    private void parseDatabaseInfo(final FirebirdPacketPayload data, final FirebirdDatabaseInfoPacketType type) {
        // TODO implement other request types handle
        switch (type) {
            case DB_SQL_DIALECT:
                data.writeInt1(FirebirdDatabaseInfoPacketType.DB_SQL_DIALECT.getCode());
                data.writeInt2LE(1);
                data.writeInt1(SQL_DIALECT);
                break;
            case ODS_VERSION:
                data.writeInt1(FirebirdDatabaseInfoPacketType.ODS_VERSION.getCode());
                data.writeInt2LE(4);
                data.writeInt4LE(MAJOR_VERSION);
                break;
            case ODS_MINOR_VERSION:
                data.writeInt1(FirebirdDatabaseInfoPacketType.ODS_MINOR_VERSION.getCode());
                data.writeInt2LE(4);
                data.writeInt4LE(MINOR_VERSION);
                break;
            case FIREBIRD_VERSION:
                data.writeInt1(FirebirdDatabaseInfoPacketType.FIREBIRD_VERSION.getCode());
                data.writeInt2LE(FB_VERSION.length() + 2);
                data.writeInt1(1);
                data.writeInt1(FB_VERSION.length());
                data.writeBytes(FB_VERSION.getBytes(StandardCharsets.UTF_8));
                break;
            default:
                throw new FirebirdProtocolException("Unknown database information request type %d", type.getCode());
        }
    }
}
