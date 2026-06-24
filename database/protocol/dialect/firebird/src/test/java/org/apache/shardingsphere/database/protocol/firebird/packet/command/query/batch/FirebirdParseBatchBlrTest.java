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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.firebirdsql.gds.BlrConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdParseBatchBlrTest {
    
    @Test
    void assertParseMixedFields() {
        ByteBuf blr = createBlr(BlrConstants.blr_version5,
                new byte[]{
                        (byte) BlrConstants.blr_text, 3, 0, (byte) BlrConstants.blr_short, 0,
                        (byte) BlrConstants.blr_varying2, 4, 0, 5, 0, (byte) BlrConstants.blr_short, 0,
                        (byte) BlrConstants.blr_int64, -2, (byte) BlrConstants.blr_short, 0,
                        (byte) BlrConstants.blr_bool, (byte) BlrConstants.blr_short, 0},
                8);
        FirebirdParseBatchBlr actual = FirebirdParseBatchBlr.parse(blr, blr.readableBytes());
        assertThat(actual.getFields().size(), is(4));
        assertDescriptor(actual.getFields().get(0), FirebirdBinaryColumnType.LEGACY_TEXT, 3, 0, 0);
        assertDescriptor(actual.getFields().get(1), FirebirdBinaryColumnType.VARYING, 7, 4, 6);
        assertDescriptor(actual.getFields().get(2), FirebirdBinaryColumnType.INT64, 8, -2, 16);
        assertDescriptor(actual.getFields().get(3), FirebirdBinaryColumnType.BOOLEAN, 1, 0, 26);
        assertThat(actual.getMessageLength(), is(30));
        assertThat(actual.getNetLength(), is(28));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validSingleFieldArguments")
    void assertParseSingleField(final String name, final int version, final byte[] fieldBlr, final FirebirdBinaryColumnType expectedType,
                                final int expectedLength, final int expectedScale, final int expectedMessageLength, final int expectedNetLength) {
        ByteBuf blr = createBlr(version, fieldBlr, 2);
        FirebirdParseBatchBlr actual = FirebirdParseBatchBlr.parse(blr, blr.readableBytes());
        assertThat(actual.getFields().size(), is(1));
        assertDescriptor(actual.getFields().get(0), expectedType, expectedLength, expectedScale, 0);
        assertThat(actual.getMessageLength(), is(expectedMessageLength));
        assertThat(actual.getNetLength(), is(expectedNetLength));
    }
    
    @Test
    void assertParseEmptyMessage() {
        ByteBuf blr = createBlr(BlrConstants.blr_version4, new byte[0], 0);
        FirebirdParseBatchBlr actual = FirebirdParseBatchBlr.parse(blr, blr.readableBytes());
        assertThat(actual.getFields().size(), is(0));
        assertThat(actual.getMessageLength(), is(0));
        assertThat(actual.getNetLength(), is(0));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidHeaderArguments")
    void assertParseInvalidHeader(final String name, final byte[] blrBytes, final int blrLength, final String expectedMessage) {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> FirebirdParseBatchBlr.parse(Unpooled.wrappedBuffer(blrBytes), blrLength));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("truncatedBlrArguments")
    void assertParseTruncatedBlr(final String name, final byte[] blrBytes) {
        assertThrows(IndexOutOfBoundsException.class, () -> FirebirdParseBatchBlr.parse(Unpooled.wrappedBuffer(blrBytes), blrBytes.length));
    }
    
    @Test
    void assertParseInvalidNullIndicator() {
        ByteBuf blr = createBlr(BlrConstants.blr_version5, new byte[]{(byte) BlrConstants.blr_long, 0, (byte) BlrConstants.blr_bool, 0}, 2);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> FirebirdParseBatchBlr.parse(blr, blr.readableBytes()));
        assertThat(actual.getMessage(), is("Expected blr_short NULL indicator, got: " + BlrConstants.blr_bool));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTerminatorArguments")
    void assertParseInvalidTerminator(final String name, final byte[] blrBytes, final String expectedMessage) {
        ByteBuf blr = Unpooled.wrappedBuffer(blrBytes);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> FirebirdParseBatchBlr.parse(blr, blr.readableBytes()));
        assertThat(actual.getMessage(), is(expectedMessage));
    }

    @Test
    void assertParseUnsupportedType() {
        ByteBuf blr = createBlr(BlrConstants.blr_version5, new byte[]{(byte) BlrConstants.blr_dec64, (byte) BlrConstants.blr_short, 0}, 2);
        assertThrows(IllegalArgumentException.class, () -> FirebirdParseBatchBlr.parse(blr, blr.readableBytes()));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("blobFieldArguments")
    void assertParseRejectsBlob(final String name, final byte[] fieldBlr, final int scale) {
        ByteBuf blr = createBlr(BlrConstants.blr_version5, fieldBlr, 2);
        FirebirdProtocolException actual = assertThrows(FirebirdProtocolException.class, () -> FirebirdParseBatchBlr.parse(blr, blr.readableBytes()));
        assertThat(actual.getMessage(), is("BLOB fields are not supported in Firebird batch operations"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("blobFieldArguments")
    void assertParseForFramingAcceptsBlobStructurally(final String name, final byte[] fieldBlr, final int expectedScale) {
        ByteBuf blr = createBlr(BlrConstants.blr_version5, fieldBlr, 2);
        FirebirdParseBatchBlr actual = FirebirdParseBatchBlr.parseForFraming(blr, blr.readableBytes());
        assertThat(actual.getFields().size(), is(1));
        assertDescriptor(actual.getFields().get(0), FirebirdBinaryColumnType.BLOB, 8, expectedScale, 0);
    }
    
    private static Stream<Arguments> validSingleFieldArguments() {
        return Stream.of(
                Arguments.of("legacy_text", BlrConstants.blr_version5, field(BlrConstants.blr_text, 3, 0), FirebirdBinaryColumnType.LEGACY_TEXT, 3, 0, 6, 4),
                Arguments.of("legacy_varying", BlrConstants.blr_version5, field(BlrConstants.blr_varying, 5, 0), FirebirdBinaryColumnType.LEGACY_VARYING, 7, 0, 10, 12),
                Arguments.of("text", BlrConstants.blr_version5, field(BlrConstants.blr_text2, 4, 0, 3, 0), FirebirdBinaryColumnType.TEXT, 3, 4, 6, 4),
                Arguments.of("varying", BlrConstants.blr_version5, field(BlrConstants.blr_varying2, 4, 0, 5, 0), FirebirdBinaryColumnType.VARYING, 7, 4, 10, 12),
                Arguments.of("short", BlrConstants.blr_version5, field(BlrConstants.blr_short, -1), FirebirdBinaryColumnType.SHORT, 2, -1, 4, 4),
                Arguments.of("long", BlrConstants.blr_version5, field(BlrConstants.blr_long, -2), FirebirdBinaryColumnType.LONG, 4, -2, 6, 4),
                Arguments.of("int64", BlrConstants.blr_version5, field(BlrConstants.blr_int64, -3), FirebirdBinaryColumnType.INT64, 8, -3, 10, 8),
                Arguments.of("int128", BlrConstants.blr_version5, field(BlrConstants.blr_int128, -4), FirebirdBinaryColumnType.INT128, 16, -4, 18, 16),
                Arguments.of("float", BlrConstants.blr_version5, field(BlrConstants.blr_float), FirebirdBinaryColumnType.FLOAT, 4, 0, 6, 4),
                Arguments.of("date", BlrConstants.blr_version5, field(BlrConstants.blr_sql_date), FirebirdBinaryColumnType.DATE, 4, 0, 6, 4),
                Arguments.of("time", BlrConstants.blr_version5, field(BlrConstants.blr_sql_time), FirebirdBinaryColumnType.TIME, 4, 0, 6, 4),
                Arguments.of("double", BlrConstants.blr_version5, field(BlrConstants.blr_double), FirebirdBinaryColumnType.DOUBLE, 8, 0, 10, 8),
                Arguments.of("d_float", BlrConstants.blr_version5, field(BlrConstants.blr_d_float), FirebirdBinaryColumnType.D_FLOAT, 8, 0, 10, 8),
                Arguments.of("timestamp", BlrConstants.blr_version5, field(BlrConstants.blr_timestamp), FirebirdBinaryColumnType.TIMESTAMP, 8, 0, 10, 8),
                Arguments.of("boolean", BlrConstants.blr_version5, field(BlrConstants.blr_bool), FirebirdBinaryColumnType.BOOLEAN, 1, 0, 4, 4));
    }
    
    private static Stream<Arguments> blobFieldArguments() {
        return Stream.of(
                Arguments.of("blob2", new byte[]{(byte) BlrConstants.blr_blob2, 0, 0, 0, 0, (byte) BlrConstants.blr_short, 0}, 0),
                Arguments.of("quad", new byte[]{(byte) BlrConstants.blr_quad, -3, (byte) BlrConstants.blr_short, 0}, -3));
    }
    
    private static Stream<Arguments> invalidTerminatorArguments() {
        return Stream.of(
                Arguments.of("missing_terminators", longFieldBlr(), "Expected blr_end"),
                Arguments.of("wrong_blr_end", longFieldBlr(BlrConstants.blr_message, BlrConstants.blr_eoc), "Expected blr_end"),
                Arguments.of("truncated_terminator", longFieldBlr(BlrConstants.blr_end), "Expected blr_eoc"),
                Arguments.of("wrong_blr_eoc", longFieldBlr(BlrConstants.blr_end, BlrConstants.blr_end), "Expected blr_eoc"),
                Arguments.of("trailing_bytes", longFieldBlr(BlrConstants.blr_end, BlrConstants.blr_eoc, 0), "Unexpected trailing bytes in BLR"));
    }

    private static byte[] longFieldBlr(final int... terminator) {
        byte[] base = {(byte) BlrConstants.blr_version5, (byte) BlrConstants.blr_begin, (byte) BlrConstants.blr_message, 0,
                2, 0, (byte) BlrConstants.blr_long, 0, (byte) BlrConstants.blr_short, 0};
        return append(base, terminator);
    }

    private static Stream<Arguments> invalidHeaderArguments() {
        return Stream.of(
                Arguments.of("too_short", new byte[]{(byte) BlrConstants.blr_version5, (byte) BlrConstants.blr_begin, (byte) BlrConstants.blr_message}, 3, "BLR is too short: 3"),
                Arguments.of("unsupported_version", header(99, BlrConstants.blr_begin, BlrConstants.blr_message), 6, "Unsupported BLR version: 99"),
                Arguments.of("missing_begin", header(BlrConstants.blr_version5, BlrConstants.blr_end, BlrConstants.blr_message), 6, "Expected blr_begin"),
                Arguments.of("missing_message", header(BlrConstants.blr_version5, BlrConstants.blr_begin, BlrConstants.blr_end), 6, "Expected blr_message"));
    }
    
    private static Stream<Arguments> truncatedBlrArguments() {
        return Stream.of(
                Arguments.of("missing_count", new byte[]{(byte) BlrConstants.blr_version5, (byte) BlrConstants.blr_begin, (byte) BlrConstants.blr_message, 0}),
                Arguments.of("truncated_text_length", append(messageHeader(BlrConstants.blr_version5, BlrConstants.blr_begin, BlrConstants.blr_message), 2, 0, BlrConstants.blr_text, 3)),
                Arguments.of("truncated_null_indicator", append(messageHeader(BlrConstants.blr_version5, BlrConstants.blr_begin, BlrConstants.blr_message),
                        2, 0, BlrConstants.blr_long, 0, BlrConstants.blr_short)));
    }
    
    private static byte[] field(final int... values) {
        byte[] result = new byte[values.length + 2];
        for (int i = 0; i < values.length; i++) {
            result[i] = (byte) values[i];
        }
        result[values.length] = (byte) BlrConstants.blr_short;
        return result;
    }
    
    private static byte[] header(final int version, final int begin, final int message) {
        return append(messageHeader(version, begin, message), 0, 0);
    }
    
    private static byte[] messageHeader(final int version, final int begin, final int message) {
        return new byte[]{(byte) version, (byte) begin, (byte) message, 0};
    }
    
    private static byte[] append(final byte[] prefix, final int... values) {
        byte[] result = new byte[prefix.length + values.length];
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        for (int i = 0; i < values.length; i++) {
            result[prefix.length + i] = (byte) values[i];
        }
        return result;
    }
    
    private static ByteBuf createBlr(final int version, final byte[] fields, final int fieldCount) {
        return Unpooled.buffer().writeByte(version).writeByte(BlrConstants.blr_begin).writeByte(BlrConstants.blr_message).writeByte(0)
                .writeShortLE(fieldCount).writeBytes(fields).writeByte(BlrConstants.blr_end).writeByte(BlrConstants.blr_eoc);
    }
    
    private static void assertDescriptor(final FirebirdBatchColumnDescriptor actual, final FirebirdBinaryColumnType expectedType,
                                         final int expectedLength, final int expectedScale, final int expectedOffset) {
        assertThat(actual.getType(), is(expectedType));
        assertThat(actual.getLength(), is(expectedLength));
        assertThat(actual.getScale(), is(expectedScale));
        assertThat(actual.getOffset(), is(expectedOffset));
    }
}
