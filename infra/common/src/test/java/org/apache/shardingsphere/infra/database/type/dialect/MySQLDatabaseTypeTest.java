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

package org.apache.shardingsphere.infra.database.type.dialect;

import org.apache.shardingsphere.infra.database.metadata.dialect.MySQLDataSourceMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class MySQLDatabaseTypeTest {
    
    @Test
    public void assertGetQuoteCharacter() {
        assertThat(new MySQLDatabaseType().getQuoteCharacter(), is(QuoteCharacter.BACK_QUOTE));
    }
    
    @Test
    public void assertGetJdbcUrlPrefixes() {
        assertThat(new MySQLDatabaseType().getJdbcUrlPrefixes(), is(Arrays.asList("jdbc:mysql:", "jdbc:mysqlx:")));
    }
    
    @Test
    public void assertGetDataSourceMetaData() {
        assertThat(new MySQLDatabaseType().getDataSourceMetaData("jdbc:mysql://127.0.0.1/foo_ds", "root"), instanceOf(MySQLDataSourceMetaData.class));
    }
    
    @Test
    public void assertGetSystemDatabases() {
        assertTrue(new MySQLDatabaseType().getSystemDatabaseSchemaMap().containsKey("information_schema"));
        assertTrue(new MySQLDatabaseType().getSystemDatabaseSchemaMap().containsKey("performance_schema"));
        assertTrue(new MySQLDatabaseType().getSystemDatabaseSchemaMap().containsKey("mysql"));
        assertTrue(new MySQLDatabaseType().getSystemDatabaseSchemaMap().containsKey("sys"));
        assertTrue(new MySQLDatabaseType().getSystemDatabaseSchemaMap().containsKey("shardingsphere"));
    }
    
    @Test
    public void assertGetSystemSchemas() {
        assertThat(new MySQLDatabaseType().getSystemSchemas(), is(new HashSet<>(Arrays.asList("information_schema", "performance_schema", "mysql", "sys", "shardingsphere"))));
    }
}
