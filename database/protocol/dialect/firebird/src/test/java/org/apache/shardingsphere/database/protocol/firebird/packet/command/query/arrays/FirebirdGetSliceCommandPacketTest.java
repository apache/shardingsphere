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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.arrays;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.BlrConstants;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdGetSliceCommandPacketTest {
    
    @Test
    void assertReadPacket() {
        ByteBuf input = createRequest();
        try {
            new FirebirdGetSliceCommandPacket(new FirebirdPacketPayload(input, StandardCharsets.UTF_8));
            assertThat(input.readInt(), is(FirebirdCommandPacketType.COMMIT.getValue()));
        } finally {
            input.release();
        }
    }
    
    @Test
    void assertGetLength() {
        ByteBuf input = createRequest();
        try {
            assertThat(FirebirdGetSliceCommandPacket.getLength(new FirebirdPacketPayload(input, StandardCharsets.UTF_8)), is(44));
            assertThat(input.readerIndex(), is(Integer.BYTES));
        } finally {
            input.release();
        }
    }
    
    private static ByteBuf createRequest() {
        return Unpooled.buffer().writeInt(0).writeInt(FirebirdCommandPacketType.GET_SLICE.getValue()).writeInt(1).writeLong(2L).writeInt(4)
                .writeInt(6).writeBytes(new byte[]{1, 6, 1, BlrConstants.blr_long, 0, (byte) 255}).writeZero(2)
                .writeInt(4).writeInt(7).writeInt(0).writeInt(FirebirdCommandPacketType.COMMIT.getValue()).skipBytes(Integer.BYTES);
    }
}
