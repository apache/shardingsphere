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

package org.apache.shardingsphere.database.connector.core.type;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseTypeRegistryTest {
    
    private final DatabaseType trunkDatabaseType = TypedSPILoader.getService(DatabaseType.class, "TRUNK");
    
    private final DatabaseType branchDatabaseType = TypedSPILoader.getService(DatabaseType.class, "BRANCH");
    
    @Test
    void assertGetAllBranchDatabaseTypesWithTrunkType() {
        assertThat(new DatabaseTypeRegistry(trunkDatabaseType).getAllBranchDatabaseTypes(), is(Collections.singletonList(branchDatabaseType)));
    }
    
    @Test
    void assertGetAllBranchDatabaseTypesWithBranchType() {
        assertTrue(new DatabaseTypeRegistry(branchDatabaseType).getAllBranchDatabaseTypes().isEmpty());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDefaultSchemaNameArguments")
    void assertGetDefaultSchemaName(final String name, final String databaseType, final String databaseName, final String expectedSchemaName) {
        assertThat(new DatabaseTypeRegistry(TypedSPILoader.getService(DatabaseType.class, databaseType)).getDefaultSchemaName(databaseName), is(expectedSchemaName));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("formatIdentifierPatternArguments")
    void assertFormatIdentifierPattern(final String name, final IdentifierPatternType identifierPatternType, final String expectedIdentifierPattern) throws ReflectiveOperationException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getIdentifierPatternType()).thenReturn(identifierPatternType);
        DatabaseTypeRegistry databaseTypeRegistry = new DatabaseTypeRegistry(trunkDatabaseType);
        Plugins.getMemberAccessor().set(DatabaseTypeRegistry.class.getDeclaredField("dialectDatabaseMetaData"), databaseTypeRegistry, dialectDatabaseMetaData);
        assertThat(databaseTypeRegistry.formatIdentifierPattern("Foo"), is(expectedIdentifierPattern));
    }
    
    private static Stream<Arguments> getDefaultSchemaNameArguments() {
        return Stream.of(
                Arguments.of("database type contains default schema", "TRUNK", "FOO", "test"),
                Arguments.of("database type does not contain default schema", "BRANCH", "FOO", "FOO"),
                Arguments.of("database name is null", "BRANCH", null, null));
    }
    
    private static Stream<Arguments> formatIdentifierPatternArguments() {
        return Stream.of(
                Arguments.of("identifier pattern upper case", IdentifierPatternType.UPPER_CASE, "FOO"),
                Arguments.of("identifier pattern lower case", IdentifierPatternType.LOWER_CASE, "foo"),
                Arguments.of("identifier pattern keep origin", IdentifierPatternType.KEEP_ORIGIN, "Foo"));
    }
}
