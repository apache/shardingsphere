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

package org.apache.shardingsphere.database.protocol.postgresql.packet.handshake;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.postgresql.packet.ByteBufTestUtils;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class PostgreSQLComStartupPacketTest {
    
    @Test
    void assertNewPostgreSQLComStartupPacket() {
        Map<String, String> parametersMap = createParametersMap();
        int packetMessageLength = getPacketMessageLength(parametersMap);
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(packetMessageLength);
        PostgreSQLPacketPayload payload = createPayload(parametersMap, packetMessageLength, byteBuf);
        PostgreSQLComStartupPacket actual = new PostgreSQLComStartupPacket(payload);
        assertThat(actual.getDatabase(), is("test_db"));
        assertThat(actual.getUsername(), is("postgres"));
        assertThat(actual.getClientEncoding(), is("UTF8"));
        assertThat(byteBuf.writerIndex(), is(packetMessageLength));
    }
    
    private Map<String, String> createParametersMap() {
        Map<String, String> result = new LinkedHashMap<>(2, 1F);
        result.put("database", "test_db");
        result.put("user", "postgres");
        result.put("client_encoding", "UTF8");
        return result;
    }
    
    private int getPacketMessageLength(final Map<String, String> parametersMap) {
        int result = 4 + 4;
        for (Entry<String, String> entry : parametersMap.entrySet()) {
            result += entry.getKey().length() + 1;
            result += entry.getValue().length() + 1;
        }
        return result;
    }
    
    private PostgreSQLPacketPayload createPayload(final Map<String, String> actualParametersMap, final int actualMessageLength, final ByteBuf byteBuf) {
        PostgreSQLPacketPayload result = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        result.writeInt4(actualMessageLength);
        result.writeInt4(196608);
        for (Entry<String, String> entry : actualParametersMap.entrySet()) {
            result.writeStringNul(entry.getKey());
            result.writeStringNul(entry.getValue());
        }
        return result;
    }
    
    @Test
    void assertWrite() {
        assertDoesNotThrow(() -> new PostgreSQLComStartupPacket(mock(PostgreSQLPacketPayload.class)).write(mock(PostgreSQLPacketPayload.class)));
    }
}
