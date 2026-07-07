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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.blob;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class FirebirdBlobInfoReturnPacketTest {
    
    private static final String SWITCH_CLASS_NAME =
            "org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.blob.FirebirdBlobInfoReturnPacket$1";
    
    private static final String SWITCH_FIELD_NAME =
            "$SwitchMap$org$apache$shardingsphere$database$protocol$firebird$packet$command$query$info$type$blob$FirebirdBlobInfoPacketType";
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertGetInfoItems() {
        List<FirebirdInfoPacketType> expectedInfoItems = Collections.singletonList(FirebirdCommonInfoPacketType.END);
        assertThat(new FirebirdBlobInfoReturnPacket(expectedInfoItems, 0).getInfoItems(), is(expectedInfoItems));
    }
    
    @Test
    void assertWriteCommonInfo() {
        FirebirdBlobInfoReturnPacket packet = new FirebirdBlobInfoReturnPacket(Collections.singletonList(FirebirdCommonInfoPacketType.END), 0);
        packet.write((PacketPayload) payload);
        inOrder(payload).verify(payload).writeInt1(FirebirdCommonInfoPacketType.END.getCode());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("blobInfoCases")
    void assertWriteBlobInfo(final String name, final FirebirdBlobInfoPacketType type, final int blobLength, final int expectedValue) {
        FirebirdBlobInfoReturnPacket packet = new FirebirdBlobInfoReturnPacket(Collections.singletonList(type), blobLength);
        packet.write((PacketPayload) payload);
        InOrder order = inOrder(payload);
        order.verify(payload).writeInt1(type.getCode());
        order.verify(payload).writeInt2LE(4);
        order.verify(payload).writeInt4LE(expectedValue);
    }
    
    @Test
    void assertWriteCommonInfoWithUnsupportedType() {
        assertThrows(FirebirdProtocolException.class, () -> new FirebirdBlobInfoReturnPacket(Collections.singletonList(FirebirdCommonInfoPacketType.TRUNCATED), 0).write((PacketPayload) payload));
    }
    
    @Test
    void assertWriteBlobInfoWithUnknownType() {
        int[] switchMap = getSwitchMap();
        int typeIndex = FirebirdBlobInfoPacketType.TYPE.ordinal();
        int actualSwitchValue = switchMap[typeIndex];
        switchMap[typeIndex] = 0;
        try {
            assertThrows(FirebirdProtocolException.class, () -> new FirebirdBlobInfoReturnPacket(Collections.singletonList(FirebirdBlobInfoPacketType.TYPE), 0).write((PacketPayload) payload));
        } finally {
            switchMap[typeIndex] = actualSwitchValue;
        }
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private int[] getSwitchMap() {
        return (int[]) Plugins.getMemberAccessor().get(Class.forName(SWITCH_CLASS_NAME).getDeclaredField(SWITCH_FIELD_NAME), null);
    }
    
    private static Stream<Arguments> blobInfoCases() {
        return Stream.of(
                Arguments.of("NUM_SEGMENTS without data", FirebirdBlobInfoPacketType.NUM_SEGMENTS, 0, 0),
                Arguments.of("NUM_SEGMENTS with data", FirebirdBlobInfoPacketType.NUM_SEGMENTS, 3, 1),
                Arguments.of("MAX_SEGMENT with data", FirebirdBlobInfoPacketType.MAX_SEGMENT, 4, 4),
                Arguments.of("TOTAL_LENGTH with data", FirebirdBlobInfoPacketType.TOTAL_LENGTH, 5, 5),
                Arguments.of("TYPE", FirebirdBlobInfoPacketType.TYPE, 1, 0));
    }
}
