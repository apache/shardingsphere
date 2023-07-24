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

package org.apache.shardingsphere.infra.database.mysql;

import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLDatabaseTypeTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Test
    void assertGetQuoteCharacter() {
        assertThat(databaseType.getQuoteCharacter(), is(QuoteCharacter.BACK_QUOTE));
    }
    
    @Test
    void assertGetJdbcUrlPrefixes() {
        assertThat(databaseType.getJdbcUrlPrefixes(), is(Arrays.asList("jdbc:mysql:", "jdbc:mysqlx:")));
    }
    
    @Test
    void assertGetDataSourceMetaData() {
        assertThat(databaseType.getDataSourceMetaData("jdbc:mysql://127.0.0.1/foo_ds", "root"), CoreMatchers.instanceOf(MySQLDataSourceMetaData.class));
    }
    
    @Test
    void assertGetSystemDatabases() {
        assertTrue(databaseType.getSystemDatabaseSchemaMap().containsKey("information_schema"));
        assertTrue(databaseType.getSystemDatabaseSchemaMap().containsKey("performance_schema"));
        assertTrue(databaseType.getSystemDatabaseSchemaMap().containsKey("mysql"));
        assertTrue(databaseType.getSystemDatabaseSchemaMap().containsKey("sys"));
        assertTrue(databaseType.getSystemDatabaseSchemaMap().containsKey("shardingsphere"));
    }
    
    @Test
    void assertGetSystemSchemas() {
        assertThat(databaseType.getSystemSchemas(), is(new HashSet<>(Arrays.asList("information_schema", "performance_schema", "mysql", "sys", "shardingsphere"))));
    }
}
