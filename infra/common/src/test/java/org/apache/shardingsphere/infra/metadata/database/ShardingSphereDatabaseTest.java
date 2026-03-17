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

package org.apache.shardingsphere.infra.metadata.database;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.LookupMode;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.MetadataIdentifierCaseSensitivity;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.decorator.RuleConfigurationDecorator;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingSphereDatabaseTest {
    
    private final DatabaseType postgreSQLDatabaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Test
    void assertGetAllSchemas() {
        DatabaseType databaseType = mock(DatabaseType.class);
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mock(ResourceMetaData.class), ruleMetaData,
                Collections.singleton(createSchema("foo_schema", databaseType)));
        List<ShardingSphereSchema> actualSchemas = new LinkedList<>(database.getAllSchemas());
        assertThat(actualSchemas.size(), is(1));
        assertThat(actualSchemas.get(0).getName(), is("foo_schema"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("containsSchemaArguments")
    void assertContainsSchema(final String name, final String schemaName, final boolean expectedContainsSchema) {
        DatabaseType databaseType = mock(DatabaseType.class);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mock(ResourceMetaData.class), new RuleMetaData(Collections.emptyList()),
                Collections.singleton(createSchema("foo_schema", databaseType)));
        boolean actualContainsSchema = database.containsSchema(schemaName);
        assertThat(actualContainsSchema, is(expectedContainsSchema));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSchemaArguments")
    void assertGetSchema(final String name, final String schemaName, final String expectedSchemaName) {
        DatabaseType databaseType = mock(DatabaseType.class);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mock(ResourceMetaData.class), new RuleMetaData(Collections.emptyList()),
                Collections.singleton(createSchema("foo_schema", databaseType)));
        ShardingSphereSchema actualSchema = database.getSchema(schemaName);
        if (null == expectedSchemaName) {
            assertNull(actualSchema);
            return;
        }
        assertNotNull(actualSchema);
        assertThat(actualSchema.getName(), is(expectedSchemaName));
    }
    
    @Test
    void assertAddSchema() throws ReflectiveOperationException {
        ShardingSphereSchema schema = createSchema("new_schema", mock(DatabaseType.class));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), mock(ResourceMetaData.class),
                new RuleMetaData(Collections.emptyList()), Collections.emptyList());
        database.addSchema(schema);
        Map<String, ShardingSphereSchema> actualSchemas = getSchemas(database);
        assertThat(actualSchemas.size(), is(1));
        assertThat(actualSchemas.get("new_schema"), is(schema));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("dropSchemaArguments")
    void assertDropSchema(final String name, final String schemaName, final boolean expectedSchemaRetained) throws ReflectiveOperationException {
        DatabaseType databaseType = mock(DatabaseType.class);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mock(ResourceMetaData.class), new RuleMetaData(Collections.emptyList()),
                Collections.singleton(createSchema("foo_schema", databaseType)));
        database.dropSchema(schemaName);
        boolean actualSchemaRetained = getSchemas(database).containsKey("foo_schema");
        assertThat(actualSchemaRetained, is(expectedSchemaRetained));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("isCompleteArguments")
    void assertIsComplete(final String name, final ResourceMetaData resourceMetaData, final RuleMetaData ruleMetaData, final boolean expectedComplete) {
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, Collections.emptyList());
        boolean actualComplete = database.isComplete();
        assertThat(actualComplete, is(expectedComplete));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("containsDataSourceArguments")
    void assertContainsDataSource(final String name, final ResourceMetaData resourceMetaData, final boolean expectedContainsDataSource) {
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData,
                new RuleMetaData(Collections.singleton(mock(ShardingSphereRule.class))), Collections.emptyList());
        boolean actualContainsDataSource = database.containsDataSource();
        assertThat(actualContainsDataSource, is(expectedContainsDataSource));
    }
    
    @Test
    void assertReloadRules() {
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        MutableDataNodeRuleAttribute ruleAttribute = mock(MutableDataNodeRuleAttribute.class);
        ShardingSphereRule reloadedRule = mock(ShardingSphereRule.class);
        ShardingSphereRule mutableRule = createRule(ruleConfig, new RuleAttributes(ruleAttribute));
        ShardingSphereRule immutableRule = createRule(mock(RuleConfiguration.class), new RuleAttributes());
        when(ruleAttribute.reloadRule(eq(ruleConfig), eq("foo_db"), anyMap(), anyCollection())).thenReturn(reloadedRule);
        RuleMetaData ruleMetaData = new RuleMetaData(Arrays.asList(mutableRule, immutableRule));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class),
                new ResourceMetaData(Collections.singletonMap("ds", new MockedDataSource())), ruleMetaData, Collections.emptyList());
        database.reloadRules();
        Collection<ShardingSphereRule> actualRules = database.getRuleMetaData().getRules();
        assertThat(actualRules.size(), is(2));
        assertFalse(actualRules.contains(mutableRule));
        assertTrue(actualRules.contains(immutableRule));
        assertTrue(actualRules.contains(reloadedRule));
    }
    
    @Test
    void assertReloadRulesWithoutMutableDataNodeRuleAttribute() {
        ShardingSphereRule rule = createRule(mock(RuleConfiguration.class), new RuleAttributes());
        ShardingSphereRule otherRule = createRule(mock(RuleConfiguration.class), new RuleAttributes());
        RuleMetaData ruleMetaData = new RuleMetaData(Arrays.asList(rule, otherRule));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class),
                new ResourceMetaData(Collections.singletonMap("ds", new MockedDataSource())), ruleMetaData, Collections.emptyList());
        database.reloadRules();
        Collection<ShardingSphereRule> actualRules = database.getRuleMetaData().getRules();
        assertThat(actualRules.size(), is(2));
        assertTrue(actualRules.contains(rule));
        assertTrue(actualRules.contains(otherRule));
    }
    
    @Test
    void assertCheckStorageUnitsExisted() {
        DataSourceMapperRuleAttribute ruleAttribute = mock(DataSourceMapperRuleAttribute.class);
        when(ruleAttribute.getDataSourceMapper()).thenReturn(Collections.singletonMap("logic_ds", Collections.singleton("actual_ds")));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), createEmptyResourceMetaData(),
                createRuleMetaData(ruleAttribute), Collections.emptyList());
        assertDoesNotThrow(() -> database.checkStorageUnitsExisted(Collections.singleton("logic_ds")));
    }
    
    @Test
    void assertCheckStorageUnitsExistedWithMissingStorageUnits() {
        DataSourceMapperRuleAttribute ruleAttribute = mock(DataSourceMapperRuleAttribute.class);
        when(ruleAttribute.getDataSourceMapper()).thenReturn(Collections.singletonMap("logic_ds", Collections.singleton("actual_ds")));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), createEmptyResourceMetaData(),
                createRuleMetaData(ruleAttribute), Collections.emptyList());
        MissingRequiredStorageUnitsException actualException =
                assertThrows(MissingRequiredStorageUnitsException.class, () -> database.checkStorageUnitsExisted(Collections.singleton("missing_ds")));
        assertThat(actualException.getMessage(), is("Storage units 'missing_ds' do not exist in database 'foo_db'."));
    }
    
    @Test
    void assertConstructWithProps() throws ReflectiveOperationException {
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", postgreSQLDatabaseType, createEmptyResourceMetaData(),
                new RuleMetaData(Collections.emptyList()), Collections.singleton(createSchema("foo_schema", postgreSQLDatabaseType)), createSensitiveProps());
        DatabaseIdentifierContext actualIdentifierContext = getIdentifierContext(database);
        assertThat(actualIdentifierContext.getRule(IdentifierScope.SCHEMA).getLookupMode(QuoteCharacter.NONE), is(LookupMode.EXACT));
    }
    
    @Test
    void assertRefreshIdentifierContext() throws ReflectiveOperationException {
        ShardingSphereSchema schema = createSchema("foo_schema", postgreSQLDatabaseType);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", postgreSQLDatabaseType, createEmptyResourceMetaData(),
                new RuleMetaData(Collections.emptyList()), Collections.singleton(schema));
        database.refreshIdentifierContext(createSensitiveProps());
        DatabaseIdentifierContext actualIdentifierContext = getIdentifierContext(database);
        assertThat(actualIdentifierContext.getRule(IdentifierScope.SCHEMA).getLookupMode(QuoteCharacter.NONE), is(LookupMode.EXACT));
        assertThat(getIdentifierContext(schema), is(actualIdentifierContext));
    }
    
    @Test
    void assertDecorateRuleConfiguration() {
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), createEmptyResourceMetaData(),
                new RuleMetaData(Collections.singleton(mock(ShardingSphereRule.class))), Collections.emptyList());
        try (MockedStatic<TypedSPILoader> mockedTypedSPILoader = mockStatic(TypedSPILoader.class)) {
            mockedTypedSPILoader.when(() -> TypedSPILoader.findService(RuleConfigurationDecorator.class, ruleConfig.getClass())).thenReturn(Optional.empty());
            RuleConfiguration actualRuleConfig = database.decorateRuleConfiguration(ruleConfig);
            assertThat(actualRuleConfig, is(ruleConfig));
        }
    }
    
    @Test
    void assertDecorateRuleConfigurationWithDecorator() {
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        @SuppressWarnings("unchecked")
        RuleConfigurationDecorator<RuleConfiguration> decorator = mock(RuleConfigurationDecorator.class);
        RuleConfiguration decoratedRuleConfig = mock(RuleConfiguration.class);
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(mock(ShardingSphereRule.class)));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class),
                new ResourceMetaData(Collections.singletonMap("ds", new MockedDataSource())), ruleMetaData, Collections.emptyList());
        try (MockedStatic<TypedSPILoader> mockedTypedSPILoader = mockStatic(TypedSPILoader.class)) {
            mockedTypedSPILoader.when(() -> TypedSPILoader.findService(RuleConfigurationDecorator.class, ruleConfig.getClass())).thenReturn(Optional.of(decorator));
            when(decorator.decorate(eq("foo_db"), anyMap(), eq(ruleMetaData.getRules()), eq(ruleConfig))).thenReturn(decoratedRuleConfig);
            RuleConfiguration actualRuleConfig = database.decorateRuleConfiguration(ruleConfig);
            assertThat(actualRuleConfig, is(decoratedRuleConfig));
            verify(decorator).decorate(eq("foo_db"), anyMap(), eq(ruleMetaData.getRules()), eq(ruleConfig));
        }
    }
    
    private static Stream<Arguments> containsSchemaArguments() {
        return Stream.of(
                Arguments.of("null schema name returns false", null, false),
                Arguments.of("existing schema returns true", "foo_schema", true),
                Arguments.of("missing schema returns false", "missing_schema", false));
    }
    
    private static Stream<Arguments> getSchemaArguments() {
        return Stream.of(
                Arguments.of("null schema name returns null", null, null),
                Arguments.of("existing schema returns schema", "foo_schema", "foo_schema"),
                Arguments.of("missing schema returns null", "missing_schema", null));
    }
    
    private static Stream<Arguments> dropSchemaArguments() {
        return Stream.of(
                Arguments.of("null schema name keeps schema", null, true),
                Arguments.of("missing schema keeps schema", "missing_schema", true),
                Arguments.of("existing schema removes schema", "foo_schema", false));
    }
    
    private static Stream<Arguments> isCompleteArguments() {
        return Stream.of(
                Arguments.of("rules and storage units exist", createResourceMetaData("ds"),
                        new RuleMetaData(Collections.singleton(mock(ShardingSphereRule.class))), true),
                Arguments.of("missing rules returns false", createResourceMetaData("ds"),
                        new RuleMetaData(Collections.emptyList()), false),
                Arguments.of("missing storage units returns false", createEmptyResourceMetaData(),
                        new RuleMetaData(Collections.singleton(mock(ShardingSphereRule.class))), false));
    }
    
    private static Stream<Arguments> containsDataSourceArguments() {
        return Stream.of(
                Arguments.of("empty storage units return false", createEmptyResourceMetaData(), false),
                Arguments.of("single storage unit returns true", createResourceMetaData("ds"), true),
                Arguments.of("multiple storage units return true", createResourceMetaData("ds_0", "ds_1"), true));
    }
    
    private static ShardingSphereSchema createSchema(final String schemaName, final DatabaseType databaseType) {
        return new ShardingSphereSchema(schemaName, databaseType);
    }
    
    private static RuleMetaData createRuleMetaData(final DataSourceMapperRuleAttribute ruleAttribute) {
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        return new RuleMetaData(Collections.singleton(rule));
    }
    
    private static ShardingSphereRule createRule(final RuleConfiguration ruleConfig, final RuleAttributes attributes) {
        ShardingSphereRule result = mock(ShardingSphereRule.class);
        when(result.getConfiguration()).thenReturn(ruleConfig);
        when(result.getAttributes()).thenReturn(attributes);
        return result;
    }
    
    private static ResourceMetaData createEmptyResourceMetaData() {
        return new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap());
    }
    
    private static ResourceMetaData createResourceMetaData(final String... dataSourceNames) {
        Map<String, DataSource> dataSources = new LinkedHashMap<>(dataSourceNames.length, 1F);
        for (String each : dataSourceNames) {
            dataSources.put(each, new MockedDataSource());
        }
        return new ResourceMetaData(dataSources);
    }
    
    private static ConfigurationProperties createSensitiveProps() {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.METADATA_IDENTIFIER_CASE_SENSITIVITY.getKey(), MetadataIdentifierCaseSensitivity.SENSITIVE.name());
        return new ConfigurationProperties(props);
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, ShardingSphereSchema> getSchemas(final ShardingSphereDatabase database) throws ReflectiveOperationException {
        return (Map<String, ShardingSphereSchema>) Plugins.getMemberAccessor().get(ShardingSphereDatabase.class.getDeclaredField("schemas"), database);
    }
    
    private static DatabaseIdentifierContext getIdentifierContext(final ShardingSphereDatabase database) throws ReflectiveOperationException {
        return (DatabaseIdentifierContext) Plugins.getMemberAccessor().get(ShardingSphereDatabase.class.getDeclaredField("identifierContext"), database);
    }
    
    private static DatabaseIdentifierContext getIdentifierContext(final ShardingSphereSchema schema) throws ReflectiveOperationException {
        return (DatabaseIdentifierContext) Plugins.getMemberAccessor().get(ShardingSphereSchema.class.getDeclaredField("identifierContext"), schema);
    }
}
