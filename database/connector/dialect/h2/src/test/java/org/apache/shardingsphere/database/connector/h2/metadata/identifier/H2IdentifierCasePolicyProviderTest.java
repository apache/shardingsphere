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

package org.apache.shardingsphere.database.connector.h2.metadata.identifier;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyProvider;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyProviderContext;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class H2IdentifierCasePolicyProviderTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    private final IdentifierCasePolicyProvider provider = DatabaseTypedSPILoader.getService(IdentifierCasePolicyProvider.class, DATABASE_TYPE);
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideArguments")
    void assertProvide(final String name, final IdentifierCasePolicyProviderContext context, final String expectedDefinition, final boolean expectedMatch) {
        IdentifierCasePolicySet actual = provider.provide(context);
        IdentifierCasePolicy actualSchemaPolicy = actual.getPolicy(IdentifierScope.SCHEMA);
        assertThat(actualSchemaPolicy.normalizeForDefinition("Foo_Schema", QuoteCharacter.NONE), is(expectedDefinition));
        assertThat(actualSchemaPolicy.matches(expectedDefinition, "FOO_SCHEMA", QuoteCharacter.NONE), is(expectedMatch));
        IdentifierCasePolicy actualTablePolicy = actual.getPolicy(IdentifierScope.TABLE);
        assertThat(actualTablePolicy.normalizeForDefinition("Foo_Table", QuoteCharacter.NONE), is("foo_table"));
        assertTrue(actualTablePolicy.matches("foo_table", "FOO_TABLE", QuoteCharacter.NONE));
    }
    
    private static Stream<Arguments> provideArguments() throws SQLException {
        return Stream.of(
                Arguments.of("null data source", new IdentifierCasePolicyProviderContext(DATABASE_TYPE, null), "foo_schema", true),
                Arguments.of("null connection", new IdentifierCasePolicyProviderContext(DATABASE_TYPE, mock(DataSource.class)), "foo_schema", true),
                Arguments.of("stores upper case identifiers", new IdentifierCasePolicyProviderContext(DATABASE_TYPE, mockDataSource(true, false, false, false)), "FOO_SCHEMA", true),
                Arguments.of("stores lower case identifiers", new IdentifierCasePolicyProviderContext(DATABASE_TYPE, mockDataSource(false, true, false, false)), "foo_schema", true),
                Arguments.of("supports mixed case identifiers", new IdentifierCasePolicyProviderContext(DATABASE_TYPE, mockDataSource(false, false, false, true)), "Foo_Schema", false),
                Arguments.of("stores mixed case identifiers", new IdentifierCasePolicyProviderContext(DATABASE_TYPE, mockDataSource(false, false, true, false)), "Foo_Schema", true),
                Arguments.of("unknown identifier case behavior", new IdentifierCasePolicyProviderContext(DATABASE_TYPE, mockDataSource(false, false, false, false)), "foo_schema", true),
                Arguments.of("SQL exception", new IdentifierCasePolicyProviderContext(DATABASE_TYPE, mockFailingDataSource()), "foo_schema", true));
    }
    
    private static DataSource mockDataSource(final boolean storesUpperCaseIdentifiers, final boolean storesLowerCaseIdentifiers,
                                             final boolean storesMixedCaseIdentifiers, final boolean supportsMixedCaseIdentifiers) throws SQLException {
        DataSource result = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(result.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.storesUpperCaseIdentifiers()).thenReturn(storesUpperCaseIdentifiers);
        when(metaData.storesLowerCaseIdentifiers()).thenReturn(storesLowerCaseIdentifiers);
        when(metaData.storesMixedCaseIdentifiers()).thenReturn(storesMixedCaseIdentifiers);
        when(metaData.supportsMixedCaseIdentifiers()).thenReturn(supportsMixedCaseIdentifiers);
        return result;
    }
    
    private static DataSource mockFailingDataSource() throws SQLException {
        DataSource result = mock(DataSource.class);
        when(result.getConnection()).thenThrow(new SQLException("failed"));
        return result;
    }
}
