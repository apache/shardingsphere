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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.LookupMode;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.StandardIdentifierCasePolicy;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseIdentifierContextTest {
    
    @Test
    void assertRefresh() {
        DatabaseIdentifierContext context = new DatabaseIdentifierContext(new IdentifierCasePolicySet(createLowerRule()));
        context.refresh(new IdentifierCasePolicySet(createUpperRule()), new IdentifierCasePolicySet(createLowerRule()),
                new IdentifierCasePolicySet(createUpperRule()), true);
        assertThat(context.normalizeProtocol(IdentifierScope.TABLE, new IdentifierValue("Foo")), is("FOO"));
        assertThat(context.normalizeStorage(IdentifierScope.TABLE, new IdentifierValue("Foo")), is("foo"));
        assertTrue(context.matchesMetaData(IdentifierScope.TABLE, "FOO", new IdentifierValue("foo")));
        assertTrue(context.isHeterogeneousTableLookupEnabled());
    }
    
    @Test
    void assertMatchesMetaData() {
        assertTrue(createContextWithDistinctPolicies().matchesMetaData(IdentifierScope.TABLE, "foo", new IdentifierValue("FOO")));
    }
    
    @Test
    void assertNormalizeProtocol() {
        assertThat(createContextWithDistinctPolicies().normalizeProtocol(IdentifierScope.TABLE, new IdentifierValue("Foo")), is("FOO"));
    }
    
    @Test
    void assertNormalizeStorage() {
        assertThat(createContextWithDistinctPolicies().normalizeStorage(IdentifierScope.TABLE, new IdentifierValue("Foo")), is("foo"));
    }
    
    @Test
    void assertNormalizeStorageWithQuotedIdentifier() {
        assertThat(createContextWithDistinctPolicies().normalizeStorage(IdentifierScope.TABLE, new IdentifierValue("Foo", QuoteCharacter.QUOTE)), is("Foo"));
    }
    
    private DatabaseIdentifierContext createContextWithDistinctPolicies() {
        return new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newUpperCasePolicySet(), IdentifierCasePolicyFactory.newLowerCasePolicySet(),
                IdentifierCasePolicyFactory.newInsensitivePolicySet(), false);
    }
    
    private IdentifierCasePolicy createLowerRule() {
        return new StandardIdentifierCasePolicy(LookupMode.EXACT, LookupMode.NORMALIZED,
                each -> each.toLowerCase(Locale.ENGLISH), each -> true);
    }
    
    private IdentifierCasePolicy createUpperRule() {
        return new StandardIdentifierCasePolicy(LookupMode.EXACT, LookupMode.NORMALIZED,
                each -> each.toUpperCase(Locale.ENGLISH), each -> true);
    }
}
