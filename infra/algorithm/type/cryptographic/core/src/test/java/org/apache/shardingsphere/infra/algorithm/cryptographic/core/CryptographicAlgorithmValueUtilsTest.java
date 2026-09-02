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

package org.apache.shardingsphere.infra.algorithm.cryptographic.core;

import org.apache.shardingsphere.infra.algorithm.cryptographic.spi.CryptographicContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CryptographicAlgorithmValueUtilsTest {
    
    @Test
    void assertConvertToBytesWithDefaultContext() {
        assertThat(CryptographicAlgorithmValueUtils.convertToBytes("test", null), is("test".getBytes(StandardCharsets.UTF_8)));
    }
    
    @Test
    void assertConvertToBytesWithPlainCharset() {
        assertThat(CryptographicAlgorithmValueUtils.convertToBytes("test", new CryptographicContext(StandardCharsets.UTF_16BE, false)), is("test".getBytes(StandardCharsets.UTF_16BE)));
    }
    
    @Test
    void assertConvertToPlainValueWithText() {
        assertThat(CryptographicAlgorithmValueUtils.convertToPlainValue("test".getBytes(StandardCharsets.UTF_16BE), new CryptographicContext(StandardCharsets.UTF_16BE, false)), is("test"));
    }
    
    @Test
    void assertConvertToPlainValueWithBytes() {
        byte[] value = new byte[]{(byte) 0xFF};
        assertThat((byte[]) CryptographicAlgorithmValueUtils.convertToPlainValue(value, new CryptographicContext(StandardCharsets.UTF_8, true)), is(value));
    }
    
    @Test
    void assertDecodeBase64() {
        assertThat(CryptographicAlgorithmValueUtils.decodeBase64("dGVzdA=="), is("test".getBytes(StandardCharsets.UTF_8)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("acceptedBase64WhitespaceArguments")
    void assertDecodeBase64WithWhitespace(final String name, final String value) {
        assertThat(CryptographicAlgorithmValueUtils.decodeBase64(value), is("test".getBytes(StandardCharsets.UTF_8)));
    }
    
    private static Stream<Arguments> acceptedBase64WhitespaceArguments() {
        return Stream.of(
                Arguments.of("leading-space", " dGVzdA=="), Arguments.of("embedded-space", "dGVz dA=="), Arguments.of("trailing-space", "dGVzdA== "),
                Arguments.of("leading-tab", "\tdGVzdA=="), Arguments.of("embedded-tab", "dGVz\tdA=="), Arguments.of("trailing-tab", "dGVzdA==\t"),
                Arguments.of("leading-carriage-return", "\rdGVzdA=="), Arguments.of("embedded-carriage-return", "dGVz\rdA=="), Arguments.of("trailing-carriage-return", "dGVzdA==\r"),
                Arguments.of("leading-line-feed", "\ndGVzdA=="), Arguments.of("embedded-line-feed", "dGVz\ndA=="), Arguments.of("trailing-line-feed", "dGVzdA==\n"),
                Arguments.of("legacy-leading-form-feed", "\fdGVzdA=="), Arguments.of("legacy-trailing-vertical-tab", "dGVzdA==" + (char) 0x0B));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidBase64CharacterArguments")
    void assertDecodeBase64WithInvalidCharacter(final String name, final String value) {
        assertThrows(IllegalArgumentException.class, () -> CryptographicAlgorithmValueUtils.decodeBase64(value));
    }
    
    private static Stream<Arguments> invalidBase64CharacterArguments() {
        return Stream.of(
                Arguments.of("punctuation", "dGVz#dA=="), Arguments.of("url-safe-hyphen", "dGVz-dA=="), Arguments.of("url-safe-underscore", "dGVz_dA=="),
                Arguments.of("form-feed", "dGVz\fdA=="), Arguments.of("vertical-tab", "dGVz" + (char) 0x0B + "dA=="),
                Arguments.of("non-breaking-space", "dGVz" + (char) 0x00A0 + "dA=="), Arguments.of("unicode-em-space", "dGVz" + (char) 0x2003 + "dA=="));
    }
}
