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

package org.apache.shardingsphere.proxy.frontend.postgresql.err;

import org.apache.shardingsphere.database.exception.postgresql.exception.PostgreSQLException;
import org.apache.shardingsphere.database.exception.postgresql.vendor.PostgreSQLVendorError;
import org.apache.shardingsphere.database.protocol.postgresql.constant.PostgreSQLMessageSeverityLevel;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.ServerErrorMessage;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgreSQLErrorPacketFactoryTest {
    
    @Test
    void assertPSQLExceptionWithServerErrorMessageNotNull() throws ReflectiveOperationException {
        ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
        when(serverErrorMessage.getSeverity()).thenReturn(PostgreSQLMessageSeverityLevel.FATAL);
        when(serverErrorMessage.getSQLState()).thenReturn("sqlState");
        when(serverErrorMessage.getMessage()).thenReturn("message");
        when(serverErrorMessage.getPosition()).thenReturn(1);
        PostgreSQLErrorResponsePacket actual = PostgreSQLErrorPacketFactory.newInstance(new PSQLException(serverErrorMessage));
        Map<Character, String> fields = getFields(actual);
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_SEVERITY), is(PostgreSQLMessageSeverityLevel.FATAL));
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_CODE), is("sqlState"));
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE), is("message"));
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_POSITION), is("1"));
    }
    
    @Test
    void assertPSQLExceptionWithServerErrorMessageIsNull() throws ReflectiveOperationException {
        PostgreSQLErrorResponsePacket actual = PostgreSQLErrorPacketFactory.newInstance(new PSQLException("psqlEx", PSQLState.UNEXPECTED_ERROR, new Exception("test")));
        Map<Character, String> fields = getFields(actual);
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_CODE), is(PSQLState.UNEXPECTED_ERROR.getState()));
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE), is("psqlEx"));
    }
    
    @Test
    void assertRuntimeException() throws ReflectiveOperationException {
        PostgreSQLErrorResponsePacket actual = PostgreSQLErrorPacketFactory.newInstance(new RuntimeException("No reason"));
        Map<Character, String> fields = getFields(actual);
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE), is("Unknown exception." + System.lineSeparator() + "More details: java.lang.RuntimeException: No reason"));
    }
    
    @Test
    void assertPostgreSQLExceptionWithServerErrorMessage() throws ReflectiveOperationException {
        PostgreSQLException.ServerErrorMessage serverErrorMessage = new PostgreSQLException.ServerErrorMessage(
                PostgreSQLMessageSeverityLevel.ERROR, PostgreSQLVendorError.INVALID_PARAMETER_VALUE, "param", "value");
        PostgreSQLErrorResponsePacket actual = PostgreSQLErrorPacketFactory.newInstance(new PostgreSQLException(serverErrorMessage));
        Map<Character, String> fields = getFields(actual);
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_SEVERITY), is(PostgreSQLMessageSeverityLevel.ERROR));
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_CODE), is(PostgreSQLVendorError.INVALID_PARAMETER_VALUE.getSqlState().getValue()));
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE), is("invalid value for parameter \"param\": \"value\""));
    }
    
    @Test
    void assertSQLExceptionWithoutMessageAndSQLState() throws ReflectiveOperationException {
        PostgreSQLException cause = new PostgreSQLException(null, null);
        PostgreSQLErrorResponsePacket actual = PostgreSQLErrorPacketFactory.newInstance(cause);
        Map<Character, String> fields = getFields(actual);
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_CODE), is(PostgreSQLVendorError.SYSTEM_ERROR.getSqlState().getValue()));
        assertThat(fields.get(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE), is(cause.toString()));
    }
    
    @SuppressWarnings("unchecked")
    private Map<Character, String> getFields(final PostgreSQLErrorResponsePacket packet) throws ReflectiveOperationException {
        return (Map<Character, String>) Plugins.getMemberAccessor().get(PostgreSQLErrorResponsePacket.class.getDeclaredField("fields"), packet);
    }
}
