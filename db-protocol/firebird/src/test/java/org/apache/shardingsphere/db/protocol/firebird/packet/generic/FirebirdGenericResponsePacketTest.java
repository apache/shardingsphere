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

package org.apache.shardingsphere.db.protocol.firebird.packet.generic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.firebird.err.FirebirdStatusVector;
import org.apache.shardingsphere.db.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.db.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FirebirdGenericResponsePacketTest {
    
    @Test
    void assertGetHandleAndId() {
        FirebirdGenericResponsePacket packet = FirebirdGenericResponsePacket.getPacket().setHandle(1).setId(2);
        assertThat(packet.getHandle(), is(1));
        assertThat(packet.getId(), is(2L));
    }
    
    @Test
    void assertGetErrorStatusVector() {
        SQLException ex = new SQLException("foo", "42000", ISCConstants.isc_random + 1);
        FirebirdGenericResponsePacket packet = new FirebirdGenericResponsePacket().setErrorStatusVector(ex);
        assertThat(packet.getErrorCode(), is(ex.getErrorCode()));
        assertThat(packet.getErrorMessage(), is("foo"));
    }
    
    @Test
    void assertWriteWithoutData() {
        ByteBuf buffer = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(buffer, StandardCharsets.UTF_8);
        new FirebirdGenericResponsePacket().setHandle(3).setId(4).write(payload);
        assertThat(buffer.readInt(), is(FirebirdCommandPacketType.RESPONSE.getValue()));
        assertThat(buffer.readInt(), is(3));
        assertThat(buffer.readLong(), is(4L));
        assertThat(buffer.readInt(), is(0));
        assertThat(buffer.readInt(), is(0));
    }
    
    @Test
    void assertWriteWithDataAndStatusVector() throws ReflectiveOperationException {
        ByteBuf buffer = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(buffer, StandardCharsets.UTF_8);
        FirebirdPacket data = mock(FirebirdPacket.class);
        FirebirdStatusVector vector = mock(FirebirdStatusVector.class);
        FirebirdGenericResponsePacket packet = new FirebirdGenericResponsePacket().setHandle(1).setId(2).setData(data);
        Field field = FirebirdGenericResponsePacket.class.getDeclaredField("statusVector");
        field.setAccessible(true);
        field.set(packet, vector);
        packet.write(payload);
        verify(data).write(payload);
        verify(vector).write(payload);
    }
}
