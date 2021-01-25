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

package org.apache.shardingsphere.proxy.frontend.postgresql;

import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.junit.Test;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.ServerErrorMessage;

import java.lang.reflect.Field;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PostgreSQLErrPacketFactoryTest {
    
    @Test
    public void assertPSQLExceptionWithServerErrorMessageNotNull() throws NoSuchFieldException, IllegalAccessException {
        ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
        when(serverErrorMessage.getSeverity()).thenReturn("severity");
        when(serverErrorMessage.getSQLState()).thenReturn("sqlState");
        when(serverErrorMessage.getMessage()).thenReturn("message");
        when(serverErrorMessage.getPosition()).thenReturn(1);
        PostgreSQLErrorResponsePacket actual = PostgreSQLErrPacketFactory.newInstance(new PSQLException(serverErrorMessage));
        Field packetField = PostgreSQLErrorResponsePacket.class.getDeclaredField("fields");
        packetField.setAccessible(true);
        Map<Character, String> fields = (Map<Character, String>) packetField.get(actual);
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_SEVERITY), is("severity"));
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_CODE), is("sqlState"));
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE), is("message"));
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_POSITION), is("1"));
    }
    
    @Test
    public void assertPSQLExceptionWithServerErrorMessageIsNull() throws NoSuchFieldException, IllegalAccessException {
        PostgreSQLErrorResponsePacket actual = PostgreSQLErrPacketFactory.newInstance(new PSQLException("psqlEx", PSQLState.UNEXPECTED_ERROR, new Exception("test")));
        Field packetField = PostgreSQLErrorResponsePacket.class.getDeclaredField("fields");
        packetField.setAccessible(true);
        Map<Character, String> fields = (Map<Character, String>) packetField.get(actual);
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_CODE), is(PSQLState.UNEXPECTED_ERROR.getState()));
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE), is("psqlEx"));
    }
    
    @Test
    public void assertRuntimeException() throws NoSuchFieldException, IllegalAccessException {
        PostgreSQLErrorResponsePacket actual = PostgreSQLErrPacketFactory.newInstance(new RuntimeException("test"));
        Field packetField = PostgreSQLErrorResponsePacket.class.getDeclaredField("fields");
        packetField.setAccessible(true);
        Map<Character, String> fields = (Map<Character, String>) packetField.get(actual);
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE), is("test"));
    }
}
