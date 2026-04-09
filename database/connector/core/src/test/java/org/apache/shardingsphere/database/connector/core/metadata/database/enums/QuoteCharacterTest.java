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

package org.apache.shardingsphere.database.connector.core.metadata.database.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class QuoteCharacterTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getQuoteCharacterArguments")
    void assertGetQuoteCharacter(final String name, final String value, final QuoteCharacter expectedQuoteCharacter) {
        assertThat(QuoteCharacter.getQuoteCharacter(value), is(expectedQuoteCharacter));
    }
    
    @Test
    void assertWrap() {
        assertThat(QuoteCharacter.BACK_QUOTE.wrap("test"), is("`test`"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("unwrapArguments")
    void assertUnwrap(final String name, final QuoteCharacter quoteCharacter, final String value, final String expectedValue) {
        assertThat(quoteCharacter.unwrap(value), is(expectedValue));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("isWrappedArguments")
    void assertIsWrapped(final String name, final QuoteCharacter quoteCharacter, final String value, final boolean expectedWrapped) {
        assertThat(quoteCharacter.isWrapped(value), is(expectedWrapped));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("unwrapTextArguments")
    void assertUnwrapText(final String name, final String text, final String expectedText) {
        assertThat(QuoteCharacter.unwrapText(text), is(expectedText));
    }
    
    @Test
    void assertUnwrapAndTrimText() {
        assertThat(QuoteCharacter.unwrapAndTrimText("` test `"), is("test"));
    }
    
    private static Stream<Arguments> getQuoteCharacterArguments() {
        return Stream.of(
                Arguments.of("null value", null, QuoteCharacter.NONE),
                Arguments.of("empty value", "", QuoteCharacter.NONE),
                Arguments.of("plain value", "test", QuoteCharacter.NONE),
                Arguments.of("back quote value", "`test`", QuoteCharacter.BACK_QUOTE),
                Arguments.of("single quote value", "'test'", QuoteCharacter.SINGLE_QUOTE),
                Arguments.of("double quote value", "\"test\"", QuoteCharacter.QUOTE),
                Arguments.of("brackets value", "[test]", QuoteCharacter.BRACKETS),
                Arguments.of("parentheses value", "(test)", QuoteCharacter.PARENTHESES));
    }
    
    private static Stream<Arguments> unwrapArguments() {
        return Stream.of(
                Arguments.of("wrapped by back quote", QuoteCharacter.BACK_QUOTE, "`test`", "test"),
                Arguments.of("start delimiter mismatch", QuoteCharacter.BACK_QUOTE, "test", "test"),
                Arguments.of("end delimiter mismatch", QuoteCharacter.BACK_QUOTE, "`test'", "`test'"));
    }
    
    private static Stream<Arguments> isWrappedArguments() {
        return Stream.of(
                Arguments.of("start delimiter mismatch", QuoteCharacter.SINGLE_QUOTE, "test'", false),
                Arguments.of("end delimiter mismatch", QuoteCharacter.SINGLE_QUOTE, "'test\"", false),
                Arguments.of("fully wrapped by single quote", QuoteCharacter.SINGLE_QUOTE, "'test'", true),
                Arguments.of("none quote character", QuoteCharacter.NONE, "test", true));
    }
    
    private static Stream<Arguments> unwrapTextArguments() {
        return Stream.of(
                Arguments.of("plain text", "test", "test"),
                Arguments.of("back quote text", "`test`", "test"),
                Arguments.of("single quote text", "'test'", "test"),
                Arguments.of("double quote text", "\"test\"", "test"),
                Arguments.of("brackets text", "[test]", "test"),
                Arguments.of("parentheses text", "(test)", "test"),
                Arguments.of("unrecognized wrapper text", "{test}", "{test}"),
                Arguments.of("unmatched back quote text", "`test'", "`test'"));
    }
}
