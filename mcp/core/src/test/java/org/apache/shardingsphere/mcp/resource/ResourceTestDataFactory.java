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

package org.apache.shardingsphere.mcp.resource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceTestDataFactory {
    
    /**
     * Create database metadata catalog.
     *
     * @return database metadata catalog
     */
    public static MCPDatabaseMetadataCatalog createDatabaseMetadataCatalog() {
        Map<String, MCPDatabaseMetadata> databaseMetadataMap = new LinkedHashMap<>(2, 1F);
        databaseMetadataMap.put("logic_db", new MCPDatabaseMetadata("logic_db", "MySQL", "", List.of(
                new MCPSchemaMetadata("logic_db", "public", List.of(
                        new MCPTableMetadata("logic_db", "public", "orders",
                                List.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "order_id")),
                                List.of(new MCPIndexMetadata("logic_db", "public", "orders", "order_idx"))),
                        new MCPTableMetadata("logic_db", "public", "order_items",
                                List.of(new MCPColumnMetadata("logic_db", "public", "order_items", "", "item_id")), List.of())),
                        List.of(new MCPViewMetadata("logic_db", "public", "orders_view",
                                List.of(new MCPColumnMetadata("logic_db", "public", "", "orders_view", "order_id"))))))));
        databaseMetadataMap.put("warehouse", new MCPDatabaseMetadata("warehouse", "Hive", "", List.of(
                new MCPSchemaMetadata("warehouse", "warehouse", List.of(new MCPTableMetadata("warehouse", "warehouse", "facts", List.of(), List.of())), List.of()))));
        return new MCPDatabaseMetadataCatalog(databaseMetadataMap);
    }
}
