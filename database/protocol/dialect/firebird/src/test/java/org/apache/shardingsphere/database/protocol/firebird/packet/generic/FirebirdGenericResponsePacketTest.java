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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdGenericResponsePacketTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    void assertGetPacket() {
        assertThat(FirebirdGenericResponsePacket.getPacket(), isA(FirebirdGenericResponsePacket.class));
    }
    
    @Test
    void assertSetHandle() {
        FirebirdGenericResponsePacket packet = new FirebirdGenericResponsePacket();
        assertThat(packet.setHandle(1), is(packet));
        assertThat(packet.getHandle(), is(1));
    }
    
    @Test
    void assertSetIdWithInt() {
        FirebirdGenericResponsePacket packet = new FirebirdGenericResponsePacket();
        assertThat(packet.setId(2), is(packet));
        assertThat(packet.getId(), is(2L));
    }
    
    @Test
    void assertSetIdWithLong() {
        FirebirdGenericResponsePacket packet = new FirebirdGenericResponsePacket();
        assertThat(packet.setId(2L), is(packet));
        assertThat(packet.getId(), is(2L));
    }
    
    @Test
    void assertSetData() {
        FirebirdGenericResponsePacket packet = new FirebirdGenericResponsePacket();
        FirebirdPacket data = mock(FirebirdPacket.class);
        assertThat(packet.setData(data), is(packet));
        assertThat(packet.getData(), is(data));
    }
    
    @Test
    void assertSetErrorStatusVector() {
        SQLException ex = new SQLException("foo_error", "42000", ISCConstants.isc_random + 1);
        FirebirdGenericResponsePacket packet = new FirebirdGenericResponsePacket();
        assertThat(packet.setErrorStatusVector(ex), is(packet));
        assertNotNull(packet.getStatusVector());
        assertThat(packet.getErrorCode(), is(ex.getErrorCode()));
        assertThat(packet.getErrorMessage(), is("foo_error"));
    }
    
    @Test
    void assertGetErrorCodeWithNullStatusVector() {
        assertThat(new FirebirdGenericResponsePacket().getErrorCode(), is(-1));
    }
    
    @Test
    void assertGetErrorMessageWithNullStatusVector() {
        assertThat(new FirebirdGenericResponsePacket().getErrorMessage(), is(""));
    }
    
    @Test
    void assertSetWriteZeroStatementId() {
        FirebirdGenericResponsePacket packet = new FirebirdGenericResponsePacket();
        FirebirdGenericResponsePacket actual = packet.setWriteZeroStatementId(true);
        assertThat(actual, is(packet));
        assertTrue(packet.isWriteZeroStatementId());
    }
    
    @Test
    void assertWriteWithoutDataAndStatusVector() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        FirebirdGenericResponsePacket.getPacket().setHandle(3).setId(4L).write(payload);
        verify(payload).writeInt4(FirebirdCommandPacketType.RESPONSE.getValue());
        verify(payload).writeInt4(3);
        verify(payload).writeInt8(4L);
        verify(payload).writeInt4(0);
        verify(byteBuf).writeZero(4);
    }
    
    @Test
    void assertWriteWithDataAndStatusVector() {
        when(byteBuf.writeZero(4)).thenReturn(byteBuf);
        when(byteBuf.readableBytes()).thenReturn(4, 8);
        SQLException ex = new SQLException("foo_error", "42000", ISCConstants.isc_arith_except);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        FirebirdPacket data = mock(FirebirdPacket.class);
        new FirebirdGenericResponsePacket().setData(data).setHandle(1).setId(2L).setErrorStatusVector(ex).write(payload);
        verify(data).write(payload);
        verify(payload, atLeastOnce()).writeInt4(ISCConstants.isc_arg_gds);
        verify(payload, atLeastOnce()).writeInt4(ISCConstants.isc_arg_string);
        verify(payload).writeString("foo_error");
        verify(payload, atLeastOnce()).writeInt4(ISCConstants.isc_arg_end);
    }
    
    @Test
    void assertWriteWithZeroStatementId() {
        FirebirdGenericResponsePacket packet = new FirebirdGenericResponsePacket().setHandle(8).setWriteZeroStatementId(true).setId(5L);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        packet.write(payload);
        verify(payload).writeInt4(FirebirdCommandPacketType.RESPONSE.getValue());
        verify(payload).writeInt8(5L);
        verify(payload, never()).writeInt4(8);
        verify(payload, atLeastOnce()).writeInt4(0);
        verify(byteBuf).writeZero(4);
        assertFalse(packet.isWriteZeroStatementId());
        assertNull(packet.getData());
    }
}
