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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.util.FirebirdDateTimeUtils;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.OffsetTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdTimeTZBinaryProtocolValueTest {
    
    // 01:02:03 is 37230000 Firebird fractions, 0x023815B0, and the +02:00 offset is 120 minutes, 0x78, inside a longword
    private static final byte[] ENCODED_TIME_TZ = {0x02, 0x38, 0x15, (byte) 0xB0, 0, 0, 0, 0x78};
    
    @Test
    void assertWrite() {
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new FirebirdTimeTZBinaryProtocolValue().write(payload, OffsetTime.parse("01:02:03+02:00"));
        byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        assertThat(actual, is(ENCODED_TIME_TZ));
    }
    
    @Test
    void assertRead() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.wrappedBuffer(ENCODED_TIME_TZ), StandardCharsets.UTF_8);
        assertThat(new FirebirdTimeTZBinaryProtocolValue().read(payload), is(FirebirdDateTimeUtils.getTime(37230000)));
        assertThat(payload.getByteBuf().readableBytes(), is(0));
    }
    
    @Test
    void assertGetLength() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        assertThat(new FirebirdTimeTZBinaryProtocolValue().getLength(payload), is(8));
    }
}
