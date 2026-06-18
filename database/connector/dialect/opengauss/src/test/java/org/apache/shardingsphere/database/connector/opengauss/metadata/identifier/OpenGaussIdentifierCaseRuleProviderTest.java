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

package org.apache.shardingsphere.database.connector.opengauss.metadata.identifier;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRule;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleProvider;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleProviderContext;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.LookupMode;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenGaussIdentifierCaseRuleProviderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private final IdentifierCaseRuleProvider provider = DatabaseTypedSPILoader.getService(IdentifierCaseRuleProvider.class, databaseType);
    
    @Test
    void assertGetDatabaseType() {
        assertThat(provider, isA(OpenGaussIdentifierCaseRuleProvider.class));
        assertThat(provider.getDatabaseType(), is("openGauss"));
    }
    
    @Test
    void assertProvide() {
        IdentifierCaseRuleProviderContext context = new IdentifierCaseRuleProviderContext(databaseType, null);
        IdentifierCaseRule tableRule = provider.provide(context).orElseThrow(AssertionError::new).getRule(IdentifierScope.TABLE);
        assertThat(tableRule.getLookupMode(QuoteCharacter.NONE), is(LookupMode.NORMALIZED));
        assertTrue(tableRule.matches("foo", "FOO", QuoteCharacter.NONE));
        assertFalse(tableRule.matches("Foo", "foo", QuoteCharacter.NONE));
        IdentifierCaseRule schemaRule = provider.provide(context).orElseThrow(AssertionError::new).getRule(IdentifierScope.SCHEMA);
        assertThat(schemaRule.getLookupMode(QuoteCharacter.NONE), is(LookupMode.NORMALIZED));
        assertTrue(schemaRule.matches("UPPER_SCHEMA", "upper_schema", QuoteCharacter.NONE));
        assertFalse(schemaRule.matches("UPPER_SCHEMA", "upper_schema", QuoteCharacter.QUOTE));
    }
}
