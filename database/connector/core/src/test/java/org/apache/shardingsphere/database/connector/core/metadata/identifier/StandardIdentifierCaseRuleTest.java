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

package org.apache.shardingsphere.database.connector.core.metadata.identifier;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardIdentifierCaseRuleTest {
    
    private final StandardIdentifierCaseRule rule = new StandardIdentifierCaseRule(LookupMode.EXACT, LookupMode.NORMALIZED,
            each -> each.toLowerCase(Locale.ENGLISH), each -> each.equals(each.toLowerCase(Locale.ENGLISH)));
    
    @Test
    void assertGetLookupModeWithQuotedIdentifier() {
        assertThat(rule.getLookupMode(QuoteCharacter.QUOTE), is(LookupMode.EXACT));
    }
    
    @Test
    void assertGetLookupModeWithUnquotedIdentifier() {
        assertThat(rule.getLookupMode(QuoteCharacter.NONE), is(LookupMode.NORMALIZED));
    }
    
    @Test
    void assertNormalize() {
        assertThat(rule.normalize("Foo"), is("foo"));
    }
    
    @Test
    void assertNormalizeWithNullValue() {
        assertNull(rule.normalize(null));
    }
    
    @Test
    void assertMatchesWithQuotedNormalizedLookup() {
        StandardIdentifierCaseRule actual = new StandardIdentifierCaseRule(LookupMode.NORMALIZED, LookupMode.NORMALIZED,
                each -> each.toLowerCase(Locale.ENGLISH), each -> each.equals(each.toLowerCase(Locale.ENGLISH)));
        assertTrue(actual.matches("t_mask", "T_MASK", QuoteCharacter.BACK_QUOTE));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("matchesArguments")
    void assertMatches(final String name, final String storedName, final String actualIdentifier, final QuoteCharacter quoteCharacter, final boolean expected) {
        assertThat(rule.matches(storedName, actualIdentifier, quoteCharacter), is(expected));
    }
    
    private static Stream<Arguments> matchesArguments() {
        return Stream.of(
                Arguments.of("quoted_match", "Foo", "Foo", QuoteCharacter.QUOTE, true),
                Arguments.of("quoted_mismatch", "Foo", "foo", QuoteCharacter.QUOTE, false),
                Arguments.of("quoted_null_names", null, null, QuoteCharacter.QUOTE, true),
                Arguments.of("quoted_null_actual_identifier", "Foo", null, QuoteCharacter.QUOTE, false),
                Arguments.of("unquoted_match", "foo", "FOO", QuoteCharacter.NONE, true),
                Arguments.of("unquoted_non_default_stored_name", "Foo", "FOO", QuoteCharacter.NONE, false),
                Arguments.of("unquoted_null_names", null, null, QuoteCharacter.NONE, true),
                Arguments.of("unquoted_null_actual_identifier", "foo", null, QuoteCharacter.NONE, false),
                Arguments.of("unquoted_different_actual_identifier", "foo", "bar", QuoteCharacter.NONE, false));
    }
}
