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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind;

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.postgresql.constant.PostgreSQLValueFormat;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PostgreSQLComBindPacketTest {
    
    private static final byte[] NEW_INSTANCE_PACKET_BYTES = {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x53, 0x5F, 0x31, 0x00
    };
    
    private static final byte[] READ_PARAMETERS_NULL_PACKET_BYTES = {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x53, 0x5F, 0x31, 0x00,
            0x00, 0x00, 0x00, 0x01, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };
    
    private static final byte[] READ_PARAMETERS_EMPTY_TEXT_PACKET_BYTES = {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x53, 0x5F, 0x31, 0x00,
            0x00, 0x00,
            0x00, 0x01,
            0x00, 0x00, 0x00, 0x02, 0x31, 0x31
    };
    
    private static final byte[] READ_PARAMETERS_BINARY_PACKET_BYTES = {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x53, 0x5F, 0x31, 0x00,
            0x00, 0x01, 0x00, 0x01,
            0x00, 0x01,
            0x00, 0x00, 0x00, 0x04,
            0x00, 0x00, 0x00, 0x0A
    };
    
    private static final byte[] READ_PARAMETERS_MIXED_PACKET_BYTES = {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x53, 0x5F, 0x31, 0x00,
            0x00, 0x02, 0x00, 0x00, 0x00, 0x01,
            0x00, 0x02,
            0x00, 0x00, 0x00, 0x02, 0x31, 0x32,
            0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x03
    };
    
    private static final byte[] READ_RESULT_FORMATS_EMPTY_PACKET_BYTES = {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x53, 0x5F, 0x31, 0x00, 0x00, 0x00
    };
    
    private static final byte[] READ_RESULT_FORMATS_SINGLE_PACKET_BYTES = {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x53, 0x5F, 0x31, 0x00,
            0x00, 0x01, 0x00, 0x01
    };
    
    private static final byte[] READ_RESULT_FORMATS_MULTI_PACKET_BYTES = {
            0x00, 0x00, 0x00, 0x00, 0x00, 0x53, 0x5F, 0x31, 0x00,
            0x00, 0x02, 0x00, 0x00, 0x00, 0x01
    };
    
    @Test
    void assertNewInstance() {
        PostgreSQLComBindPacket actual = new PostgreSQLComBindPacket(new PostgreSQLPacketPayload(Unpooled.wrappedBuffer(NEW_INSTANCE_PACKET_BYTES), StandardCharsets.UTF_8));
        assertThat(actual.getPortal(), is(""));
        assertThat(actual.getStatementId(), is("S_1"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertReadParametersArguments")
    void assertReadParameters(final String name, final byte[] packetBytes, final List<PostgreSQLBinaryColumnType> paramTypes, final List<Object> expected) {
        PostgreSQLComBindPacket packet = new PostgreSQLComBindPacket(new PostgreSQLPacketPayload(Unpooled.wrappedBuffer(packetBytes), StandardCharsets.UTF_8));
        assertThat(packet.readParameters(paramTypes), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertReadResultFormatsArguments")
    void assertReadResultFormats(final String name, final byte[] packetBytes, final List<PostgreSQLValueFormat> expected) {
        PostgreSQLComBindPacket packet = new PostgreSQLComBindPacket(new PostgreSQLPacketPayload(Unpooled.wrappedBuffer(packetBytes), StandardCharsets.UTF_8));
        assertThat(packet.readResultFormats(), is(expected));
    }
    
    @Test
    void assertWrite() {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(Unpooled.wrappedBuffer(NEW_INSTANCE_PACKET_BYTES), StandardCharsets.UTF_8);
        PostgreSQLComBindPacket packet = new PostgreSQLComBindPacket(payload);
        assertDoesNotThrow(() -> packet.write(payload));
    }
    
    @Test
    void assertGetIdentifier() {
        PostgreSQLComBindPacket actual = new PostgreSQLComBindPacket(new PostgreSQLPacketPayload(Unpooled.wrappedBuffer(NEW_INSTANCE_PACKET_BYTES), StandardCharsets.UTF_8));
        assertThat(actual.getIdentifier(), is(PostgreSQLCommandPacketType.BIND_COMMAND));
    }
    
    private static Stream<Arguments> assertReadParametersArguments() {
        return Stream.of(
                Arguments.of("null parameter with empty format list", READ_PARAMETERS_NULL_PACKET_BYTES,
                        Collections.singletonList(PostgreSQLBinaryColumnType.INT4), Collections.singletonList(null)),
                Arguments.of("text parameter with empty format list", READ_PARAMETERS_EMPTY_TEXT_PACKET_BYTES,
                        Collections.singletonList(PostgreSQLBinaryColumnType.INT4), Collections.singletonList(11)),
                Arguments.of("single binary format parameter", READ_PARAMETERS_BINARY_PACKET_BYTES,
                        Collections.singletonList(PostgreSQLBinaryColumnType.INT4), Collections.singletonList(10)),
                Arguments.of("multi format text and binary parameters", READ_PARAMETERS_MIXED_PACKET_BYTES,
                        Arrays.asList(PostgreSQLBinaryColumnType.INT4, PostgreSQLBinaryColumnType.INT4), Arrays.asList(12, 3)));
    }
    
    private static Stream<Arguments> assertReadResultFormatsArguments() {
        return Stream.of(
                Arguments.of("empty result formats", READ_RESULT_FORMATS_EMPTY_PACKET_BYTES, Collections.emptyList()),
                Arguments.of("single result format", READ_RESULT_FORMATS_SINGLE_PACKET_BYTES, Collections.singletonList(PostgreSQLValueFormat.BINARY)),
                Arguments.of("multiple result formats", READ_RESULT_FORMATS_MULTI_PACKET_BYTES, Arrays.asList(PostgreSQLValueFormat.TEXT, PostgreSQLValueFormat.BINARY)));
    }
}
