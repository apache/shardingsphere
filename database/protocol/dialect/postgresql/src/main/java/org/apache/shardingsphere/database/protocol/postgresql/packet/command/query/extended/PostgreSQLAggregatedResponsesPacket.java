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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.util.List;

/**
 * PostgreSQL aggregated responses packet.
 */
@RequiredArgsConstructor
public final class PostgreSQLAggregatedResponsesPacket extends PostgreSQLPacket {
    
    private final List<DatabasePacket> packets;
    
    @Override
    protected void write(final PostgreSQLPacketPayload payload) {
        ByteBuf byteBuf = payload.getByteBuf();
        for (DatabasePacket each : packets) {
            if (!(each instanceof PostgreSQLIdentifierPacket)) {
                each.write(payload);
                continue;
            }
            PostgreSQLIdentifierPacket eachPacket = (PostgreSQLIdentifierPacket) each;
            byteBuf.writeByte(eachPacket.getIdentifier().getValue());
            int from = byteBuf.writerIndex();
            byteBuf.writeInt(0);
            eachPacket.write(payload);
            int length = byteBuf.writerIndex() - from;
            byteBuf.setInt(from, length);
        }
    }
}
