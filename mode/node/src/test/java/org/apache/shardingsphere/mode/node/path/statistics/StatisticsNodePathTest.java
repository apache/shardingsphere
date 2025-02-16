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

package org.apache.shardingsphere.mode.node.path.statistics;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class StatisticsNodePathTest {
    
    @Test
    void assertGetDatabasesRootPath() {
        assertThat(StatisticsNodePathGenerator.getDatabasesRootPath(), is("/statistics/databases"));
    }
    
    @Test
    void assertGetDatabasePath() {
        assertThat(StatisticsNodePathGenerator.getDatabasePath("foo_db"), is("/statistics/databases/foo_db"));
    }
    
    @Test
    void assertGetSchemaRootPath() {
        assertThat(StatisticsNodePathGenerator.getSchemaRootPath("foo_db"), is("/statistics/databases/foo_db/schemas"));
    }
    
    @Test
    void assertGetSchemaPath() {
        assertThat(StatisticsNodePathGenerator.getSchemaPath("foo_db", "db_schema"), is("/statistics/databases/foo_db/schemas/db_schema"));
    }
    
    @Test
    void assertGetTableRootPath() {
        assertThat(StatisticsNodePathGenerator.getTableRootPath("foo_db", "db_schema"), is("/statistics/databases/foo_db/schemas/db_schema/tables"));
    }
    
    @Test
    void assertGetTablePath() {
        assertThat(StatisticsNodePathGenerator.getTablePath("foo_db", "db_schema", "tbl_name"), is("/statistics/databases/foo_db/schemas/db_schema/tables/tbl_name"));
    }
    
    @Test
    void assertGetTableRowPath() {
        assertThat(StatisticsNodePathGenerator.getTableRowPath("foo_db", "db_schema", "tbl_name", "key"), is("/statistics/databases/foo_db/schemas/db_schema/tables/tbl_name/key"));
    }
    
    @Test
    void assertGetJobPath() {
        assertThat(StatisticsNodePathGenerator.getJobPath(), is("/statistics/job"));
    }
}
