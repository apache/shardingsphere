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

package org.apache.shardingsphere.mcp.metadata.query;

import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.resource.ResourceTestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataQueryServiceTest {
    
    private final MetadataQueryService metadataQueryService = new MetadataQueryService(ResourceTestDataFactory.createDatabaseMetadataCatalog());
    
    @Test
    void assertQueryDatabases() {
        List<MCPDatabaseMetadata> actual = metadataQueryService.queryDatabases();
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getDatabase(), is("logic_db"));
        assertThat(actual.get(1).getDatabase(), is("warehouse"));
    }
    
    @Test
    void assertQueryDatabase() {
        Optional<MCPDatabaseMetadata> actual = metadataQueryService.queryDatabase("logic_db");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDatabase(), is("logic_db"));
        assertThat(actual.get().getSchemas().get(0).getSchema(), is("public"));
    }
    
    @Test
    void assertQueryTablesBySchema() {
        List<MCPTableMetadata> actual = metadataQueryService.queryTables("logic_db", "public");
        assertThat(actual.size(), is(2));
        assertThat(actual.get(1).getTable(), is("orders"));
    }
    
    @Test
    void assertQueryTableColumn() {
        Optional<MCPColumnMetadata> actual = metadataQueryService.queryTableColumn("logic_db", "public", "orders", "order_id");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getColumn(), is("order_id"));
        assertThat(actual.get().getTable(), is("orders"));
    }
    
    @Test
    void assertQueryIndexesWithUnsupportedIndexType() {
        MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class, () -> metadataQueryService.queryIndexes("warehouse", "warehouse", "facts"));
        assertThat(actual.getMessage(), is("Index resources are not supported for the current database."));
    }
}
