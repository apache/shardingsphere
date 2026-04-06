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

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContextTestFactory;
import org.apache.shardingsphere.mcp.execute.MCPJdbcStatementExecutor;
import org.apache.shardingsphere.mcp.metadata.model.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPViewMetadata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * Resource test data factory.
 */
public final class ResourceTestDataFactory {
    
    private ResourceTestDataFactory() {
    }
    
    /**
     * Create runtime context.
     *
     * @return runtime context
     */
    public static MCPRuntimeContext createRuntimeContext() {
        return new MCPRuntimeContextTestFactory().create(createDatabaseMetadataSnapshots(), mock(MCPJdbcStatementExecutor.class));
    }
    
    /**
     * Create database metadata snapshots.
     *
     * @return database metadata snapshots
     */
    public static DatabaseMetadataSnapshots createDatabaseMetadataSnapshots() {
        Map<String, MCPDatabaseMetadata> result = new LinkedHashMap<>(2, 1F);
        result.put("logic_db", new MCPDatabaseMetadata("logic_db", "MySQL", "", List.of(
                new MCPSchemaMetadata("logic_db", "public", List.of(
                        new MCPTableMetadata("logic_db", "public", "orders",
                                List.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "order_id")),
                                List.of(new MCPIndexMetadata("logic_db", "public", "orders", "order_idx"))),
                        new MCPTableMetadata("logic_db", "public", "order_items",
                                List.of(new MCPColumnMetadata("logic_db", "public", "order_items", "", "item_id")), List.of())),
                        List.of(new MCPViewMetadata("logic_db", "public", "orders_view",
                                List.of(new MCPColumnMetadata("logic_db", "public", "", "orders_view", "order_id"))))))));
        result.put("warehouse", new MCPDatabaseMetadata("warehouse", "Hive", "", List.of(
                new MCPSchemaMetadata("warehouse", "warehouse", List.of(new MCPTableMetadata("warehouse", "warehouse", "facts", List.of(), List.of())), List.of()))));
        return new DatabaseMetadataSnapshots(result);
    }
}
