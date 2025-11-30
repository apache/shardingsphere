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

package org.apache.shardingsphere.infra.metadata.database.schema.builder;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemSchemaBuilderTest {
    
    @Test
    void assertBuildForMySQL() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ConfigurationProperties configProps = new ConfigurationProperties(PropertiesBuilder.build());
        Map<String, ShardingSphereSchema> actualInformationSchema = SystemSchemaBuilder.build("information_schema", databaseType, configProps);
        assertThat(actualInformationSchema.size(), is(1));
        assertTrue(actualInformationSchema.containsKey("information_schema"));
        assertThat(actualInformationSchema.get("information_schema").getAllTables().size(), is(95));
        Map<String, ShardingSphereSchema> actualMySQLSchema = SystemSchemaBuilder.build("mysql", databaseType, configProps);
        assertThat(actualMySQLSchema.size(), is(1));
        assertTrue(actualMySQLSchema.containsKey("mysql"));
        assertThat(actualMySQLSchema.get("mysql").getAllTables().size(), is(40));
        Map<String, ShardingSphereSchema> actualPerformanceSchema = SystemSchemaBuilder.build("performance_schema", databaseType, configProps);
        assertThat(actualPerformanceSchema.size(), is(1));
        assertTrue(actualPerformanceSchema.containsKey("performance_schema"));
        assertThat(actualPerformanceSchema.get("performance_schema").getAllTables().size(), is(114));
        Map<String, ShardingSphereSchema> actualSysSchema = SystemSchemaBuilder.build("sys", databaseType, configProps);
        assertThat(actualSysSchema.size(), is(1));
        assertTrue(actualSysSchema.containsKey("sys"));
        assertThat(actualSysSchema.get("sys").getAllTables().size(), is(53));
    }
    
    @Test
    void assertBuildForPostgreSQL() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        Map<String, ShardingSphereSchema> actual = SystemSchemaBuilder.build("sharding_db", databaseType, new ConfigurationProperties(PropertiesBuilder.build()));
        assertThat(actual.size(), is(3));
        assertTrue(actual.containsKey("information_schema"));
        assertTrue(actual.containsKey("pg_catalog"));
        assertTrue(actual.containsKey("shardingsphere"));
        assertThat(actual.get("information_schema").getAllTables().size(), is(69));
        assertThat(actual.get("pg_catalog").getAllTables().size(), is(134));
        assertThat(actual.get("shardingsphere").getAllTables().size(), is(1));
    }
    
    @Test
    void assertBuildForOpenGaussSQL() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
        Map<String, ShardingSphereSchema> actual = SystemSchemaBuilder.build("sharding_db", databaseType, new ConfigurationProperties(PropertiesBuilder.build()));
        assertThat(actual.size(), is(16));
        assertTrue(actual.containsKey("pg_catalog"));
        assertTrue(actual.containsKey("shardingsphere"));
        assertThat(actual.get("information_schema").getAllTables().size(), is(66));
        assertThat(actual.get("pg_catalog").getAllTables().size(), is(240));
        assertThat(actual.get("shardingsphere").getAllTables().size(), is(1));
    }
    
    @Test
    void assertBuildForPostgreSQLWhenSystemSchemaMetadataDisabled() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        Properties props = PropertiesBuilder.build(new Property(TemporaryConfigurationPropertyKey.SYSTEM_SCHEMA_METADATA_ASSEMBLY_ENABLED.getKey(), Boolean.FALSE.toString()));
        Map<String, ShardingSphereSchema> actual = SystemSchemaBuilder.build("sharding_db", databaseType, new ConfigurationProperties(props));
        assertThat(actual.size(), is(3));
        ShardingSphereSchema informationSchema = actual.get("information_schema");
        assertThat(informationSchema.getAllTables().size(), is(3));
        assertTrue(informationSchema.containsTable("columns"));
        assertTrue(informationSchema.containsTable("tables"));
        assertTrue(informationSchema.containsTable("views"));
        assertFalse(informationSchema.containsTable("attributes"));
        ShardingSphereSchema pgCatalog = actual.get("pg_catalog");
        assertThat(pgCatalog.getAllTables().size(), is(9));
        assertTrue(pgCatalog.containsTable("pg_class"));
        assertTrue(pgCatalog.containsTable("pg_tables"));
        assertFalse(pgCatalog.containsTable("pg_indexes"));
        ShardingSphereSchema shardingsphereSchema = actual.get("shardingsphere");
        assertThat(shardingsphereSchema.getAllTables().size(), is(1));
        assertTrue(shardingsphereSchema.containsTable("cluster_information"));
    }
}
