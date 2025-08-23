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
import org.apache.shardingsphere.database.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostgreSQLAggregatedResponsesPacketTest {
    
    @Test
    void assertWrite() {
        PostgreSQLIdentifierPacket identifierPacket = mock(PostgreSQLIdentifierPacket.class);
        when(identifierPacket.getIdentifier()).thenReturn(PostgreSQLMessagePacketType.READY_FOR_QUERY);
        PostgreSQLPacket nonIdentifierPacket = mock(PostgreSQLPacket.class);
        PostgreSQLAggregatedResponsesPacket packet = new PostgreSQLAggregatedResponsesPacket(Arrays.asList(nonIdentifierPacket, identifierPacket));
        PostgreSQLPacketPayload payload = mock(PostgreSQLPacketPayload.class);
        ByteBuf byteBuf = mock(ByteBuf.class);
        when(byteBuf.writerIndex()).thenReturn(1, 10);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        packet.write(payload);
        verify(nonIdentifierPacket).write(payload);
        verify(byteBuf).writeByte(PostgreSQLMessagePacketType.READY_FOR_QUERY.getValue());
        verify(byteBuf).writeInt(0);
        verify(identifierPacket).write(payload);
        verify(byteBuf).setInt(1, 10 - 1);
    }
}
