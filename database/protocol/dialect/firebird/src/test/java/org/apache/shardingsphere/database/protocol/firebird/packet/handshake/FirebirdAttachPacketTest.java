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

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdAttachPacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertConstructor() {
        FirebirdAttachPacket actual = createPacket();
        assertThat(actual.getId(), is(100));
        assertThat(actual.getDatabase(), is("db"));
    }
    
    @Test
    void assertGetEncoding() {
        assertThat(createPacket().getEncoding(), is("UTF8"));
    }
    
    @Test
    void assertGetAuthData() {
        assertThat(createPacket().getAuthData(), is("ad"));
    }
    
    @Test
    void assertGetUsername() {
        assertThat(createPacket().getUsername(), is("user"));
    }
    
    @Test
    void assertGetEncPassword() {
        assertThat(createPacket().getEncPassword(), is("passwd"));
    }
    
    @Test
    void assertWrite() {
        FirebirdAttachPacket packet = createPacket();
        assertDoesNotThrow(() -> packet.write(payload));
    }
    
    private FirebirdAttachPacket createPacket() {
        when(payload.readInt4()).thenReturn(100);
        when(payload.readString()).thenReturn("db");
        ByteBuf dpb = mock(ByteBuf.class);
        when(payload.readBuffer()).thenReturn(dpb);
        when(dpb.readUnsignedByte()).thenReturn((short) 1, (short) FirebirdDatabaseParameterBufferType.LC_CTYPE.getCode(), (short) FirebirdDatabaseParameterBufferType.SPECIFIC_AUTH_DATA.getCode(),
                (short) FirebirdDatabaseParameterBufferType.USER_NAME.getCode(), (short) FirebirdDatabaseParameterBufferType.PASSWORD_ENC.getCode());
        when(dpb.isReadable()).thenReturn(true, true, true, true, false);
        when(dpb.readByte()).thenReturn((byte) 4, (byte) 2, (byte) 4, (byte) 6);
        ByteBuf encodingSlice = mock(ByteBuf.class);
        when(encodingSlice.toString(StandardCharsets.UTF_8)).thenReturn("UTF8");
        ByteBuf authDataSlice = mock(ByteBuf.class);
        when(authDataSlice.toString(StandardCharsets.UTF_8)).thenReturn("ad");
        ByteBuf usernameSlice = mock(ByteBuf.class);
        when(usernameSlice.toString(StandardCharsets.UTF_8)).thenReturn("user");
        ByteBuf encPasswordSlice = mock(ByteBuf.class);
        when(encPasswordSlice.toString(StandardCharsets.UTF_8)).thenReturn("passwd");
        when(dpb.readSlice(anyInt())).thenReturn(encodingSlice, authDataSlice, usernameSlice, encPasswordSlice);
        return new FirebirdAttachPacket(payload);
    }
}
