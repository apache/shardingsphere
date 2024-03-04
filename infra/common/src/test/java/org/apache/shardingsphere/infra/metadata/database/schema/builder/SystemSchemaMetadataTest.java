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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemSchemaMetadataTest {
    
    @Test
    void assertValueOfSchemaPathSuccess() {
        Collection<String> actualInformationSchema = SystemSchemaMetadata.getTables("MySQL", "information_schema");
        assertThat(actualInformationSchema.size(), is(61));
        Collection<String> actualMySQLSchema = SystemSchemaMetadata.getTables("MySQL", "mysql");
        assertThat(actualMySQLSchema.size(), is(31));
        Collection<String> actualPerformanceSchema = SystemSchemaMetadata.getTables("MySQL", "performance_schema");
        assertThat(actualPerformanceSchema.size(), is(87));
        Collection<String> actualSysSchema = SystemSchemaMetadata.getTables("MySQL", "sys");
        assertThat(actualSysSchema.size(), is(54));
        Collection<String> actualShardingSphereSchema = SystemSchemaMetadata.getTables("MySQL", "shardingsphere");
        assertThat(actualShardingSphereSchema.size(), is(2));
        Collection<String> actualPgInformationSchema = SystemSchemaMetadata.getTables("PostgreSQL", "information_schema");
        assertThat(actualPgInformationSchema.size(), is(69));
        Collection<String> actualPgCatalog = SystemSchemaMetadata.getTables("PostgreSQL", "pg_catalog");
        assertThat(actualPgCatalog.size(), is(134));
        Collection<String> actualOgInformationSchema = SystemSchemaMetadata.getTables("openGauss", "information_schema");
        assertThat(actualOgInformationSchema.size(), is(66));
        Collection<String> actualOgPgCatalog = SystemSchemaMetadata.getTables("openGauss", "pg_catalog");
        assertThat(actualOgPgCatalog.size(), is(240));
        
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
        assertTrue(SystemSchemaMetadata.isSystemTable("shardingsphere", "cluster_information"));
        assertTrue(SystemSchemaMetadata.isSystemTable("shardingsphere", "sharding_table_statistics"));
        assertFalse(SystemSchemaMetadata.isSystemTable("shardingsphere", "nonexistent"));
    }
}
