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

package org.apache.shardingsphere.database.protocol.postgresql.payload;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.postgresql.packet.ByteBufTestUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PostgreSQLPacketPayloadTest {
    
    @Test
    void assertReadWrite() {
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(16, 128);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        byte expectedInt1 = (byte) 'i';
        payload.writeInt1(expectedInt1);
        assertThat(payload.readInt1(), is((int) expectedInt1));
        short expectedInt2 = Short.MAX_VALUE;
        payload.writeInt2(expectedInt2);
        assertThat(payload.readInt2(), is((int) expectedInt2));
        int expectedInt4 = Integer.MAX_VALUE;
        payload.writeInt4(expectedInt4);
        assertThat(payload.readInt4(), is(expectedInt4));
        long expectedInt8 = Long.MAX_VALUE;
        payload.writeInt8(expectedInt8);
        assertThat(payload.readInt8(), is(expectedInt8));
        payload.writeInt4(1);
        payload.skipReserved(4);
        String expectedString = "user";
        payload.writeStringEOF(expectedString);
        assertThat(byteBuf.readCharSequence(expectedString.length(), StandardCharsets.ISO_8859_1).toString(), is(expectedString));
        payload.writeStringNul(expectedString);
        assertThat(payload.bytesBeforeZero(), is(expectedString.length()));
        assertThat(payload.readStringNul(), is(expectedString));
        assertThat(payload.getByteBuf(), is(byteBuf));
    }
}
