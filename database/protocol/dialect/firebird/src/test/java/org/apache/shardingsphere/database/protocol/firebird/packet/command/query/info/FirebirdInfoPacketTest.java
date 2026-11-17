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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdInfoPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    void assertGetLength() {
        when(payload.getBufferLength(12)).thenReturn(8);
        assertThat(FirebirdInfoPacket.getLength(payload), is(24));
    }
    
    @ParameterizedTest(name = "{0}")
    @CsvSource({"1, 1", "0, 0", "32767, 32767", "32768, 32768", "-32768, 32768", "-536, 65000", "-1, 65535"})
    void assertNewInstance(final int wireHandle, final int expectedHandle) {
        when(payload.readInt4()).thenReturn(wireHandle, 2, 100);
        when(payload.readBuffer()).thenReturn(byteBuf);
        when(byteBuf.isReadable()).thenReturn(true, true, false);
        when(byteBuf.readByte()).thenReturn((byte) FirebirdSQLInfoPacketType.RECORDS.getCode(), (byte) FirebirdCommonInfoPacketType.END.getCode());
        FirebirdInfoPacket actual = new FirebirdInfoPacket(payload, FirebirdSQLInfoPacketType::valueOf);
        assertThat(actual.getHandle(), is(expectedHandle));
        assertThat(actual.getIncarnation(), is(2));
        assertThat(actual.getMaxLength(), is(100));
        List<FirebirdInfoPacketType> infoItems = actual.getInfoItems();
        assertThat(infoItems.get(0), is(FirebirdSQLInfoPacketType.RECORDS));
        assertThat(infoItems.get(1), is(FirebirdCommonInfoPacketType.END));
    }
    
    @Test
    void assertNewInstanceWithDataAfterEnd() {
        ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{1, 0});
        try {
            when(payload.readBuffer()).thenReturn(buffer);
            FirebirdInfoPacket actual = new FirebirdInfoPacket(payload, FirebirdSQLInfoPacketType::valueOf);
            assertThat(actual.getInfoItems(), is(Collections.singletonList(FirebirdCommonInfoPacketType.END)));
            assertThat(buffer.readableBytes(), is(1));
        } finally {
            buffer.release();
        }
    }
    
    @Test
    void assertNewInstanceWithoutEnd() {
        ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{(byte) FirebirdSQLInfoPacketType.RECORDS.getCode()});
        try {
            when(payload.readBuffer()).thenReturn(buffer);
            FirebirdInfoPacket actual = new FirebirdInfoPacket(payload, FirebirdSQLInfoPacketType::valueOf);
            assertThat(actual.getInfoItems(), is(Collections.singletonList(FirebirdSQLInfoPacketType.RECORDS)));
        } finally {
            buffer.release();
        }
    }
    
    @Test
    void assertWrite() {
        FirebirdInfoPacket packet = new FirebirdInfoPacket(1, 2, 100);
        packet.write((PacketPayload) payload);
        verifyNoInteractions(payload);
    }
}
