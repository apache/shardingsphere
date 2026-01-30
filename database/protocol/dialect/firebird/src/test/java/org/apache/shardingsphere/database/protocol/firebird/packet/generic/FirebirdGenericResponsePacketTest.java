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

package org.apache.shardingsphere.database.protocol.firebird.packet.generic;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.atLeastOnce;

@ExtendWith(MockitoExtension.class)
class FirebirdGenericResponsePacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Mock
    private FirebirdPacket data;
    
    @Test
    void assertGetHandleAndId() {
        FirebirdGenericResponsePacket packet = FirebirdGenericResponsePacket.getPacket().setHandle(1).setId(2);
        assertThat(packet.getHandle(), is(1));
        assertThat(packet.getId(), is(2L));
    }
    
    @Test
    void assertGetErrorStatusVector() {
        SQLException ex = new SQLException("foo_error", "42000", ISCConstants.isc_random + 1);
        FirebirdGenericResponsePacket packet = new FirebirdGenericResponsePacket().setErrorStatusVector(ex);
        assertThat(packet.getErrorCode(), is(ex.getErrorCode()));
        assertThat(packet.getErrorMessage(), is("foo_error"));
    }
    
    @Test
    void assertWriteWithoutData() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        new FirebirdGenericResponsePacket().setHandle(3).setId(4).write(payload);
        verify(payload).writeInt4(FirebirdCommandPacketType.RESPONSE.getValue());
        verify(payload).writeInt4(3);
        verify(payload).writeInt8(4L);
        verify(payload).writeInt4(0);
        verify(byteBuf).writeZero(4);
    }
    
    @Test
    void assertWriteWithDataAndStatusVector() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.writeZero(4)).thenReturn(byteBuf);
        when(byteBuf.readableBytes()).thenReturn(4, 8);
        SQLException ex = new SQLException("foo_error", "42000", ISCConstants.isc_arith_except);
        FirebirdGenericResponsePacket packet = new FirebirdGenericResponsePacket().setHandle(1).setId(2).setData(data).setErrorStatusVector(ex);
        packet.write(payload);
        verify(data).write(payload);
        verify(payload, atLeastOnce()).writeInt4(ISCConstants.isc_arg_gds);
        verify(payload, atLeastOnce()).writeInt4(ISCConstants.isc_arg_string);
        verify(payload).writeString("foo_error");
        verify(payload, atLeastOnce()).writeInt4(ISCConstants.isc_arg_end);
    }
}
