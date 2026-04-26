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

class MCPSchemaMetadataTest {
    
    @Test
    void assertNewWithDefaultSequences() {
        MCPSchemaMetadata actual = new MCPSchemaMetadata("foo_db", "foo_schema", List.of(), List.of());
        assertThat(actual.getDatabase(), is("foo_db"));
        assertThat(actual.getSchema(), is("foo_schema"));
        assertTrue(actual.getTables().isEmpty());
        assertTrue(actual.getViews().isEmpty());
        assertTrue(actual.getSequences().isEmpty());
    }
    
    @Test
    void assertCreateSummary() {
        MCPSchemaMetadata actual = createSchemaMetadata().createSummary();
        assertThat(actual.getDatabase(), is("foo_db"));
        assertThat(actual.getSchema(), is("foo_schema"));
        assertTrue(actual.getTables().isEmpty());
        assertTrue(actual.getViews().isEmpty());
        assertTrue(actual.getSequences().isEmpty());
    }
    
    @Test
    void assertCreateDetail() {
        MCPSchemaMetadata actual = createSchemaMetadata().createDetail();
        assertThat(actual.getDatabase(), is("foo_db"));
        assertThat(actual.getSchema(), is("foo_schema"));
        assertThat(actual.getTables().get(0).getTable(), is("bar_table"));
        assertThat(actual.getTables().get(1).getTable(), is("foo_table"));
        assertTrue(actual.getTables().get(0).getColumns().isEmpty());
        assertThat(actual.getViews().get(0).getView(), is("bar_view"));
        assertThat(actual.getViews().get(1).getView(), is("foo_view"));
        assertTrue(actual.getViews().get(0).getColumns().isEmpty());
        assertThat(actual.getSequences().get(0).getSequence(), is("bar_sequence"));
        assertThat(actual.getSequences().get(1).getSequence(), is("foo_sequence"));
    }
    
    private MCPSchemaMetadata createSchemaMetadata() {
        return new MCPSchemaMetadata("foo_db", "foo_schema",
                List.of(new MCPTableMetadata("foo_db", "foo_schema", "foo_table", List.of(new MCPColumnMetadata("foo_db", "foo_schema", "foo_table", "", "foo_column")), List.of()),
                        new MCPTableMetadata("foo_db", "foo_schema", "bar_table", List.of(), List.of())),
                List.of(new MCPViewMetadata("foo_db", "foo_schema", "foo_view", List.of(new MCPColumnMetadata("foo_db", "foo_schema", "", "foo_view", "foo_column"))),
                        new MCPViewMetadata("foo_db", "foo_schema", "bar_view", List.of())),
                List.of(new MCPSequenceMetadata("foo_db", "foo_schema", "foo_sequence"), new MCPSequenceMetadata("foo_db", "foo_schema", "bar_sequence")));
    }
}
