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

import org.apache.shardingsphere.mode.node.path.metadata.DatabaseMetaDataNodePath;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseMetaDataNodePathTest {
    
    @Test
    void assertGetRootPath() {
        MatcherAssert.assertThat(DatabaseMetaDataNodePath.getRootPath(), is("/metadata"));
    }
    
    @Test
    void assertGetDatabasePath() {
        assertThat(DatabaseMetaDataNodePath.getDatabasePath("foo_db"), is("/metadata/foo_db"));
    }
    
    @Test
    void assertGetSchemaRootPath() {
        assertThat(DatabaseMetaDataNodePath.getSchemaRootPath("foo_db"), is("/metadata/foo_db/schemas"));
    }
    
    @Test
    void assertGetSchemaPath() {
        assertThat(DatabaseMetaDataNodePath.getSchemaPath("foo_db", "foo_schema"), is("/metadata/foo_db/schemas/foo_schema"));
    }
    
    @Test
    void assertGetTableRootPath() {
        assertThat(DatabaseMetaDataNodePath.getTableRootPath("foo_db", "foo_schema"), is("/metadata/foo_db/schemas/foo_schema/tables"));
    }
    
    @Test
    void assertGetVersionPath() {
        assertThat(DatabaseMetaDataNodePath.getVersionPath("foo_rule", "1"), is("foo_rule/1"));
    }
    
    @Test
    void assertFindDatabaseNameWithNotContainsChildPath() {
        Optional<String> actual = DatabaseMetaDataNodePath.findDatabaseName("/metadata/foo_db", false);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_db"));
    }
    
    @Test
    void assertNotFindDatabaseNameWithNotContainsChildPath() {
        Optional<String> actual = DatabaseMetaDataNodePath.findDatabaseName("/metadata/foo_db/schemas/foo_schema", false);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertFindDatabaseNameWithContainsChildPath() {
        Optional<String> actual = DatabaseMetaDataNodePath.findDatabaseName("/metadata/foo_db/schemas/foo_schema", true);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_db"));
    }
    
    @Test
    void assertNotFindDatabaseNameWithContainsChildPath() {
        Optional<String> actual = DatabaseMetaDataNodePath.findDatabaseName("/xxx/foo_db/schemas/foo_schema", true);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertFindSchemaNameWithNotContainsChildPath() {
        Optional<String> actual = DatabaseMetaDataNodePath.findSchemaName("/metadata/foo_db/schemas/foo_schema", false);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_schema"));
    }
    
    @Test
    void assertNotFindSchemaNameWithNotContainsChildPath() {
        Optional<String> actual = DatabaseMetaDataNodePath.findSchemaName("/metadata/foo_db/schemas/foo_schema/tables", false);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertFindSchemaNameWithContainsChildPath() {
        Optional<String> actual = DatabaseMetaDataNodePath.findSchemaName("/metadata/foo_db/schemas/foo_schema/tables", true);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_schema"));
    }
    
    @Test
    void assertNotFindSchemaNameWithContainsChildPath() {
        Optional<String> actual = DatabaseMetaDataNodePath.findSchemaName("/xxx/foo_db/schemas/foo_schema/tables", true);
        assertFalse(actual.isPresent());
    }
}
