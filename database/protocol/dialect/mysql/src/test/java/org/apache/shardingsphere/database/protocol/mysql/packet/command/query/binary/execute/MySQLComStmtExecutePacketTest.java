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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.execute;

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLNewParametersBoundFlag;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.MySQLPreparedStatementParameterType;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinitionFlag;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLComStmtExecutePacketTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("withoutParameterArguments")
    void assertNewWithoutParameter(final String name, final byte[] data, final int expectedStatementId, final int expectedFlags) {
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 0);
        assertThat(actual.getStatementId(), is(expectedStatementId));
        assertThat(actual.getFlags(), is(expectedFlags));
        assertNull(actual.getNewParametersBoundFlag());
        assertTrue(actual.getNewParameterTypes().isEmpty());
    }
    
    @Test
    void assertNewWithInvalidIterationCount() {
        byte[] data = {0x01, 0x00, 0x00, 0x00, 0x09, 0x02, 0x00, 0x00, 0x00};
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class, () -> new MySQLComStmtExecutePacket(payload, 0));
    }
    
    @Test
    void assertNewWithParameterTypeNotExist() {
        byte[] data = {0x01, 0x00, 0x00, 0x00, 0x09, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00};
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 1);
        assertThat(actual.getNewParametersBoundFlag(), is(MySQLNewParametersBoundFlag.PARAMETER_TYPE_NOT_EXIST));
        assertTrue(actual.getNewParameterTypes().isEmpty());
    }
    
    @Test
    void assertReadParametersWithSignedInteger() throws SQLException {
        byte[] data = {0x01, 0x00, 0x00, 0x00, 0x09, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01, 0x03, 0x00, 0x01, 0x00, 0x00, 0x00};
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 1);
        List<MySQLPreparedStatementParameterType> parameterTypes = actual.getNewParameterTypes();
        assertThat(actual.readParameters(parameterTypes, Collections.emptySet(), Collections.singletonList(0), Collections.emptyList()), is(Collections.<Object>singletonList(1)));
    }
    
    @Test
    void assertReadParametersWithUnsignedInteger() throws SQLException {
        byte[] data = {0x01, 0x00, 0x00, 0x00, 0x09, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01, 0x03, 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 1);
        List<MySQLPreparedStatementParameterType> parameterTypes = actual.getNewParameterTypes();
        int unsignedFlag = MySQLColumnDefinitionFlag.UNSIGNED.getValue();
        assertThat(actual.readParameters(parameterTypes, Collections.emptySet(), Collections.singletonList(unsignedFlag), Collections.emptyList()), is(Collections.<Object>singletonList(4294967295L)));
    }
    
    @Test
    void assertReadParametersWithNullParameter() throws SQLException {
        byte[] data = {0x01, 0x00, 0x00, 0x00, 0x09, 0x01, 0x00, 0x00, 0x00, 0x01, 0x01, 0x03, 0x00};
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 1);
        List<MySQLPreparedStatementParameterType> parameterTypes = actual.getNewParameterTypes();
        List<Integer> parameterFlags = Collections.singletonList(0);
        assertThat(actual.readParameters(parameterTypes, Collections.emptySet(), parameterFlags, Collections.emptyList()), is(Collections.singletonList(null)));
    }
    
    @Test
    void assertReadParametersWithLongDataParameter() throws SQLException {
        byte[] data = {0x02, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01, (byte) 0xfc, 0x00};
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 1);
        List<MySQLPreparedStatementParameterType> parameterTypes = actual.getNewParameterTypes();
        assertThat(actual.readParameters(parameterTypes, Collections.singleton(0), Collections.emptyList(), Collections.emptyList()), is(Collections.singletonList(null)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("stringParameterTypesBoundToBlobColumnsArguments")
    void assertReadParametersWithStringTypeBoundToBlobColumn(final String name, final MySQLBinaryColumnType parameterType,
                                                             final MySQLBinaryColumnType parameterColumnType) throws SQLException {
        byte[] data = createPacketData(parameterType, new byte[]{(byte) 0xac, (byte) 0xed, (byte) 0xff});
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 1);
        List<MySQLPreparedStatementParameterType> parameterTypes = actual.getNewParameterTypes();
        Object actualValue = actual.readParameters(parameterTypes, Collections.emptySet(), Collections.singletonList(0), Collections.singletonList(parameterColumnType)).get(0);
        assertTrue(actualValue instanceof byte[]);
        assertArrayEquals(new byte[]{(byte) 0xac, (byte) 0xed, (byte) 0xff}, (byte[]) actualValue);
    }
    
    private static Stream<Arguments> stringParameterTypesBoundToBlobColumnsArguments() {
        return Stream.of(
                Arguments.of("string-parameter-bound-to-tiny-blob-column", MySQLBinaryColumnType.STRING, MySQLBinaryColumnType.TINY_BLOB),
                Arguments.of("string-parameter-bound-to-blob-column", MySQLBinaryColumnType.STRING, MySQLBinaryColumnType.BLOB),
                Arguments.of("string-parameter-bound-to-medium-blob-column", MySQLBinaryColumnType.STRING, MySQLBinaryColumnType.MEDIUM_BLOB),
                Arguments.of("string-parameter-bound-to-long-blob-column", MySQLBinaryColumnType.STRING, MySQLBinaryColumnType.LONG_BLOB),
                Arguments.of("var-string-parameter-bound-to-tiny-blob-column", MySQLBinaryColumnType.VAR_STRING, MySQLBinaryColumnType.TINY_BLOB),
                Arguments.of("var-string-parameter-bound-to-blob-column", MySQLBinaryColumnType.VAR_STRING, MySQLBinaryColumnType.BLOB),
                Arguments.of("var-string-parameter-bound-to-medium-blob-column", MySQLBinaryColumnType.VAR_STRING, MySQLBinaryColumnType.MEDIUM_BLOB),
                Arguments.of("var-string-parameter-bound-to-long-blob-column", MySQLBinaryColumnType.VAR_STRING, MySQLBinaryColumnType.LONG_BLOB),
                Arguments.of("varchar-parameter-bound-to-tiny-blob-column", MySQLBinaryColumnType.VARCHAR, MySQLBinaryColumnType.TINY_BLOB),
                Arguments.of("varchar-parameter-bound-to-blob-column", MySQLBinaryColumnType.VARCHAR, MySQLBinaryColumnType.BLOB),
                Arguments.of("varchar-parameter-bound-to-medium-blob-column", MySQLBinaryColumnType.VARCHAR, MySQLBinaryColumnType.MEDIUM_BLOB),
                Arguments.of("varchar-parameter-bound-to-long-blob-column", MySQLBinaryColumnType.VARCHAR, MySQLBinaryColumnType.LONG_BLOB));
    }
    
    @DisplayName("assertReadParametersWithStringDecoding")
    @ParameterizedTest(name = "{0}")
    @MethodSource("stringDecodingArguments")
    void assertReadParametersWithStringDecoding(final String name, final MySQLBinaryColumnType parameterType,
                                                final List<MySQLBinaryColumnType> parameterColumnTypes, final boolean expectedString) throws SQLException {
        byte[] data = createPacketData(parameterType, new byte[]{0x61});
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 1);
        List<MySQLPreparedStatementParameterType> parameterTypes = actual.getNewParameterTypes();
        List<Integer> parameterFlags = Collections.singletonList(0);
        Object actualValue = actual.readParameters(parameterTypes, Collections.emptySet(), parameterFlags, parameterColumnTypes).get(0);
        assertThat(actualValue instanceof String, is(expectedString));
        if (expectedString) {
            assertThat(actualValue, is("a"));
            return;
        }
        assertArrayEquals(new byte[]{0x61}, (byte[]) actualValue);
    }
    
    private static Stream<Arguments> stringDecodingArguments() {
        return Stream.of(
                Arguments.of("string-column", MySQLBinaryColumnType.STRING, Collections.singletonList(MySQLBinaryColumnType.STRING), true),
                Arguments.of("var-string-column", MySQLBinaryColumnType.STRING, Collections.singletonList(MySQLBinaryColumnType.VAR_STRING), true),
                Arguments.of("varchar-column", MySQLBinaryColumnType.STRING, Collections.singletonList(MySQLBinaryColumnType.VARCHAR), true),
                Arguments.of("missing-column-type", MySQLBinaryColumnType.STRING, Collections.emptyList(), false),
                Arguments.of("blob-parameter-type", MySQLBinaryColumnType.BLOB, Collections.singletonList(MySQLBinaryColumnType.VAR_STRING), false));
    }
    
    private static Stream<Arguments> withoutParameterArguments() {
        return Stream.of(
                Arguments.of("statement-id-1", new byte[]{0x01, 0x00, 0x00, 0x00, 0x09, 0x01, 0x00, 0x00, 0x00}, 1, 9),
                Arguments.of("statement-id-2", new byte[]{0x02, 0x00, 0x00, 0x00, 0x05, 0x01, 0x00, 0x00, 0x00}, 2, 5),
                Arguments.of("statement-id-256", new byte[]{0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00}, 256, 0));
    }
    
    private byte[] createPacketData(final MySQLBinaryColumnType parameterType, final byte[] value) {
        List<Byte> result = new ArrayList<>();
        byte[] fixedPrefix = {0x01, 0x00, 0x00, 0x00, 0x09, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01};
        for (byte each : fixedPrefix) {
            result.add(each);
        }
        result.add((byte) parameterType.getValue());
        result.add((byte) 0x00);
        result.add((byte) value.length);
        for (byte each : value) {
            result.add(each);
        }
        byte[] bytes = new byte[result.size()];
        for (int i = 0; i < result.size(); i++) {
            bytes[i] = result.get(i);
        }
        return bytes;
    }
}
