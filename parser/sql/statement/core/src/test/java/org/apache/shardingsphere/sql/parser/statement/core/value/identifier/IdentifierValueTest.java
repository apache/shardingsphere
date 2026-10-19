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

package org.apache.shardingsphere.sql.parser.statement.core.value.identifier;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class IdentifierValueTest {
    
    @Test
    void assertGetIdentifierValueWithSingleQuote() {
        String text = "'ms_group_${0..1}'";
        assertThat(new IdentifierValue(text).getValue(), is("ms_group_${0..1}"));
    }
    
    @Test
    void assertGetIdentifierValueWithQuote() {
        String text = "\"ms_group_${0..1}\"";
        assertThat(new IdentifierValue(text).getValue(), is("ms_group_${0..1}"));
    }
    
    @Test
    void assertGetIdentifierValueWithBackQuote() {
        String text = "`t_order`";
        assertThat(new IdentifierValue(text).getValue(), is("t_order"));
    }
    
    @Test
    void assertGetIdentifierValueWithReservedBracket() {
        String text = "ds_${[1,2]}.t_order";
        assertThat(new IdentifierValue(text).getValue(), is("ds_${[1,2]}.t_order"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDecodedQuotedNames")
    void assertFromDecodedQuotedName(final String name, final String value, final String expectedQuotedValue) {
        IdentifierValue actual = IdentifierValue.fromDecodedQuotedName(value);
        assertThat(actual.getValue(), is(value));
        assertThat(actual.getValueWithQuoteCharacters(), is(expectedQuotedValue));
    }
    
    private static Stream<Arguments> getDecodedQuotedNames() {
        return Stream.of(
                Arguments.of("normal name", "foo_table", "\"foo_table\""),
                Arguments.of("single interior quote", "foo_\"table", "\"foo_\"\"table\""),
                Arguments.of("adjacent interior quotes", "foo_\"\"table", "\"foo_\"\"\"\"table\""));
    }
    
    @Test
    void assertFromDecodedQuotedNameWithExistingEquality() {
        IdentifierValue actual = IdentifierValue.fromDecodedQuotedName("foo_\"table");
        IdentifierValue expected = new IdentifierValue("foo_\"table", QuoteCharacter.QUOTE);
        assertThat(actual, is(expected));
        assertThat(actual.hashCode(), is(expected.hashCode()));
    }
    
    @Test
    void assertGetValueWithQuoteCharactersWithNullValue() {
        assertThat(new IdentifierValue(null).getValueWithQuoteCharacters(), is(""));
    }
    
    @Test
    void assertGetValueWithQuoteCharactersWithValue() {
        String text = "[foo]";
        assertThat(new IdentifierValue(text).getValueWithQuoteCharacters(), is("[foo]"));
    }
    
    @Test
    void assertGetValueWithQuoteCharactersWithEscapedRawName() {
        IdentifierValue actual = new IdentifierValue("\"foo_\"\"table\"");
        assertThat(actual.getValue(), is("foo_\"\"table"));
        assertThat(actual.getValueWithQuoteCharacters(), is("\"foo_\"\"table\""));
    }
    
    @Test
    void assertGetValueWithQuoteCharactersWithEscapedExplicitName() {
        IdentifierValue actual = new IdentifierValue("foo_\"\"table", QuoteCharacter.QUOTE);
        assertThat(actual.getValue(), is("foo_\"\"table"));
        assertThat(actual.getValueWithQuoteCharacters(), is("\"foo_\"\"table\""));
    }
}
