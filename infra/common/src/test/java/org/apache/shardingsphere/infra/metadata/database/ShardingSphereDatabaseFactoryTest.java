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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.SystemSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ShardingSphereDatabaseFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final ConfigurationProperties props = new ConfigurationProperties(new Properties());
    
    private final DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.singleton(new FixtureRuleConfiguration()));
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createWithSystemDatabaseArguments")
    void assertCreateWithSystemDatabase(final String name, final DatabaseType protocolType, final String databaseName, final String expectedDefaultSchemaName, final int expectedSchemaCount) {
        ShardingSphereDatabase actual = ShardingSphereDatabaseFactory.create(databaseName, protocolType, props);
        assertThat(actual.getName(), is(databaseName));
        assertThat(actual.getProtocolType(), is(protocolType));
        assertThat(actual.getAllSchemas().size(), is(expectedSchemaCount));
        assertTrue(actual.containsSchema(expectedDefaultSchemaName));
        assertTrue(actual.getRuleMetaData().getRules().isEmpty());
    }
    
    @Test
    void assertCreateWithDatabaseConfiguration() throws SQLException {
        DatabaseType protocolType = mock(DatabaseType.class);
        Map<String, ShardingSphereSchema> systemSchemas = new LinkedHashMap<>(2, 1F);
        systemSchemas.put("system_schema", new ShardingSphereSchema("system_schema", protocolType));
        systemSchemas.put("metadata_schema", new ShardingSphereSchema("metadata_schema", protocolType));
        try (
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, (mock, context) -> when(mock.getDefaultSchemaName("foo_db")).thenReturn("foo_schema"));
                MockedStatic<DatabaseRulesBuilder> mockedRulesBuilder = mockStatic(DatabaseRulesBuilder.class);
                MockedStatic<GenericSchemaBuilder> mockedGenericSchemaBuilder = mockStatic(GenericSchemaBuilder.class);
                MockedStatic<SystemSchemaBuilder> mockedSystemSchemaBuilder = mockStatic(SystemSchemaBuilder.class)) {
            ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
            mockedRulesBuilder.when(() -> DatabaseRulesBuilder.build(eq("foo_db"), eq(protocolType), eq(databaseConfig), eq(computeNodeInstanceContext), any(ResourceMetaData.class)))
                    .thenReturn(Collections.singleton(mock(ShardingSphereRule.class)));
            mockedGenericSchemaBuilder.when(() -> GenericSchemaBuilder.build(eq(protocolType), any(GenericSchemaBuilderMaterial.class)))
                    .thenReturn(Collections.singletonMap("foo_schema", new ShardingSphereSchema("foo_schema", protocolType)));
            mockedSystemSchemaBuilder.when(() -> SystemSchemaBuilder.build("foo_db", protocolType, props)).thenReturn(systemSchemas);
            ShardingSphereDatabase actual = ShardingSphereDatabaseFactory.create("foo_db", protocolType, databaseConfig, props, computeNodeInstanceContext);
            assertThat(actual.getName(), is("foo_db"));
            assertThat(actual.getProtocolType(), is(protocolType));
            assertThat(actual.getAllSchemas().size(), is(3));
            assertThat(actual.getRuleMetaData().getRules().size(), is(1));
            assertTrue(actual.containsSchema("foo_schema"));
            assertTrue(actual.containsSchema("system_schema"));
            assertTrue(actual.containsSchema("metadata_schema"));
        }
    }
    
    @Test
    void assertCreateWithSchemas() {
        ShardingSphereDatabase actual = ShardingSphereDatabaseFactory.create(
                "foo_db", databaseType, databaseConfig, mock(), Collections.singletonList(new ShardingSphereSchema("foo_schema", databaseType)));
        assertThat(actual.getName(), is("foo_db"));
        assertThat(actual.getProtocolType(), is(databaseType));
        assertThat(actual.getAllSchemas().size(), is(1));
        assertThat(actual.getRuleMetaData().getRules().size(), is(1));
        assertTrue(actual.containsSchema("foo_schema"));
    }
    
    private static Stream<Arguments> createWithSystemDatabaseArguments() {
        return Stream.of(
                Arguments.of("fixture database adds default schema", TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), "system_db", "system_db", 1),
                Arguments.of("mysql system database keeps existing default schema", TypedSPILoader.getService(DatabaseType.class, "MySQL"), "mysql", "mysql", 1),
                Arguments.of("mysql database adds default schema", TypedSPILoader.getService(DatabaseType.class, "MySQL"), "foo_db", "foo_db", 1),
                Arguments.of("oracle database formats default schema to upper case", TypedSPILoader.getService(DatabaseType.class, "Oracle"), "foo_db", "FOO_DB", 1));
    }
}
