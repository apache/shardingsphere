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

import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemSchemaMetadataTest {
    
    @Test
    void assertValueOfSchemaPathSuccess() {
        Optional<Collection<String>> actualInformationSchema = SystemSchemaMetadata.getTables("MySQL", "information_schema");
        assertTrue(actualInformationSchema.isPresent());
        assertThat(actualInformationSchema.get().size(), is(61));
        Optional<Collection<String>> actualMySQLSchema = SystemSchemaMetadata.getTables("MySQL", "mysql");
        assertTrue(actualMySQLSchema.isPresent());
        assertThat(actualMySQLSchema.get().size(), is(31));
        Optional<Collection<String>> actualPerformanceSchema = SystemSchemaMetadata.getTables("MySQL", "performance_schema");
        assertTrue(actualPerformanceSchema.isPresent());
        assertThat(actualPerformanceSchema.get().size(), is(87));
        Optional<Collection<String>> actualSysSchema = SystemSchemaMetadata.getTables("MySQL", "sys");
        assertTrue(actualSysSchema.isPresent());
        assertThat(actualSysSchema.get().size(), is(54));
        Optional<Collection<String>> actualPgInformationSchema = SystemSchemaMetadata.getTables("PostgreSQL", "information_schema");
        assertTrue(actualPgInformationSchema.isPresent());
        assertThat(actualPgInformationSchema.get().size(), is(69));
        Optional<Collection<String>> actualPgCatalog = SystemSchemaMetadata.getTables("PostgreSQL", "pg_catalog");
        assertTrue(actualPgCatalog.isPresent());
        assertThat(actualPgCatalog.get().size(), is(134));
        Optional<Collection<String>> actualOgInformationSchema = SystemSchemaMetadata.getTables("openGauss", "information_schema");
        assertTrue(actualOgInformationSchema.isPresent());
        assertThat(actualOgInformationSchema.get().size(), is(66));
        Optional<Collection<String>> actualOgPgCatalog = SystemSchemaMetadata.getTables("openGauss", "pg_catalog");
        assertTrue(actualOgPgCatalog.isPresent());
        assertThat(actualOgPgCatalog.get().size(), is(240));
        
    }
    
    @Test
    void assertIsisSystemTable() {
        assertTrue(SystemSchemaMetadata.isSystemTable("information_schema", "columns"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_database"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_tables"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_aggregate"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_am"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_amop"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_amproc"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_attrdef"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_attribute"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_auth_members"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_authid"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_available_extension_versions"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_available_extensions"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_backend_memory_contexts"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_cast"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_range"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_replication_origin"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_rewrite"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_seclabel"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_sequence"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_roles"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_user_mapping"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_stat_database_conflicts"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_stat_gssapi"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_stat_progress_analyze"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_stat_progress_basebackup"));
        assertTrue(SystemSchemaMetadata.isSystemTable("pg_catalog", "pg_stat_progress_cluster"));
        assertFalse(SystemSchemaMetadata.isSystemTable("sharding_db", "t_order"));
    }
}
