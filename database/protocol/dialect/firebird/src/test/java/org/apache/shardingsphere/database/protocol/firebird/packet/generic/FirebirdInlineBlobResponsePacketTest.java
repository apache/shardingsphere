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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdGetBlobSegmentResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.blob.FirebirdBlobInfoPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.blob.FirebirdBlobInfoReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
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
        assertWriteBytes(7, 9L, blobInfo, blobData, new byte[]{0, 0, 0, 114, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 8, 6, 4, 0, 11, 0, 0, 0, 1, 0, 0, 0, 7, 5, 0, 1, 2, 3, 4, 5, 0});
    }
    
    @Test
    void assertWriteWithUnalignedBuffers() {
        FirebirdPacket blobInfo = new FirebirdGetBlobSegmentResponsePacket(new byte[]{1, 2, 3});
        FirebirdPacket blobData = new FirebirdGetBlobSegmentResponsePacket(new byte[]{4, 5});
        assertWriteBytes(1, 2L, blobInfo, blobData, new byte[]{0, 0, 0, 114, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 5, 3, 0, 1, 2, 3, 0, 0, 0, 0, 0, 0, 4, 2, 0, 4, 5});
    }
    
    private static void assertWriteBytes(final int transactionId, final long blobId, final FirebirdPacket blobInfo, final FirebirdPacket blobData, final byte[] expectedBytes) {
        ByteBuf byteBuf = Unpooled.buffer();
        PacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new FirebirdInlineBlobResponsePacket(transactionId, blobId, blobInfo, blobData).write(payload);
        byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        assertThat(actual, is(expectedBytes));
    }
}
