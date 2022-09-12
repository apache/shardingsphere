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
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class SystemSchemaBuilderRuleTest {
    
    @Test
    public void assertValueOfSchemaPathSuccess() {
        SystemSchemaBuilderRule actual = SystemSchemaBuilderRule.valueOf(new MySQLDatabaseType().getType(), "information_schema");
        assertThat(actual, is(SystemSchemaBuilderRule.MYSQL_INFORMATION_SCHEMA));
        assertThat(actual.getTables(), is(new HashSet<>(Arrays.asList("columns", "engines", "parameters", "routines", "schemata", "tables", "views"))));
    }
    
    @Test(expected = NullPointerException.class)
    public void assertValueOfSchemaPathFailure() {
        SystemSchemaBuilderRule.valueOf(new MySQLDatabaseType().getType(), "test");
    }
    
    @Test
    public void assertIsisSystemTable() {
        assertTrue(SystemSchemaBuilderRule.isSystemTable("information_schema", "columns"));
        assertTrue(SystemSchemaBuilderRule.isSystemTable("pg_catalog", "pg_database"));
        assertFalse(SystemSchemaBuilderRule.isSystemTable("sharding_db", "t_order"));
    }
}
