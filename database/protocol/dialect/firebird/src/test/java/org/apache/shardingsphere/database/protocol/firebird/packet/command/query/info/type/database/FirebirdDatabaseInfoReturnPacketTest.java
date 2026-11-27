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
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.constant.FirebirdArchType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FirebirdDatabaseInfoReturnPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("databaseInfoTypes")
    void assertWriteWithDatabaseInfoType(final String name, final FirebirdDatabaseInfoPacketType infoType, final int expectedLength, final int expectedValue, final boolean int1Value) {
        new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(infoType), 0).write(payload);
        InOrder order = inOrder(payload);
        order.verify(payload).writeInt1(infoType.getCode());
        order.verify(payload).writeInt2LE(expectedLength);
        if (int1Value) {
            order.verify(payload).writeInt1(expectedValue);
        } else {
            order.verify(payload).writeInt4LE(expectedValue);
        }
        order.verify(payload).writeInt1(FirebirdCommonInfoPacketType.END.getCode());
        order.verifyNoMoreInteractions();
    }
    
    @ParameterizedTest(name = "charset {0}")
    @CsvSource({"0, 6504000000000001", "3, 6504000300000001", "4, 6504000400000001", "52, 6504003400000001"})
    void assertWriteAttachmentCharset(final int attachmentCharsetId, final String expectedHex) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(FirebirdDatabaseInfoPacketType.ATT_CHARSET), attachmentCharsetId)
                    .write((PacketPayload) new FirebirdPacketPayload(buffer, StandardCharsets.UTF_8));
            assertThat(ByteBufUtil.hexDump(buffer), is(expectedHex));
        } finally {
            buffer.release();
        }
    }
    
    @Test
    void assertWriteFirebirdVersion() {
        new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(FirebirdDatabaseInfoPacketType.FIREBIRD_VERSION), 0).write(payload);
        String fbVersion = String.format("%s-%s%d.%d.%d.%d Firebird %d.%d (ShardingSphere-Proxy)", FirebirdArchType.ARCHITECTURE.getIdentifier(), "V", 5, 0, 0, 0, 5, 0);
        InOrder order = inOrder(payload);
        order.verify(payload).writeInt1(FirebirdDatabaseInfoPacketType.FIREBIRD_VERSION.getCode());
        order.verify(payload).writeInt2LE(fbVersion.length() + 2);
        order.verify(payload).writeInt1(1);
        order.verify(payload).writeInt1(fbVersion.length());
        order.verify(payload).writeBytes(fbVersion.getBytes(StandardCharsets.UTF_8));
        order.verify(payload).writeInt1(FirebirdCommonInfoPacketType.END.getCode());
        order.verifyNoMoreInteractions();
    }
    
    @Test
    void assertParseDatabaseInfoWithUnknownType() {
        assertThrows(DatabaseProtocolException.class, () -> new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(FirebirdDatabaseInfoPacketType.DB_ID), 0).write(payload));
    }
    
    @Test
    void assertWriteCommonInfo() {
        new FirebirdDatabaseInfoReturnPacket(Collections.singletonList(FirebirdCommonInfoPacketType.END), 0).write(payload);
        verify(payload).writeInt1(FirebirdCommonInfoPacketType.END.getCode());
    }
    
    @Test
    void assertWriteWithEmptyInfo() {
        new FirebirdDatabaseInfoReturnPacket(Collections.emptyList(), 0).write(payload);
        InOrder order = inOrder(payload);
        order.verify(payload).writeInt1(FirebirdCommonInfoPacketType.END.getCode());
        order.verifyNoMoreInteractions();
    }
    
    @Test
    void assertWriteWithDataAfterEnd() {
        new FirebirdDatabaseInfoReturnPacket(Arrays.asList(FirebirdDatabaseInfoPacketType.ODS_VERSION, FirebirdCommonInfoPacketType.END,
                FirebirdDatabaseInfoPacketType.DB_ID, FirebirdCommonInfoPacketType.END), 0).write(payload);
        InOrder order = inOrder(payload);
        order.verify(payload).writeInt1(FirebirdDatabaseInfoPacketType.ODS_VERSION.getCode());
        order.verify(payload).writeInt2LE(4);
        order.verify(payload).writeInt4LE(13);
        order.verify(payload).writeInt1(FirebirdCommonInfoPacketType.END.getCode());
        order.verifyNoMoreInteractions();
    }
    
    private static Stream<Arguments> databaseInfoTypes() {
        return Stream.of(
                Arguments.of("db sql dialect", FirebirdDatabaseInfoPacketType.DB_SQL_DIALECT, 1, 3, true),
                Arguments.of("ods version", FirebirdDatabaseInfoPacketType.ODS_VERSION, 4, 13, false),
                Arguments.of("ods minor version", FirebirdDatabaseInfoPacketType.ODS_MINOR_VERSION, 4, 1, false));
    }
}
