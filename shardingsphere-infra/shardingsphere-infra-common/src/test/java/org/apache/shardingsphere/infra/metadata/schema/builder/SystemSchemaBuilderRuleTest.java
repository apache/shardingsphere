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

package org.apache.shardingsphere.infra.metadata.schema.builder;

import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SystemSchemaBuilderRuleTest {
    
    @Test
    public void assertValueOfSchemaPathSuccess() {
        String databaseName = new MySQLDatabaseType().getName().toLowerCase();
        String schemaName = "information_schema";
        SystemSchemaBuilderRule actual = SystemSchemaBuilderRule.valueOf(databaseName, schemaName);
        assertThat(actual, is(SystemSchemaBuilderRule.MYSQL_INFORMATION_SCHEMA));
        assertThat(actual.getTables(), is(Arrays.asList("columns", "tables", "views")));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertValueOfSchemaPathFailure() {
        String databaseName = new MySQLDatabaseType().getName().toLowerCase();
        String schemaName = "test";
        SystemSchemaBuilderRule.valueOf(databaseName, schemaName);
    }
}
