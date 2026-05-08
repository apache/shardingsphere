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

package org.apache.shardingsphere.mcp.core.resource.handler.capability;

import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeStatusHandlerTest {

    @Test
    void assertHandle() {
        try (MCPRequestScope requestScope = new MCPRequestScope(ResourceTestDataFactory.createRuntimeContext(ResourceTestDataFactory.createDatabaseMetadata(), "http"))) {
            Map<String, Object> actual = new RuntimeStatusHandler().handle(requestScope, new MCPUriVariables(Map.of())).toPayload();
            assertThat(actual.get("status"), is("available"));
            assertThat(actual.get("active_transport"), is("http"));
            assertThat(actual.get("configured_database_count"), is(3));
            assertTrue(((List<?>) actual.get("databases")).stream().map(each -> ((Map<?, ?>) each).get("database")).anyMatch("logic_db"::equals));
            assertTrue(String.valueOf(actual.get("capability_fingerprint")).matches("[0-9a-f]{64}"));
            assertRuntimeCapability((List<?>) actual.get("databases"), "logic_db");
            assertThat(extractResourceUris((List<?>) actual.get("resources_to_read")), is(List.of("shardingsphere://capabilities", "shardingsphere://databases")));
        }
    }

    private void assertRuntimeCapability(final List<?> databases, final String databaseName) {
        Map<?, ?> actualDatabase = databases.stream().map(each -> (Map<?, ?>) each).filter(each -> databaseName.equals(each.get("database"))).findFirst().orElseThrow();
        Map<?, ?> actualCapabilities = (Map<?, ?>) actualDatabase.get("capabilities");
        assertTrue((Boolean) actualCapabilities.get("available"));
        assertTrue(((List<?>) actualCapabilities.get("supported_statement_classes")).contains("QUERY"));
        assertTrue(((List<?>) actualCapabilities.get("supported_metadata_object_types")).contains("TABLE"));
    }

    private List<String> extractResourceUris(final List<?> resources) {
        return resources.stream().map(each -> (String) ((Map<?, ?>) each).get("uri")).toList();
    }
}
