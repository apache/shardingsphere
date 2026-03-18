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

package org.apache.shardingsphere.database.protocol.opengauss.packet.command.generic;

import org.apache.shardingsphere.database.protocol.payload.PacketPayload;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opengauss.util.ServerErrorMessage;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenGaussErrorResponsePacketTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("constructWithServerErrorMessageArguments")
    void assertConstructWithServerErrorMessage(final String name, final ServerErrorMessage serverErrorMessage, final List<Integer> expectedFieldTypes, final List<String> expectedFieldValues) {
        new OpenGaussErrorResponsePacket(serverErrorMessage).write((PacketPayload) payload);
        assertWrittenFields(expectedFieldTypes, expectedFieldValues);
    }
    
    @Test
    void assertConstructWithSeverityAndMessage() {
        OpenGaussErrorResponsePacket actual = new OpenGaussErrorResponsePacket("FATAL", "3D000", "database \"test\" does not exist");
        actual.write((PacketPayload) payload);
        assertWrittenFields(Arrays.asList(
                (int) OpenGaussErrorResponsePacket.FIELD_TYPE_SEVERITY,
                (int) OpenGaussErrorResponsePacket.FIELD_TYPE_CODE,
                (int) OpenGaussErrorResponsePacket.FIELD_TYPE_MESSAGE,
                (int) OpenGaussErrorResponsePacket.FIELD_TYPE_ERROR_CODE,
                0), Arrays.asList("FATAL", "3D000", "database \"test\" does not exist", "0"));
        assertThat(actual.getIdentifier(), is(PostgreSQLMessagePacketType.ERROR_RESPONSE));
    }
    
    private void assertWrittenFields(final List<Integer> expectedFieldTypes, final List<String> expectedFieldValues) {
        ArgumentCaptor<Integer> actualFieldTypes = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> actualFieldValues = ArgumentCaptor.forClass(String.class);
        verify(payload, times(expectedFieldTypes.size())).writeInt1(actualFieldTypes.capture());
        verify(payload, times(expectedFieldValues.size())).writeStringNul(actualFieldValues.capture());
        assertThat(actualFieldTypes.getAllValues(), is(expectedFieldTypes));
        assertThat(actualFieldValues.getAllValues(), is(expectedFieldValues));
        verifyNoMoreInteractions(payload);
    }
    
    private static Stream<Arguments> constructWithServerErrorMessageArguments() {
        return Stream.of(
                Arguments.of("all_fields_present", createFullServerErrorMessage(), Arrays.asList(
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_SEVERITY,
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_CODE,
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_MESSAGE,
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_ERROR_CODE,
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_DETAIL,
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_HINT,
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_POSITION,
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_INTERNAL_POSITION,
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_INTERNAL_QUERY,
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_WHERE,
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_FILE,
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_LINE,
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_ROUTINE,
                        0), Arrays.asList("FATAL", "3D000", "database \"test\" does not exist", "-1", "detail", "hint", "1", "2", "internal query", "where", "file", "3", "routine")),
                Arguments.of("missing_optional_fields", mock(ServerErrorMessage.class), Arrays.asList(
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_ERROR_CODE,
                        0), Collections.singletonList("0")),
                Arguments.of("required_fields_and_error_code_only", createRequiredAndErrorCodeServerErrorMessage(), Arrays.asList(
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_SEVERITY,
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_CODE,
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_MESSAGE,
                        (int) OpenGaussErrorResponsePacket.FIELD_TYPE_ERROR_CODE,
                        0), Arrays.asList("FATAL", "3D000", "database \"test\" does not exist", "42")));
    }
    
    private static ServerErrorMessage createFullServerErrorMessage() {
        ServerErrorMessage result = mock(ServerErrorMessage.class);
        when(result.getSeverity()).thenReturn("FATAL");
        when(result.getSQLState()).thenReturn("3D000");
        when(result.getMessage()).thenReturn("database \"test\" does not exist");
        when(result.getERRORCODE()).thenReturn("-1");
        when(result.getDetail()).thenReturn("detail");
        when(result.getHint()).thenReturn("hint");
        when(result.getPosition()).thenReturn(1);
        when(result.getInternalPosition()).thenReturn(2);
        when(result.getInternalQuery()).thenReturn("internal query");
        when(result.getWhere()).thenReturn("where");
        when(result.getFile()).thenReturn("file");
        when(result.getLine()).thenReturn(3);
        when(result.getRoutine()).thenReturn("routine");
        return result;
    }
    
    private static ServerErrorMessage createRequiredAndErrorCodeServerErrorMessage() {
        ServerErrorMessage result = mock(ServerErrorMessage.class);
        when(result.getSeverity()).thenReturn("FATAL");
        when(result.getSQLState()).thenReturn("3D000");
        when(result.getMessage()).thenReturn("database \"test\" does not exist");
        when(result.getERRORCODE()).thenReturn("42");
        return result;
    }
}
