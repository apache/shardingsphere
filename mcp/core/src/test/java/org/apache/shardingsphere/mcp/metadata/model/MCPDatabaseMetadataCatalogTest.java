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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPDatabaseMetadataCatalogTest {
    
    @Test
    void assertNewWithCopiedMetadata() {
        Map<String, MCPDatabaseMetadata> databaseMetadataMap = new LinkedHashMap<>(1, 1F);
        databaseMetadataMap.put("logic_db", createDatabaseMetadata("logic_db", "MySQL", "orders"));
        MCPDatabaseMetadataCatalog actual = new MCPDatabaseMetadataCatalog(databaseMetadataMap);
        databaseMetadataMap.clear();
        assertThat(actual.getDatabaseMetadataMap().size(), is(1));
        assertThat(actual.getDatabaseMetadataMap().get("logic_db").getDatabaseType(), is("MySQL"));
    }
    
    @Test
    void assertFindMetadata() {
        Optional<MCPDatabaseMetadata> actual = createDatabaseMetadataCatalog().findMetadata("logic_db");
        assertTrue(actual.isPresent());
        assertThat(actual.orElseThrow().getDatabaseType(), is("MySQL"));
    }
    
    @Test
    void assertFindMetadataWhenNotFound() {
        Optional<MCPDatabaseMetadata> actual = createDatabaseMetadataCatalog().findMetadata("missing_db");
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertReplaceMetadata() {
        MCPDatabaseMetadataCatalog metadataCatalog = createDatabaseMetadataCatalog();
        MCPDatabaseMetadata replacement = createDatabaseMetadata("logic_db", "MySQL", "orders_archive");
        metadataCatalog.replaceMetadata("logic_db", replacement);
        assertThat(metadataCatalog.getDatabaseMetadataMap().get("logic_db"), is(replacement));
    }
    
    private MCPDatabaseMetadataCatalog createDatabaseMetadataCatalog() {
        Map<String, MCPDatabaseMetadata> databaseMetadataMap = new LinkedHashMap<>(3, 1F);
        databaseMetadataMap.put("logic_db", createDatabaseMetadata("logic_db", "MySQL", "orders"));
        databaseMetadataMap.put("analytics_db", createDatabaseMetadata("analytics_db", "PostgreSQL", "metrics"));
        databaseMetadataMap.put("warehouse", createDatabaseMetadata("warehouse", "Hive", "facts"));
        return new MCPDatabaseMetadataCatalog(databaseMetadataMap);
    }
    
    private MCPDatabaseMetadata createDatabaseMetadata(final String databaseName, final String databaseType, final String tableName) {
        return new MCPDatabaseMetadata(databaseName, databaseType, "",
                List.of(new MCPSchemaMetadata(databaseName, "public", List.of(new MCPTableMetadata(databaseName, "public", tableName, List.of(), List.of())), List.of())));
    }
}
