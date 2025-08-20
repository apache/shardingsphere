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

package org.apache.shardingsphere.proxy.frontend.opengauss.err;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.protocol.opengauss.packet.command.generic.OpenGaussErrorResponsePacket;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;
import org.opengauss.util.PSQLException;
import org.opengauss.util.ServerErrorMessage;

import java.sql.SQLException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class OpenGaussErrorPacketFactoryTest {
    
    @Test
    void assertNewInstanceWithServerErrorMessage() {
        String encodedMessage = "SFATAL\0C3D000\0Mdatabase \"test\" does not exist\0c-1\0Ddetail\0Hhint\0P1\0p2\0qinternal query\0Wwhere\0Ffile\0L3\0Rroutine\0a0.0.0.0:1";
        PSQLException cause = new PSQLException(new ServerErrorMessage(encodedMessage));
        OpenGaussErrorResponsePacket actual = OpenGaussErrorPacketFactory.newInstance(cause);
        Map<Character, String> actualFields = getFieldsInPacket(actual);
        assertThat(actualFields.size(), is(13));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_SEVERITY), is("FATAL"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_CODE), is("3D000"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_MESSAGE), is("database \"test\" does not exist"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_ERROR_CODE), is("-1"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_DETAIL), is("detail"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_HINT), is("hint"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_POSITION), is("1"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_INTERNAL_POSITION), is("2"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_INTERNAL_QUERY), is("internal query"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_WHERE), is("where"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_FILE), is("file"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_LINE), is("3"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_ROUTINE), is("routine"));
    }
    
    @Test
    void assertNewInstanceWithSQLException() {
        SQLException cause = new SQLException("database \"test\" does not exist", "3D000", null);
        OpenGaussErrorResponsePacket actual = OpenGaussErrorPacketFactory.newInstance(cause);
        Map<Character, String> actualFields = getFieldsInPacket(actual);
        assertThat(actualFields.size(), is(4));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_SEVERITY), is("ERROR"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_CODE), is("3D000"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_MESSAGE), is("database \"test\" does not exist"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_ERROR_CODE), is("0"));
    }
    
    @Test
    void assertNewInstanceWithUnknownException() {
        Exception cause = new RuntimeException("No reason");
        OpenGaussErrorResponsePacket actual = OpenGaussErrorPacketFactory.newInstance(cause);
        Map<Character, String> actualFields = getFieldsInPacket(actual);
        assertThat(actualFields.size(), is(4));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_SEVERITY), is("ERROR"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_CODE), is("58000"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_MESSAGE), is("Unknown exception." + System.lineSeparator() + "More details: java.lang.RuntimeException: No reason"));
        assertThat(actualFields.get(OpenGaussErrorResponsePacket.FIELD_TYPE_ERROR_CODE), is("0"));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<Character, String> getFieldsInPacket(final OpenGaussErrorResponsePacket packet) {
        return (Map<Character, String>) Plugins.getMemberAccessor().get(OpenGaussErrorResponsePacket.class.getDeclaredField("fields"), packet);
    }
}
