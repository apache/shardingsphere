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
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdBatchBlobStreamCommandPacketTest {
    
    @Test
    void assertConstructor() {
        FirebirdBatchBlobStreamCommandPacket actual = new FirebirdBatchBlobStreamCommandPacket(new FirebirdPacketPayload(createPacket(24L), StandardCharsets.UTF_8));
        assertThat(actual.getStatementHandle(), is(7));
        assertThat(actual.getLength(), is(24L));
        assertThat(actual.getData().readableBytes(), is(21));
    }
    
    @Test
    void assertGetLength() {
        ByteBuf packet = createPacket(24L);
        packet.writeInt(FirebirdCommandPacketType.BATCH_EXEC.getValue());
        FirebirdBatchBlobStream blobStream = new FirebirdBatchBlobStream();
        assertThat(FirebirdBatchBlobStreamCommandPacket.getLength(new FirebirdPacketPayload(packet, StandardCharsets.UTF_8), blobStream), is(33));
        ByteBuf nextData = Unpooled.buffer();
        nextData.writeLong(2L);
        nextData.writeInt(0);
        nextData.writeInt(0);
        assertThat(blobStream.skip(nextData, 16L), is(16L));
    }
    
    @Test
    void assertGetLengthWithIncompletePacket() {
        ByteBuf packet = createPacket(24L);
        ByteBuf incompletePacket = packet.slice(0, packet.readableBytes() - 1);
        FirebirdPacketPayload payload = new FirebirdPacketPayload(incompletePacket, StandardCharsets.UTF_8);
        assertThrows(IndexOutOfBoundsException.class, () -> FirebirdBatchBlobStreamCommandPacket.getLength(payload, new FirebirdBatchBlobStream()));
    }
    
    private ByteBuf createPacket(final long length) {
        ByteBuf result = Unpooled.buffer();
        result.writeInt(FirebirdCommandPacketType.BATCH_BLOB_STREAM.getValue());
        result.writeInt(7);
        result.writeInt((int) length);
        result.writeLong(1L);
        result.writeInt(5);
        result.writeInt(0);
        result.writeBytes(ByteBufUtil.decodeHexDump("6162636465"));
        return result;
    }
}
