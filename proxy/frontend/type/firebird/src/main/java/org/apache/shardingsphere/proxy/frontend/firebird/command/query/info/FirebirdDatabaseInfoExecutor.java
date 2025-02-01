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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.info;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.firebird.constant.FirebirdArchType;
import org.apache.shardingsphere.db.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.database.FirebirdDatabaseInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;

/**
 * Database info command executor for Firebird.
 */
@RequiredArgsConstructor
public final class FirebirdDatabaseInfoExecutor implements CommandExecutor {

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

    private final FirebirdInfoPacket packet;
    private final ConnectionSession connectionSession;

    @Override
    public Collection<DatabasePacket> execute() {
        ByteBuf data = packet.getPayload().getByteBuf().alloc().buffer();
        for (FirebirdInfoPacketType type : packet.getInfoItems()) {
            if (type.isCommon()) {
                FirebirdCommonInfoPacketType.parseCommonInfo(data, (FirebirdCommonInfoPacketType) type);
            } else {
                parseDatabaseInfo(data, (FirebirdDatabaseInfoPacketType) type);
            }
        }
        return Collections.singleton(new FirebirdGenericResponsePacket().setData(data));
    }
    
    private void parseDatabaseInfo(ByteBuf data, FirebirdDatabaseInfoPacketType type) {
        //TODO implement other request types handle
        switch (type) {
            case DB_SQL_DIALECT:
                data.writeByte(FirebirdDatabaseInfoPacketType.DB_SQL_DIALECT.getCode());
                data.writeShortLE(1);
                data.writeByte(SQL_DIALECT);
                break;
            case ODS_VERSION:
                data.writeByte(FirebirdDatabaseInfoPacketType.ODS_VERSION.getCode());
                data.writeShortLE(4);
                data.writeIntLE(MAJOR_VERSION);
                break;
            case ODS_MINOR_VERSION:
                data.writeByte(FirebirdDatabaseInfoPacketType.ODS_MINOR_VERSION.getCode());
                data.writeShortLE(4);
                data.writeIntLE(MINOR_VERSION);
                break;
            case FIREBIRD_VERSION:
                data.writeByte(FirebirdDatabaseInfoPacketType.FIREBIRD_VERSION.getCode());
                data.writeShortLE(FB_VERSION.length() + 2);
                data.writeByte(1);
                data.writeByte(FB_VERSION.length());
                data.writeBytes(FB_VERSION.getBytes(StandardCharsets.UTF_8));
                break;
            default:
                throw new FirebirdProtocolException("Unknown database information request type %d", type.getCode());
        }
    }
}
