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

package org.apache.shardingsphere.mode.node.path;

import org.apache.shardingsphere.mode.node.path.metadata.ShardingSphereDataNodePath;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShardingSphereDataNodePathTest {
    
    @Test
    void assertGetDatabasesRootPath() {
        assertThat(ShardingSphereDataNodePath.getDatabasesRootPath(), is("/statistics/databases"));
    }
    
    @Test
    void assertGetDatabasePath() {
        assertThat(ShardingSphereDataNodePath.getDatabasePath("foo_db"), is("/statistics/databases/foo_db"));
    }
    
    @Test
    void assertGetSchemaRootPath() {
        assertThat(ShardingSphereDataNodePath.getSchemaRootPath("foo_db"), is("/statistics/databases/foo_db/schemas"));
    }
    
    @Test
    void assertGetSchemaPath() {
        assertThat(ShardingSphereDataNodePath.getSchemaPath("foo_db", "db_schema"), is("/statistics/databases/foo_db/schemas/db_schema"));
    }
    
    @Test
    void assertGetTableRootPath() {
        assertThat(ShardingSphereDataNodePath.getTableRootPath("foo_db", "db_schema"), is("/statistics/databases/foo_db/schemas/db_schema/tables"));
    }
    
    @Test
    void assertGetTablePath() {
        assertThat(ShardingSphereDataNodePath.getTablePath("foo_db", "db_schema", "tbl_name"), is("/statistics/databases/foo_db/schemas/db_schema/tables/tbl_name"));
    }
    
    @Test
    void assertGetTableRowPath() {
        assertThat(ShardingSphereDataNodePath.getTableRowPath("foo_db", "db_schema", "tbl_name", "key"), is("/statistics/databases/foo_db/schemas/db_schema/tables/tbl_name/key"));
    }
    
    @Test
    void assertFindDatabaseNameWithNotContainsChildPath() {
        assertThat(ShardingSphereDataNodePath.findDatabaseName("/statistics/databases/foo_db", false), is(Optional.of("foo_db")));
        assertThat(ShardingSphereDataNodePath.findDatabaseName("/statistics/databases", false), is(Optional.empty()));
    }
    
    @Test
    void assertFindDatabaseNameWithContainsChildPath() {
        assertThat(ShardingSphereDataNodePath.findDatabaseName("/statistics/databases/foo_db", true), is(Optional.of("foo_db")));
        assertThat(ShardingSphereDataNodePath.findDatabaseName("/statistics/databases/foo_db/schemas/db_schema", true), is(Optional.of("foo_db")));
        assertThat(ShardingSphereDataNodePath.findDatabaseName("/statistics/databases", true), is(Optional.empty()));
    }
    
    @Test
    void assertFindSchemaNameWithNotContainsChildPath() {
        assertThat(ShardingSphereDataNodePath.findSchemaName("/statistics/databases/foo_db/schemas/foo_schema", false), is(Optional.of("foo_schema")));
        assertThat(ShardingSphereDataNodePath.findSchemaName("/statistics/databases/foo_db", false), is(Optional.empty()));
    }
    
    @Test
    void assertFindSchemaNameWithContainsChildPath() {
        assertThat(ShardingSphereDataNodePath.findSchemaName("/statistics/databases/foo_db/schemas/foo_schema", true), is(Optional.of("foo_schema")));
        assertThat(ShardingSphereDataNodePath.findSchemaName("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl", true), is(Optional.of("foo_schema")));
        assertThat(ShardingSphereDataNodePath.findSchemaName("/statistics/databases/foo_db", true), is(Optional.empty()));
    }
    
    @Test
    void assertFindTableNameWithNotContainsChildPath() {
        assertThat(ShardingSphereDataNodePath.findTableName("/statistics/databases/foo_db/schemas/foo_schema/tables/tbl_name", false), is(Optional.of("tbl_name")));
        assertThat(ShardingSphereDataNodePath.findTableName("/statistics/databases/foo_db/schemas/foo_schema", false), is(Optional.empty()));
    }
    
    @Test
    void assertFindTableNameWithContainsChildPath() {
        assertThat(ShardingSphereDataNodePath.findTableName("/statistics/databases/foo_db/schemas/foo_schema/tables/tbl_name", true), is(Optional.of("tbl_name")));
        assertThat(ShardingSphereDataNodePath.findTableName("/statistics/databases/foo_db/schemas/foo_schema/tables/tbl_name/key", true), is(Optional.of("tbl_name")));
        assertThat(ShardingSphereDataNodePath.findTableName("/statistics/databases/foo_db/schemas/foo_schema/tables", true), is(Optional.empty()));
    }
    
    @Test
    void assertFindRowUniqueKey() {
        assertThat(ShardingSphereDataNodePath.findRowUniqueKey("/statistics/databases/foo_db/schemas/foo_schema/tables/tbl_name/key"), is(Optional.of("key")));
        assertThat(ShardingSphereDataNodePath.findRowUniqueKey("/statistics/databases/foo_db/schemas/foo_schema/tables/tbl_name"), is(Optional.empty()));
    }
    
    @Test
    void assertGetJobPath() {
        assertThat(ShardingSphereDataNodePath.getJobPath(), is("/statistics/job"));
    }
}
