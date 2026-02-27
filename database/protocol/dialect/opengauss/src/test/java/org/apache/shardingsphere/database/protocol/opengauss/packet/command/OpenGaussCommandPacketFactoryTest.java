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

package org.apache.shardingsphere.database.protocol.opengauss.packet.command;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.opengauss.packet.command.bind.OpenGaussComBatchBindPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.admin.PostgreSQLUnsupportedCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLAggregatedCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.close.PostgreSQLComClosePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.flush.PostgreSQLComFlushPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.sync.PostgreSQLComSyncPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.simple.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLComTerminationPacket;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenGaussCommandPacketFactoryTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("nonExtendedProtocolCases")
    void assertNewInstanceWithNonExtendedProtocol(final String name, final PostgreSQLCommandPacketType commandPacketType,
                                                  final Class<? extends PostgreSQLCommandPacket> expectedPacketClass) {
        PostgreSQLPacketPayload payload = mock(PostgreSQLPacketPayload.class, RETURNS_DEEP_STUBS);
        when(payload.readStringNul()).thenReturn("");
        ByteBuf byteBuf = payload.getByteBuf();
        assertThat(OpenGaussCommandPacketFactory.newInstance(commandPacketType, payload), isA(expectedPacketClass));
        verify(byteBuf).skipBytes(1);
    }
    
    @Test
    void assertNewInstanceWithExtendedProtocolWithoutPackets() {
        PostgreSQLPacketPayload payload = mock(PostgreSQLPacketPayload.class);
        PostgreSQLCommandPacket actual = OpenGaussCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.PARSE_COMMAND, payload);
        assertThat(actual, isA(PostgreSQLAggregatedCommandPacket.class));
        assertTrue(((PostgreSQLAggregatedCommandPacket) actual).getPackets().isEmpty());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("extendedProtocolCases")
    void assertNewInstanceWithExtendedProtocol(final String name, final int commandTypeValue, final Class<? extends PostgreSQLCommandPacket> expectedPacketClass) {
        PostgreSQLPacketPayload payload = createExtendedPayload(commandTypeValue);
        try (MockedConstruction<?> ignored = mockCommandPacketConstruction(expectedPacketClass)) {
            PostgreSQLCommandPacket actual = OpenGaussCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.PARSE_COMMAND, payload);
            assertThat(actual, isA(PostgreSQLAggregatedCommandPacket.class));
            List<PostgreSQLCommandPacket> actualPackets = ((PostgreSQLAggregatedCommandPacket) actual).getPackets();
            assertThat(actualPackets.size(), is(1));
            assertThat(actualPackets.get(0), isA(expectedPacketClass));
        }
    }
    
    private static MockedConstruction<?> mockCommandPacketConstruction(final Class<? extends PostgreSQLCommandPacket> expectedPacketClass) {
        if (OpenGaussComBatchBindPacket.class == expectedPacketClass) {
            return mockConstruction(OpenGaussComBatchBindPacket.class);
        }
        if (PostgreSQLComParsePacket.class == expectedPacketClass) {
            return mockConstruction(PostgreSQLComParsePacket.class);
        }
        if (PostgreSQLComBindPacket.class == expectedPacketClass) {
            return mockConstruction(PostgreSQLComBindPacket.class);
        }
        if (PostgreSQLComDescribePacket.class == expectedPacketClass) {
            return mockConstruction(PostgreSQLComDescribePacket.class);
        }
        if (PostgreSQLComExecutePacket.class == expectedPacketClass) {
            return mockConstruction(PostgreSQLComExecutePacket.class);
        }
        if (PostgreSQLComSyncPacket.class == expectedPacketClass) {
            return mockConstruction(PostgreSQLComSyncPacket.class);
        }
        if (PostgreSQLComClosePacket.class == expectedPacketClass) {
            return mockConstruction(PostgreSQLComClosePacket.class);
        }
        if (PostgreSQLComFlushPacket.class == expectedPacketClass) {
            return mockConstruction(PostgreSQLComFlushPacket.class);
        }
        throw new IllegalArgumentException(String.format("Unsupported expected packet class: %s", expectedPacketClass.getName()));
    }
    
    private PostgreSQLPacketPayload createExtendedPayload(final int commandTypeValue) {
        PostgreSQLPacketPayload result = mock(PostgreSQLPacketPayload.class);
        ByteBuf byteBuf = mock(ByteBuf.class);
        when(result.hasCompletePacket()).thenReturn(true, false);
        when(result.readInt1()).thenReturn(commandTypeValue);
        when(result.getByteBuf()).thenReturn(byteBuf);
        when(result.getCharset()).thenReturn(StandardCharsets.UTF_8);
        when(byteBuf.readerIndex()).thenReturn(0);
        when(byteBuf.getInt(0)).thenReturn(4);
        when(byteBuf.readSlice(4)).thenReturn(Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0}));
        return result;
    }
    
    private static Stream<Arguments> nonExtendedProtocolCases() {
        return Stream.of(
                Arguments.of("simpleQueryType", PostgreSQLCommandPacketType.SIMPLE_QUERY, PostgreSQLComQueryPacket.class),
                Arguments.of("terminationType", PostgreSQLCommandPacketType.TERMINATE, PostgreSQLComTerminationPacket.class),
                Arguments.of("unsupportedPasswordType", PostgreSQLCommandPacketType.PASSWORD, PostgreSQLUnsupportedCommandPacket.class));
    }
    
    private static Stream<Arguments> extendedProtocolCases() {
        return Stream.of(
                Arguments.of("batchBindType", (int) OpenGaussCommandPacketType.BATCH_BIND_COMMAND.getValue(), OpenGaussComBatchBindPacket.class),
                Arguments.of("parseType", (int) PostgreSQLCommandPacketType.PARSE_COMMAND.getValue(), PostgreSQLComParsePacket.class),
                Arguments.of("bindType", (int) PostgreSQLCommandPacketType.BIND_COMMAND.getValue(), PostgreSQLComBindPacket.class),
                Arguments.of("describeType", (int) PostgreSQLCommandPacketType.DESCRIBE_COMMAND.getValue(), PostgreSQLComDescribePacket.class),
                Arguments.of("executeType", (int) PostgreSQLCommandPacketType.EXECUTE_COMMAND.getValue(), PostgreSQLComExecutePacket.class),
                Arguments.of("syncType", (int) PostgreSQLCommandPacketType.SYNC_COMMAND.getValue(), PostgreSQLComSyncPacket.class),
                Arguments.of("closeType", (int) PostgreSQLCommandPacketType.CLOSE_COMMAND.getValue(), PostgreSQLComClosePacket.class),
                Arguments.of("flushType", (int) PostgreSQLCommandPacketType.FLUSH_COMMAND.getValue(), PostgreSQLComFlushPacket.class));
    }
}
