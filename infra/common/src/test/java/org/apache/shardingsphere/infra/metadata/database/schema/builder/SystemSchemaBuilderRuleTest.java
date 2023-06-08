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

import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
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
        SystemSchemaBuilderRule actualPgCatalog = SystemSchemaBuilderRule.valueOf(new PostgreSQLDatabaseType().getType(), "pg_catalog");
        assertThat(actualPgCatalog, is(SystemSchemaBuilderRule.POSTGRESQL_PG_CATALOG));
        assertThat(actualPgCatalog.getTables().size(), is(9));
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
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_roles"));
        assertFalse(SystemSchemaBuilderRule.isSystemTable("sharding_db", "t_order"));
    }
}
