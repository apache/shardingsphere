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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdArchType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class FirebirdDatabaseInfoReturnPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("databaseInfoTypes")
    void assertWriteWithDatabaseInfoType(final String name, final FirebirdDatabaseInfoPacketType infoType, final int expectedLength, final int expectedValue, final boolean int1Value) {
        new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(infoType), 0).write((PacketPayload) payload);
        InOrder order = inOrder(payload);
        order.verify(payload).writeInt1(infoType.getCode());
        order.verify(payload).writeInt2LE(expectedLength);
        if (int1Value) {
            order.verify(payload).writeInt1(expectedValue);
        } else {
            order.verify(payload).writeInt4LE(expectedValue);
        }
    }
    
    @Test
    void assertWriteFirebirdVersion() {
        new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(FirebirdDatabaseInfoPacketType.FIREBIRD_VERSION), 0).write((PacketPayload) payload);
        String fbVersion = String.format("%s-%s%d.%d.%d.%d Firebird %d.%d (ShardingSphere-Proxy)", FirebirdArchType.ARCHITECTURE.getIdentifier(), "V", 5, 0, 0, 0, 5, 0);
        InOrder order = inOrder(payload);
        order.verify(payload).writeInt1(FirebirdDatabaseInfoPacketType.FIREBIRD_VERSION.getCode());
        order.verify(payload).writeInt2LE(fbVersion.length() + 2);
        order.verify(payload).writeInt1(1);
        order.verify(payload).writeInt1(fbVersion.length());
        order.verify(payload).writeBytes(fbVersion.getBytes(StandardCharsets.UTF_8));
    }
    
    @Test
    void assertWriteUnsupportedInfoType() {
        new FirebirdDatabaseInfoReturnPacket(Arrays.asList(FirebirdDatabaseInfoPacketType.LICENSE, FirebirdDatabaseInfoPacketType.ATT_CHARSET), 52).write((PacketPayload) payload);
        InOrder order = inOrder(payload);
        order.verify(payload).writeInt1(FirebirdCommonInfoPacketType.ERROR.getCode());
        order.verify(payload).writeInt2LE(5);
        order.verify(payload).writeInt1(FirebirdDatabaseInfoPacketType.LICENSE.getCode());
        order.verify(payload).writeInt4LE(335544341);
        order.verify(payload).writeInt1(FirebirdDatabaseInfoPacketType.ATT_CHARSET.getCode());
        order.verify(payload).writeInt2LE(4);
        order.verify(payload).writeInt4LE(52);
        order.verify(payload).writeInt1(FirebirdCommonInfoPacketType.END.getCode());
        order.verifyNoMoreInteractions();
    }
    
    @Test
    void assertWriteCommonInfo() {
        new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(FirebirdCommonInfoPacketType.END), 0).write((PacketPayload) payload);
        verify(payload).writeInt1(FirebirdCommonInfoPacketType.END.getCode());
    }
    
    @Test
    void assertWriteEmptyInfo() {
        new FirebirdDatabaseInfoReturnPacket(Collections.emptyList(), 0).write((PacketPayload) payload);
        verify(payload).writeInt1(FirebirdCommonInfoPacketType.END.getCode());
        verifyNoMoreInteractions(payload);
    }
    
    @ParameterizedTest(name = "attachment charset {0}")
    @MethodSource("attachmentCharsets")
    void assertWriteAttachmentCharset(final int charsetId) {
        ByteBuf actual = Unpooled.buffer();
        try {
            FirebirdPacketPayload actualPayload = new FirebirdPacketPayload(actual, StandardCharsets.UTF_8);
            new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(FirebirdDatabaseInfoPacketType.ATT_CHARSET), charsetId).write((PacketPayload) actualPayload);
            byte[] actualBytes = new byte[actual.readableBytes()];
            actual.readBytes(actualBytes);
            assertThat(actualBytes, is(new byte[]{101, 4, 0, (byte) charsetId, 0, 0, 0, 1}));
        } finally {
            actual.release();
        }
    }
    
    @Test
    void assertWriteStopsAtFirstEnd() {
        new FirebirdDatabaseInfoReturnPacket(Arrays.asList(FirebirdCommonInfoPacketType.END, FirebirdDatabaseInfoPacketType.ATT_CHARSET, FirebirdCommonInfoPacketType.END),
                4).write((PacketPayload) payload);
        verify(payload).writeInt1(FirebirdCommonInfoPacketType.END.getCode());
        verifyNoMoreInteractions(payload);
    }
    
    @ParameterizedTest(name = "unknown info code {0} between supported items")
    @ValueSource(ints = {0, 65, 255})
    void assertWriteUnknownInfo(final int code) {
        ByteBuf request = Unpooled.buffer();
        ByteBuf actual = Unpooled.buffer();
        try {
            FirebirdPacketPayload requestPayload = new FirebirdPacketPayload(request, StandardCharsets.UTF_8);
            FirebirdPacketPayload actualPayload = new FirebirdPacketPayload(actual, StandardCharsets.UTF_8);
            requestPayload.writeInt4(40);
            requestPayload.writeInt4(0);
            requestPayload.writeInt4(0);
            requestPayload.writeBuffer(new byte[]{62, (byte) code, 101, 1});
            requestPayload.writeInt4(1024);
            new FirebirdDatabaseInfoReturnPacket(FirebirdDatabaseInfoPacketType.createPacket(requestPayload).getInfoItems(), 4).write((PacketPayload) actualPayload);
            byte[] actualBytes = new byte[actual.readableBytes()];
            actual.readBytes(actualBytes);
            assertThat(actualBytes, is(new byte[]{62, 1, 0, 3, 3, 5, 0, (byte) code, 21, 0, 0, 20, 101, 4, 0, 4, 0, 0, 0, 1}));
        } finally {
            request.release();
            actual.release();
        }
    }
    
    private static Stream<Arguments> attachmentCharsets() {
        return Stream.of(Arguments.of(0), Arguments.of(3), Arguments.of(4), Arguments.of(52));
    }
    
    private static Stream<Arguments> databaseInfoTypes() {
        return Stream.of(
                Arguments.of("db sql dialect", FirebirdDatabaseInfoPacketType.DB_SQL_DIALECT, 1, 3, true),
                Arguments.of("ods version", FirebirdDatabaseInfoPacketType.ODS_VERSION, 4, 13, false),
                Arguments.of("ods minor version", FirebirdDatabaseInfoPacketType.ODS_MINOR_VERSION, 4, 1, false));
    }
}
