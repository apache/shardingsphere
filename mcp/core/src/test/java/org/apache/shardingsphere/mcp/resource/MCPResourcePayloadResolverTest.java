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

import org.apache.shardingsphere.mcp.capability.ServiceCapability;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContextTestFactory;
import org.apache.shardingsphere.mcp.execute.MCPJdbcStatementExecutor;
import org.apache.shardingsphere.mcp.metadata.DatabaseMetadataSnapshot;
import org.apache.shardingsphere.mcp.metadata.DatabaseMetadataSnapshots;
import org.apache.shardingsphere.mcp.metadata.MetadataObject;
import org.apache.shardingsphere.mcp.metadata.MetadataObjectType;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MCPResourcePayloadResolverTest {
    
    @Test
    void assertResolveWithUnsupportedResourceUri() {
        Object actual = createResolver().resolve("unsupported://resource");
        assertThat(actual, isA(Map.class));
        assertThat(((Map<?, ?>) actual).get("error_code"), is("invalid_request"));
        assertThat(((Map<?, ?>) actual).get("message"), is("Unsupported resource URI."));
    }
    
    @Test
    void assertResolveServiceCapabilities() {
        Object actual = createResolver().resolve("shardingsphere://capabilities");
        assertThat(actual, isA(ServiceCapability.class));
        assertTrue(((ServiceCapability) actual).getSupportedResources().contains("shardingsphere://databases/{database}/capabilities"));
        assertTrue(((ServiceCapability) actual).getSupportedTools().contains("get_capabilities"));
    }
    
    @Test
    void assertResolveDatabaseCapabilities() {
        Object actual = createResolver().resolve("shardingsphere://databases/logic_db/capabilities");
        assertThat(actual, isA(Map.class));
        assertThat(((Map<?, ?>) actual).get("database"), is("logic_db"));
        assertThat(((Map<?, ?>) actual).get("databaseType"), is("MySQL"));
        assertTrue((Boolean) ((Map<?, ?>) actual).get("supportsTransactionControl"));
    }
    
    @Test
    void assertResolveWithUnknownDatabaseCapabilities() {
        Object actual = createResolver().resolve("shardingsphere://databases/missing_db/capabilities");
        assertThat(actual, isA(Map.class));
        assertThat(((Map<?, ?>) actual).get("error_code"), is("not_found"));
        assertThat(((Map<?, ?>) actual).get("message"), is("Database capability does not exist."));
    }
    
    @Test
    void assertResolveMetadataItems() {
        Object actual = createResolver().resolve("shardingsphere://databases/logic_db/schemas/public/tables");
        assertThat(actual, isA(Map.class));
        assertThat(((List<?>) ((Map<?, ?>) actual).get("items")).size(), is(2));
        assertThat(((MetadataObject) ((List<?>) ((Map<?, ?>) actual).get("items")).get(0)).getName(), is("order_items"));
    }
    
    @Test
    void assertResolveWithUnsupportedIndexResource() {
        Object actual = createResolver().resolve("shardingsphere://databases/warehouse/schemas/warehouse/tables/facts/indexes");
        assertThat(actual, isA(Map.class));
        assertThat(((Map<?, ?>) actual).get("error_code"), is("unsupported"));
        assertThat(((Map<?, ?>) actual).get("message"), is("Index resources are not supported for the current database."));
    }
    
    private MCPResourcePayloadResolver createResolver() {
        return new MCPResourcePayloadResolver(new MCPRuntimeContextTestFactory().create(createDatabaseMetadataSnapshots(), mock(MCPJdbcStatementExecutor.class)));
    }
    
    private DatabaseMetadataSnapshots createDatabaseMetadataSnapshots() {
        Map<String, DatabaseMetadataSnapshot> databaseSnapshots = new LinkedHashMap<>();
        databaseSnapshots.put("logic_db", new DatabaseMetadataSnapshot("MySQL", "", List.of(
                new MetadataObject("logic_db", "public", MetadataObjectType.SCHEMA, "public", "", ""),
                new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "orders", "", ""),
                new MetadataObject("logic_db", "public", MetadataObjectType.TABLE, "order_items", "", ""))));
        databaseSnapshots.put("warehouse", new DatabaseMetadataSnapshot("Hive", "", List.of(
                new MetadataObject("warehouse", "warehouse", MetadataObjectType.SCHEMA, "warehouse", "", ""),
                new MetadataObject("warehouse", "warehouse", MetadataObjectType.TABLE, "facts", "", ""))));
        return new DatabaseMetadataSnapshots(databaseSnapshots);
    }
}
