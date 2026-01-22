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

package org.apache.shardingsphere.infra.metadata.database.schema.manager;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemSchemaManagerTest {
    
    @Test
    void assertValueOfSchemaPathSuccess() {
        Collection<String> actualInformationSchema = SystemSchemaManager.getTables("MySQL", "information_schema");
        assertThat(actualInformationSchema.size(), is(95));
        Collection<String> actualMySQLSchema = SystemSchemaManager.getTables("MySQL", "mysql");
        assertThat(actualMySQLSchema.size(), is(40));
        Collection<String> actualPerformanceSchema = SystemSchemaManager.getTables("MySQL", "performance_schema");
        assertThat(actualPerformanceSchema.size(), is(114));
        Collection<String> actualSysSchema = SystemSchemaManager.getTables("MySQL", "sys");
        assertThat(actualSysSchema.size(), is(53));
        Collection<String> actualShardingSphereSchema = SystemSchemaManager.getTables("MySQL", "shardingsphere");
        assertThat(actualShardingSphereSchema.size(), is(1));
        Collection<String> actualPgInformationSchema = SystemSchemaManager.getTables("PostgreSQL", "information_schema");
        assertThat(actualPgInformationSchema.size(), is(69));
        Collection<String> actualPgCatalog = SystemSchemaManager.getTables("PostgreSQL", "pg_catalog");
        assertThat(actualPgCatalog.size(), is(134));
        Collection<String> actualOgInformationSchema = SystemSchemaManager.getTables("openGauss", "information_schema");
        assertThat(actualOgInformationSchema.size(), is(66));
        Collection<String> actualOgPgCatalog = SystemSchemaManager.getTables("openGauss", "pg_catalog");
        assertThat(actualOgPgCatalog.size(), is(240));
        Collection<String> actualOracleSysSchema = SystemSchemaManager.getTables("Oracle", "sys");
        assertThat(actualOracleSysSchema.size(), is(2054));
    }
    
    @Test
    void assertIsSystemTable() {
        assertTrue(SystemSchemaManager.isSystemTable("information_schema", "columns"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_database"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_tables"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_aggregate"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_am"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_amop"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_amproc"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_attrdef"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_attribute"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_auth_members"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_authid"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_available_extension_versions"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_available_extensions"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_backend_memory_contexts"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_cast"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_range"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_replication_origin"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_rewrite"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_seclabel"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_sequence"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_roles"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_user_mapping"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_stat_database_conflicts"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_stat_gssapi"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_stat_progress_analyze"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_stat_progress_basebackup"));
        assertTrue(SystemSchemaManager.isSystemTable("pg_catalog", "pg_stat_progress_cluster"));
        assertTrue(SystemSchemaManager.isSystemTable("sys", "all_sequences"));
        assertTrue(SystemSchemaManager.isSystemTable("sys", "all_synonyms"));
        assertFalse(SystemSchemaManager.isSystemTable("sharding_db", "t_order"));
        assertTrue(SystemSchemaManager.isSystemTable("shardingsphere", "cluster_information"));
        assertFalse(SystemSchemaManager.isSystemTable("shardingsphere", "foo_tbl"));
    }
    
    @Test
    void assertIsSystemTableWithDatabaseTypeAndNullSchema() {
        assertTrue(SystemSchemaManager.isSystemTable("MySQL", null, "columns"));
        assertTrue(SystemSchemaManager.isSystemTable("PostgreSQL", null, "pg_database"));
        assertFalse(SystemSchemaManager.isSystemTable("MySQL", null, "foo_tbl"));
    }
    
    @Test
    void assertIsSystemTableWithDatabaseTypeAndSchema() {
        assertTrue(SystemSchemaManager.isSystemTable("MySQL", "information_schema", "columns"));
        assertTrue(SystemSchemaManager.isSystemTable("Oracle", "sys", "all_sequences"));
        assertTrue(SystemSchemaManager.isSystemTable("Oracle", "sys", "all_synonyms"));
        assertTrue(SystemSchemaManager.isSystemTable("PostgreSQL", "pg_catalog", "pg_database"));
        assertTrue(SystemSchemaManager.isSystemTable("MySQL", "shardingsphere", "cluster_information"));
        assertFalse(SystemSchemaManager.isSystemTable("MySQL", "information_schema", "foo_tbl"));
        assertFalse(SystemSchemaManager.isSystemTable("NO_DB", "foo_schema", "foo_tbl"));
    }
    
    @Test
    void assertIsSystemTableWithTableNames() {
        assertTrue(SystemSchemaManager.isSystemTable("MySQL", "information_schema", Arrays.asList("columns", "tables", "schemata")));
        assertFalse(SystemSchemaManager.isSystemTable("MySQL", "information_schema", Arrays.asList("columns", "nonexistent_table")));
        assertTrue(SystemSchemaManager.isSystemTable("PostgreSQL", "pg_catalog", Arrays.asList("pg_database", "pg_tables")));
        assertFalse(SystemSchemaManager.isSystemTable("NO_DB", "foo_schema", Collections.singleton("foo_tbl")));
        assertTrue(SystemSchemaManager.isSystemTable("MySQL", "foo_schema", Collections.emptyList()));
    }
    
    @Test
    void assertGetAllInputStreams() {
        java.util.Collection<java.io.InputStream> actual = SystemSchemaManager.getAllInputStreams("MySQL", "information_schema");
        assertThat(actual.size(), is(95));
        for (InputStream each : actual) {
            assertNotNull(each);
        }
    }
}
