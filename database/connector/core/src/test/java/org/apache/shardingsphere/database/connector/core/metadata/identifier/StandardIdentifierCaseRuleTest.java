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

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardIdentifierCaseRuleTest {
    
    @Test
    void assertGetLookupModeWithQuotedIdentifier() {
        StandardIdentifierCaseRule rule = createPostgreSQLRule();
        LookupMode actualLookupMode = rule.getLookupMode(QuoteCharacter.QUOTE);
        assertThat(actualLookupMode, is(LookupMode.EXACT));
    }
    
    @Test
    void assertGetLookupModeWithUnquotedIdentifier() {
        StandardIdentifierCaseRule rule = createPostgreSQLRule();
        LookupMode actualLookupMode = rule.getLookupMode(QuoteCharacter.NONE);
        assertThat(actualLookupMode, is(LookupMode.NORMALIZED));
    }
    
    @Test
    void assertNormalize() {
        StandardIdentifierCaseRule rule = createPostgreSQLRule();
        String actualValue = rule.normalize("Foo");
        assertThat(actualValue, is("foo"));
    }
    
    @Test
    void assertMatchesWithQuotedIdentifier() {
        StandardIdentifierCaseRule rule = createPostgreSQLRule();
        assertTrue(rule.matches("Foo", "Foo", QuoteCharacter.QUOTE));
    }
    
    @Test
    void assertMatchesWithUnquotedIdentifier() {
        StandardIdentifierCaseRule rule = createPostgreSQLRule();
        assertTrue(rule.matches("foo", "FOO", QuoteCharacter.NONE));
    }
    
    @Test
    void assertNotMatchesWithUnquotedIdentifierForNonDefaultStoredName() {
        StandardIdentifierCaseRule rule = createPostgreSQLRule();
        assertFalse(rule.matches("Foo", "FOO", QuoteCharacter.NONE));
    }
    
    private StandardIdentifierCaseRule createPostgreSQLRule() {
        return new StandardIdentifierCaseRule(LookupMode.EXACT, LookupMode.NORMALIZED,
                each -> each.toLowerCase(Locale.ENGLISH), each -> each.equals(each.toLowerCase(Locale.ENGLISH)));
    }
}
