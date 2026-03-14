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
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ShardingSphereDatabaseFactoryTest {
    
    private final DatabaseType fixtureDatabaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final DatabaseType postgresqlDatabaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
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
        ShardingSphereDatabase actual = ShardingSphereDatabaseFactory.create("foo_db", postgresqlDatabaseType, databaseConfig, props, mock(ComputeNodeInstanceContext.class));
        assertThat(actual.getName(), is("foo_db"));
        assertThat(actual.getProtocolType(), is(postgresqlDatabaseType));
        assertThat(actual.getAllSchemas().size(), is(4));
        assertThat(actual.getRuleMetaData().getRules().size(), is(1));
        assertTrue(actual.containsSchema("public"));
        assertTrue(actual.containsSchema("information_schema"));
        assertTrue(actual.containsSchema("pg_catalog"));
        assertTrue(actual.containsSchema("shardingsphere"));
    }
    
    @Test
    void assertCreateWithSchemas() {
        ShardingSphereDatabase actual = ShardingSphereDatabaseFactory.create(
                "foo_db", fixtureDatabaseType, databaseConfig, mock(ComputeNodeInstanceContext.class), Collections.singletonList(new ShardingSphereSchema("foo_schema", fixtureDatabaseType)));
        assertThat(actual.getName(), is("foo_db"));
        assertThat(actual.getProtocolType(), is(fixtureDatabaseType));
        assertThat(actual.getAllSchemas().size(), is(1));
        assertThat(actual.getRuleMetaData().getRules().size(), is(1));
        assertTrue(actual.containsSchema("foo_schema"));
    }
    
    @Test
    void assertCreateWithoutSystemSchema() throws SQLException {
        ShardingSphereDatabase actual = ShardingSphereDatabaseFactory.createWithoutSystemSchema("foo_db", postgresqlDatabaseType, databaseConfig, props, mock(ComputeNodeInstanceContext.class));
        assertThat(actual.getName(), is("foo_db"));
        assertThat(actual.getProtocolType(), is(postgresqlDatabaseType));
        assertThat(actual.getAllSchemas().size(), is(1));
        assertThat(actual.getRuleMetaData().getRules().size(), is(1));
        assertTrue(actual.containsSchema("public"));
        assertFalse(actual.containsSchema("information_schema"));
    }
    
    private static Stream<Arguments> createWithSystemDatabaseArguments() {
        return Stream.of(
                Arguments.of("fixture database adds default schema", TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), "system_db", "system_db", 1),
                Arguments.of("mysql system database keeps existing default schema", TypedSPILoader.getService(DatabaseType.class, "MySQL"), "mysql", "mysql", 1),
                Arguments.of("postgresql database adds public schema", TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"), "sharding_db", "public", 4));
    }
}
