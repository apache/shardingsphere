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

package org.apache.shardingsphere.database.protocol.firebird.packet.generic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdGetBlobSegmentResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.blob.FirebirdBlobInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.blob.FirebirdBlobInfoReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdInlineBlobResponsePacketTest {
    
    @Test
    void assertWrite() {
        FirebirdPacket blobInfo = new FirebirdBlobInfoReturnPacket(Collections.singletonList(FirebirdBlobInfoPacketType.TOTAL_LENGTH), 11);
        FirebirdPacket blobData = new FirebirdGetBlobSegmentResponsePacket(new byte[]{1, 2, 3, 4, 5});
        ByteBuf byteBuf = Unpooled.buffer();
        new FirebirdInlineBlobResponsePacket(7, 9L, blobInfo, blobData).write(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8));
        assertThat(byteBuf.readInt(), is(FirebirdCommandPacketType.INLINE_BLOB.getValue()));
        assertThat(byteBuf.readInt(), is(7));
        assertThat(byteBuf.readLong(), is(9L));
        assertBuffer(byteBuf, blobInfo);
        assertBuffer(byteBuf, blobData);
        assertThat(byteBuf.readableBytes(), is(0));
    }
    
    @Test
    void assertWriteWithUnalignedBuffers() {
        FirebirdPacket blobInfo = new FirebirdGetBlobSegmentResponsePacket(new byte[]{1, 2, 3});
        FirebirdPacket blobData = new FirebirdGetBlobSegmentResponsePacket(new byte[]{4, 5});
        ByteBuf byteBuf = Unpooled.buffer();
        new FirebirdInlineBlobResponsePacket(1, 2L, blobInfo, blobData).write(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8));
        assertThat(byteBuf.readInt(), is(FirebirdCommandPacketType.INLINE_BLOB.getValue()));
        assertThat(byteBuf.readInt(), is(1));
        assertThat(byteBuf.readLong(), is(2L));
        assertBuffer(byteBuf, blobInfo);
        assertBuffer(byteBuf, blobData);
        assertThat(byteBuf.readableBytes(), is(0));
    }
    
    @Test
    void assertGetters() {
        FirebirdPacket blobInfo = new FirebirdGetBlobSegmentResponsePacket(new byte[]{1});
        FirebirdPacket blobData = new FirebirdGetBlobSegmentResponsePacket(new byte[]{2});
        FirebirdInlineBlobResponsePacket packet = new FirebirdInlineBlobResponsePacket(3, 4L, blobInfo, blobData);
        assertThat(packet.getTransactionId(), is(3));
        assertThat(packet.getBlobId(), is(4L));
        assertThat(packet.getBlobInfo(), is(blobInfo));
        assertThat(packet.getBlobData(), is(blobData));
    }
    
    private static void assertBuffer(final ByteBuf actual, final FirebirdPacket expected) {
        int expectedLength = writeToNewBuffer(expected);
        assertThat(actual.readInt(), is(expectedLength));
        byte[] actualContent = new byte[expectedLength];
        actual.readBytes(actualContent);
        assertThat(actualContent, is(writeToNewArray(expected)));
        actual.skipBytes((4 - expectedLength) & 3);
    }
    
    private static int writeToNewBuffer(final FirebirdPacket packet) {
        return writeToNewArray(packet).length;
    }
    
    private static byte[] writeToNewArray(final FirebirdPacket packet) {
        ByteBuf byteBuf = Unpooled.buffer();
        packet.write(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8));
        byte[] result = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(result);
        return result;
    }
}
