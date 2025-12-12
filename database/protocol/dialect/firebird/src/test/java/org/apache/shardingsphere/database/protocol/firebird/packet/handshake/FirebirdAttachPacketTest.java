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

package org.apache.shardingsphere.database.protocol.firebird.packet.handshake;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.firebird.constant.buffer.type.FirebirdDatabaseParameterBufferType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdAttachPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertAttachPacket() {
        when(payload.readInt4()).thenReturn(100);
        when(payload.readString()).thenReturn("db");
        ByteBuf dpb = mock(ByteBuf.class);
        when(payload.readBuffer()).thenReturn(dpb);
        when(dpb.readUnsignedByte()).thenReturn(
                (short) 1,
                (short) FirebirdDatabaseParameterBufferType.LC_CTYPE.getCode(),
                (short) FirebirdDatabaseParameterBufferType.SPECIFIC_AUTH_DATA.getCode(),
                (short) FirebirdDatabaseParameterBufferType.USER_NAME.getCode(),
                (short) FirebirdDatabaseParameterBufferType.PASSWORD_ENC.getCode());
        when(dpb.isReadable()).thenReturn(true, true, true, true, false);
        when(dpb.readByte()).thenReturn((byte) 4, (byte) 2, (byte) 4, (byte) 6);
        ByteBuf slice1 = mock(ByteBuf.class);
        when(slice1.toString(java.nio.charset.StandardCharsets.UTF_8)).thenReturn("UTF8");
        ByteBuf slice2 = mock(ByteBuf.class);
        when(slice2.toString(java.nio.charset.StandardCharsets.UTF_8)).thenReturn("ad");
        ByteBuf slice3 = mock(ByteBuf.class);
        when(slice3.toString(java.nio.charset.StandardCharsets.UTF_8)).thenReturn("user");
        ByteBuf slice4 = mock(ByteBuf.class);
        when(slice4.toString(java.nio.charset.StandardCharsets.UTF_8)).thenReturn("passwd");
        when(dpb.readSlice(anyInt())).thenReturn(slice1, slice2, slice3, slice4);
        FirebirdAttachPacket packet = new FirebirdAttachPacket(payload);
        assertThat(packet.getId(), is(100));
        assertThat(packet.getDatabase(), is("db"));
        assertThat(packet.getEncoding(), is("UTF8"));
        assertThat(packet.getAuthData(), is("ad"));
        assertThat(packet.getUsername(), is("user"));
        assertThat(packet.getEncPassword(), is("passwd"));
    }
}
