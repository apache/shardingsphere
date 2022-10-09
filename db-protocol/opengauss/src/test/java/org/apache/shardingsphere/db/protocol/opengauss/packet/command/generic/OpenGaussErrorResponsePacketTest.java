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

package org.apache.shardingsphere.db.protocol.opengauss.packet.command.generic;

import org.apache.shardingsphere.dialect.postgresql.vendor.PostgreSQLVendorError;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLMessageSeverityLevel;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class OpenGaussErrorResponsePacketTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Test
    public void assertToServerErrorMessage() {
        OpenGaussErrorResponsePacket responsePacket = createErrorResponsePacket();
        String expectedMessage = "SFATAL\0C3D000\0Mdatabase \"test\" does not exist\0c-1\0Ddetail\0Hhint\0P1\0p2\0qinternal query\0"
                + "Wwhere\0Ffile\0L3\0Rroutine\0a0.0.0.0:1";
        assertThat(responsePacket.toServerErrorMessage(), is(expectedMessage));
    }
    
    @Test
    public void assertWrite() {
        OpenGaussErrorResponsePacket responsePacket = createErrorResponsePacket();
        responsePacket.write(payload);
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_SEVERITY);
        verify(payload).writeStringNul("FATAL");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_CODE);
        verify(payload).writeStringNul("3D000");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_MESSAGE);
        verify(payload).writeStringNul("database \"test\" does not exist");
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_ERRORCODE);
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
        verify(payload).writeInt1(OpenGaussErrorResponsePacket.FIELD_TYPE_SOCKET_ADDRESS);
        verify(payload).writeStringNul("routine");
        verify(payload).writeInt1(0);
    }
    
    private OpenGaussErrorResponsePacket createErrorResponsePacket() {
        Map<Character, String> serverErrorMessages = new LinkedHashMap<>();
        serverErrorMessages.put(OpenGaussErrorResponsePacket.FIELD_TYPE_SEVERITY, PostgreSQLMessageSeverityLevel.FATAL);
        serverErrorMessages.put(OpenGaussErrorResponsePacket.FIELD_TYPE_CODE, PostgreSQLVendorError.INVALID_CATALOG_NAME.getSqlState().getValue());
        serverErrorMessages.put(OpenGaussErrorResponsePacket.FIELD_TYPE_MESSAGE, "database \"test\" does not exist");
        serverErrorMessages.put(OpenGaussErrorResponsePacket.FIELD_TYPE_ERRORCODE, "-1");
        serverErrorMessages.put(OpenGaussErrorResponsePacket.FIELD_TYPE_DETAIL, "detail");
        serverErrorMessages.put(OpenGaussErrorResponsePacket.FIELD_TYPE_HINT, "hint");
        serverErrorMessages.put(OpenGaussErrorResponsePacket.FIELD_TYPE_POSITION, "1");
        serverErrorMessages.put(OpenGaussErrorResponsePacket.FIELD_TYPE_INTERNAL_POSITION, "2");
        serverErrorMessages.put(OpenGaussErrorResponsePacket.FIELD_TYPE_INTERNAL_QUERY, "internal query");
        serverErrorMessages.put(OpenGaussErrorResponsePacket.FIELD_TYPE_WHERE, "where");
        serverErrorMessages.put(OpenGaussErrorResponsePacket.FIELD_TYPE_FILE, "file");
        serverErrorMessages.put(OpenGaussErrorResponsePacket.FIELD_TYPE_LINE, "3");
        serverErrorMessages.put(OpenGaussErrorResponsePacket.FIELD_TYPE_ROUTINE, "routine");
        serverErrorMessages.put(OpenGaussErrorResponsePacket.FIELD_TYPE_SOCKET_ADDRESS, "0.0.0.0:1");
        return new OpenGaussErrorResponsePacket(serverErrorMessages);
    }
}
