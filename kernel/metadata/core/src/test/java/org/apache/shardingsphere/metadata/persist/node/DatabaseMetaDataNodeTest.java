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
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseMetaDataNodeTest {
    
    @Test
    void assertGetDatabaseName() {
        Optional<String> actual = DatabaseMetaDataNode.getDatabaseName("/metadata/foo_db");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_db"));
    }
    
    @Test
    void assertGetDatabaseNameBySchemaNode() {
        Optional<String> actual = DatabaseMetaDataNode.getDatabaseNameBySchemaNode("/metadata/foo_db/schemas/foo_schema");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_db"));
    }
    
    @Test
    void assertGetSchemaName() {
        Optional<String> actual = DatabaseMetaDataNode.getSchemaName("/metadata/foo_db/schemas/foo_schema");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_schema"));
    }
    
    @Test
    void assertGetSchemaNameByTableNode() {
        Optional<String> actual = DatabaseMetaDataNode.getSchemaNameByTableNode("/metadata/foo_db/schemas/foo_schema/tables");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_schema"));
    }
}
