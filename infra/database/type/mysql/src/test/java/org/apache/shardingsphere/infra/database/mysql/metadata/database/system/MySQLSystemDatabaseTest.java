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

package org.apache.shardingsphere.infra.database.mysql.metadata.database.system;

import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.metadata.database.system.DialectSystemDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLSystemDatabaseTest {
    
    private final DialectSystemDatabase systemDatabase = DatabaseTypedSPILoader.getService(DialectSystemDatabase.class, TypedSPILoader.getService(DatabaseType.class, "MySQL"));
    
    @Test
    void assertGetSystemDatabases() {
        assertTrue(systemDatabase.getSystemDatabaseSchemaMap().containsKey("information_schema"));
        assertTrue(systemDatabase.getSystemDatabaseSchemaMap().containsKey("performance_schema"));
        assertTrue(systemDatabase.getSystemDatabaseSchemaMap().containsKey("mysql"));
        assertTrue(systemDatabase.getSystemDatabaseSchemaMap().containsKey("sys"));
        assertTrue(systemDatabase.getSystemDatabaseSchemaMap().containsKey("shardingsphere"));
    }
    
    @Test
    void assertGetSystemSchemas() {
        assertThat(systemDatabase.getSystemSchemas(), is(new HashSet<>(Arrays.asList("information_schema", "performance_schema", "mysql", "sys", "shardingsphere"))));
    }
}
