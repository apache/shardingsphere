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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query;

import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLColumnDefinition41PacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    void assertNewWithPayload() {
        mockPayloadForNewPacket("def", 0x0cL);
        assertDoesNotThrow(() -> new MySQLColumnDefinition41Packet(payload));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertNewWithInvalidPayloadArguments")
    void assertNewWithInvalidPayload(final String name, final String catalog, final long nextLength) {
        when(payload.readStringLenenc()).thenReturn(catalog, "logic_db", "tbl", "tbl_org", "id", "id_org");
        if ("def".equals(catalog)) {
            when(payload.readIntLenenc()).thenReturn(nextLength);
        }
        assertThrows(IllegalArgumentException.class, () -> new MySQLColumnDefinition41Packet(payload));
    }
    
    @Test
    void assertWriteWithoutDefaultValues() {
        mockPayloadForNewPacket("def", 0x0cL);
        MySQLColumnDefinition41Packet actual = new MySQLColumnDefinition41Packet(payload);
        actual.write((PacketPayload) payload);
        verifyWrite("logic_db", "tbl", "tbl_org", "id", "id_org", MySQLConstants.DEFAULT_CHARSET.getId(), 10, MySQLBinaryColumnType.LONG.getValue(), 2, 1);
        verify(payload, never()).writeIntLenenc(0);
        verify(payload, never()).writeStringLenenc("");
    }
    
    @Test
    void assertWriteWithDefaultValues() {
        MySQLColumnDefinition41Packet actual = new MySQLColumnDefinition41Packet(
                MySQLConstants.DEFAULT_CHARSET.getId(), "logic_db", "tbl", "tbl_org", "id", "id_org", 10, MySQLBinaryColumnType.LONG, 1, true);
        actual.write((PacketPayload) payload);
        verifyWrite("logic_db", "tbl", "tbl_org", "id", "id_org", MySQLConstants.DEFAULT_CHARSET.getId(), 10, MySQLBinaryColumnType.LONG.getValue(), 0, 1);
        verify(payload).writeIntLenenc(0);
        verify(payload).writeStringLenenc("");
    }
    
    private void mockPayloadForNewPacket(final String catalog, final long nextLength) {
        when(payload.readStringLenenc()).thenReturn(catalog, "logic_db", "tbl", "tbl_org", "id", "id_org");
        when(payload.readIntLenenc()).thenReturn(nextLength);
        when(payload.readInt2()).thenReturn(MySQLConstants.DEFAULT_CHARSET.getId(), 2);
        when(payload.readInt4()).thenReturn(10);
        when(payload.readInt1()).thenReturn(MySQLBinaryColumnType.LONG.getValue(), 1);
    }
    
    private static Stream<Arguments> assertNewWithInvalidPayloadArguments() {
        return Stream.of(
                Arguments.of("catalog is invalid", "catalog", 0x0cL),
                Arguments.of("next length is negative", "def", -1L),
                Arguments.of("next length is zero", "def", 0L));
    }
    
    private void verifyWrite(final String schema, final String table, final String orgTable, final String name, final String orgName,
                             final int characterSet, final int columnLength, final int columnType, final int flags, final int decimals) {
        verify(payload).writeStringLenenc("def");
        verify(payload).writeStringLenenc(schema);
        verify(payload).writeStringLenenc(table);
        verify(payload).writeStringLenenc(orgTable);
        verify(payload).writeStringLenenc(name);
        verify(payload).writeStringLenenc(orgName);
        verify(payload).writeIntLenenc(0x0c);
        verify(payload).writeInt2(characterSet);
        verify(payload).writeInt4(columnLength);
        verify(payload).writeInt1(columnType);
        verify(payload).writeInt2(flags);
        verify(payload).writeInt1(decimals);
        verify(payload).writeReserved(2);
    }
}
