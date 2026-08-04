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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentifierCasePolicyFactoryTest {
    
    @Test
    void assertNewLowerCasePolicySet() {
        IdentifierCasePolicy actual = IdentifierCasePolicyFactory.newLowerCasePolicySet().getPolicy(IdentifierScope.TABLE);
        assertThat(actual.normalizeForDefinition("Foo", QuoteCharacter.NONE), is("foo"));
        assertFalse(actual.matches("Foo", "FOO", QuoteCharacter.NONE));
    }
    
    @Test
    void assertNewUpperCasePolicySet() {
        IdentifierCasePolicy actual = IdentifierCasePolicyFactory.newUpperCasePolicySet().getPolicy(IdentifierScope.TABLE);
        assertThat(actual.normalizeForDefinition("Foo", QuoteCharacter.NONE), is("FOO"));
        assertFalse(actual.matches("Foo", "foo", QuoteCharacter.NONE));
    }
    
    @Test
    void assertNewSensitivePolicySet() {
        IdentifierCasePolicy actual = IdentifierCasePolicyFactory.newSensitivePolicySet().getPolicy(IdentifierScope.TABLE);
        assertThat(actual.getLookupMode(QuoteCharacter.NONE), is(LookupMode.EXACT));
        assertFalse(actual.matches("Foo", "foo", QuoteCharacter.NONE));
    }
    
    @Test
    void assertNewInsensitivePolicySet() {
        IdentifierCasePolicy actual = IdentifierCasePolicyFactory.newInsensitivePolicySet().getPolicy(IdentifierScope.TABLE);
        assertThat(actual.getLookupMode(QuoteCharacter.QUOTE), is(LookupMode.EXACT));
        assertTrue(actual.matches("Foo", "FOO", QuoteCharacter.NONE));
        assertFalse(actual.matches("t_mask", "T_MASK", QuoteCharacter.BACK_QUOTE));
    }
    
    @Test
    void assertNewCasePreservingInsensitivePolicySet() {
        IdentifierCasePolicy actual = IdentifierCasePolicyFactory.newCasePreservingInsensitivePolicySet().getPolicy(IdentifierScope.TABLE);
        assertThat(actual.normalizeForDefinition("Foo", QuoteCharacter.QUOTE), is("Foo"));
        assertThat(actual.normalizeForDefinition("Foo", QuoteCharacter.NONE), is("Foo"));
        assertTrue(actual.matches("Foo", "FOO", QuoteCharacter.BACK_QUOTE));
        assertTrue(actual.matches("Foo", "FOO", QuoteCharacter.NONE));
    }
    
    @Test
    void assertNewLowerCaseInsensitivePolicySet() {
        IdentifierCasePolicy actual = IdentifierCasePolicyFactory.newLowerCaseInsensitivePolicySet().getPolicy(IdentifierScope.TABLE);
        assertThat(actual.normalizeForDefinition("Foo", QuoteCharacter.QUOTE), is("foo"));
        assertThat(actual.normalizeForDefinition("Foo", QuoteCharacter.NONE), is("foo"));
        assertTrue(actual.matches("Foo", "FOO", QuoteCharacter.BACK_QUOTE));
        assertTrue(actual.matches("Foo", "FOO", QuoteCharacter.NONE));
    }
    
    @Test
    void assertNewQuotedInsensitivePolicySet() {
        IdentifierCasePolicy actual = IdentifierCasePolicyFactory.newQuotedInsensitivePolicySet().getPolicy(IdentifierScope.TABLE);
        assertThat(actual.getLookupMode(QuoteCharacter.QUOTE), is(LookupMode.NORMALIZED));
        assertThat(actual.getLookupMode(QuoteCharacter.NONE), is(LookupMode.NORMALIZED));
        assertTrue(actual.matches("t_mask", "T_MASK", QuoteCharacter.BACK_QUOTE));
    }
}
