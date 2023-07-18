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

import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.opengauss.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.PostgreSQLDatabaseType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemSchemaBuilderRuleTest {
    
    @Test
    void assertValueOfSchemaPathSuccess() {
        SystemSchemaBuilderRule actualInformationSchema = SystemSchemaBuilderRule.valueOf(new MySQLDatabaseType().getType(), "information_schema");
        assertThat(actualInformationSchema, is(SystemSchemaBuilderRule.MYSQL_INFORMATION_SCHEMA));
        assertThat(actualInformationSchema.getTables().size(), is(61));
        SystemSchemaBuilderRule actualMySQLSchema = SystemSchemaBuilderRule.valueOf(new MySQLDatabaseType().getType(), "mysql");
        assertThat(actualMySQLSchema, is(SystemSchemaBuilderRule.MYSQL_MYSQL));
        assertThat(actualMySQLSchema.getTables().size(), is(31));
        SystemSchemaBuilderRule actualPerformanceSchema = SystemSchemaBuilderRule.valueOf(new MySQLDatabaseType().getType(), "performance_schema");
        assertThat(actualPerformanceSchema, is(SystemSchemaBuilderRule.MYSQL_PERFORMANCE_SCHEMA));
        assertThat(actualPerformanceSchema.getTables().size(), is(87));
        SystemSchemaBuilderRule actualSysSchema = SystemSchemaBuilderRule.valueOf(new MySQLDatabaseType().getType(), "sys");
        assertThat(actualSysSchema, is(SystemSchemaBuilderRule.MYSQL_SYS));
        assertThat(actualSysSchema.getTables().size(), is(53));
        SystemSchemaBuilderRule actualPgInformationSchema = SystemSchemaBuilderRule.valueOf(new PostgreSQLDatabaseType().getType(), "information_schema");
        assertThat(actualPgInformationSchema, is(SystemSchemaBuilderRule.POSTGRESQL_INFORMATION_SCHEMA));
        assertThat(actualPgInformationSchema.getTables().size(), is(69));
        SystemSchemaBuilderRule actualPgCatalog = SystemSchemaBuilderRule.valueOf(new PostgreSQLDatabaseType().getType(), "pg_catalog");
        assertThat(actualPgCatalog, is(SystemSchemaBuilderRule.POSTGRESQL_PG_CATALOG));
        assertThat(actualPgCatalog.getTables().size(), is(134));
        SystemSchemaBuilderRule actualOgInformationSchema = SystemSchemaBuilderRule.valueOf(new OpenGaussDatabaseType().getType(), "information_schema");
        assertThat(actualOgInformationSchema, is(SystemSchemaBuilderRule.OPEN_GAUSS_INFORMATION_SCHEMA));
        assertThat(actualOgInformationSchema.getTables().size(), is(66));
        SystemSchemaBuilderRule actualOgPgCatalog = SystemSchemaBuilderRule.valueOf(new OpenGaussDatabaseType().getType(), "pg_catalog");
        assertThat(actualOgPgCatalog, is(SystemSchemaBuilderRule.OPEN_GAUSS_PG_CATALOG));
        assertThat(actualOgPgCatalog.getTables().size(), is(240));
    }
    
    @Test
    void assertValueOfSchemaPathFailure() {
        assertThrows(NullPointerException.class, () -> SystemSchemaBuilderRule.valueOf(new MySQLDatabaseType().getType(), "test"));
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
