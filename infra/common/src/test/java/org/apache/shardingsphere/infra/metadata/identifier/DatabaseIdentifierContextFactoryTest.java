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
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRule;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.LookupMode;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.MetadataIdentifierCaseSensitivity;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseIdentifierContextFactoryTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertCreateDefault() {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.createDefault();
        IdentifierCaseRule actualRule = actual.getRule(IdentifierScope.TABLE);
        assertThat(actualRule.getLookupMode(QuoteCharacter.NONE), is(LookupMode.NORMALIZED));
        assertTrue(actualRule.matches("Foo", "foo", QuoteCharacter.NONE));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createWithProtocolTypeAndPropsArguments")
    void assertCreateWithProtocolTypeAndProps(final String name, final DatabaseType protocolType, final ConfigurationProperties props, final LookupMode expectedLookupMode,
                                              final String actualIdentifier, final String logicIdentifier, final boolean expectedMatched) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.create(protocolType, props);
        IdentifierCaseRule actualRule = actual.getRule(IdentifierScope.TABLE);
        assertThat(actualRule.getLookupMode(QuoteCharacter.NONE), is(expectedLookupMode));
        assertThat(actualRule.matches(actualIdentifier, logicIdentifier, QuoteCharacter.NONE), is(expectedMatched));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createWithResourceMetaDataAndPropsArguments")
    void assertCreateWithResourceMetaDataAndProps(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                  final ConfigurationProperties props, final LookupMode expectedLookupMode,
                                                  final String actualIdentifier, final String logicIdentifier, final boolean expectedMatched) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.create(protocolType, resourceMetaData, props);
        IdentifierCaseRule actualRule = actual.getRule(IdentifierScope.TABLE);
        assertThat(actualRule.getLookupMode(QuoteCharacter.NONE), is(expectedLookupMode));
        assertThat(actualRule.matches(actualIdentifier, logicIdentifier, QuoteCharacter.NONE), is(expectedMatched));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("refreshWithProtocolTypeAndPropsArguments")
    void assertRefreshWithProtocolTypeAndProps(final String name, final DatabaseType protocolType, final ConfigurationProperties props, final LookupMode expectedLookupMode,
                                               final String actualIdentifier, final String logicIdentifier, final boolean expectedMatched) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.createDefault();
        DatabaseIdentifierContextFactory.refresh(actual, protocolType, props);
        IdentifierCaseRule actualRule = actual.getRule(IdentifierScope.TABLE);
        assertThat(actualRule.getLookupMode(QuoteCharacter.NONE), is(expectedLookupMode));
        assertThat(actualRule.matches(actualIdentifier, logicIdentifier, QuoteCharacter.NONE), is(expectedMatched));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("refreshWithResourceMetaDataAndPropsArguments")
    void assertRefreshWithResourceMetaDataAndProps(final String name, final DatabaseType protocolType, final ResourceMetaData resourceMetaData,
                                                   final ConfigurationProperties props, final LookupMode expectedLookupMode,
                                                   final String actualIdentifier, final String logicIdentifier, final boolean expectedMatched) {
        DatabaseIdentifierContext actual = DatabaseIdentifierContextFactory.createDefault();
        DatabaseIdentifierContextFactory.refresh(actual, protocolType, resourceMetaData, props);
        IdentifierCaseRule actualRule = actual.getRule(IdentifierScope.TABLE);
        assertThat(actualRule.getLookupMode(QuoteCharacter.NONE), is(expectedLookupMode));
        assertThat(actualRule.matches(actualIdentifier, logicIdentifier, QuoteCharacter.NONE), is(expectedMatched));
    }
    
    private static Stream<Arguments> createWithProtocolTypeAndPropsArguments() {
        return Stream.of(
                Arguments.of("null protocol type and null props use insensitive rules", null, null, LookupMode.NORMALIZED, "Foo", "foo", true),
                Arguments.of("sensitive props override protocol rules", DATABASE_TYPE,
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.EXACT, "Foo", "foo", false),
                Arguments.of("insensitive props normalize identifiers", DATABASE_TYPE,
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.INSENSITIVE), LookupMode.NORMALIZED, "Foo", "foo", true));
    }
    
    private static Stream<Arguments> createWithResourceMetaDataAndPropsArguments() {
        return Stream.of(
                Arguments.of("null resource metadata and null props use insensitive rules", null, null, null, LookupMode.NORMALIZED, "Foo", "foo", true),
                Arguments.of("null storage units keep explicit sensitive rules", DATABASE_TYPE, new ResourceMetaData(Collections.emptyMap(), null),
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.EXACT, "Foo", "foo", false),
                Arguments.of("empty storage units keep explicit sensitive rules", DATABASE_TYPE, new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()),
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.EXACT, "Foo", "foo", false),
                Arguments.of("first data source path keeps explicit sensitive rules", DATABASE_TYPE, createResourceMetaDataWithFirstDataSource(),
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.EXACT, "Foo", "foo", false));
    }
    
    private static Stream<Arguments> refreshWithProtocolTypeAndPropsArguments() {
        return Stream.of(
                Arguments.of("null protocol type and null props refresh to insensitive rules", null, null, LookupMode.NORMALIZED, "Foo", "foo", true),
                Arguments.of("sensitive props refresh to exact lookup", DATABASE_TYPE,
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.EXACT, "Foo", "foo", false),
                Arguments.of("insensitive props refresh to normalized lookup", DATABASE_TYPE,
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.INSENSITIVE), LookupMode.NORMALIZED, "Foo", "foo", true));
    }
    
    private static Stream<Arguments> refreshWithResourceMetaDataAndPropsArguments() {
        return Stream.of(
                Arguments.of("null resource metadata and null props refresh to insensitive rules", null, null, null, LookupMode.NORMALIZED, "Foo", "foo", true),
                Arguments.of("null storage units refresh to exact lookup", DATABASE_TYPE, new ResourceMetaData(Collections.emptyMap(), null),
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.EXACT, "Foo", "foo", false),
                Arguments.of("empty storage units refresh to exact lookup", DATABASE_TYPE, new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()),
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.EXACT, "Foo", "foo", false),
                Arguments.of("first data source path refreshes to exact lookup", DATABASE_TYPE, createResourceMetaDataWithFirstDataSource(),
                        createConfigurationProperties(MetadataIdentifierCaseSensitivity.SENSITIVE), LookupMode.EXACT, "Foo", "foo", false));
    }
    
    private static ConfigurationProperties createConfigurationProperties(final MetadataIdentifierCaseSensitivity caseSensitivity) {
        return new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.METADATA_IDENTIFIER_CASE_SENSITIVITY.getKey(), caseSensitivity.name())));
    }
    
    private static ResourceMetaData createResourceMetaDataWithFirstDataSource() {
        ResourceMetaData result = mock(ResourceMetaData.class);
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getDataSource()).thenReturn(mock(DataSource.class));
        when(result.getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        return result;
    }
}
