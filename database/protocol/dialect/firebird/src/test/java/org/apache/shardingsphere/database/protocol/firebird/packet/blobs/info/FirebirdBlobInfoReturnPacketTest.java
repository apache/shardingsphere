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

package org.apache.shardingsphere.database.protocol.firebird.packet.blobs.info;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.commonrequests.FirebirdCommonInfoItems;
import org.apache.shardingsphere.database.protocol.firebird.packet.commonrequests.FirebirdInfoItem;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FirebirdBlobInfoReturnPacketTest {
    
    private static final String SWITCH_CLASS_NAME = "org.apache.shardingsphere.database.protocol.firebird.packet.blobs.info.FirebirdBlobInfoReturnPacket$1";
    
    private static final String SWITCH_FIELD_NAME = "$SwitchMap$org$apache$shardingsphere$database$protocol$firebird$packet$blobs$info$FirebirdBlobInfoItems";
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertGetInfoItems() {
        List<FirebirdInfoItem> expectedItems = Collections.singletonList(FirebirdCommonInfoItems.END);
        assertThat(new FirebirdBlobInfoReturnPacket(expectedItems, 0, false).getItems(), is(expectedItems));
    }
    
    @Test
    void assertWriteCommonInfoWithRequestedEndWritesSingleTerminator() {
        FirebirdBlobInfoReturnPacket packet = new FirebirdBlobInfoReturnPacket(Collections.singletonList(FirebirdCommonInfoItems.END), 0, false);
        packet.write((PacketPayload) payload);
        verify(payload).writeInt1(FirebirdCommonInfoItems.END.getCode());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("blobInfoCases")
    void assertWriteBlobInfo(final String name, final FirebirdBlobInfoItems type, final int blobLength, final boolean streamBlob, final int expectedValue) {
        FirebirdBlobInfoReturnPacket packet = new FirebirdBlobInfoReturnPacket(Collections.singletonList(type), blobLength, streamBlob);
        packet.write((PacketPayload) payload);
        InOrder order = inOrder(payload);
        order.verify(payload).writeInt1(type.getCode());
        order.verify(payload).writeInt2LE(4);
        order.verify(payload).writeInt4LE(expectedValue);
        order.verify(payload).writeInt1(FirebirdCommonInfoItems.END.getCode());
    }
    
    @Test
    void assertWriteCommonInfoWithUnsupportedType() {
        assertThrows(DatabaseProtocolException.class,
                () -> new FirebirdBlobInfoReturnPacket(Collections.singletonList(FirebirdCommonInfoItems.TRUNCATED), 0, false).write((PacketPayload) payload));
    }
    
    @Test
    void assertWriteBlobInfoWithUnknownType() {
        int[] switchMap = getSwitchMap();
        int typeIndex = FirebirdBlobInfoItems.TYPE.ordinal();
        int actualSwitchValue = switchMap[typeIndex];
        switchMap[typeIndex] = 0;
        try {
            assertThrows(DatabaseProtocolException.class, () -> new FirebirdBlobInfoReturnPacket(Collections.singletonList(FirebirdBlobInfoItems.TYPE), 0, false).write((PacketPayload) payload));
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
                Arguments.of("NUM_SEGMENTS without data", FirebirdBlobInfoItems.NUM_SEGMENTS, 0, false, 0),
                Arguments.of("NUM_SEGMENTS with data", FirebirdBlobInfoItems.NUM_SEGMENTS, 3, false, 1),
                Arguments.of("MAX_SEGMENT with data", FirebirdBlobInfoItems.MAX_SEGMENT, 4, false, 4),
                Arguments.of("TOTAL_LENGTH with data", FirebirdBlobInfoItems.TOTAL_LENGTH, 5, false, 5),
                Arguments.of("TYPE of segmented BLOB", FirebirdBlobInfoItems.TYPE, 1, false, 0),
                Arguments.of("TYPE of stream BLOB", FirebirdBlobInfoItems.TYPE, 1, true, 1));
    }
}
