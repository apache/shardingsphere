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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgreSQLCommandPacketFactoryTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("nonExtendedProtocolCases")
    void assertNewInstanceWithNonExtendedProtocol(final String name, final PostgreSQLCommandPacketType commandPacketType,
                                                  final int packetTypeValue, final byte[] packetBody, final Class<? extends PostgreSQLCommandPacket> expectedPacketClass) {
        assertThat(PostgreSQLCommandPacketFactory.newInstance(commandPacketType, createPayload(packetTypeValue, packetBody)), isA(expectedPacketClass));
    }
    
    @Test
    void assertNewInstanceWithExtendedProtocolWithoutPackets() {
        assertTrue(((PostgreSQLAggregatedCommandPacket) PostgreSQLCommandPacketFactory.newInstance(
                PostgreSQLCommandPacketType.PARSE_COMMAND, new PostgreSQLPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8))).getPackets().isEmpty());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("extendedProtocolCases")
    void assertNewInstanceWithExtendedProtocol(final String name, final int packetTypeValue, final byte[] packetBody,
                                               final Class<? extends PostgreSQLCommandPacket> expectedPacketClass) {
        PostgreSQLCommandPacket actual = PostgreSQLCommandPacketFactory.newInstance(PostgreSQLCommandPacketType.PARSE_COMMAND, createPayload(packetTypeValue, packetBody));
        List<PostgreSQLCommandPacket> actualPackets = ((PostgreSQLAggregatedCommandPacket) actual).getPackets();
        assertThat(actualPackets.size(), is(1));
        assertThat(actualPackets.get(0), isA(expectedPacketClass));
    }
    
    private static PostgreSQLPacketPayload createPayload(final int packetTypeValue, final byte[] packetBody) {
        ByteBuf byteBuf = Unpooled.buffer(1 + Integer.BYTES + packetBody.length);
        byteBuf.writeByte(packetTypeValue);
        byteBuf.writeInt(Integer.BYTES + packetBody.length);
        byteBuf.writeBytes(packetBody);
        return new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
    }
    
    private static Stream<Arguments> nonExtendedProtocolCases() {
        return Stream.of(
                Arguments.of("simple_query", PostgreSQLCommandPacketType.SIMPLE_QUERY, (int) PostgreSQLCommandPacketType.SIMPLE_QUERY.getValue(),
                        createQueryBody(), PostgreSQLComQueryPacket.class),
                Arguments.of("termination", PostgreSQLCommandPacketType.TERMINATE, (int) PostgreSQLCommandPacketType.TERMINATE.getValue(),
                        new byte[0], PostgreSQLComTerminationPacket.class),
                Arguments.of("unsupported_password", PostgreSQLCommandPacketType.PASSWORD, (int) PostgreSQLCommandPacketType.PASSWORD.getValue(),
                        new byte[0], PostgreSQLUnsupportedCommandPacket.class));
    }
    
    private static byte[] createQueryBody() {
        byte[] queryBytes = "SELECT 1".getBytes(StandardCharsets.UTF_8);
        ByteBuf byteBuf = Unpooled.buffer(queryBytes.length + 1);
        byteBuf.writeBytes(queryBytes);
        byteBuf.writeByte(0);
        byte[] result = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(0, result);
        return result;
    }
    
    private static Stream<Arguments> extendedProtocolCases() {
        return Stream.of(
                Arguments.of("parse", (int) PostgreSQLCommandPacketType.PARSE_COMMAND.getValue(), new byte[]{0, 0}, PostgreSQLComParsePacket.class),
                Arguments.of("bind", (int) PostgreSQLCommandPacketType.BIND_COMMAND.getValue(), new byte[]{0, 0}, PostgreSQLComBindPacket.class),
                Arguments.of("describe", (int) PostgreSQLCommandPacketType.DESCRIBE_COMMAND.getValue(), new byte[]{'S', 0}, PostgreSQLComDescribePacket.class),
                Arguments.of("execute", (int) PostgreSQLCommandPacketType.EXECUTE_COMMAND.getValue(), new byte[]{0, 0, 0, 0, 0}, PostgreSQLComExecutePacket.class),
                Arguments.of("sync", (int) PostgreSQLCommandPacketType.SYNC_COMMAND.getValue(), new byte[0], PostgreSQLComSyncPacket.class),
                Arguments.of("close", (int) PostgreSQLCommandPacketType.CLOSE_COMMAND.getValue(), new byte[]{'S', 0}, PostgreSQLComClosePacket.class),
                Arguments.of("flush", (int) PostgreSQLCommandPacketType.FLUSH_COMMAND.getValue(), new byte[0], PostgreSQLComFlushPacket.class));
    }
}
