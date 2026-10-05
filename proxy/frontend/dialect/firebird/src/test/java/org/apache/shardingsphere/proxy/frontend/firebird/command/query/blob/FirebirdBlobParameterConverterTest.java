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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BlobFilterNotFoundException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BlobTruncationException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ConversionErrorException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.MalformedStringException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.NumericOutOfRangeException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ParameterConversionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.StringTruncationException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.TransliterationFailedException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.UnsupportedBlobFilterException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchColumnDescriptor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdParseBatchBlr;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdReturnColumnPacket;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.firebirdsql.gds.BlrConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdBlobParameterConverterTest {
    
    private static final int NONE = 0;
    
    private static final int UTF8 = 4;
    
    private static final int WIN1251 = 52;
    
    private static final Charset WIN1251_CHARSET = Charset.forName("windows-1251");
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("readOnMoveArguments")
    void assertIsReadOnMove(final String name, final FirebirdBatchColumnDescriptor field, final FirebirdReturnColumnPacket parameter, final int connectionCharsetId, final boolean expected) {
        assertThat(new FirebirdBlobParameterConverter(connectionCharsetId).isReadOnMove(field, parameter), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("textBlobArguments")
    void assertConvertToTextBlob(final String name, final FirebirdBatchColumnDescriptor field, final byte[] content, final int connectionCharsetId, final String expected) {
        assertThat(new FirebirdBlobParameterConverter(connectionCharsetId).convert(content, field, createBlobParameter(1)), is(expected));
    }
    
    @Test
    void assertConvertToBinaryBlob() {
        byte[] content = "Привет".getBytes(WIN1251_CHARSET);
        assertThat(new FirebirdBlobParameterConverter(UTF8).convert(content, createField(1, WIN1251), createBlobParameter(0)), sameInstance(content));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("textArguments")
    void assertConvertToText(final String name, final FirebirdBatchColumnDescriptor field, final byte[] content, final Integer length, final String expected) {
        assertThat(new FirebirdBlobParameterConverter(UTF8).convert(content, field, createParameter(Types.VARCHAR, length)), is(expected));
    }
    
    @Test
    void assertConvertToTextWithoutConnectionCharset() {
        byte[] content = {(byte) 0xFF, 'a'};
        assertThat((byte[]) new FirebirdBlobParameterConverter(NONE).convert(content, createField(1, NONE), createParameter(Types.VARCHAR, 20)), is(content));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("integerArguments")
    void assertConvertToInteger(final String name, final int dataType, final String value, final Number expected) {
        assertThat(new FirebirdBlobParameterConverter(UTF8).convert(value.getBytes(StandardCharsets.US_ASCII), createField(1, UTF8), createParameter(dataType, null)), is(expected));
    }
    
    @Test
    void assertConvertToOtherType() {
        assertThat(new FirebirdBlobParameterConverter(UTF8).convert("1.5e1".getBytes(StandardCharsets.US_ASCII), createField(1, UTF8), createParameter(Types.DOUBLE, null)), is("1.5e1"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("failedConversionArguments")
    void assertConvertFailed(final String name, final FirebirdBatchColumnDescriptor field, final byte[] content, final FirebirdReturnColumnPacket parameter, final int connectionCharsetId,
                             final Class<? extends SQLDialectException> expectedErrorType) {
        ParameterConversionException actual = assertThrows(ParameterConversionException.class, () -> new FirebirdBlobParameterConverter(connectionCharsetId).convert(content, field, parameter));
        assertThat(actual.getConversionError(), isA(expectedErrorType));
    }
    
    @Test
    void assertConvertWithStringTruncation() {
        byte[] content = "this-value-is-longer-than-twenty".getBytes(StandardCharsets.UTF_8);
        ParameterConversionException actual = assertThrows(ParameterConversionException.class,
                () -> new FirebirdBlobParameterConverter(UTF8).convert(content, createField(1, UTF8), createParameter(Types.VARCHAR, 20)));
        StringTruncationException actualError = (StringTruncationException) actual.getConversionError();
        assertThat(actualError.getExpectedLength(), is(20));
        assertThat(actualError.getActualLength(), is(32));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("conversionErrorArguments")
    void assertConvertWithConversionError(final String name, final byte[] content, final String expectedValue) {
        ParameterConversionException actual = assertThrows(ParameterConversionException.class,
                () -> new FirebirdBlobParameterConverter(UTF8).convert(content, createField(1, NONE), createParameter(Types.INTEGER, null)));
        assertThat(((ConversionErrorException) actual.getConversionError()).getValue(), is(expectedValue));
    }
    
    private static Stream<Arguments> readOnMoveArguments() {
        return Stream.of(
                Arguments.of("other_type", createField(0, NONE), createParameter(Types.VARCHAR, 20), UTF8, true),
                Arguments.of("text_blob_in_other_charset", createField(1, WIN1251), createBlobParameter(1), UTF8, true),
                Arguments.of("text_blob_in_connection_charset", createField(1, UTF8), createBlobParameter(1), UTF8, false),
                Arguments.of("text_blob_in_dynamic_charset", createField(1, 127), createBlobParameter(1), UTF8, false),
                Arguments.of("text_blob_without_connection_charset", createField(1, WIN1251), createBlobParameter(1), NONE, false),
                Arguments.of("untyped_blob", createField(0, NONE), createBlobParameter(1), UTF8, false),
                Arguments.of("binary_blob_parameter", createField(1, WIN1251), createBlobParameter(0), UTF8, false));
    }
    
    private static Stream<Arguments> textBlobArguments() {
        return Stream.of(
                Arguments.of("transliterated", createField(1, WIN1251), "Привет".getBytes(WIN1251_CHARSET), UTF8, "Привет"),
                Arguments.of("connection_charset", createField(1, UTF8), "Привет".getBytes(StandardCharsets.UTF_8), UTF8, "Привет"),
                Arguments.of("without_connection_charset", createField(1, WIN1251), "Привет".getBytes(WIN1251_CHARSET), NONE, "Привет"));
    }
    
    private static Stream<Arguments> textArguments() {
        return Stream.of(
                Arguments.of("connection_charset", createField(1, UTF8), "short".getBytes(StandardCharsets.UTF_8), 20, "short"),
                Arguments.of("transliterated", createField(1, WIN1251), "Привет".getBytes(WIN1251_CHARSET), 20, "Привет"),
                Arguments.of("trailing_spaces", createField(1, UTF8), "abc   ".getBytes(StandardCharsets.UTF_8), 4, "abc "),
                Arguments.of("unknown_length", createField(1, UTF8), "this-value-is-longer-than-twenty".getBytes(StandardCharsets.UTF_8), null, "this-value-is-longer-than-twenty"),
                Arguments.of("empty", createField(1, UTF8), new byte[0], 20, ""),
                Arguments.of("untyped_blob_text_filter", createField(0, NONE), new byte[]{'a', '\n', 'b', (byte) 0xC3, (byte) 0xA9, 0x01, '\t', 0x7F, 'z'}, 20, "ab...\t.z"),
                Arguments.of("quad", createQuadField(), "quad".getBytes(StandardCharsets.US_ASCII), 20, "quad"));
    }
    
    private static Stream<Arguments> integerArguments() {
        return Stream.of(
                Arguments.of("integer", Types.INTEGER, "231", 231),
                Arguments.of("rounded_up", Types.INTEGER, "2.5", 3),
                Arguments.of("rounded_down_negative", Types.INTEGER, "-2.5", -3),
                Arguments.of("exponent", Types.INTEGER, "1.5e1", 15),
                Arguments.of("positive_exponent", Types.INTEGER, "1e3", 1000),
                Arguments.of("spaces", Types.INTEGER, " 12 ", 12),
                Arguments.of("signed", Types.INTEGER, "+7", 7),
                Arguments.of("fraction_zeros", Types.INTEGER, "1.00000000000", 1),
                Arguments.of("minimum", Types.INTEGER, "-2147483648", Integer.MIN_VALUE),
                Arguments.of("hex", Types.INTEGER, "0xFFFFFFFF", -1),
                Arguments.of("small_integer_hex", Types.SMALLINT, "0xFFFF", (short) -1),
                Arguments.of("small_integer", Types.SMALLINT, "32767", Short.MAX_VALUE),
                Arguments.of("big_integer_hex", Types.BIGINT, "0xFFFFFFFF", 4294967295L),
                Arguments.of("big_integer_minimum", Types.BIGINT, "-9223372036854775808", Long.MIN_VALUE));
    }
    
    private static Stream<Arguments> failedConversionArguments() {
        return Stream.of(
                Arguments.of("malformed_text", createField(1, UTF8), new byte[]{(byte) 0xFF, (byte) 0xFE}, createParameter(Types.VARCHAR, 20), UTF8, MalformedStringException.class),
                Arguments.of("transliteration", createField(1, UTF8), "中".getBytes(StandardCharsets.UTF_8), createParameter(Types.VARCHAR, 20), WIN1251, TransliterationFailedException.class),
                Arguments.of("integer_transliteration", createField(1, UTF8), "é".getBytes(StandardCharsets.UTF_8), createParameter(Types.INTEGER, null), UTF8, TransliterationFailedException.class),
                Arguments.of("blob_truncation", createField(1, UTF8), new byte[65536], createParameter(Types.VARCHAR, null), UTF8, BlobTruncationException.class),
                Arguments.of("internal_filter", createField(2, NONE), new byte[]{1}, createParameter(Types.VARCHAR, 20), UTF8, UnsupportedBlobFilterException.class),
                Arguments.of("missing_filter", createField(4, NONE), new byte[]{1}, createParameter(Types.VARCHAR, 20), UTF8, BlobFilterNotFoundException.class),
                Arguments.of("integer_overflow", createField(1, UTF8), "2147483648".getBytes(StandardCharsets.US_ASCII), createParameter(Types.INTEGER, null), UTF8, NumericOutOfRangeException.class),
                Arguments.of("fraction_overflow", createField(1, UTF8), "1.23456789012".getBytes(StandardCharsets.US_ASCII), createParameter(Types.INTEGER, null), UTF8,
                        NumericOutOfRangeException.class),
                Arguments.of("exponent_overflow", createField(1, UTF8), "1e3276".getBytes(StandardCharsets.US_ASCII), createParameter(Types.BIGINT, null), UTF8, NumericOutOfRangeException.class),
                Arguments.of("scale_overflow", createField(1, UTF8), "400e2".getBytes(StandardCharsets.US_ASCII), createParameter(Types.SMALLINT, null), UTF8, NumericOutOfRangeException.class));
    }
    
    private static Stream<Arguments> conversionErrorArguments() {
        return Stream.of(
                Arguments.of("letters", "from-blobs".getBytes(StandardCharsets.US_ASCII), "from-blobs"),
                Arguments.of("empty", new byte[0], ""),
                Arguments.of("inner_space", "12 3".getBytes(StandardCharsets.US_ASCII), "12 3"),
                Arguments.of("hex_with_space", "0x7FFFFFFF ".getBytes(StandardCharsets.US_ASCII), "0x7FFFFFFF "),
                Arguments.of("hex_digit", "0x1G".getBytes(StandardCharsets.US_ASCII), "0x1G"),
                Arguments.of("exponent_without_digits", "1e".getBytes(StandardCharsets.US_ASCII), "1e"),
                Arguments.of("second_point", "1.2.3".getBytes(StandardCharsets.US_ASCII), "1.2.3"),
                Arguments.of("unprintable", new byte[]{'1', 0x01, (byte) 0xD0}, "1#x01#xd0"));
    }
    
    private static FirebirdBatchColumnDescriptor createField(final int subType, final int charsetId) {
        return parseField(Unpooled.buffer().writeByte(BlrConstants.blr_blob2).writeShortLE(subType).writeShortLE(charsetId));
    }
    
    private static FirebirdBatchColumnDescriptor createQuadField() {
        return parseField(Unpooled.buffer().writeByte(BlrConstants.blr_quad).writeByte(0));
    }
    
    private static FirebirdBatchColumnDescriptor parseField(final ByteBuf fieldBlr) {
        ByteBuf blr = Unpooled.buffer().writeByte(BlrConstants.blr_version5).writeByte(BlrConstants.blr_begin).writeByte(BlrConstants.blr_message).writeByte(0).writeShortLE(2)
                .writeBytes(fieldBlr).writeByte(BlrConstants.blr_short).writeByte(0).writeByte(BlrConstants.blr_end).writeByte(BlrConstants.blr_eoc);
        return FirebirdParseBatchBlr.parse(blr, blr.readableBytes()).getFields().get(0);
    }
    
    private static FirebirdReturnColumnPacket createParameter(final int dataType, final Integer length) {
        ShardingSphereColumn column = new ShardingSphereColumn("C", dataType, false, false, true, true, false, true);
        return new FirebirdReturnColumnPacket(Collections.emptyList(), 1, createTable(), column, null, null, null, length, false, null);
    }
    
    private static FirebirdReturnColumnPacket createBlobParameter(final int subType) {
        ShardingSphereColumn column = new ShardingSphereColumn("C", Types.BLOB, false, false, true, true, false, true);
        return new FirebirdReturnColumnPacket(Collections.emptyList(), 1, createTable(), column, null, null, null, null, true, subType);
    }
    
    private static ShardingSphereTable createTable() {
        return new ShardingSphereTable("T", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }
}
