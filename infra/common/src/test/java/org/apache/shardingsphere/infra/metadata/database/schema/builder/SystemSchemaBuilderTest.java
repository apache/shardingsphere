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
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemSchemaBuilderTest {
    
    @Test
    void assertBuildForMySQL() {
        Map<String, ShardingSphereSchema> actualInformationSchema = SystemSchemaBuilder.build("information_schema", new MySQLDatabaseType());
        assertThat(actualInformationSchema.size(), is(1));
        assertTrue(actualInformationSchema.containsKey("information_schema"));
        assertThat(actualInformationSchema.get("information_schema").getTables().size(), is(61));
        Map<String, ShardingSphereSchema> actualMySQLSchema = SystemSchemaBuilder.build("mysql", new MySQLDatabaseType());
        assertThat(actualMySQLSchema.size(), is(1));
        assertTrue(actualMySQLSchema.containsKey("mysql"));
        assertThat(actualMySQLSchema.get("mysql").getTables().size(), is(31));
        Map<String, ShardingSphereSchema> actualPerformanceSchema = SystemSchemaBuilder.build("performance_schema", new MySQLDatabaseType());
        assertThat(actualPerformanceSchema.size(), is(1));
        assertTrue(actualPerformanceSchema.containsKey("performance_schema"));
        assertThat(actualPerformanceSchema.get("performance_schema").getTables().size(), is(87));
    }
    
    @Test
    void assertBuildForPostgreSQL() {
        Map<String, ShardingSphereSchema> actual = SystemSchemaBuilder.build("sharding_db", new PostgreSQLDatabaseType());
        assertThat(actual.size(), is(3));
        assertTrue(actual.containsKey("information_schema"));
        assertTrue(actual.containsKey("pg_catalog"));
        assertTrue(actual.containsKey("shardingsphere"));
        assertThat(actual.get("information_schema").getTables().size(), is(3));
        assertThat(actual.get("pg_catalog").getTables().size(), is(24));
        assertThat(actual.get("shardingsphere").getTables().size(), is(2));
    }
    
    @Test
    void assertBuildForOpenGaussSQL() {
        Map<String, ShardingSphereSchema> actual = SystemSchemaBuilder.build("sharding_db", new OpenGaussDatabaseType());
        assertThat(actual.size(), is(16));
        assertTrue(actual.containsKey("pg_catalog"));
        assertTrue(actual.containsKey("shardingsphere"));
        assertThat(actual.get("pg_catalog").getTables().size(), is(2));
        assertThat(actual.get("shardingsphere").getTables().size(), is(2));
    }
}
