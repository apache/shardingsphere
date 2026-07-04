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
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyProvider;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyProviderContext;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.LookupMode;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultIdentifierCasePolicyProviderTest {
    
    @Test
    void assertIsDefault() {
        IdentifierCasePolicyProvider actual = TypedSPILoader.getService(IdentifierCasePolicyProvider.class, null);
        assertThat(actual, isA(DefaultIdentifierCasePolicyProvider.class));
        assertTrue(actual.isDefault());
    }
    
    @Test
    void assertProvideSQL92PolicySet() {
        IdentifierCasePolicyProvider actual = TypedSPILoader.getService(IdentifierCasePolicyProvider.class, null);
        IdentifierCasePolicy actualPolicy =
                actual.provide(new IdentifierCasePolicyProviderContext(TypedSPILoader.getService(DatabaseType.class, "SQL92"), null))
                        .orElseThrow(AssertionError::new).getPolicy(IdentifierScope.TABLE);
        assertThat(actualPolicy.getLookupMode(QuoteCharacter.NONE), is(LookupMode.NORMALIZED));
        assertTrue(actualPolicy.matches("foo", "FOO", QuoteCharacter.NONE));
        assertThat(actualPolicy.getLookupMode(QuoteCharacter.BACK_QUOTE), is(LookupMode.EXACT));
    }
}
