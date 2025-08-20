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

import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.opengauss.util.ServerErrorMessage;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OpenGaussErrorResponsePacketTest {
    
    @Test
    void assertWritePacketFromServerErrorMessage() {
        String encodedMessage = "SFATAL\0C3D000\0Mdatabase \"test\" does not exist\0c-1\0Ddetail\0Hhint\0P1\0p2\0qinternal query\0Wwhere\0Ffile\0L3\0Rroutine\0a0.0.0.0:1";
        OpenGaussErrorResponsePacket packet = new OpenGaussErrorResponsePacket(new ServerErrorMessage(encodedMessage));
        PostgreSQLPacketPayload payload = mock(PostgreSQLPacketPayload.class);
        packet.write(payload);
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_SEVERITY);
        verify(payload).writeStringNul("FATAL");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_CODE);
        verify(payload).writeStringNul("3D000");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_MESSAGE);
        verify(payload).writeStringNul("database \"test\" does not exist");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_ERROR_CODE);
        verify(payload).writeStringNul("-1");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_DETAIL);
        verify(payload).writeStringNul("detail");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_HINT);
        verify(payload).writeStringNul("hint");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_POSITION);
        verify(payload).writeStringNul("1");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_INTERNAL_POSITION);
        verify(payload).writeStringNul("2");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_INTERNAL_QUERY);
        verify(payload).writeStringNul("internal query");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_WHERE);
        verify(payload).writeStringNul("where");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_FILE);
        verify(payload).writeStringNul("file");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_LINE);
        verify(payload).writeStringNul("3");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_ROUTINE);
        verify(payload).writeStringNul("routine");
        verify(payload).writeInt1(0);
        assertThat(packet.getIdentifier(), is(PostgreSQLMessagePacketType.ERROR_RESPONSE));
    }
    
    @Test
    void assertWritePacketFromSeverityAndMessage() {
        OpenGaussErrorResponsePacket packet = new OpenGaussErrorResponsePacket("FATAL", "3D000", "database \"test\" does not exist");
        PostgreSQLPacketPayload payload = mock(PostgreSQLPacketPayload.class);
        packet.write(payload);
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_SEVERITY);
        verify(payload).writeStringNul("FATAL");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_CODE);
        verify(payload).writeStringNul("3D000");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_MESSAGE);
        verify(payload).writeStringNul("database \"test\" does not exist");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_ERROR_CODE);
        verify(payload).writeStringNul("0");
    }
}
