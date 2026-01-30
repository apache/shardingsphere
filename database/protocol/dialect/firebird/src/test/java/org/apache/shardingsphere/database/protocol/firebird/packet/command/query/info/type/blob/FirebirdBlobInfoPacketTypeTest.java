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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.blob;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdBlobInfoPacketTypeTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    void assertValueOf() {
        FirebirdBlobInfoPacketType actual = FirebirdBlobInfoPacketType.valueOf(FirebirdBlobInfoPacketType.NUM_SEGMENTS.getCode());
        assertThat(actual, is(FirebirdBlobInfoPacketType.NUM_SEGMENTS));
        assertFalse(actual.isCommon());
    }
    
    @Test
    void assertCreatePacket() {
        when(payload.readInt4()).thenReturn(1, 2, 100);
        when(payload.readBuffer()).thenReturn(byteBuf);
        when(byteBuf.isReadable()).thenReturn(true, false);
        when(byteBuf.readByte()).thenReturn((byte) FirebirdBlobInfoPacketType.TYPE.getCode());
        FirebirdInfoPacket actual = FirebirdBlobInfoPacketType.createPacket(payload);
        assertThat(actual, isA(FirebirdInfoPacket.class));
        List<?> actualInfoItems = actual.getInfoItems();
        assertThat(actualInfoItems.size(), is(1));
        assertThat(actualInfoItems.get(0), is(FirebirdBlobInfoPacketType.TYPE));
        assertThat(actual.getMaxLength(), is(100));
    }
}
