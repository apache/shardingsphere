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

package org.apache.shardingsphere.database.connector.core.metadata.manager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemTableManagerTest {
    
    private static ClassLoader originalClassLoader;
    
    private static URLClassLoader schemaClassLoader;
    
    @BeforeAll
    static void setUp() throws Exception {
        originalClassLoader = Thread.currentThread().getContextClassLoader();
        URL[] urls = {
                Paths.get("../dialect/mysql/src/main/resources").toUri().toURL(),
                Paths.get("../dialect/postgresql/src/main/resources").toUri().toURL(),
                Paths.get("../dialect/opengauss/src/main/resources").toUri().toURL(),
                Paths.get("../dialect/firebird/src/main/resources").toUri().toURL()
        };
        schemaClassLoader = new URLClassLoader(urls, originalClassLoader);
        Thread.currentThread().setContextClassLoader(schemaClassLoader);
        Class.forName(SystemTableManager.class.getName(), true, schemaClassLoader);
    }
    
    @AfterAll
    static void tearDown() throws Exception {
        Thread.currentThread().setContextClassLoader(originalClassLoader);
        schemaClassLoader.close();
    }
    
    @Test
    void assertValueOfSchemaPathSuccess() {
        Collection<String> actualInformationSchema = SystemTableManager.getTables("MySQL", "information_schema");
        assertThat(actualInformationSchema.size(), is(95));
        Collection<String> actualMySQLSchema = SystemTableManager.getTables("MySQL", "mysql");
        assertThat(actualMySQLSchema.size(), is(40));
        Collection<String> actualPerformanceSchema = SystemTableManager.getTables("MySQL", "performance_schema");
        assertThat(actualPerformanceSchema.size(), is(114));
        Collection<String> actualSysSchema = SystemTableManager.getTables("MySQL", "sys");
        assertThat(actualSysSchema.size(), is(53));
        Collection<String> actualShardingSphereSchema = SystemTableManager.getTables("MySQL", "shardingsphere");
        assertThat(actualShardingSphereSchema.size(), is(1));
        Collection<String> actualPgInformationSchema = SystemTableManager.getTables("PostgreSQL", "information_schema");
        assertThat(actualPgInformationSchema.size(), is(69));
        Collection<String> actualPgCatalog = SystemTableManager.getTables("PostgreSQL", "pg_catalog");
        assertThat(actualPgCatalog.size(), is(134));
        Collection<String> actualOgInformationSchema = SystemTableManager.getTables("openGauss", "information_schema");
        assertThat(actualOgInformationSchema.size(), is(66));
        Collection<String> actualOgPgCatalog = SystemTableManager.getTables("openGauss", "pg_catalog");
        assertThat(actualOgPgCatalog.size(), is(240));
        Collection<String> actualFbCatalog = SystemTableManager.getTables("Firebird", "system_tables");
        assertThat(actualFbCatalog.size(), is(50));
    }
    
    @Test
    void assertIsSystemTable() {
        assertTrue(SystemTableManager.isSystemTable("information_schema", "columns"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_database"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_tables"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_aggregate"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_am"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_amop"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_amproc"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_attrdef"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_attribute"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_auth_members"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_authid"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_available_extension_versions"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_available_extensions"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_backend_memory_contexts"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_cast"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_range"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_replication_origin"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_rewrite"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_seclabel"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_sequence"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_roles"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_user_mapping"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_stat_database_conflicts"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_stat_gssapi"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_stat_progress_analyze"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_stat_progress_basebackup"));
        assertTrue(SystemTableManager.isSystemTable("pg_catalog", "pg_stat_progress_cluster"));
        assertFalse(SystemTableManager.isSystemTable("sharding_db", "t_order"));
        assertTrue(SystemTableManager.isSystemTable("shardingsphere", "cluster_information"));
        assertFalse(SystemTableManager.isSystemTable("shardingsphere", "nonexistent"));
        assertTrue(SystemTableManager.isSystemTable("system_tables", "RDB$functions"));
        assertTrue(SystemTableManager.isSystemTable("system_tables", "RDB$RELATIONS"));
        assertFalse(SystemTableManager.isSystemTable("system_tables", "RDB$REL"));
    }
}
