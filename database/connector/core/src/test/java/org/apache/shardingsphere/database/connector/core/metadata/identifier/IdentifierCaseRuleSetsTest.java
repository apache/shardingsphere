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
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentifierCaseRuleSetsTest {
    
    @Test
    void assertNewLowerCaseRuleSet() {
        IdentifierCaseRule actual = IdentifierCaseRuleSets.newLowerCaseRuleSet().getRule(IdentifierScope.TABLE);
        assertThat(actual.normalize("Foo"), is("foo"));
        assertFalse(actual.matches("Foo", "FOO", QuoteCharacter.NONE));
    }
    
    @Test
    void assertNewUpperCaseRuleSet() {
        IdentifierCaseRule actual = IdentifierCaseRuleSets.newUpperCaseRuleSet().getRule(IdentifierScope.TABLE);
        assertThat(actual.normalize("Foo"), is("FOO"));
        assertFalse(actual.matches("Foo", "foo", QuoteCharacter.NONE));
    }
    
    @Test
    void assertNewSensitiveRuleSet() {
        IdentifierCaseRule actual = IdentifierCaseRuleSets.newSensitiveRuleSet().getRule(IdentifierScope.TABLE);
        assertThat(actual.getLookupMode(QuoteCharacter.NONE), is(LookupMode.EXACT));
        assertFalse(actual.matches("Foo", "foo", QuoteCharacter.NONE));
    }
    
    @Test
    void assertNewInsensitiveRuleSet() {
        IdentifierCaseRule actual = IdentifierCaseRuleSets.newInsensitiveRuleSet().getRule(IdentifierScope.TABLE);
        assertThat(actual.getLookupMode(QuoteCharacter.QUOTE), is(LookupMode.EXACT));
        assertTrue(actual.matches("Foo", "FOO", QuoteCharacter.NONE));
        assertFalse(actual.matches("t_mask", "T_MASK", QuoteCharacter.BACK_QUOTE));
    }
    
    @Test
    void assertNewMySQLInsensitiveRuleSet() {
        IdentifierCaseRule actual = IdentifierCaseRuleSets.newMySQLInsensitiveRuleSet().getRule(IdentifierScope.TABLE);
        assertThat(actual.getLookupMode(QuoteCharacter.QUOTE), is(LookupMode.NORMALIZED));
        assertThat(actual.getLookupMode(QuoteCharacter.NONE), is(LookupMode.NORMALIZED));
        assertTrue(actual.matches("t_mask", "T_MASK", QuoteCharacter.BACK_QUOTE));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("newDialectDefaultRuleSetArguments")
    void assertNewDialectDefaultRuleSet(final String name, final IdentifierPatternType identifierPatternType, final boolean caseSensitive,
                                        final LookupMode expectedQuotedLookupMode, final LookupMode expectedUnquotedLookupMode,
                                        final String storedName, final String actualIdentifier, final boolean expected) {
        IdentifierCaseRule actual = IdentifierCaseRuleSets.newDialectDefaultRuleSet(identifierPatternType, caseSensitive).getRule(IdentifierScope.TABLE);
        assertThat(actual.getLookupMode(QuoteCharacter.QUOTE), is(expectedQuotedLookupMode));
        assertThat(actual.getLookupMode(QuoteCharacter.NONE), is(expectedUnquotedLookupMode));
        assertThat(actual.matches(storedName, actualIdentifier, QuoteCharacter.NONE), is(expected));
    }
    
    private static Stream<Arguments> newDialectDefaultRuleSetArguments() {
        return Stream.of(
                Arguments.of("lower_case", IdentifierPatternType.LOWER_CASE, false, LookupMode.EXACT, LookupMode.NORMALIZED, "foo", "FOO", true),
                Arguments.of("upper_case", IdentifierPatternType.UPPER_CASE, false, LookupMode.EXACT, LookupMode.NORMALIZED, "FOO", "foo", true),
                Arguments.of("keep_origin_sensitive", IdentifierPatternType.KEEP_ORIGIN, true, LookupMode.EXACT, LookupMode.EXACT, "Foo", "foo", false),
                Arguments.of("keep_origin_insensitive", IdentifierPatternType.KEEP_ORIGIN, false, LookupMode.EXACT, LookupMode.NORMALIZED, "Foo", "FOO", true));
    }
}
