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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBpbVersionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidClumpletStructureException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdBatchBlobStreamTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidStructureBpbArguments")
    void assertSetDefaultBpbWithInvalidStructure(final String name, final byte[] bpb, final String expectedReason, final int expectedData) {
        InvalidClumpletStructureException actual = assertThrows(InvalidClumpletStructureException.class, () -> new FirebirdBatchBlobStream().setDefaultBpb(Unpooled.wrappedBuffer(bpb)));
        assertThat(actual.getReason(), is(expectedReason));
        assertThat(actual.getData(), is(expectedData));
    }
    
    private static Stream<Arguments> invalidStructureBpbArguments() {
        return Stream.of(
                Arguments.of("empty_bpb", new byte[0], "empty buffer", 0),
                Arguments.of("clumplet_without_length", new byte[]{1, 4}, "buffer end before end of clumplet - no length component", 1),
                Arguments.of("clumplet_too_long", new byte[]{1, 4, 9, 1}, "buffer end before end of clumplet - clumplet too long", 11),
                Arguments.of("type_clumplet_too_long", new byte[]{1, 3, 5, 1}, "buffer end before end of clumplet - clumplet too long", 7),
                Arguments.of("type_longer_than_integer", new byte[]{1, 3, 5, 1, 0, 0, 0, 0}, "length of integer exceeds 4 bytes", 5));
    }
    
    @Test
    void assertSetDefaultBpbWithWrongVersion() {
        InvalidBpbVersionException actual = assertThrows(InvalidBpbVersionException.class, () -> new FirebirdBatchBlobStream().setDefaultBpb(Unpooled.wrappedBuffer(new byte[]{2, 3, 1, 0})));
        assertThat(actual.getVersion(), is(2));
        assertThat(actual.getExpectedVersion(), is(1));
    }
    
    @Test
    void assertCopy() {
        FirebirdBatchBlobStream blobStream = new FirebirdBatchBlobStream();
        ByteBuf data = Unpooled.buffer();
        writeHeader(data, 1L, 20, 0);
        data.writeBytes(new byte[16]);
        getLength(blobStream.copy(), data, 32L);
        assertThat(getLength(blobStream, Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4}), 4L), is(12));
    }
    
    private static void writeHeader(final ByteBuf data, final long batchBlobId, final int blobLength, final int bpbLength) {
        data.writeLong(batchBlobId);
        data.writeInt(blobLength);
        data.writeInt(bpbLength);
    }
    
    private static int getLength(final FirebirdBatchBlobStream blobStream, final ByteBuf data, final long length) {
        ByteBuf packet = Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_BLOB_STREAM.getValue()).writeInt(1).writeInt((int) length).writeBytes(data);
        return FirebirdBatchBlobStreamCommandPacket.getLength(new FirebirdPacketPayload(packet, StandardCharsets.UTF_8), blobStream);
    }
}
