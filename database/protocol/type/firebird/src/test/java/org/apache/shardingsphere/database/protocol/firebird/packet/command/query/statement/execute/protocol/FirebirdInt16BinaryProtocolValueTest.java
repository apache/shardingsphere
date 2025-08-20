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
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdInt16BinaryProtocolValueTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Mock
    private ByteBuf result;
    
    @Test
    void assertRead() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readSlice(16)).thenReturn(result);
        assertThat(new FirebirdInt16BinaryProtocolValue().read(payload), is(result));
    }
    
    @Test
    void assertWriteWithInteger() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        new FirebirdInt16BinaryProtocolValue().write(payload, 1);
        verify(byteBuf).writeZero(12);
        verify(payload).writeInt4(1);
    }
    
    @Test
    void assertWriteWithBigDecimal() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        new FirebirdInt16BinaryProtocolValue().write(payload, BigDecimal.ONE);
        verify(byteBuf).writeZero(15);
        verify(byteBuf).writeBytes(new byte[]{1});
    }
}
