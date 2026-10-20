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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLNewParametersBoundFlag;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.MySQLPreparedStatementParameterType;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        byte[] data = {0x01, 0x00, 0x00, 0x00, 0x09, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01, 0x03, 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 1);
        List<MySQLPreparedStatementParameterType> parameterTypes = actual.getNewParameterTypes();
        assertThat(actual.readParameters(parameterTypes, Collections.emptySet(), Collections.emptyList()), is(Collections.<Object>singletonList(-1)));
    }
    
    @Test
    void assertReadParametersWithUnsignedInteger() throws SQLException {
        byte[] data = {0x01, 0x00, 0x00, 0x00, 0x09, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01, 0x03, (byte) 0x80, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 1);
        List<MySQLPreparedStatementParameterType> parameterTypes = actual.getNewParameterTypes();
        assertThat(actual.readParameters(parameterTypes, Collections.emptySet(), Collections.emptyList()), is(Collections.<Object>singletonList(4294967295L)));
    }
    
    @Test
    void assertReadParametersWithNullParameter() throws SQLException {
        byte[] data = {0x01, 0x00, 0x00, 0x00, 0x09, 0x01, 0x00, 0x00, 0x00, 0x01, 0x01, 0x03, 0x00};
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 1);
        List<MySQLPreparedStatementParameterType> parameterTypes = actual.getNewParameterTypes();
        assertThat(actual.readParameters(parameterTypes, Collections.emptySet(), Collections.emptyList()), is(Collections.singletonList(null)));
    }
    
    @Test
    void assertReadParametersWithLongDataParameter() throws SQLException {
        byte[] data = {0x02, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01, (byte) 0xfc, 0x00};
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 1);
        List<MySQLPreparedStatementParameterType> parameterTypes = actual.getNewParameterTypes();
        assertThat(actual.readParameters(parameterTypes, Collections.singleton(0), Collections.emptyList()), is(Collections.singletonList(null)));
    }
    
    @Test
    void assertReadParametersWithDateParameter() throws SQLException {
        byte[] data = {0x01, 0x00, 0x00, 0x00, 0x09, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01, 0x0a, 0x00, 0x04, (byte) 0xe1, 0x07, 0x08, 0x08};
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 1);
        Object actualValue = actual.readParameters(actual.getNewParameterTypes(), Collections.emptySet(), Collections.singletonList(MySQLBinaryColumnType.DATE)).get(0);
        assertThat(actualValue, isA(Date.class));
        assertThat(actualValue, is(Date.valueOf("2017-08-08")));
    }
    
    @Test
    void assertReadParametersWithDatetimeParameterBoundToDateColumn() throws SQLException {
        byte[] data = {0x01, 0x00, 0x00, 0x00, 0x09, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01, 0x0c, 0x00, 0x04, (byte) 0xe1, 0x07, 0x08, 0x08};
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 1);
        Object actualValue = actual.readParameters(actual.getNewParameterTypes(), Collections.emptySet(), Collections.singletonList(MySQLBinaryColumnType.DATE)).get(0);
        assertThat(actualValue, isA(Date.class));
        assertThat(actualValue, is(Date.valueOf("2017-08-08")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("stringParameterTypesBoundToBlobColumnsArguments")
    void assertReadParametersWithStringTypeBoundToBlobColumn(final String name, final MySQLBinaryColumnType parameterType,
                                                             final MySQLBinaryColumnType parameterColumnType) throws SQLException {
        byte[] data = createPacketData(parameterType, new byte[]{(byte) 0xac, (byte) 0xed, (byte) 0xff});
        MySQLPacketPayload payload = new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8);
        MySQLComStmtExecutePacket actual = new MySQLComStmtExecutePacket(payload, 1);
        List<MySQLPreparedStatementParameterType> parameterTypes = actual.getNewParameterTypes();
        Object actualValue = actual.readParameters(parameterTypes, Collections.emptySet(), Collections.singletonList(parameterColumnType)).get(0);
        assertTrue(actualValue instanceof byte[]);
        assertThat((byte[]) actualValue, is(new byte[]{(byte) 0xac, (byte) 0xed, (byte) 0xff}));
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
    void assertReadParametersWithStringDecoding(final String name, final MySQLBinaryColumnType columnType, final String expected, final Charset charset) throws SQLException {
        ByteBuf buffer = Unpooled.wrappedBuffer(createPacketData(MySQLBinaryColumnType.STRING, expected.getBytes(charset)));
        try {
            MySQLComStmtExecutePacket packet = new MySQLComStmtExecutePacket(new MySQLPacketPayload(buffer, charset), 1);
            Object actual = packet.readParameters(packet.getNewParameterTypes(), Collections.emptySet(), Collections.singletonList(columnType)).get(0);
            assertThat(actual, isA(String.class));
            assertThat(actual, is(expected));
        } finally {
            buffer.release();
        }
    }
    
    private static Stream<Arguments> stringDecodingArguments() {
        return Stream.of(
                Arguments.of("string-column", MySQLBinaryColumnType.STRING, "0", StandardCharsets.UTF_8),
                Arguments.of("var-string-column", MySQLBinaryColumnType.VAR_STRING, "0", StandardCharsets.UTF_8),
                Arguments.of("varchar-column", MySQLBinaryColumnType.VARCHAR, "0", StandardCharsets.UTF_8),
                Arguments.of("tiny-column", MySQLBinaryColumnType.TINY, "48", StandardCharsets.UTF_8),
                Arguments.of("short-column", MySQLBinaryColumnType.SHORT, "48", StandardCharsets.UTF_8),
                Arguments.of("long-column", MySQLBinaryColumnType.LONG, "48", StandardCharsets.UTF_8),
                Arguments.of("longlong-column", MySQLBinaryColumnType.LONGLONG, "48", StandardCharsets.UTF_8),
                Arguments.of("int24-column", MySQLBinaryColumnType.INT24, "48", StandardCharsets.UTF_8),
                Arguments.of("float-column", MySQLBinaryColumnType.FLOAT, "48", StandardCharsets.UTF_8),
                Arguments.of("double-column", MySQLBinaryColumnType.DOUBLE, "48", StandardCharsets.UTF_8),
                Arguments.of("decimal-column", MySQLBinaryColumnType.DECIMAL, "48", StandardCharsets.UTF_8),
                Arguments.of("newdecimal-column", MySQLBinaryColumnType.NEWDECIMAL, "48", StandardCharsets.UTF_8),
                Arguments.of("negative-number", MySQLBinaryColumnType.LONGLONG, "-48", StandardCharsets.UTF_8),
                Arguments.of("fractional-number", MySQLBinaryColumnType.NEWDECIMAL, "48.123456", StandardCharsets.UTF_8),
                Arguments.of("unsigned-bigint-boundary", MySQLBinaryColumnType.LONGLONG, "18446744073709551615", StandardCharsets.UTF_8),
                Arguments.of("invalid-number", MySQLBinaryColumnType.LONGLONG, "foo_invalid", StandardCharsets.UTF_8),
                Arguments.of("empty-string", MySQLBinaryColumnType.LONG, "", StandardCharsets.UTF_8),
                Arguments.of("numeric-column-charset", MySQLBinaryColumnType.LONGLONG, "é", StandardCharsets.ISO_8859_1),
                Arguments.of("character-column-charset", MySQLBinaryColumnType.VAR_STRING, "foo_中文", StandardCharsets.UTF_8));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("withoutStringDecodingArguments")
    void assertReadParametersWithoutStringDecoding(final String name, final MySQLBinaryColumnType paramType, final List<MySQLBinaryColumnType> columnTypes) throws SQLException {
        byte[] expected = {0x34, 0x38};
        ByteBuf buffer = Unpooled.wrappedBuffer(createPacketData(paramType, expected));
        try {
            MySQLComStmtExecutePacket packet = new MySQLComStmtExecutePacket(new MySQLPacketPayload(buffer, StandardCharsets.UTF_8), 1);
            Object actual = packet.readParameters(packet.getNewParameterTypes(), Collections.emptySet(), columnTypes).get(0);
            assertThat(actual, isA(byte[].class));
            assertThat((byte[]) actual, is(expected));
        } finally {
            buffer.release();
        }
    }
    
    private static Stream<Arguments> withoutStringDecodingArguments() {
        return Stream.of(
                Arguments.of("missing-column-type", MySQLBinaryColumnType.STRING, Collections.emptyList()),
                Arguments.of("unknown-column-type", MySQLBinaryColumnType.STRING, Collections.singletonList(MySQLBinaryColumnType.NULL)),
                Arguments.of("date-column", MySQLBinaryColumnType.STRING, Collections.singletonList(MySQLBinaryColumnType.DATE)),
                Arguments.of("blob-parameter-character-column", MySQLBinaryColumnType.BLOB, Collections.singletonList(MySQLBinaryColumnType.VAR_STRING)),
                Arguments.of("blob-parameter-numeric-column", MySQLBinaryColumnType.BLOB, Collections.singletonList(MySQLBinaryColumnType.LONGLONG)));
    }
    
    @Test
    void assertReadParametersWithNullStringBoundToNumericColumn() throws SQLException {
        byte[] data = {1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, (byte) 0xfe, 0};
        ByteBuf buffer = Unpooled.wrappedBuffer(data);
        try {
            MySQLComStmtExecutePacket packet = new MySQLComStmtExecutePacket(new MySQLPacketPayload(buffer, StandardCharsets.UTF_8), 1);
            assertNull(packet.readParameters(packet.getNewParameterTypes(), Collections.emptySet(), Collections.singletonList(MySQLBinaryColumnType.LONGLONG)).get(0));
        } finally {
            buffer.release();
        }
    }
    
    @Test
    void assertReadParametersWithReusedStringTypeBoundToNumericColumn() throws SQLException {
        byte[] data = {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 2, 0x34, 0x38};
        ByteBuf buffer = Unpooled.wrappedBuffer(data);
        try {
            MySQLComStmtExecutePacket packet = new MySQLComStmtExecutePacket(new MySQLPacketPayload(buffer, StandardCharsets.UTF_8), 1);
            List<MySQLPreparedStatementParameterType> paramTypes = Collections.singletonList(new MySQLPreparedStatementParameterType(MySQLBinaryColumnType.STRING, 0));
            Object actual = packet.readParameters(paramTypes, Collections.emptySet(), Collections.singletonList(MySQLBinaryColumnType.LONGLONG)).get(0);
            assertThat(actual, isA(String.class));
            assertThat(actual, is("48"));
        } finally {
            buffer.release();
        }
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
