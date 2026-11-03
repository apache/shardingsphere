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

package org.apache.shardingsphere.proxy.backend.firebird.handler.admin.executor.variable.charset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdSjisCharsetTest {
    
    private static final Charset CHARSET = FirebirdSjisCharset.INSTANCE;
    
    @Test
    void assertContains() {
        assertTrue(CHARSET.contains(CHARSET));
        assertTrue(CHARSET.contains(StandardCharsets.US_ASCII));
        assertFalse(CHARSET.contains(StandardCharsets.UTF_8));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("decodeCases")
    void assertDecode(final String label, final byte[] bytes, final String expected) throws CharacterCodingException {
        assertThat(CHARSET.newDecoder().decode(ByteBuffer.wrap(bytes)).toString(), is(expected));
    }
    
    private static Stream<Arguments> decodeCases() {
        return Stream.of(
                Arguments.of("yen sign", new byte[]{0x5C}, "¥"),
                Arguments.of("overline", new byte[]{0x7E}, "‾"),
                Arguments.of("horizontal bar", new byte[]{(byte) 0x81, 0x5C}, "―"),
                Arguments.of("backslash", new byte[]{(byte) 0x81, 0x5F}, "\\"),
                Arguments.of("multiply", new byte[]{(byte) 0x81, 0x7F}, "×"),
                Arguments.of("single replacement", new byte[]{(byte) 0xA0}, "�"),
                Arguments.of("double replacement", new byte[]{(byte) 0x80, 0x40}, "�"));
    }
    
    @Test
    void assertEncodeFirebirdMappings() throws CharacterCodingException {
        ByteBuffer actual = CHARSET.newEncoder().encode(CharBuffer.wrap("\\―¥‾"));
        byte[] actualBytes = new byte[actual.remaining()];
        actual.get(actualBytes);
        assertThat(actualBytes, is(new byte[]{(byte) 0x81, 0x5F, (byte) 0x81, 0x5C, 0x5C, 0x7E}));
    }
    
    @Test
    void assertRejectUnrepresentableCharacters() {
        assertThrows(CharacterCodingException.class, () -> CHARSET.newEncoder().encode(CharBuffer.wrap("—")));
        assertThrows(CharacterCodingException.class, () -> CHARSET.newEncoder().encode(CharBuffer.wrap("＼")));
        assertThrows(CharacterCodingException.class, () -> CHARSET.newEncoder().encode(CharBuffer.wrap("😀")));
    }
    
    @Test
    void assertRejectMalformedInput() {
        assertThrows(CharacterCodingException.class, () -> CHARSET.newDecoder().decode(ByteBuffer.wrap(new byte[]{(byte) 0x81, (byte) 0xFE})));
        assertThrows(CharacterCodingException.class, () -> CHARSET.newDecoder().decode(ByteBuffer.wrap(new byte[]{(byte) 0xFF})));
        assertThrows(CharacterCodingException.class, () -> CHARSET.newDecoder().decode(ByteBuffer.wrap(new byte[]{(byte) 0x81})));
    }
    
    @Test
    void assertDecoderLeavesUnconsumedBytesOnOverflowOrUnderflow() {
        ByteBuffer bytes = ByteBuffer.wrap(new byte[]{(byte) 0x81, 0x5F});
        assertThat(CHARSET.newDecoder().decode(bytes, CharBuffer.allocate(0), false), is(CoderResult.OVERFLOW));
        assertThat(bytes.position(), is(0));
        ByteBuffer partial = ByteBuffer.wrap(new byte[]{(byte) 0x81});
        assertThat(CHARSET.newDecoder().decode(partial, CharBuffer.allocate(1), false), is(CoderResult.UNDERFLOW));
        assertThat(partial.position(), is(0));
    }
    
    @Test
    void assertEncoderLeavesUnconsumedCharactersOnOverflow() {
        CharBuffer chars = CharBuffer.wrap("\\");
        assertThat(CHARSET.newEncoder().encode(chars, ByteBuffer.allocate(1), false), is(CoderResult.OVERFLOW));
        assertThat(chars.position(), is(0));
    }
    
}
