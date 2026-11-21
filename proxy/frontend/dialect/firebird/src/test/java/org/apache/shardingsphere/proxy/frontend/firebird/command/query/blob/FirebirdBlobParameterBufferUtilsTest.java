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

import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BlobFilterNotFoundException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.UnsupportedBlobFilterException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdBlobParameterBufferUtilsTest {
    
    private static final Charset WIN1252 = Charset.forName("windows-1252");
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("unchangedDataArguments")
    void assertCreateTransliteratorWithUnchangedData(final String name, final byte[] bpb, final int connectionCharsetId) {
        assertFalse(FirebirdBlobParameterBufferUtils.createTransliterator(bpb, connectionCharsetId).isPresent());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transliterationArguments")
    void assertCreateTransliterator(final String name, final byte[] bpb, final int connectionCharsetId, final byte[] data, final byte[] expectedData) {
        Optional<FirebirdBlobTransliterator> actual = FirebirdBlobParameterBufferUtils.createTransliterator(bpb, connectionCharsetId);
        assertTrue(actual.isPresent());
        assertThat(actual.get().put(data), is(expectedData));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("unsupportedFilterArguments")
    void assertCreateTransliteratorWithUnsupportedFilter(final String name, final byte[] bpb, final int connectionCharsetId) {
        assertThrows(UnsupportedBlobFilterException.class, () -> FirebirdBlobParameterBufferUtils.createTransliterator(bpb, connectionCharsetId));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("missingFilterArguments")
    void assertCreateTransliteratorWithMissingFilter(final String name, final byte[] bpb, final int expectedSourceType, final int expectedTargetType) {
        BlobFilterNotFoundException actual = assertThrows(BlobFilterNotFoundException.class, () -> FirebirdBlobParameterBufferUtils.createTransliterator(bpb, 4));
        assertThat(actual.getSourceType(), is(expectedSourceType));
        assertThat(actual.getTargetType(), is(expectedTargetType));
    }
    
    private static Stream<Arguments> unchangedDataArguments() {
        return Stream.of(
                Arguments.of("empty_bpb", new byte[0], 4),
                Arguments.of("unknown_version", new byte[]{2, 2, 1, 1, 4, 1, 4, 5, 1, 53}, 4),
                Arguments.of("blob_type_only", new byte[]{1, 3, 1, 1}, 4),
                Arguments.of("untyped_to_text", new byte[]{1, 2, 1, 1}, 4),
                Arguments.of("text_to_binary_charset", new byte[]{1, 1, 1, 2, 2, 1, 1, 5, 1, 1}, 4),
                Arguments.of("untyped_to_text_with_charsets", new byte[]{1, 2, 1, 1, 4, 1, 4, 5, 1, 53}, 4),
                Arguments.of("same_charset", new byte[]{1, 1, 1, 1, 2, 1, 1, 4, 1, 4, 5, 1, 4}, 4),
                Arguments.of("charset_to_none", new byte[]{1, 1, 1, 1, 2, 1, 1, 4, 1, 4, 5, 1, 0}, 4),
                Arguments.of("source_connection_charset", new byte[]{1, 1, 1, 1, 2, 1, 1, 4, 1, 127, 5, 1, 4}, 4),
                Arguments.of("none_connection_charset", new byte[]{1, 1, 1, 1, 2, 1, 1, 4, 1, 127, 5, 1, 53}, 0));
    }
    
    private static Stream<Arguments> transliterationArguments() {
        return Stream.of(
                Arguments.of("charset_conversion", new byte[]{1, 1, 1, 1, 2, 1, 1, 4, 1, 4, 5, 1, 53}, 4, "é".getBytes(StandardCharsets.UTF_8), "é".getBytes(WIN1252)),
                Arguments.of("target_connection_charset_conversion", new byte[]{1, 1, 1, 1, 2, 1, 1, 4, 1, 4, 5, 1, 127}, 53, "é".getBytes(StandardCharsets.UTF_8), "é".getBytes(WIN1252)),
                Arguments.of("two_bytes_values", new byte[]{1, 1, 2, 1, 0, 2, 2, 1, 0, 4, 2, 53, 0, 5, 2, 4, 0}, 4, "é".getBytes(WIN1252), "é".getBytes(StandardCharsets.UTF_8)));
    }
    
    private static Stream<Arguments> unsupportedFilterArguments() {
        return Stream.of(
                Arguments.of("sub_type_conversion", new byte[]{1, 1, 1, 2, 2, 1, 1}, 4),
                Arguments.of("unknown_connection_charset", new byte[]{1, 1, 1, 1, 2, 1, 1, 4, 1, 127, 5, 1, 4}, 127),
                Arguments.of("charset_without_java_charset", new byte[]{1, 1, 1, 1, 2, 1, 1, 4, 1, 19, 5, 1, 4}, 4));
    }
    
    private static Stream<Arguments> missingFilterArguments() {
        return Stream.of(
                Arguments.of("untyped_to_blr", new byte[]{1, 2, 1, 2}, 0, 2),
                Arguments.of("ranges_to_text", new byte[]{1, 1, 1, 4, 2, 1, 1}, 4, 1),
                Arguments.of("sub_type_without_internal_filter_to_text", new byte[]{1, 1, 1, 10, 2, 1, 1}, 10, 1),
                Arguments.of("negative_sub_type_to_text", new byte[]{1, 1, 1, -1, 2, 1, 1}, -1, 1),
                Arguments.of("two_bytes_negative_sub_type_to_text", new byte[]{1, 1, 2, -2, -1, 2, 1, 1}, -2, 1));
    }
}
