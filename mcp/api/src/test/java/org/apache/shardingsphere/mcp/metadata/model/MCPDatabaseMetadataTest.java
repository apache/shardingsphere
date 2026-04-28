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

package org.apache.shardingsphere.mcp.metadata.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPDatabaseMetadataTest {
    
    @Test
    void assertCreateSummary() {
        MCPDatabaseMetadata actual = createDatabaseMetadata().createSummary();
        assertThat(actual.getDatabase(), is("foo_db"));
        assertThat(actual.getDatabaseType(), is("MySQL"));
        assertThat(actual.getDatabaseVersion(), is("8.0"));
        assertTrue(actual.getSchemas().isEmpty());
    }
    
    @Test
    void assertCreateDetail() {
        MCPDatabaseMetadata actual = createDatabaseMetadata().createDetail();
        assertThat(actual.getDatabase(), is("foo_db"));
        assertThat(actual.getDatabaseType(), is("MySQL"));
        assertThat(actual.getDatabaseVersion(), is("8.0"));
        assertThat(actual.getSchemas().get(0).getSchema(), is("bar_schema"));
        assertThat(actual.getSchemas().get(1).getSchema(), is("foo_schema"));
        assertThat(actual.getSchemas().get(1).getTables().get(0).getTable(), is("bar_table"));
        assertTrue(actual.getSchemas().get(1).getTables().get(0).getColumns().isEmpty());
    }
    
    private MCPDatabaseMetadata createDatabaseMetadata() {
        return new MCPDatabaseMetadata("foo_db", "MySQL", "8.0", List.of(new MCPSchemaMetadata("foo_db", "foo_schema",
                List.of(new MCPTableMetadata("foo_db", "foo_schema", "foo_table", List.of(new MCPColumnMetadata("foo_db", "foo_schema", "foo_table", "", "foo_column")), List.of()),
                        new MCPTableMetadata("foo_db", "foo_schema", "bar_table", List.of(), List.of())),
                List.of(new MCPViewMetadata("foo_db", "foo_schema", "foo_view", List.of())),
                List.of(new MCPSequenceMetadata("foo_db", "foo_schema", "foo_sequence"))), new MCPSchemaMetadata("foo_db", "bar_schema", List.of(), List.of(), List.of())));
    }
}
