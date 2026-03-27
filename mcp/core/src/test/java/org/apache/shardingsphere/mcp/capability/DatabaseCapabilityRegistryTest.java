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

package org.apache.shardingsphere.mcp.capability;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseCapabilityRegistryTest {
    
    @Test
    void assertRegister() {
        DatabaseCapabilityRegistry registry = new DatabaseCapabilityRegistry();
        
        registry.register(createCapability());
        
        Optional<DatabaseCapability> actual = registry.find("MYSQL", "");
        
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDatabaseType(), is("MYSQL"));
    }
    
    @Test
    void assertRegisterWithNull() {
        DatabaseCapabilityRegistry registry = new DatabaseCapabilityRegistry();
        
        assertThrows(NullPointerException.class, () -> registry.register(null));
    }
    
    @Test
    void assertFind() {
        DatabaseCapabilityRegistry registry = new DatabaseCapabilityRegistry();
        registry.register(createCapability());
        
        Optional<DatabaseCapability> actual = registry.find(" mysql ", "");
        
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDatabaseType(), is("MYSQL"));
    }
    
    @Test
    void assertFindWithUnknownDatabaseType() {
        DatabaseCapabilityRegistry registry = new DatabaseCapabilityRegistry();
        registry.register(createCapability());
        
        Optional<DatabaseCapability> actual = registry.find("postgresql", "");
        
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertFindWithNullDatabaseType() {
        DatabaseCapabilityRegistry registry = new DatabaseCapabilityRegistry();
        
        assertThrows(NullPointerException.class, () -> registry.find(null, ""));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("versionAwareExplainAnalyzeCases")
    void assertFindWithVersionAwareExplainAnalyze(final String caseName, final String databaseType, final String databaseVersion, final boolean expected) {
        DatabaseCapabilityRegistry registry = DatabaseCapabilityRegistry.createDefault();
        
        Optional<DatabaseCapability> actual = registry.find(databaseType, databaseVersion);
        
        assertTrue(actual.isPresent(), caseName);
        assertThat(actual.get().isSupportsExplainAnalyze(), is(expected));
        assertThat(actual.get().getSupportedStatementClasses().contains(StatementClass.EXPLAIN_ANALYZE), is(expected));
    }
    
    private DatabaseCapability createCapability() {
        return new DatabaseCapability("mysql", EnumSet.of(SupportedObjectType.TABLE), EnumSet.of(StatementClass.QUERY),
                TransactionCapability.LOCAL_WITH_SAVEPOINT, true, false);
    }
    
    private static Stream<Arguments> versionAwareExplainAnalyzeCases() {
        return Stream.of(
                Arguments.of("MYSQL baseline before 8.0.18", "MYSQL", "8.0.17", false),
                Arguments.of("MYSQL 8.0.18", "MYSQL", "8.0.18", true),
                Arguments.of("MYSQL unparseable version", "MYSQL", "unknown", false),
                Arguments.of("POSTGRESQL baseline", "POSTGRESQL", "16.4", true),
                Arguments.of("H2 baseline", "H2", "2.2.224", true));
    }
}
