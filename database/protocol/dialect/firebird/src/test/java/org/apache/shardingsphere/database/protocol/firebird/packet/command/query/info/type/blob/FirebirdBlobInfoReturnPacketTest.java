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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdBlobRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdBlobInfoReturnPacketTest {
    
    private static final int CONNECTION_ID = 1;
    
    private static final int BLOB_HANDLE = 2;
    
    @AfterEach
    void tearDown() {
        FirebirdBlobRegistry.clearSegment();
        FirebirdBlobRegistry.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertGetInfoItems() {
        List<FirebirdInfoPacketType> expectedInfoItems = Collections.singletonList(FirebirdCommonInfoPacketType.END);
        assertThat(new FirebirdBlobInfoReturnPacket(expectedInfoItems, CONNECTION_ID, BLOB_HANDLE).getInfoItems(), is(expectedInfoItems));
    }
    
    @Test
    void assertWriteCommonInfo() {
        FirebirdPacketPayload payload = createPayload();
        FirebirdBlobInfoReturnPacket packet = new FirebirdBlobInfoReturnPacket(Collections.singletonList(FirebirdCommonInfoPacketType.END), CONNECTION_ID, BLOB_HANDLE);
        packet.write((PacketPayload) payload);
        assertThat(payload.getByteBuf().readByte(), is((byte) FirebirdCommonInfoPacketType.END.getCode()));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("blobInfoCases")
    void assertWriteBlobInfo(final String name, final FirebirdBlobInfoPacketType type, final byte[] segment, final int expectedValue) {
        FirebirdBlobRegistry.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBlobRegistry.getInstance().openBlob(CONNECTION_ID, BLOB_HANDLE, segment);
        FirebirdPacketPayload payload = createPayload();
        FirebirdBlobInfoReturnPacket packet = new FirebirdBlobInfoReturnPacket(Collections.singletonList(type), CONNECTION_ID, BLOB_HANDLE);
        packet.write((PacketPayload) payload);
        ByteBuf byteBuf = payload.getByteBuf();
        assertThat(byteBuf.readByte(), is((byte) type.getCode()));
        assertThat(byteBuf.readShortLE(), is((short) 4));
        assertThat(byteBuf.readIntLE(), is(expectedValue));
    }
    
    @Test
    void assertWriteCommonInfoWithUnsupportedType() {
        FirebirdBlobInfoReturnPacket packet = new FirebirdBlobInfoReturnPacket(Collections.singletonList(FirebirdCommonInfoPacketType.TRUNCATED), CONNECTION_ID, BLOB_HANDLE);
        assertThrows(FirebirdProtocolException.class, () -> packet.write((PacketPayload) createPayload()));
    }
    
    private FirebirdPacketPayload createPayload() {
        return new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
    }
    
    private static Stream<Arguments> blobInfoCases() {
        return Stream.of(
                Arguments.of("NUM_SEGMENTS without segment", FirebirdBlobInfoPacketType.NUM_SEGMENTS, null, 0),
                Arguments.of("NUM_SEGMENTS with segment", FirebirdBlobInfoPacketType.NUM_SEGMENTS, new byte[]{1, 2, 3}, 1),
                Arguments.of("MAX_SEGMENT with segment", FirebirdBlobInfoPacketType.MAX_SEGMENT, new byte[]{1, 2, 3, 4}, 4),
                Arguments.of("TOTAL_LENGTH with segment", FirebirdBlobInfoPacketType.TOTAL_LENGTH, new byte[]{1, 2, 3, 4, 5}, 5),
                Arguments.of("TYPE", FirebirdBlobInfoPacketType.TYPE, new byte[]{7}, 0));
    }
}
