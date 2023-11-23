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

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemSchemaBuilderRuleTest {
    
    @Test
    void assertValueOfSchemaPathSuccess() {
        Optional<SystemSchemaBuilderRule> actualInformationSchema = SystemSchemaBuilderRule.findBuilderRule("MySQL", "information_schema");
        assertTrue(actualInformationSchema.isPresent());
        assertThat(actualInformationSchema.get(), is(SystemSchemaBuilderRule.MYSQL_INFORMATION_SCHEMA));
        assertThat(actualInformationSchema.get().getTables().size(), is(61));
        Optional<SystemSchemaBuilderRule> actualMySQLSchema = SystemSchemaBuilderRule.findBuilderRule("MySQL", "mysql");
        assertTrue(actualMySQLSchema.isPresent());
        assertThat(actualMySQLSchema.get(), is(SystemSchemaBuilderRule.MYSQL_MYSQL));
        assertThat(actualMySQLSchema.get().getTables().size(), is(31));
        Optional<SystemSchemaBuilderRule> actualPerformanceSchema = SystemSchemaBuilderRule.findBuilderRule("MySQL", "performance_schema");
        assertTrue(actualPerformanceSchema.isPresent());
        assertThat(actualPerformanceSchema.get(), is(SystemSchemaBuilderRule.MYSQL_PERFORMANCE_SCHEMA));
        assertThat(actualPerformanceSchema.get().getTables().size(), is(87));
        Optional<SystemSchemaBuilderRule> actualSysSchema = SystemSchemaBuilderRule.findBuilderRule("MySQL", "sys");
        assertTrue(actualSysSchema.isPresent());
        assertThat(actualSysSchema.get(), is(SystemSchemaBuilderRule.MYSQL_SYS));
        assertThat(actualSysSchema.get().getTables().size(), is(53));
        Optional<SystemSchemaBuilderRule> actualPgInformationSchema = SystemSchemaBuilderRule.findBuilderRule("PostgreSQL", "information_schema");
        assertTrue(actualPgInformationSchema.isPresent());
        assertThat(actualPgInformationSchema.get(), is(SystemSchemaBuilderRule.POSTGRESQL_INFORMATION_SCHEMA));
        assertThat(actualPgInformationSchema.get().getTables().size(), is(69));
        Optional<SystemSchemaBuilderRule> actualPgCatalog = SystemSchemaBuilderRule.findBuilderRule("PostgreSQL", "pg_catalog");
        assertTrue(actualPgCatalog.isPresent());
        assertThat(actualPgCatalog.get(), is(SystemSchemaBuilderRule.POSTGRESQL_PG_CATALOG));
        assertThat(actualPgCatalog.get().getTables().size(), is(134));
        Optional<SystemSchemaBuilderRule> actualOgInformationSchema = SystemSchemaBuilderRule.findBuilderRule("openGauss", "information_schema");
        assertTrue(actualOgInformationSchema.isPresent());
        assertThat(actualOgInformationSchema.get(), is(SystemSchemaBuilderRule.OPEN_GAUSS_INFORMATION_SCHEMA));
        assertThat(actualOgInformationSchema.get().getTables().size(), is(66));
        Optional<SystemSchemaBuilderRule> actualOgPgCatalog = SystemSchemaBuilderRule.findBuilderRule("openGauss", "pg_catalog");
        assertTrue(actualOgPgCatalog.isPresent());
        assertThat(actualOgPgCatalog.get(), is(SystemSchemaBuilderRule.OPEN_GAUSS_PG_CATALOG));
        assertThat(actualOgPgCatalog.get().getTables().size(), is(240));
        
    }
    
    @Test
    void assertNullableValueOfSchemaPath() {
        Optional<SystemSchemaBuilderRule> unknownSchema = SystemSchemaBuilderRule.findBuilderRule("UnKnown", "public");
        assertFalse(unknownSchema.isPresent());
    }
    
    @Test
    void assertIsisSystemTable() {
        assertTrue(SystemSchemaBuilderRule.isSystemTable("information_schema", "columns"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_database"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_tables"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_aggregate"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_am"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_amop"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_amproc"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_attrdef"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_attribute"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_auth_members"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_authid"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_available_extension_versions"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_available_extensions"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_backend_memory_contexts"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_cast"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_range"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_replication_origin"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_rewrite"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_seclabel"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_sequence"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_roles"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_user_mapping"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_stat_database_conflicts"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_stat_gssapi"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_stat_progress_analyze"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_stat_progress_basebackup"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_stat_progress_cluster"));
        assertFalse(SystemSchemaBuilderRule.isSystemTable("sharding_db", "t_order"));
    }
}
