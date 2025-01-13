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

package org.apache.shardingsphere.mode.node.path.metadata;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableMetaDataNodePathTest {
    
    @Test
    void assertGetMetaDataTablesPath() {
        assertThat(TableMetaDataNodePath.getMetaDataTablesPath("foo_db", "foo_schema"), is("/metadata/foo_db/schemas/foo_schema/tables"));
    }
    
    @Test
    void assertGetTableActiveVersionPath() {
        assertThat(TableMetaDataNodePath.getTableActiveVersionPath("foo_db", "foo_schema", "foo_tbl"), is("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version"));
    }
    
    @Test
    void assertGetTableVersionsPath() {
        assertThat(TableMetaDataNodePath.getTableVersionsPath("foo_db", "foo_schema", "foo_tbl"), is("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions"));
    }
    
    @Test
    void assertGetTableVersionPath() {
        assertThat(TableMetaDataNodePath.getTableVersionPath("foo_db", "foo_schema", "foo_tbl", "0"), is("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/0"));
    }
    
    @Test
    void assertGetTablePath() {
        assertThat(TableMetaDataNodePath.getTablePath("foo_db", "foo_schema", "foo_tbl"), is("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl"));
    }
    
    @Test
    void assertFindTableNameByActiveVersionPath() {
        Optional<String> actual = TableMetaDataNodePath.getTableNameByActiveVersionPath("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_tbl"));
    }
    
    @Test
    void assertFindTableNameByActiveVersionPathIfNotFound() {
        assertFalse(TableMetaDataNodePath.getTableNameByActiveVersionPath("/xxx/foo_db/schemas/foo_schema/tables/foo_tbl/active_version").isPresent());
    }
    
    @Test
    void assertFindTableName() {
        Optional<String> actual = TableMetaDataNodePath.findTableName("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_tbl"));
    }
    
    @Test
    void assertFindTableNameIfNotFound() {
        assertFalse(TableMetaDataNodePath.findTableName("/xxx/foo_db/schemas/foo_schema/tables/foo_tbl").isPresent());
    }
    
    @Test
    void assertIsTableActiveVersionPath() {
        assertTrue(TableMetaDataNodePath.isTableActiveVersionPath("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version"));
    }
    
    @Test
    void assertIsTablePath() {
        assertTrue(TableMetaDataNodePath.isTablePath("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl"));
    }
}
