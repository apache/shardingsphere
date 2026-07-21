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
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentifierCasePolicyResolverTest {
    
    @Test
    void assertResolveWithAutoPostgreSQLRule() {
        IdentifierCasePolicy actual = IdentifierCasePolicyResolver.resolveProtocol(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"))
                .getPolicy(IdentifierScope.TABLE);
        assertTrue(actual.matches("foo", "FOO", QuoteCharacter.NONE));
        assertFalse(actual.matches("Foo", "foo", QuoteCharacter.NONE));
    }
    
    @Test
    void assertResolveWithAutoOracleRule() {
        IdentifierCasePolicy actual = IdentifierCasePolicyResolver.resolveProtocol(TypedSPILoader.getService(DatabaseType.class, "Oracle"))
                .getPolicy(IdentifierScope.TABLE);
        assertTrue(actual.matches("FOO", "foo", QuoteCharacter.NONE));
        assertFalse(actual.matches("Foo", "foo", QuoteCharacter.NONE));
    }
    
    @Test
    void assertResolveWithAutoMySQLRule() {
        IdentifierCasePolicy actual = IdentifierCasePolicyResolver.resolveProtocol(TypedSPILoader.getService(DatabaseType.class, "MySQL"))
                .getPolicy(IdentifierScope.TABLE);
        assertTrue(actual.matches("Foo", "foo", QuoteCharacter.NONE));
        assertTrue(actual.matches("Foo", "foo", QuoteCharacter.BACK_QUOTE));
    }
}
