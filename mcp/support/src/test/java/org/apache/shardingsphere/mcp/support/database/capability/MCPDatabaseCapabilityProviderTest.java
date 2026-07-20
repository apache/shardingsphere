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

package org.apache.shardingsphere.mcp.support.database.capability;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.support.database.metadata.TransactionCapability;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DefaultSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaSemantics;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.sequence.DialectSequenceOption;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.MCPJdbcDatabaseProfileLoader;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MCPDatabaseCapabilityProviderTest {
    
    @Test
    void assertProvide() {
        Optional<MCPDatabaseCapability> actual = createCapabilityProvider().provide("logic_db");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDatabaseName(), is("logic_db"));
        assertThat(actual.get().getDatabaseType(), is("MySQL"));
        assertThat(actual.get().getSupportedMetadataObjectTypes(),
                is(EnumSet.of(SupportedMCPMetadataObjectType.SCHEMA, SupportedMCPMetadataObjectType.TABLE, SupportedMCPMetadataObjectType.VIEW, SupportedMCPMetadataObjectType.COLUMN,
                        SupportedMCPMetadataObjectType.INDEX)));
        assertThat(actual.get().getTransactionCapability(), is(TransactionCapability.LOCAL_WITH_SAVEPOINT));
        assertTrue(actual.get().supportsTransactionControl());
        assertTrue(actual.get().supportsSavepoint());
        assertThat(actual.get().getDefaultSchemaSemantics(), is(DialectSchemaSemantics.DATABASE_AS_SCHEMA));
        assertThat(actual.get().getSchemaExecutionSemantics(), is(SchemaExecutionSemantics.FIXED_TO_DATABASE));
        assertFalse(actual.get().supportsCrossSchemaSql());
        assertTrue(actual.get().supportsExplain());
        assertFalse(actual.get().getIdentifierCasePolicySet().getPolicy(IdentifierScope.TABLE).matches("phone", "Phone", QuoteCharacter.NONE));
    }
    
    @Test
    void assertProvideWithoutSequence() {
        Optional<MCPDatabaseCapability> actual = createCapabilityProvider().provide("warehouse");
        assertTrue(actual.isPresent());
        assertTrue(actual.get().getSupportedMetadataObjectTypes().contains(SupportedMCPMetadataObjectType.INDEX));
        assertFalse(actual.get().getSupportedMetadataObjectTypes().contains(SupportedMCPMetadataObjectType.SEQUENCE));
        assertThat(actual.get().getTransactionCapability(), is(TransactionCapability.NONE));
        assertFalse(actual.get().supportsTransactionControl());
        assertFalse(actual.get().supportsSavepoint());
        assertThat(actual.get().getDefaultSchemaSemantics(), is(DialectSchemaSemantics.DATABASE_AS_SCHEMA));
        assertThat(actual.get().getSchemaExecutionSemantics(), is(SchemaExecutionSemantics.FIXED_TO_DATABASE));
    }
    
    @Test
    void assertProvideWithoutCapabilityOption() {
        MCPDatabaseCapabilityProvider provider = createCapabilityProvider("FixtureDB", new CapabilityFixture(false, false, false, DialectSchemaSemantics.NATIVE_SCHEMA));
        assertThat(provider.findDatabaseProfile("logic_db").orElseThrow().getDatabaseType(), is("FixtureDB"));
        assertFalse(provider.provide("logic_db").isPresent());
    }
    
    @Test
    void assertPreserveScopedIdentifierCasePolicies() {
        IdentifierCasePolicySet insensitivePolicySet = IdentifierCasePolicyFactory.newInsensitivePolicySet();
        IdentifierCasePolicySet scopedPolicySet = new IdentifierCasePolicySet(
                insensitivePolicySet.getPolicy(IdentifierScope.TABLE),
                Map.of(IdentifierScope.TABLE, IdentifierCasePolicyFactory.newSensitivePolicySet().getPolicy(IdentifierScope.TABLE),
                        IdentifierScope.COLUMN, insensitivePolicySet.getPolicy(IdentifierScope.COLUMN)));
        CapabilityFixture capabilityFixture = new CapabilityFixture(true, true, false, DialectSchemaSemantics.DATABASE_AS_SCHEMA);
        MCPDatabaseCapabilityProvider provider = createCapabilityProvider(
                Map.of("logic_db", createDatabaseProfile("logic_db", "MySQL", capabilityFixture, scopedPolicySet)), Map.of("MySQL", capabilityFixture));
        IdentifierCasePolicySet actual = provider.provide("logic_db").orElseThrow().getIdentifierCasePolicySet();
        assertFalse(actual.getPolicy(IdentifierScope.TABLE).matches("phone", "Phone", QuoteCharacter.NONE));
        assertTrue(actual.getPolicy(IdentifierScope.COLUMN).matches("phone", "Phone", QuoteCharacter.NONE));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideCapabilityMatrixArguments")
    void assertProvideWithCapabilityMatrix(final String name, final String databaseType, final boolean expectedTransactionControl,
                                           final boolean expectedSavepoint, final boolean expectedSequenceSupport,
                                           final SchemaExecutionSemantics expectedSchemaExecutionSemantics, final boolean expectedExplainSupport) {
        CapabilityFixture capabilityFixture = new CapabilityFixture(expectedTransactionControl, expectedSavepoint, expectedSequenceSupport,
                SchemaExecutionSemantics.FIXED_TO_DATABASE == expectedSchemaExecutionSemantics ? DialectSchemaSemantics.DATABASE_AS_SCHEMA : DialectSchemaSemantics.NATIVE_SCHEMA);
        Optional<MCPDatabaseCapability> actual = createCapabilityProvider(databaseType, capabilityFixture).provide("logic_db");
        assertTrue(actual.isPresent());
        assertThat(actual.get().supportsTransactionControl(), is(expectedTransactionControl));
        assertThat(actual.get().supportsSavepoint(), is(expectedSavepoint));
        assertTrue(actual.get().getSupportedMetadataObjectTypes().contains(SupportedMCPMetadataObjectType.INDEX));
        assertThat(actual.get().getSupportedMetadataObjectTypes().contains(SupportedMCPMetadataObjectType.SEQUENCE), is(expectedSequenceSupport));
        assertThat(actual.get().getSchemaExecutionSemantics(), is(expectedSchemaExecutionSemantics));
        assertThat(actual.get().supportsCrossSchemaSql(), is(SchemaExecutionSemantics.BEST_EFFORT == expectedSchemaExecutionSemantics));
        assertThat(actual.get().supportsExplain(), is(expectedExplainSupport));
    }
    
    private MCPDatabaseCapabilityProvider createCapabilityProvider() {
        Map<String, CapabilityFixture> capabilityFixtures = Map.of(
                "MySQL", new CapabilityFixture(true, true, false, DialectSchemaSemantics.DATABASE_AS_SCHEMA),
                "Hive", new CapabilityFixture(false, false, false, DialectSchemaSemantics.DATABASE_AS_SCHEMA));
        Map<String, RuntimeDatabaseProfile> databaseProfiles = new LinkedHashMap<>(2, 1F);
        databaseProfiles.put("logic_db", createDatabaseProfile("logic_db", "MySQL", capabilityFixtures.get("MySQL"), IdentifierCasePolicyFactory.newSensitivePolicySet()));
        databaseProfiles.put("warehouse", createDatabaseProfile("warehouse", "Hive", capabilityFixtures.get("Hive"), IdentifierCasePolicyFactory.newSensitivePolicySet()));
        return createCapabilityProvider(databaseProfiles, capabilityFixtures);
    }
    
    private MCPDatabaseCapabilityProvider createCapabilityProvider(final String databaseType, final CapabilityFixture capabilityFixture) {
        return createCapabilityProvider(Map.of("logic_db", createDatabaseProfile(
                "logic_db", databaseType, capabilityFixture, IdentifierCasePolicyFactory.newSensitivePolicySet())),
                Map.of(databaseType, capabilityFixture));
    }
    
    private MCPDatabaseCapabilityProvider createCapabilityProvider(final Map<String, RuntimeDatabaseProfile> databaseProfiles,
                                                                   final Map<String, CapabilityFixture> capabilityFixtures) {
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = new LinkedHashMap<>(databaseProfiles.size(), 1F);
        for (String each : databaseProfiles.keySet()) {
            runtimeDatabases.put(each, mock(RuntimeDatabaseConfiguration.class));
        }
        try (
                MockedConstruction<MCPJdbcDatabaseProfileLoader> ignored = mockConstruction(MCPJdbcDatabaseProfileLoader.class,
                        (mock, context) -> when(mock.load(any())).thenReturn(databaseProfiles));
                MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS);
                MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            for (Entry<String, CapabilityFixture> entry : capabilityFixtures.entrySet()) {
                mockDatabaseType(entry.getKey(), entry.getValue(), typedSPILoader, databaseTypedSPILoader);
            }
            return new MCPDatabaseCapabilityProvider(runtimeDatabases);
        }
    }
    
    private void mockDatabaseType(final String databaseType, final CapabilityFixture capabilityFixture, final MockedStatic<TypedSPILoader> typedSPILoader,
                                  final MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader) {
        DatabaseType databaseTypeFromSPI = mock(DatabaseType.class);
        when(databaseTypeFromSPI.getType()).thenReturn(databaseType);
        when(databaseTypeFromSPI.getTrunkDatabaseType()).thenReturn(Optional.empty());
        typedSPILoader.when(() -> TypedSPILoader.findService(DatabaseType.class, databaseType)).thenReturn(Optional.of(databaseTypeFromSPI));
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getSchemaOption()).thenReturn(
                new DefaultSchemaOption(false, null, capabilityFixture.schemaSemantics));
        when(dialectDatabaseMetaData.getSequenceOption()).thenReturn(
                capabilityFixture.sequenceSupported ? Optional.of(new DialectSequenceOption("SELECT SEQUENCE_SCHEMA, SEQUENCE_NAME FROM TEST_SEQUENCES")) : Optional.empty());
        databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectDatabaseMetaData.class, databaseTypeFromSPI)).thenReturn(Optional.of(dialectDatabaseMetaData));
    }
    
    private RuntimeDatabaseProfile createDatabaseProfile(final String databaseName, final String databaseType, final CapabilityFixture capabilityFixture,
                                                         final IdentifierCasePolicySet identifierCasePolicySet) {
        TransactionCapability transactionCapability = capabilityFixture.transactionSupported
                ? capabilityFixture.savepointSupported ? TransactionCapability.LOCAL_WITH_SAVEPOINT : TransactionCapability.LOCAL
                : TransactionCapability.NONE;
        return new RuntimeDatabaseProfile(databaseName, databaseType, "", transactionCapability, identifierCasePolicySet);
    }
    
    private static Stream<Arguments> provideCapabilityMatrixArguments() {
        return Stream.of(
                Arguments.of("mysql", "MySQL", true, true, false, SchemaExecutionSemantics.FIXED_TO_DATABASE, true),
                Arguments.of("postgresql", "PostgreSQL", true, true, true, SchemaExecutionSemantics.BEST_EFFORT, true),
                Arguments.of("open gauss", "openGauss", true, true, true, SchemaExecutionSemantics.BEST_EFFORT, true),
                Arguments.of("sql server", "SQLServer", true, true, true, SchemaExecutionSemantics.BEST_EFFORT, false),
                Arguments.of("mariadb", "MariaDB", true, true, true, SchemaExecutionSemantics.FIXED_TO_DATABASE, true),
                Arguments.of("oracle", "Oracle", true, true, true, SchemaExecutionSemantics.BEST_EFFORT, false),
                Arguments.of("clickhouse", "ClickHouse", false, false, false, SchemaExecutionSemantics.FIXED_TO_DATABASE, true),
                Arguments.of("hive", "Hive", false, false, false, SchemaExecutionSemantics.FIXED_TO_DATABASE, true),
                Arguments.of("presto", "Presto", true, false, false, SchemaExecutionSemantics.BEST_EFFORT, true),
                Arguments.of("firebird", "Firebird", true, true, true, SchemaExecutionSemantics.BEST_EFFORT, false));
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class CapabilityFixture {
        
        private final boolean transactionSupported;
        
        private final boolean savepointSupported;
        
        private final boolean sequenceSupported;
        
        private final DialectSchemaSemantics schemaSemantics;
        
    }
}
