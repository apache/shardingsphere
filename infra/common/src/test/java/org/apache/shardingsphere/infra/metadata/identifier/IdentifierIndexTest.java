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

package org.apache.shardingsphere.infra.metadata.identifier;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRule;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleSet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.LookupMode;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.StandardIdentifierCaseRule;
import org.apache.shardingsphere.infra.exception.kernel.metadata.AmbiguousIdentifierException;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdentifierIndexTest {
    
    @Test
    void assertRebuild() {
        IdentifierIndex<String> index = new IdentifierIndex<>(new DatabaseIdentifierContext(new IdentifierCaseRuleSet(createExactRule())), IdentifierScope.TABLE);
        index.rebuild(createSingleValueMap("Foo", "value_1"));
        index.rebuild(createSingleValueMap("Bar", "value_2"));
        Optional<String> actualValue = index.find(new IdentifierValue("\"Bar\""));
        assertThat(actualValue, is(Optional.of("value_2")));
    }
    
    @Test
    void assertGetAll() {
        IdentifierIndex<String> index = new IdentifierIndex<>(new DatabaseIdentifierContext(new IdentifierCaseRuleSet(createExactRule())), IdentifierScope.TABLE);
        Map<String, String> values = new LinkedHashMap<>(2, 1F);
        values.put("Foo", "value_1");
        values.put("Bar", "value_2");
        index.rebuild(values);
        assertThat(index.getAll(), contains("value_1", "value_2"));
    }
    
    @Test
    void assertGetAllNames() {
        IdentifierIndex<String> index = new IdentifierIndex<>(new DatabaseIdentifierContext(new IdentifierCaseRuleSet(createExactRule())), IdentifierScope.TABLE);
        Map<String, String> values = new LinkedHashMap<>(2, 1F);
        values.put("Foo", "value_1");
        values.put("Bar", "value_2");
        index.rebuild(values);
        assertThat(index.getAllNames(), contains("Foo", "Bar"));
    }
    
    @Test
    void assertFindWithExactLookup() {
        IdentifierIndex<String> index = new IdentifierIndex<>(new DatabaseIdentifierContext(new IdentifierCaseRuleSet(createExactRule())), IdentifierScope.TABLE);
        index.rebuild(createSingleValueMap("Foo", "value_1"));
        Optional<String> actualValue = index.find(new IdentifierValue("\"Foo\""));
        assertThat(actualValue, is(Optional.of("value_1")));
    }
    
    @Test
    void assertPut() {
        IdentifierIndex<String> index = new IdentifierIndex<>(new DatabaseIdentifierContext(new IdentifierCaseRuleSet(createExactRule())), IdentifierScope.TABLE);
        index.put("Foo", "value_1");
        assertThat(index.find(new IdentifierValue("\"Foo\"")), is(Optional.of("value_1")));
    }
    
    @Test
    void assertPutOverridesExistingValue() {
        IdentifierIndex<String> index = new IdentifierIndex<>(new DatabaseIdentifierContext(new IdentifierCaseRuleSet(createExactRule())), IdentifierScope.TABLE);
        index.put("Foo", "value_1");
        index.put("Foo", "value_2");
        assertThat(index.find(new IdentifierValue("\"Foo\"")), is(Optional.of("value_2")));
    }
    
    @Test
    void assertFindWithNormalizedLookup() {
        IdentifierIndex<String> index = new IdentifierIndex<>(new DatabaseIdentifierContext(new IdentifierCaseRuleSet(createPostgreSQLRule())), IdentifierScope.TABLE);
        index.rebuild(createSingleValueMap("foo", "value_1"));
        Optional<String> actualValue = index.find(new IdentifierValue("FOO"));
        assertThat(actualValue, is(Optional.of("value_1")));
    }
    
    @Test
    void assertFindWithNormalizedLookupIgnoresNonMatchingStoredCase() {
        IdentifierIndex<String> index = new IdentifierIndex<>(new DatabaseIdentifierContext(new IdentifierCaseRuleSet(createPostgreSQLRule())), IdentifierScope.TABLE);
        Map<String, String> values = new LinkedHashMap<>(2, 1F);
        values.put("Foo", "value_1");
        values.put("foo", "value_2");
        index.rebuild(values);
        Optional<String> actualValue = index.find(new IdentifierValue("FOO"));
        assertThat(actualValue, is(Optional.of("value_2")));
    }
    
    @Test
    void assertFindWithQuotedNormalizedLookupReturnsEmptyWhenActualNameDiffers() {
        IdentifierIndex<String> index = new IdentifierIndex<>(new DatabaseIdentifierContext(new IdentifierCaseRuleSet(createMySQLInsensitiveRule())), IdentifierScope.TABLE);
        index.rebuild(createSingleValueMap("Foo", "value_1"));
        Optional<String> actualValue = index.find(new IdentifierValue("\"FOO\""));
        assertThat(actualValue, is(Optional.empty()));
    }
    
    @Test
    void assertRemove() {
        IdentifierIndex<String> index = new IdentifierIndex<>(new DatabaseIdentifierContext(new IdentifierCaseRuleSet(createExactRule())), IdentifierScope.TABLE);
        index.put("Foo", "value_1");
        String actualValue = index.remove("Foo");
        assertThat(actualValue, is("value_1"));
        assertThat(index.find(new IdentifierValue("\"Foo\"")), is(Optional.empty()));
    }
    
    @Test
    void assertRemoveReturnsNullWhenMissing() {
        IdentifierIndex<String> index = new IdentifierIndex<>(new DatabaseIdentifierContext(new IdentifierCaseRuleSet(createExactRule())), IdentifierScope.TABLE);
        assertNull(index.remove("Foo"));
    }
    
    @Test
    void assertFindReturnsEmpty() {
        IdentifierIndex<String> index = new IdentifierIndex<>(new DatabaseIdentifierContext(new IdentifierCaseRuleSet(createPostgreSQLRule())), IdentifierScope.TABLE);
        index.rebuild(createSingleValueMap("foo", "value_1"));
        Optional<String> actualValue = index.find(new IdentifierValue("bar"));
        assertThat(actualValue, is(Optional.empty()));
    }
    
    @Test
    void assertIsEmpty() {
        IdentifierIndex<String> index = new IdentifierIndex<>(new DatabaseIdentifierContext(new IdentifierCaseRuleSet(createExactRule())), IdentifierScope.TABLE);
        assertTrue(index.isEmpty());
        index.put("Foo", "value_1");
        assertFalse(index.isEmpty());
    }
    
    @Test
    void assertSize() {
        IdentifierIndex<String> index = new IdentifierIndex<>(new DatabaseIdentifierContext(new IdentifierCaseRuleSet(createExactRule())), IdentifierScope.TABLE);
        assertThat(index.size(), is(0));
        index.put("Foo", "value_1");
        assertThat(index.size(), is(1));
    }
    
    @Test
    void assertFindThrowsAmbiguousIdentifierException() {
        IdentifierIndex<String> index = new IdentifierIndex<>(new DatabaseIdentifierContext(new IdentifierCaseRuleSet(createMySQLInsensitiveRule())), IdentifierScope.TABLE);
        index.rebuild(createAmbiguousValueMap());
        AmbiguousIdentifierException actualException = assertThrows(AmbiguousIdentifierException.class, () -> index.find(new IdentifierValue("FOO")));
        assertThat(actualException.getMessage(), is("Identifier 'FOO' is ambiguous, matched actual identifiers: Foo, foo."));
    }
    
    private IdentifierCaseRule createExactRule() {
        return new StandardIdentifierCaseRule(LookupMode.EXACT, LookupMode.EXACT, each -> each, each -> true);
    }
    
    private IdentifierCaseRule createPostgreSQLRule() {
        return new StandardIdentifierCaseRule(LookupMode.EXACT, LookupMode.NORMALIZED,
                each -> each.toLowerCase(Locale.ENGLISH), each -> each.equals(each.toLowerCase(Locale.ENGLISH)));
    }
    
    private IdentifierCaseRule createMySQLInsensitiveRule() {
        return new StandardIdentifierCaseRule(LookupMode.NORMALIZED, LookupMode.NORMALIZED,
                each -> each.toLowerCase(Locale.ENGLISH), each -> true);
    }
    
    private Map<String, String> createSingleValueMap(final String key, final String value) {
        Map<String, String> result = new LinkedHashMap<>(1, 1F);
        result.put(key, value);
        return result;
    }
    
    private Map<String, String> createAmbiguousValueMap() {
        Map<String, String> result = new LinkedHashMap<>(2, 1F);
        result.put("Foo", "value_1");
        result.put("foo", "value_2");
        return result;
    }
}
