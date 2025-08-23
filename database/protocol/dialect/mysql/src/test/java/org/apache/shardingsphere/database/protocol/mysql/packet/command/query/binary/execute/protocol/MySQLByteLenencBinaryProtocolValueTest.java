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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MySQLByteLenencBinaryProtocolValueTest {
    
    @Test
    void assertRead() {
        byte[] input = {0x0d, 0x0a, 0x33, 0x18, 0x01, 0x4a, 0x08, 0x0a, (byte) 0x9a, 0x01, 0x18, 0x01, 0x4a, 0x6f};
        byte[] expected = {0x0a, 0x33, 0x18, 0x01, 0x4a, 0x08, 0x0a, (byte) 0x9a, 0x01, 0x18, 0x01, 0x4a, 0x6f};
        ByteBuf byteBuf = Unpooled.wrappedBuffer(input);
        MySQLPacketPayload payload = new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        byte[] actual = (byte[]) new MySQLByteLenencBinaryProtocolValue().read(payload, false);
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertWrite() {
        byte[] input = {0x0a, 0x33, 0x18, 0x01, 0x4a, 0x08, 0x0a, (byte) 0x9a, 0x01, 0x18, 0x01, 0x4a, 0x6f};
        byte[] expected = {0x0d, 0x0a, 0x33, 0x18, 0x01, 0x4a, 0x08, 0x0a, (byte) 0x9a, 0x01, 0x18, 0x01, 0x4a, 0x6f};
        ByteBuf actual = Unpooled.wrappedBuffer(new byte[expected.length]).writerIndex(0);
        MySQLPacketPayload payload = new MySQLPacketPayload(actual, StandardCharsets.UTF_8);
        new MySQLByteLenencBinaryProtocolValue().write(payload, input);
        assertThat(ByteBufUtil.getBytes(actual), is(expected));
    }
}
