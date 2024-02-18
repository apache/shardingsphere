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

class ShardingSphereDataNodeTest {
    
    @Test
    void assertGetShardingSphereDataNodePath() {
        assertThat(ShardingSphereDataNode.getShardingSphereDataNodePath(), is("/statistics"));
    }
    
    @Test
    void assertGetDatabaseNamePath() {
        assertThat(ShardingSphereDataNode.getDatabaseNamePath("db_path"), is("/statistics/db_path"));
    }
    
    @Test
    void assertGetTablesPath() {
        assertThat(ShardingSphereDataNode.getTablesPath("db_name", "db_schema"), is("/statistics/db_name/schemas/db_schema/tables"));
    }
    
    @Test
    void assertGetSchemaDataPath() {
        assertThat(ShardingSphereDataNode.getSchemaDataPath("db_name", "db_schema"), is("/statistics/db_name/schemas/db_schema"));
    }
    
    @Test
    void assertGetSchemasPath() {
        assertThat(ShardingSphereDataNode.getSchemasPath("db_name"), is("/statistics/db_name/schemas"));
    }
    
    @Test
    void assertGetTablePath() {
        assertThat(ShardingSphereDataNode.getTablePath("db_name", "db_schema", "tbl_name"), is("/statistics/db_name/schemas/db_schema/tables/tbl_name"));
    }
    
    @Test
    void assertGetTableRowPath() {
        assertThat(ShardingSphereDataNode.getTableRowPath("db_name", "db_schema", "tbl_name", "key"), is("/statistics/db_name/schemas/db_schema/tables/tbl_name/key"));
    }
    
    @Test
    void assertGetDatabaseNameHappyPath() {
        assertThat(ShardingSphereDataNode.getDatabaseName("/statistics/db_name"), is(Optional.of("db_name")));
    }
    
    @Test
    void assertGetDatabaseNameDbNameNotFoundScenario() {
        assertThat(ShardingSphereDataNode.getDatabaseName("/statistics"), is(Optional.empty()));
    }
    
    @Test
    void assertGetSchemaNameHappyPath() {
        assertThat(ShardingSphereDataNode.getSchemaName("/statistics/db_name/schemas/db_schema"), is(Optional.of("db_schema")));
    }
    
    @Test
    void assertGetSchemaNameSchemaNameNotFoundScenario() {
        assertThat(ShardingSphereDataNode.getSchemaName("/statistics/db_name"), is(Optional.empty()));
    }
    
    @Test
    void assertGetDatabaseNameByDatabasePathHappyPath() {
        assertThat(ShardingSphereDataNode.getDatabaseNameByDatabasePath("/statistics/db_name"), is(Optional.of("db_name")));
    }
    
    @Test
    void assertGetDatabaseNameByDatabasePathDbNameNotFoundScenario() {
        assertThat(ShardingSphereDataNode.getDatabaseNameByDatabasePath("/statistics"), is(Optional.empty()));
    }
    
    @Test
    void assertGetSchemaNameBySchemaPathHappyPath() {
        assertThat(ShardingSphereDataNode.getSchemaNameBySchemaPath("/statistics/db_name/schemas/db_schema"), is(Optional.of("db_schema")));
    }
    
    @Test
    void assertGetSchemaNameBySchemaPathSchemaNameNotFoundScenario() {
        assertThat(ShardingSphereDataNode.getSchemaNameBySchemaPath("/statistics/db_name"), is(Optional.empty()));
    }
    
    @Test
    void assertGetTableNameHappyPath() {
        assertThat(ShardingSphereDataNode.getTableName("/statistics/db_name/schemas/db_schema/tables/tbl_name"), is(Optional.of("tbl_name")));
    }
    
    @Test
    void assertGetTableNameTableNameNotFoundScenario() {
        assertThat(ShardingSphereDataNode.getTableName("/statistics/db_name/schemas/db_schema"), is(Optional.empty()));
    }
    
    @Test
    void assertGetTableNameByRowPathHappyPath() {
        assertThat(ShardingSphereDataNode.getTableNameByRowPath("/statistics/db_name/schemas/db_schema/tables/tbl_name"), is(Optional.of("tbl_name")));
    }
    
    @Test
    void assertGetTableNameByRowPathTableNameNotFoundScenario() {
        assertThat(ShardingSphereDataNode.getTableNameByRowPath("/statistics/db_name/schemas/db_schema"), is(Optional.empty()));
    }
    
    @Test
    void assertGetRowUniqueKeyHappyPath() {
        assertThat(ShardingSphereDataNode.getRowUniqueKey("/statistics/db_name/schemas/db_schema/tables/tbl_name/key"), is(Optional.of("key")));
    }
    
    @Test
    void assertGetRowUniqueKeyUniqueKeyNotFoundScenario() {
        assertThat(ShardingSphereDataNode.getRowUniqueKey("/statistics/db_name/schemas/db_schema/tables/tbl_name"), is(Optional.empty()));
    }
}
