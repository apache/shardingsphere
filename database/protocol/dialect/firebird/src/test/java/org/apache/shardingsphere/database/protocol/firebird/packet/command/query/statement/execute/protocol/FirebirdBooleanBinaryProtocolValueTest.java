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
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdBooleanBinaryProtocolValueTest {
    
    @Test
    void assertReadTrue() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[]{1, 0, 0, 0});
        assertTrue((Boolean) new FirebirdBooleanBinaryProtocolValue().read(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8)));
        assertThat(byteBuf.readerIndex(), is(4));
    }
    
    @Test
    void assertReadFalse() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0});
        assertFalse((Boolean) new FirebirdBooleanBinaryProtocolValue().read(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8)));
        assertThat(byteBuf.readerIndex(), is(4));
    }
    
    @Test
    void assertReadFalseWithUnexpectedValue() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[]{2, 0, 0, 0});
        assertFalse((Boolean) new FirebirdBooleanBinaryProtocolValue().read(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8)));
        assertThat(byteBuf.readerIndex(), is(4));
    }
    
    @Test
    void assertWriteWithTrue() {
        ByteBuf byteBuf = Unpooled.buffer();
        new FirebirdBooleanBinaryProtocolValue().write(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8), true);
        assertThat(byteBuf.readByte(), is((byte) 1));
        assertThat(byteBuf.readByte(), is((byte) 0));
        assertThat(byteBuf.readByte(), is((byte) 0));
        assertThat(byteBuf.readByte(), is((byte) 0));
    }
    
    @Test
    void assertWriteWithFalse() {
        ByteBuf byteBuf = Unpooled.buffer();
        new FirebirdBooleanBinaryProtocolValue().write(new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8), false);
        assertThat(byteBuf.readByte(), is((byte) 0));
        assertThat(byteBuf.readByte(), is((byte) 0));
        assertThat(byteBuf.readByte(), is((byte) 0));
        assertThat(byteBuf.readByte(), is((byte) 0));
    }
    
    @Test
    void assertGetLength() {
        assertThat(new FirebirdBooleanBinaryProtocolValue().getLength(new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8)), is(4));
    }
}
