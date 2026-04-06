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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPDatabaseMetadataCatalogTest {
    
    @Test
    void assertReplaceMetadata() {
        MCPDatabaseMetadataCatalog metadataCatalog = createDatabaseMetadataCatalog();
        metadataCatalog.replaceMetadata("logic_db", new MCPDatabaseMetadata("logic_db", "MySQL", "", List.of(new MCPSchemaMetadata("logic_db", "public", List.of(
                new MCPTableMetadata("logic_db", "public", "orders_archive", List.of(), List.of())), List.of()))));
        assertThat(metadataCatalog.findMetadata("logic_db").map(MCPDatabaseMetadata::getDatabaseType).orElseThrow(), is("MySQL"));
        assertThat(metadataCatalog.findMetadata("logic_db").orElseThrow().getSchemas().get(0).getTables().get(0).getTable(), is("orders_archive"));
        assertThat(metadataCatalog.findMetadata("analytics_db").orElseThrow().getSchemas().get(0).getTables().get(0).getTable(), is("metrics"));
    }
    
    private MCPDatabaseMetadataCatalog createDatabaseMetadataCatalog() {
        Map<String, MCPDatabaseMetadata> databaseMetadataMap = new LinkedHashMap<>(3, 1F);
        databaseMetadataMap.put("logic_db", new MCPDatabaseMetadata("logic_db", "MySQL", "",
                List.of(new MCPSchemaMetadata("logic_db", "public", List.of(new MCPTableMetadata("logic_db", "public", "orders", List.of(), List.of())), List.of()))));
        databaseMetadataMap.put("analytics_db", new MCPDatabaseMetadata("analytics_db", "PostgreSQL", "",
                List.of(new MCPSchemaMetadata("analytics_db", "public", List.of(
                        new MCPTableMetadata("analytics_db", "public", "metrics", List.of(new MCPColumnMetadata("analytics_db", "public", "metrics", "", "metric_id")), List.of())), List.of()))));
        databaseMetadataMap.put("warehouse", new MCPDatabaseMetadata("warehouse", "Hive", "", List.of(
                new MCPSchemaMetadata("warehouse", "warehouse", List.of(new MCPTableMetadata("warehouse", "warehouse", "facts", List.of(), List.of())), List.of()))));
        return new MCPDatabaseMetadataCatalog(databaseMetadataMap);
    }
}
