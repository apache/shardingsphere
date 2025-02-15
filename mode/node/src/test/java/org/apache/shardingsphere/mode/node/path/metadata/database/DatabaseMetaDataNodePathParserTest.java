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

package org.apache.shardingsphere.mode.node.path.metadata.database;

import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedSchema;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseMetaDataNodePathParserTest {
    
    @Test
    void assertFindQualifiedSchemaWithNotContainsChildPath() {
        Optional<QualifiedSchema> actual = DatabaseMetaDataNodePathParser.findQualifiedSchema("/metadata/foo_db/schemas/foo_schema", false);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDatabaseName(), is("foo_db"));
        assertThat(actual.get().getSchemaName(), is("foo_schema"));
    }
    
    @Test
    void assertNotFindQualifiedSchemaWithNotContainsChildPath() {
        Optional<QualifiedSchema> actual = DatabaseMetaDataNodePathParser.findQualifiedSchema("/metadata/foo_db/schemas/foo_schema/tables", false);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertFindQualifiedSchemaWithContainsChildPath() {
        Optional<QualifiedSchema> actual = DatabaseMetaDataNodePathParser.findQualifiedSchema("/metadata/foo_db/schemas/foo_schema/tables", true);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDatabaseName(), is("foo_db"));
        assertThat(actual.get().getSchemaName(), is("foo_schema"));
    }
    
    @Test
    void assertNotFindQualifiedSchemaWithContainsChildPath() {
        Optional<QualifiedSchema> actual = DatabaseMetaDataNodePathParser.findQualifiedSchema("/xxx/foo_db/schemas/foo_schema/tables", true);
        assertFalse(actual.isPresent());
    }
}
