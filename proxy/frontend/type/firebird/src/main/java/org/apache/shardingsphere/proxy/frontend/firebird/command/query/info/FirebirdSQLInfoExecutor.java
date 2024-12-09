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
import org.apache.shardingsphere.db.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnValue;
import org.apache.shardingsphere.db.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;

import java.util.Collection;
import java.util.Collections;

import static io.netty.buffer.Unpooled.buffer;

/**
 * Database info command executor for Firebird.
 */
@RequiredArgsConstructor
public final class FirebirdSQLInfoExecutor implements CommandExecutor {

    private final FirebirdInfoPacket packet;
    private final ConnectionSession connectionSession;

    @Override
    public Collection<DatabasePacket> execute() {
        ByteBuf data = buffer(packet.getMaxLength());
        for (FirebirdInfoPacketType type : packet.getInfoItems()) {
            if (type.isCommon()) {
                FirebirdCommonInfoPacketType.parseCommonInfo(data, (FirebirdCommonInfoPacketType) type);
            } else {
                parseDatabaseInfo(data, (FirebirdSQLInfoPacketType) type);
            }
        }
        return Collections.singleton(new FirebirdGenericResponsePacket().setData(data.capacity(data.writerIndex()).array()));
    }
    
    private void parseDatabaseInfo(ByteBuf data, FirebirdSQLInfoPacketType type) {
        //TODO implement other request types handle
        switch (type) {
            case RECORDS:
                //TODO handle actual update count
                data.writeByte(FirebirdSQLInfoPacketType.RECORDS.getCode());
                data.writeShortLE(0);
                data.writeByte(FirebirdSQLInfoReturnValue.SELECT.getCode());
                data.writeShortLE(4);
                data.writeIntLE(0);
                data.writeByte(FirebirdSQLInfoReturnValue.INSERT.getCode());
                data.writeShortLE(4);
                data.writeIntLE(0);
                data.writeByte(FirebirdSQLInfoReturnValue.UPDATE.getCode());
                data.writeShortLE(4);
                data.writeIntLE(0);
                data.writeByte(FirebirdSQLInfoReturnValue.DELETE.getCode());
                data.writeShortLE(4);
                data.writeIntLE(0);
                break;
            default:
                throw new FirebirdProtocolException("Unknown database information request type %d", type.getCode());
        }
    }
}
