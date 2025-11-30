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

package org.apache.shardingsphere.database.protocol.postgresql.packet.generic;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;

/**
 * Ready for query packet for PostgreSQL.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLReadyForQueryPacket extends PostgreSQLPacket {
    
    public static final PostgreSQLReadyForQueryPacket IN_TRANSACTION = new PostgreSQLReadyForQueryPacket((byte) 'T');
    
    public static final PostgreSQLReadyForQueryPacket NOT_IN_TRANSACTION = new PostgreSQLReadyForQueryPacket((byte) 'I');
    
    public static final PostgreSQLReadyForQueryPacket TRANSACTION_FAILED = new PostgreSQLReadyForQueryPacket((byte) 'E');
    
    private static final byte[] PREFIX = {(byte) PostgreSQLMessagePacketType.READY_FOR_QUERY.getValue(), 0, 0, 0, 5};
    
    private final byte status;
    
    @Override
    protected void write(final PostgreSQLPacketPayload payload) {
        payload.getByteBuf().writeBytes(PREFIX);
        payload.getByteBuf().writeByte(status);
    }
}
