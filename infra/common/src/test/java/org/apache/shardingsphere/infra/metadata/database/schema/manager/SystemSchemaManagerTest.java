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

import java.util.Collection;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SystemSchemaManagerTest {
    
    @Test
    void assertGetAllInputStreams() {
        Collection<InputStream> actualInformationSchema = SystemSchemaManager.getAllInputStreams("MySQL", "information_schema");
        assertThat(actualInformationSchema.size(), is(95));
        Collection<InputStream> actualMySQLSchema = SystemSchemaManager.getAllInputStreams("MySQL", "mysql");
        assertThat(actualMySQLSchema.size(), is(40));
        Collection<InputStream> actualPerformanceSchema = SystemSchemaManager.getAllInputStreams("MySQL", "performance_schema");
        assertThat(actualPerformanceSchema.size(), is(114));
        Collection<InputStream> actualSysSchema = SystemSchemaManager.getAllInputStreams("MySQL", "sys");
        assertThat(actualSysSchema.size(), is(53));
        Collection<InputStream> actualShardingSphereSchema = SystemSchemaManager.getAllInputStreams("MySQL", "shardingsphere");
        assertThat(actualShardingSphereSchema.size(), is(1));
        Collection<InputStream> actualPgInformationSchema = SystemSchemaManager.getAllInputStreams("PostgreSQL", "information_schema");
        assertThat(actualPgInformationSchema.size(), is(69));
        Collection<InputStream> actualPgCatalog = SystemSchemaManager.getAllInputStreams("PostgreSQL", "pg_catalog");
        assertThat(actualPgCatalog.size(), is(134));
        Collection<InputStream> actualOgInformationSchema = SystemSchemaManager.getAllInputStreams("openGauss", "information_schema");
        assertThat(actualOgInformationSchema.size(), is(66));
        Collection<InputStream> actualOgPgCatalog = SystemSchemaManager.getAllInputStreams("openGauss", "pg_catalog");
        assertThat(actualOgPgCatalog.size(), is(240));
    }
}
