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

package org.apache.shardingsphere.metadata.persist.node;

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
    void assertGetSchemaDataPath() {
        assertThat(ShardingSphereDataNodePath.getSchemaDataPath("foo_db", "db_schema"), is("/statistics/databases/foo_db/schemas/db_schema"));
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
    void assertFindDatabaseNameHappyPath() {
        assertThat(ShardingSphereDataNodePath.findDatabaseName("/statistics/databases/foo_db"), is(Optional.of("foo_db")));
    }
    
    @Test
    void assertFindDatabaseNameDbNameNotFoundScenario() {
        assertThat(ShardingSphereDataNodePath.findDatabaseName("/statistics/databases"), is(Optional.empty()));
    }
    
    @Test
    void assertFindSchemaNameHappyPath() {
        assertThat(ShardingSphereDataNodePath.findSchemaName("/statistics/databases/foo_db/schemas/db_schema"), is(Optional.of("db_schema")));
    }
    
    @Test
    void assertFindSchemaNameSchemaNameNotFoundScenario() {
        assertThat(ShardingSphereDataNodePath.findSchemaName("/statistics/databases/foo_db"), is(Optional.empty()));
    }
    
    @Test
    void assertFindDatabaseNameByDatabasePathHappyPath() {
        assertThat(ShardingSphereDataNodePath.getDatabaseNameByDatabasePath("/statistics/databases/foo_db"), is(Optional.of("foo_db")));
    }
    
    @Test
    void assertFindDatabaseNameByDatabasePathDbNameNotFoundScenario() {
        assertThat(ShardingSphereDataNodePath.getDatabaseNameByDatabasePath("/statistics/databases"), is(Optional.empty()));
    }
    
    @Test
    void assertFindSchemaNameBySchemaPathHappyPath() {
        assertThat(ShardingSphereDataNodePath.getSchemaNameBySchemaPath("/statistics/databases/foo_db/schemas/db_schema"), is(Optional.of("db_schema")));
    }
    
    @Test
    void assertFindSchemaNameBySchemaPathSchemaNameNotFoundScenario() {
        assertThat(ShardingSphereDataNodePath.getSchemaNameBySchemaPath("/statistics//databasesdb_name"), is(Optional.empty()));
    }
    
    @Test
    void assertGetTableNameHappyPath() {
        assertThat(ShardingSphereDataNodePath.getTableName("/statistics/databases/foo_db/schemas/db_schema/tables/tbl_name"), is(Optional.of("tbl_name")));
    }
    
    @Test
    void assertGetTableNameTableNameNotFoundScenario() {
        assertThat(ShardingSphereDataNodePath.getTableName("/statistics/databases/foo_db/schemas/db_schema"), is(Optional.empty()));
    }
    
    @Test
    void assertGetTableNameByRowPathHappyPath() {
        assertThat(ShardingSphereDataNodePath.getTableNameByRowPath("/statistics/databases/foo_db/schemas/db_schema/tables/tbl_name"), is(Optional.of("tbl_name")));
    }
    
    @Test
    void assertGetTableNameByRowPathTableNameNotFoundScenario() {
        assertThat(ShardingSphereDataNodePath.getTableNameByRowPath("/statistics/databases/foo_db/schemas/db_schema"), is(Optional.empty()));
    }
    
    @Test
    void assertGetRowUniqueKeyHappyPath() {
        assertThat(ShardingSphereDataNodePath.getRowUniqueKey("/statistics/databases/foo_db/schemas/db_schema/tables/tbl_name/key"), is(Optional.of("key")));
    }
    
    @Test
    void assertGetRowUniqueKeyUniqueKeyNotFoundScenario() {
        assertThat(ShardingSphereDataNodePath.getRowUniqueKey("/statistics/databases/foo_db/schemas/db_schema/tables/tbl_name"), is(Optional.empty()));
    }
    
    @Test
    void assertGetJobPath() {
        assertThat(ShardingSphereDataNodePath.getJobPath(), is("/statistics/job"));
    }
}
