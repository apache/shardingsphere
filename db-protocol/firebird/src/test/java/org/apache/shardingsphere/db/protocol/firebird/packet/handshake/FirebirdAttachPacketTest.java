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

package org.apache.shardingsphere.db.protocol.firebird.packet.handshake;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.firebird.constant.buffer.type.FirebirdDatabaseParameterBufferType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FirebirdAttachPacketTest {
    
    private ByteBuf createDPB() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(1);
        buf.writeByte(FirebirdDatabaseParameterBufferType.LC_CTYPE.getCode());
        buf.writeByte(4);
        buf.writeBytes("UTF8".getBytes(StandardCharsets.UTF_8));
        buf.writeByte(FirebirdDatabaseParameterBufferType.SPECIFIC_AUTH_DATA.getCode());
        buf.writeByte(2);
        buf.writeBytes("ad".getBytes(StandardCharsets.UTF_8));
        buf.writeByte(FirebirdDatabaseParameterBufferType.USER_NAME.getCode());
        buf.writeByte(4);
        buf.writeBytes("user".getBytes(StandardCharsets.UTF_8));
        buf.writeByte(FirebirdDatabaseParameterBufferType.PASSWORD_ENC.getCode());
        buf.writeByte(6);
        buf.writeBytes("passwd".getBytes(StandardCharsets.UTF_8));
        return buf;
    }
    
    @Test
    void assertParseAttachPacket() {
        ByteBuf buf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(buf, StandardCharsets.UTF_8);
        payload.writeInt4(100);
        payload.writeString("db");
        payload.writeBuffer(createDPB());
        buf.readerIndex(0);
        FirebirdAttachPacket packet = new FirebirdAttachPacket(new FirebirdPacketPayload(buf, StandardCharsets.UTF_8));
        assertEquals(100, packet.getId());
        assertThat(packet.getDatabase(), is("db"));
        assertThat(packet.getEncoding(), is("UTF8"));
        assertThat(packet.getAuthData(), is("ad"));
        assertThat(packet.getUsername(), is("user"));
        assertThat(packet.getEncPassword(), is("passwd"));
    }
}
